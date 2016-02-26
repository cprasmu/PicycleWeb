	google.charts.load('current', {'packages':['corechart']});

	var dmap = null;
	var map	= null;
	var pan = null;
	
	function DMap() { 
		this.accelerationX	=0;
		this.accelerationY	=0;
		this.accelerationZ	=0;
		this.rotationAlpha	=0;
		this.rotationBeta	=0;
		this.rotationGamma	=0;
		this.AxisAx			=0;
		this.AxisAy			=0;
		this.startLoc 		= "";
 		this.endLoc 		= "";
 		this.viaLoc 		= "";
 		this.mapType 		= 0;
		this.travelMode 	= 0;
 		this.avoidHighways 	= 0;
 		this.avoidTolls 	= 0;
 		this.region 		= "";
 		this.aL 			= 0;
 		this.ai 			= 2;// mode or initial mode?
 		this.draggableRoute = false;//draggable route?
 		this.animationDelay = 1000;
 		this.bq 			= 20;//not used now was step distance
 		this.mapZoom 		= 15;
 		this.directionsRenderer = null;
 		this.dirSvc 		= new google.maps.DirectionsService();
 		//this.cn 			= [];//not found
 		this.cd 			= null; // used once - directions
 		//this.cP 			= null;//not found
 		this.dp 			= null;//not found
 		this.startPanel 	= null;
 		this.mapPanel 		= null;
 		this.streetViewPanel= null;
 		this.headerPanel 	= null;
 		this.directionsPanel= null;
 		this.waitPanel 		= null;
 		this.l 				= null;//location
 		this.as 			= null;//marker
 		this.bi 			= -1;//mode
 		this.aI 			= 0;//index of leg position?
 		this.ae 			= 0;
 		this.V 				= [];//legs
 		this.ax;				//point
 		this.F 				= false;
 		this.bk 			= false;//isAnimating
 		this.width 			= 0;//width
 		this.height 		= 0;//height
 		this.elePan 		= null;
 		this.elevationData 	= [];
 		this.chart			= null;
 		this.currentDirections =null;
 };
 

 
 function loadPage() { 
	 
 	initGeoUtils();
 	dmap = new DMap();
 	dmap.parseURLParameters();
 	dmap.initPanels();
 	dmap.initInputForm();
 	dmap.bp();
 	dmap.cK();
 	dmap.cE();
 	//if (dmap.bG && dmap.am && dmap.ai > 0) { 
 	if (dmap.startLoc && dmap.endLoc && dmap.ai > 0) { 
 		setMode(dmap.ai);
 		dmap.af();
 		dmap.bw();
 	} else { 
 		setMode(0);
 	} 
 	if (window.DeviceMotionEvent != undefined) {
 		dmap.initGryo();
 	}
 	setInterval( function() {
 		var landscapeOrientation = window.innerWidth/window.innerHeight > 1;
 		if ( landscapeOrientation) {
 			
 		} else {
 			
 		}
 		
 	}, 100);
 };
 
 function unloadPage() { 
 };

 function resizePage() {
 	if (typeof ("GMap2") == "undefined") { 
 		return;
 	} 
 	dmap.calcWindowDimensions();
 	dmap.resizeElements();
 };
 
 function setMode(a) { 
 	if (a != dmap.bi) { 
 		dmap.bi = a;
 		dmap.resizeElements();
 		//eid("dirlink").className = (a == 1) ? "mb1" : "mb0";
 		//eid("svlink").className = (a == 2) ? "mb1" : "mb0";
 		if (a == 0 || a == 1) { 
 			pauseAnimation();
 			dmap.ap(false);
 		} 
 		if (a == 2) { 
 			if (!pan) { 
 				dmap.dA();
 			} 
 			dmap.ap(true);
 		} 
 	} 
 };
 
 function resetDirections() { 
 	dmap.F = false;
 	dmap.ap(false);
 	//if (dmap.O) { 
 	//	dmap.O.setMap(null);
 	//} 
 	if (dmap.directionsRenderer) { 
 		dmap.directionsRenderer.setMap(null);
 	} 
 	
 	setMode(0);
 	dmap.bp();
 };
 
 function pauseAnimation() { 
 	if (dmap.bk && dmap.F) { 
 		playAnimation();
 	} 
 };
 
 function playAnimation() { 
 	if (!dmap.bk) { 
 		eid("playButton").value = "Pause";
 		setMode(2);
 		dmap.cc();
 	} else { 
 		dmap.F = !dmap.F;
 		eid("playButton").value = dmap.F ? "Pause" : "Play";
 		if (dmap.F) { 
 			if (map) { 
 				map.setCenter(dmap.l);
 				if (dmap.aI == 0) { 
 					//map.setZoom(dmap.al);
 					map.setZoom(dmap.mapZoom);
 				} 
 				setMode(2);
 			} 
 		} 
 	} 
 };
 
 function restartAnimation() { 
 	playAnimation();
 };
 
 function getIncrementFromBike() {
	 
	 callAjax("api/deltaDistance", function(response){
		 
		 if (dmap.F) { 
		 		var dK = response.distance;
		 		document.getElementById('distDisplay').innerHTML = "Distance : " + response.totalDistance.toFixed(1);
		 		
		 		
		 		var rowPos  = Math.round((response.totalDistance / dmap.currentDirections.routes[0].legs[0].distance.value) * 100);
		 		dmap.chart.setSelection([{'row': rowPos}]);
		 		//document.getElementById('speedDisplay').innerHTML = "Speed : " + response.speed.toFixed(1) + " Mph";
		 		$("#speedDisplay").sevenSeg({ digits: 3,decimalPoint: true,colorOff: "#003200",  colorOn: "Lime",  value: response.speed.toFixed(1) });
		 		
		 		dmap.ae += dK;
		 		var v = dmap.V[dmap.aI];
		 		while (v && dmap.ae >= v.distance) { 
		 			dmap.ae -= v.distance;
		 			dmap.aI++;
		 			if (dmap.aI < dmap.V.length) { 
		 				v = dmap.V[dmap.aI];
		 				getElevationForPoints(v);
		 			} else { 
		 				v = dmap.V[dmap.V.length - 1];
		 				dmap.l = v.end;
		 				map.setCenter(v.end);
		 				if (pan) { 
		 					pan.setPosition(dmap.l);
		 					pan.setPov({ heading: v.bearing, pitch: 0, zoom: 0 });
		 					dmap.bB(dmap.l, v.bearing);
		 				} 
		 				eid("playButton").value = "Play";
		 				dmap.F = false;
		 				dmap.aI = 0;
		 				dmap.ae = 0;
		 				v = null;
		 			} 
		 		} 
		 		if (v) { 
		 			var aA = dmap.ae / v.distance;
		 			var y = v.end.lat() * aA + v.start.lat() * (1 - aA);
		 			var x = v.end.lng() * aA + v.start.lng() * (1 - aA);
		 			var pt = new google.maps.LatLng(y, x);
		 			var pts = [dmap.ax, pt];
		 			
		 			
		 			dmap.ax = pt;
		 			dmap.l = pt;
		 			map.setCenter(dmap.l);
		 			if (pan) { 
		 				pan.setPosition(dmap.l);
		 				pan.setPov({ heading: v.bearing, pitch: 0, zoom: 0 });
		 				dmap.bB(pt, v.bearing);
		 			} 
		 		} 
		 	} 
		 	//setTimeout("animationCycle()", dmap.bH);
		 	setTimeout("animationCycle()", dmap.animationDelay);
		 	
	 });
	 
 }
 
 function callAjax(url, callbackFn){
	    var xmlhttp;
	    // compatible with IE7+, Firefox, Chrome, Opera, Safari
	    xmlhttp = new XMLHttpRequest();
	   
	    xmlhttp.onreadystatechange = function(){
	        if (xmlhttp.readyState == 4 && xmlhttp.status == 200){
	        	callbackFn(JSON.parse(xmlhttp.responseText));
	        }
	    }
	    xmlhttp.open("GET", url, true);
	    xmlhttp.setRequestHeader('Access-Control-Allow-Origin', "*");
	    xmlhttp.setRequestHeader('Access-Control-Allow-Methods', "GET");
	    xmlhttp.setRequestHeader('Access-Control-Allow-Headers', "Content-Type");
	    xmlhttp.send();
}
 
 function getElevationForPoints(v){
		//var ELEVATION_BASE_URL = 'https://maps.googleapis.com/maps/api/elevation/json';
		var ELEVATION_BASE_URL = "api/Elevation";

	 	var pointA={};
		var pointB={};
		pointA.lat = v.start.lat();
		pointA.lng = v.start.lng();
		
		pointB.lat = v.end.lat();
		pointB.lng = v.end.lng();
		
		var startStr = pointA.lat + "," + pointA.lng;
		var endStr   = pointB.lat + "," + pointB.lng;
		var pathStr  = startStr + "|" + endStr;
		var samples = 100;
		
		var elvtn_args={};
		elvtn_args.samples 	= samples;
		elvtn_args.path 	= pathStr;
		elvtn_args.key 		= "AIzaSyDVyQlW4jGu8DKHMBRzKdXS1xSyhlk2jr4";
		
		url = ELEVATION_BASE_URL + '?samples=' + encodeURIComponent( elvtn_args.samples) +  '&path='  + encodeURIComponent( elvtn_args.path ) +  '&key=' + encodeURIComponent("" + elvtn_args.key)  ;
		//console.log(url);
		
		//callAjax(url,function(response) {
			
		//	var elevationArray = [];
	//		for (var x in response.results){
	//			elevationArray.push(response.results[x].elevation);
	//		}
			
			//getChart(elevationArray);
		//});
 }
 
 function getElevationForPath(polyline,callbackFn,distanceKM){
		//var ELEVATION_BASE_URL = 'https://maps.googleapis.com/maps/api/elevation/json';
		var ELEVATION_BASE_URL = "api/Elevation";
		var pathStr  = "enc:" + polyline;
		var samples = 100;
		var distanceUnit = (distanceKM / samples) * 0.621371;
		var elvtn_args={};
		elvtn_args.samples 	= samples;
		elvtn_args.path 	= pathStr;
		elvtn_args.key 		= "AIzaSyDVyQlW4jGu8DKHMBRzKdXS1xSyhlk2jr4";
		
		url = ELEVATION_BASE_URL + '?samples=' + encodeURIComponent( elvtn_args.samples) +  '&path='  +  elvtn_args.path  +  '&key=' + encodeURIComponent("" + elvtn_args.key)  ;
		//console.log(url);
		callAjax(url,function(response) {
			
			var elevationArray = [];
			elevationArray.push(['Distance (Miles)','Elevation (Ft)']);
			
			for (var x in response.results){
				elevationArray.push([(x*distanceUnit/1000).toFixed(1) + ' Miles',(response.results[x].elevation * 3.28084) ]);
				//elevationArray.push(response.results[x].elevation.toFixed(0));
			}
			//3.28084
			//callbackFn(getChart(elevationArray));
			dmap.elevationData = elevationArray;
			callbackFn(elevationArray);
		});
}
 function getChart(data) {
	
	 //http://chart.apis.google.com/chart?cht=lc&chs=500x160&chl=Elevation%20in%20Meters&chco=orange&chds=-500,500&chxt=x,y&chxr=1,-500,500&chd=t:100,110,120,0,0,100,90,80,70,0,0,-10,200,300,400
	 
	 var CHART_BASE_URL = 'http://chart.apis.google.com/chart';
	 var chart_args = {
			 		"chds" : "-10,500",
		 			"cht": "lc",
		 			"chl":"Elevation",
		 			"chs":"500x145",
		 			"chco":"red",
		 			"chd":"t:" + data.join(),
		 			"chxt":"x,y",
		 			"chxr": "1,-50,500"
	 };

	var str = jQuery.param( chart_args );
	var chartUrl = CHART_BASE_URL + '?chds=' + chart_args.chds + '&cht=' + chart_args.cht + '&chl='+ chart_args.chl + '&chs=' + chart_args.chs + '&chco=' + chart_args.chco + '&chd=' + chart_args.chd +'&chxt=' + chart_args.chxt + '&chxr=' + chart_args.chxr;
	console.log(chartUrl);
	return chartUrl;
 }
 
 DMap.prototype.initGryo = function() { 
	 
	 window.ondevicemotion = function(e) {
			
		this.AxisAx = event.accelerationIncludingGravity.x * 5;
		this.AxisAy = event.accelerationIncludingGravity.y * 5;
			
		this.accelerationX = e.accelerationIncludingGravity.x;
		this.accelerationY = e.accelerationIncludingGravity.y;
		this.accelerationZ = e.accelerationIncludingGravity.z;
		
		if ( e.rotationRate ) {
			this.rotationAlpha = e.accelerationIncludingGravity.x;
			this.rotationBeta = e.accelerationIncludingGravity.y;
			this.rotationGamma = e.accelerationIncludingGravity.z;
		}		
	}
 }
 
 DMap.prototype.buildChart = function(elevationData) {
	 
 
	// var eledata = new google.visualization.DataTable();
	// eledata.addColumn('number', 'Elevation');
	// eledata.addRows(elevationData);
	  
	  var data = google.visualization.arrayToDataTable(elevationData);
	  
	// var data = google.visualization.arrayToDataTable([
   //    ['Elevatiom'],
   //    [400],
   //    [460],
   //    [1120],
   //    [540]
   //  ]);

     var options = {
       title: 'Journey Profile',
       hAxis: {title: 'id',  titleTextStyle: {color: '#333'}},
       vAxis: {minValue: 0},
       chartArea:{left:0,top:0,width:'90%',height:'90%'},crosshair: { orientation: 'vertical' , trigger: 'both' }
     };

     this.chart = new google.visualization.AreaChart(document.getElementById('elevationPanel'));
     this.chart.draw(data, options);

 
	 
 }
 
 function RouteLeg(pt1, pt2) { 
	 
 	this.start = pt1;
 	this.end   = pt2;
 	this.distance = Point2PointDistance(pt1, pt2);
 	this.bearing  = Point2PointBearing(pt1, pt2);
 	var x = (pt1.lng() + pt2.lng()) / 2;
 	var y = (pt1.lat() + pt2.lat()) / 2;
 	this.bD = new google.maps.LatLng(y, x);
 	
 };
 
 DMap.prototype.cj = function () { 
 	this.V = [];
 	for (var i = 0; i < this.bO.length - 1; i++) { 
 		var pt1 = this.bO[i];
 		var pt2 = this.bO[i + 1];
 		var aK = new RouteLeg(pt1, pt2);
 		this.V.push(aK);
 	} 
 };
 
 DMap.prototype.cc = function () { 
 	this.bk = true;
 	this.l = this.bO[0];
 	map.setCenter(this.l);
 	//map.setZoom(this.al);
 	map.setZoom(this.mapZoom);
 	this.ax = this.bO[0].pt;
 	this.F = true;
 	setTimeout("animationCycle()", 1);
 };
 
 function animationCycle() { 
	getIncrementFromBike();
	/*
 	if (dmap.F) { 
 		var dK = dmap.bq;
 		
 		dmap.ae += dK;
 		var v = dmap.V[dmap.aI];
 		while (v && dmap.ae >= v.distance) { 
 			dmap.ae -= v.distance;
 			dmap.aI++;
 			if (dmap.aI < dmap.V.length) { 
 				v = dmap.V[dmap.aI];
 			} else { 
 				v = dmap.V[dmap.V.length - 1];
 				dmap.l = v.end;
 				map.setCenter(v.end);
 				if (pan) { 
 					pan.setPosition(dmap.l);
 					pan.setPov({ heading: v.bearing, pitch: 0, zoom: 0 });
 					dmap.bB(dmap.l, v.bearing);
 				} 
 				eid("playButton").value = "Play";
 				dmap.F = false;
 				dmap.aI = 0;
 				dmap.ae = 0;
 				v = null;
 			} 
 		} 
 		if (v) { 
 			var aA = dmap.ae / v.distance;
 			var y = v.end.lat() * aA + v.start.lat() * (1 - aA);
 			var x = v.end.lng() * aA + v.start.lng() * (1 - aA);
 			var pt = new google.maps.LatLng(y, x);
 			var pts = [dmap.ax, pt];
 			dmap.ax = pt;
 			dmap.l = pt;
 			map.setCenter(dmap.l);
 			if (pan) { 
 				pan.setPosition(dmap.l);
 				pan.setPov({ heading: v.bearing, pitch: 0, zoom: 0 });
 				dmap.bB(pt, v.bearing);
 			} 
 		} 
 	} 
 	setTimeout("animationCycle()", dmap.bH);
 	*/
 };
 
 
 DMap.prototype.T = function (q, n) { 
 	var r = "";
 	if (q && q.length > 0 && n && n.length) { 
 		var p = n + "=";
 		var b = q.indexOf(p);
 		if (b != -1) { 
 			b += p.length;
 			var e = q.indexOf("&", b);
 			if (e == -1) e = n.length;
 			r = unescape(q.substring(b, e));
 		} 
 		
 	} 
 	return r;
 };
 
	 
 DMap.prototype.parseURLParameters = function () { 
	 
 	var K = document.location.search.substring(1) + "&";
 	var bA = this.T(K, "to");
 	if (bA) { 
 		this.endLoc = bA;
 		
 	} 
 	var co = this.T(K, "from");
 	if (co) { 
 		this.startLoc = co;
 	} 
 	var dv = this.T(K, "via");
 	if (dv) { 
 		this.viaLoc = dv;
 	} 
 	var cr = this.T(K, "maptype");
 	if (cr) { 
 		this.mapType = parseInt(cr);
 	} 
 	var aM = this.T(K, "mode");
 	if (aM) { 
 		this.travelMode = parseInt(aM);
 	} 
 	var aP = this.T(K, "ah");
 	if (aP) { 
 		this.avoidHighways = parseInt(aP);
 	} 
 	var aV = this.T(K, "at");
 	if (aV) { 
 		this.avoidTolls = parseInt(aV);
 	} 
 	var cG = this.T(K, "region");
 	if (cG) { 
 		this.region = cG;
 	} 
 	var dq = this.T(K, "units");
 	if (dq) { 
 		this.aL = parseInt(dq);
 	} 
 	var ac = this.T(K, "initial");
 	if (ac) { 
 		this.ai = parseInt(ac);
 	} 
 	var cz = this.T(K, "fi");
 	if (cz) { 
 		//this.bH = parseInt(cz);
 		this.animationDelay = parseInt(cz);
 	} 
 	var aQ = this.T(K, "fs");
 	if (aQ) { 
 		this.bq = parseInt(aQ);
 	} 
 	var cp = this.T(K, "z");
 	if (cp) { 
 		this.mapZoom = parseInt(cp);
 	} 
 	var cI = this.T(K, "draggable");
 	if (cI) { 
 		var d = parseInt(cI);
 		if (d == 0) { 
 			this.draggableRoute = false;
 		} else { 
 			this.draggableRoute = true;
 		} 
 	} 
 };
 
 var travelModes = [google.maps.DirectionsTravelMode.DRIVING, google.maps.DirectionsTravelMode.WALKING, google.maps.DirectionsTravelMode.BICYCLING];
 var unitSystems = [null, google.maps.DirectionsUnitSystem.METRIC, google.maps.DirectionsUnitSystem.IMPERIAL];
 
 function getDirections() { 

 	dmap.startLoc 	= eid("fromText").value;
 	dmap.endLoc 	= eid("toText").value;
 	dmap.viaLoc 	= eid("viaText").value;
 	dmap.region 	= eid("regionText").value;
 	dmap.mapType 	= eid("mapTypeSelect").value;
 	
 	if (!dmap.startLoc || !dmap.endLoc) { 
 		alert("Please enter a 'From' and 'To' address");
 		return;
 	} 
 	dmap.bq 			= parseInt(eid("stepText").value);
 	dmap.animationDelay = parseInt(eid("intervalText").value);
 	dmap.mapZoom 		= parseInt(eid("zoomText").value);
 	if (dmap.bq > 0 && dmap.bq < 100000000) { 
 	} else { 
 		dmap.bq = 20;
 	} 

 	if (dmap.animationDelay >= 100 && dmap.animationDelay < 60000) { 
 	} else { 
 		dmap.animationDelay = 1000;
 	} 

 	if (dmap.mapZoom >= 1 && dmap.mapZoom < 22) { 
 	} else { 
 		dmap.mapZoom = 15;
 	} 
 	dmap.dj("Getting Directions ...");
 	setMode(3);
 	if (!map) { 
 		dmap.af();
 	} else {
 		map.setOptions({mapTypeId: mapTypes[dmap.mapType]});
 	}
 	dmap.bw();
 };
 
 DMap.prototype.cE = function () { 
 	this.directionsPanel.innerHTML = "<div id='directionsDiv'></div>";
 };
 
 DMap.prototype.bw = function () { 
 
 	this.startLoc 		= eid("fromText").value;
 	this.endLoc 		= eid("toText").value;
 	this.viaLoc 		= eid("viaText").value;
 	this.draggableRoute = eid("draggableRouteBox").checked;
 	
 	
 	if (!this.directionsRenderer) { 
 		this.directionsRenderer = new google.maps.DirectionsRenderer({ map: map, draggable: true, polylineOptions: { clickable: this.draggableRoute, strokeColor: "#FF0000", strokeOpacity: 0.8, strokeWeight: 3} });
 	} 
 	
 	this.directionsRenderer.setPanel(eid("directionsDiv"));

 	google.maps.event.addListener(this.directionsRenderer, 'directions_changed', function (a) { 
 		dmap.cd = dmap.directionsRenderer.getDirections();
 		//if (dmap.cA) { 
 		if (dmap.dirSvc) { 
 			var dQ = dmap.l;
 			var bX = dmap.directionsRenderer.getDirections();
 			dmap.aw(bX, false);
 			dmap.directionsRenderer.setMap(map);
 			if (dmap.bk) { 
 				dmap.aC(dQ, true);
 			} 
 		} 
 	});
 	
 	
 	this.travelMode 	= eid("travelModeSelect").value;
 	this.avoidHighways 	= eid("avoidHighwaysBox").checked;
 	this.avoidTolls 	= eid("avoidTollsBox").checked;
 	var aR = { origin: this.startLoc, destination: this.endLoc, travelMode: travelModes[this.travelMode], avoidHighways: this.avoidHighways ? true : false, avoidTolls: this.avoidTolls ? true : false, region: this.region };

 	if (this.viaLoc) { 
 	
 		aR.waypoints = [];
 		var cg = this.viaLoc.split(",");
 		for (var i = 0;i < cg.length; i++) { 
 			var de = { location: cg[i] };
 			aR.waypoints.push(de);
 		} 
 		glog("waypoints = " + aR.waypoints);
 	} 
 	if (this.aL > 0) { 
 		aR.unitSystem = unitSystems[this.aL];
 	} 

 	dmap.dirSvc.route(aR, function (bK, bb) { 
 		if (bb == google.maps.DirectionsStatus.OK) { 
 			if (dmap.ai == 0) { 
 				dmap.ai = 2;
 			} 
 			setMode(dmap.ai);
 			dmap.directionsRenderer.setDirections(bK);
 			dmap.aw(bK, true);
		 	if (dmap.ai == 2) { } 
		} else { 
		 	setMode(0);
 			alert("Get Directions Error " + bb);
 		} });
 };
 
 DMap.prototype.aw = function (cL, cv) { 
	 
	// getElevationForPath(cL.routes[0].overview_polyline,function(url){
		// getChart(
		// dmap.elePan.innerHTML="<img src='" + url + "'>";
		 
	// });
	 var me= this;
	 this.currentDirections = cL;
	 getElevationForPath(cL.routes[0].overview_polyline,function(data){
		 
		 me.buildChart(data);
			 
	},cL.routes[0].legs[0].distance.value);
	 
 	var bP = cL.routes;
 	var ar = bP[0].legs;
 	dmap.l = ar[0].start_location;
 	dmap.bO = [];
 	dmap.bO.push(dmap.l);
 	var aW = null;
 	for (var r in bP) { 
 		var ci = bP[r];
 		var ar = ci.legs;
 		for (var i in ar) { 
 			var aK = ar[i];
 			var cl = aK.steps;
 			for (var j in cl) { 
 				var cu = cl[j];
 				var an = cu.path;
 				for (var k in an) { 
 					var pt = an[k];
 					if (aW == null) { 
 						aW = pt;
 					} else { 
 						if (Point2PointDistance(aW, pt) > 1) { 
 							dmap.bO.push(pt);
 							aW = pt;
 						} 
 					} 
 				} 
 			} 
 		} 
 	} 
 	this.aI = 0;
 	this.ae = 0;
 	this.cj();
 	if (cv) { 
 		this.bB(dmap.l, dmap.V[0].bearing);
 	} 
 	if (this.bi == 2 && pan) { 
 		var v = this.V[0];
 		pan.setPosition(this.l);
 		pan.setPov({ heading: v.bearing, pitch: 0, zoom: 0 });
 		dmap.bB(this.l, v.bearing);
 	} 
 };
 
 function swapDirections() { 
	var cJ = eid("fromText").value;
	var cD = eid("toText").value;
 	eid("fromText").value = cD;
 	eid("toText").value = cJ;
 };
 
 DMap.prototype.cK = function () { 
	 this.waitPanel.innerHTML = "<table cellspacing=0 cellpadding=8 style='width:100%;height:100%;'><tr valign='top'><td style='width:100%'>" + "<div id='waitDiv'></div>" + "</td></tr></table>";
 };
 
 DMap.prototype.dj = function (cB) { 
	 eid("waitDiv").innerHTML = cB;
 };
 
 //DMap.prototype.dw = function () { 
 DMap.prototype.initPanels = function () { 
	this.calcWindowDimensions();
 	var ag = "";
 	ag += "<div id='startPanel' style='position:absolute;display:none'></div>";
 	ag += "<div id='headerPanel' style='position:absolute;display:none'></div>";
 	ag += "<div id='directionsPanel' style='position:absolute;display:none;overflow:auto'></div>";
 	ag += "<div id='mapPanel' style='position:absolute;display:none'></div>";
 	ag += "<div id='streetViewPanel' style='position:absolute;display:none'></div>";
 	ag += "<div id='waitPanel' style='position:absolute;display:none'></div>";
 	ag += "<div id='elevationPanel' style='position:absolute;display:none'></div>"; //new
 	
 	eid("mainDiv").innerHTML = ag;
 	this.startPanel = eid("startPanel");
 	this.headerPanel = eid("headerPanel");
 	this.mapPanel = eid("mapPanel");
 	this.streetViewPanel = eid("streetViewPanel");
 	this.directionsPanel = eid("directionsPanel");
 	this.waitPanel = eid("waitPanel");
 	this.elePan = eid("elevationPanel");//new
 	this.resizeElements();
 };
 
 DMap.prototype.t = function (ab, cq, x, y, aO, aa) { 
	 	if (ab) { 
	 		ab.style.display = cq ? "block" : "none";
 			ab.style.left = x + "px";
 			ab.style.top = y + "px";
 			if (aO >= 0) { 
 				ab.style.width = aO + "px";
 			} 
 			if (aa >= 0) { 
 				ab.style.height = aa + "px";
 			} 
 		} 
};

DMap.prototype.tt = function (ab, cq, x, y, aO, aa) { 
 	if (ab) { 
 		ab.style.display = cq ? "block" : "none";
			ab.style.left = x + "px";
			ab.style.top = y + "px";
			if (aO >= 0) { 
				ab.style.width = aO + "%";
			} 
			if (aa >= 0) { 
				ab.style.height = aa + "px";
			} 
		} 
};

 DMap.prototype.resizeElements = function (a) { 
	this.calcWindowDimensions();
	var L = 30+150;
	
 	if (this.bi == 0) { 
 		var ht = this.height;
 		var ht1 = parseInt((this.height - L + 1) / 2);
 		var wd = this.width;
		this.t(this.startPanel, true, 0, 0, wd, ht);
 		this.t(this.mapPanel, false, 0, ht1, wd, ht1);
 		this.t(this.headerPanel, false, 0, 0, wd, L);
 		this.t(this.streetViewPanel, false, 0, 0, wd, ht1);
 		this.t(this.directionsPanel, false, 0, 0, 0, wd, ht1);
 		this.t(this.waitPanel, false, 0, 0, wd, ht1);
 		this.t(this.elePan, false, 0, 0, 0, wd, ht1);
 		
 	} else if (this.bi == 3) { 
 		var ht = this.height;
 		var ht1 = parseInt((this.height - L + 1) / 2);
 		var wd = this.width;
 		this.t(this.waitPanel, true, 0, 0, wd, ht);
 		this.t(this.startPanel, false, 0, 0, wd, ht);
 		this.t(this.mapPanel, false, 0, ht1, wd, ht1);
 		this.t(this.headerPanel, false, 0, 0, wd, L);
 		this.t(this.streetViewPanel, false, 0, 0, wd, ht1);
 		this.t(this.directionsPanel, false, 0, 0, 0, wd, ht1);
 		this.t(this.elePan, false, 0, 0, 0, wd, ht1);
 		
 	} else { 
 		var ht = parseInt((this.height - L + 1) / 2);
 		var wd = this.width;
 		var y1 = L;
 		var y2 = L + ht;
 		var y3 = this.height - L;
 		this.t(this.startPanel, false, 0, 0, wd, ht);
 		this.t(this.headerPanel, true, 0, 0, wd, L-150);
 		this.t(this.directionsPanel, this.bi == 1, 0, y1, wd, ht);
 		this.t(this.streetViewPanel, this.bi == 2, 0, y1, wd, ht);
 		this.t(this.mapPanel, true, 0, y2, wd, ht);
 		this.t(this.waitPanel    , false, 0, 0,  wd, ht);
 		this.tt(this.elePan, true , 0, 30, 100, ht/2);
 		
 	} 
 	
 	if (eid("directionsDiv") && (this.width > 0)) { 
 		eid("directionsDiv").style.width = (this.width - 30) + "px";
 	} 
 	if (map) { 
 		google.maps.event.trigger(map, "resize");
 	} 
 	if (pan) { 
 		google.maps.event.trigger(pan, "resize");
 	} 
 	if (this.elevationData){
 		if (this.chart){
 	
 			this.chart.draw(google.visualization.arrayToDataTable(this.elevationData));
 		}
 	}
 };
 
 DMap.prototype.calcWindowDimensions = function () { 
	 var isIE = true;
 	if (typeof (window.innerHeight) == "number") { 
 		isIE = false;
 	} 
 	if (isIE) { 
 		this.width = parseInt(document.body.offsetWidth);
 		this.height = parseInt(document.body.offsetHeight);
 	} else { 
 		this.width = parseInt(window.innerWidth);
 		this.height = parseInt(window.innerHeight);
 	} 
 };
 
 DMap.prototype.bp = function () { 
	 this.headerPanel.innerHTML = "<table cellspacing=0 cellpadding=2 style='width:100%;height:100%;background-color:gainsboro' ><tr><td style='width:130px'>" 
	 + "<input id='playButton'  type='button' onclick='playAnimation()' value='Play' style='width:60px'  /> " 
	 + "<input id='resetButton' type='button' onclick='resetDirections()' value='Reset'  style='width:60px' />" 
	 + "</td><td align='right'><div id='distDisplay'></div></td><td align='right'><div style='width: 60px;height: 25px' id='speedDisplay'></div></td></tr></table>";

	// this.by.innerHTML = "<table cellspacing=0 cellpadding=2 style='width:100%;height:100%;background-color:gainsboro' ><tr><td style='width:130px'>" + "<input id='playButton' type='button' onclick='playAnimation()' value='Play' style='width:60px'  /> " + "<input id='resetButton' type='button' onclick='resetDirections()' value='Reset'  style='width:60px' />" + "</td><td align='right'>" + "<a id='dirlink' class='mb0' href='javascript:setMode(1)' title='Display Directions' />Directions</a> " + "<a id='svlink' class='mb0' href='javascript:setMode(2)' title='Display Street View' />Street View</a>" + "</td></tr></table>";
 };
 
 var mapTypes = [google.maps.MapTypeId.ROADMAP, google.maps.MapTypeId.SATELLITE, google.maps.MapTypeId.HYBRID, google.maps.MapTypeId.TERRAIN];
 
 DMap.prototype.af = function () { 
	 
	this.mapType = eid("mapTypeSelect").value;
	
	var mapConfig = { zoom: 2, center: new google.maps.LatLng(0, 0), mapTypeId: mapTypes[this.mapType], navigationControlOptions: { style: google.maps.NavigationControlStyle.SMALL }, scaleControl: true };
 	map = new google.maps.Map(this.mapPanel, mapConfig);
 	google.maps.event.addListener(map, "click", function (a) { 
 		dmap.aC(a.latLng, true);
 	});
 	//this.cR();
 };
 /*
 DMap.prototype.cR = function () { 
	var ad = '<ins class="adsbygoogle" style="display:inline-block;width:234px;height:60px" data-ad-client="ca-pub-5408854154696215" data-ad-slot="9214035150"></ins>';
 	var adNode = document.createElement('div');
 	adNode.innerHTML = ad;
 	map.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(adNode);
 	google.maps.event.addListenerOnce(map, 'tilesloaded', function () { 
 		(adsbygoogle = window.adsbygoogle || []).push({});
 	});
 };
 */
 var pandas = [];
 DMap.prototype.ap = function (a) { 
	if (this.as) { 
		 this.as.setVisible(a);
 	} 
};
 
 DMap.prototype.bB = function (pt, df) { 
	 if (!pandas.length) { 
		for (var i = 0;i < 16; i++) { 
			 pandas[i] = new google.maps.MarkerImage("pegman.png", new google.maps.Size(49, 52), new google.maps.Point(0, i * 52), new google.maps.Point(25, 36));
 		} 
	} 
	if (!this.as) { 
		var cZ = new google.maps.MarkerImage("pegman.png", new google.maps.Size(49, 52), new google.maps.Point(0, 0), new google.maps.Point(25, 36));
 		var ak = new google.maps.Marker({ position: pt, icon: cZ, map: map, draggable: true, zIndex: 10000 });
 		google.maps.event.addListener(ak, "dragstart", function (a) { 
 			if (dmap.F) { 
 				pauseAnimation();
 			} 
 		});
 		google.maps.event.addListener(ak, "drag", function (a) { 
 			dmap.aC(a.latLng, false);
 		});
 		google.maps.event.addListener(ak, "dragend", function (a) { 
 			dmap.aC(a.latLng, true);
 		});
 		this.as = ak;
 	} else { 
 		this.as.setPosition(pt);
 	} 
	this.as.setVisible(this.bi == 2 ? true : false);
 	var n = Math.round(df / 22.5) % 16;
 	this.as.setIcon(pandas[n]);
 };
 
 DMap.prototype.aC = function (pt, dR) { 
	 var bU = 0;
 	var bd = -1;
 	for (var i in this.V) { 
 		var dg = this.V[i].bD;
 		var aS = Point2PointDistance(pt, dg);
 		if (!bU || aS < bU) { 
 			bU = aS;
 			bd = i;
 		} 
 	} 
 	var J = this.V[bd];
 	var av = J.bD;
 	if (J) { 
 		var av = this.dB(bd, pt);
 		this.l = av;
 		this.aI = bd;
 		this.ae = J.distance / 2;
 		this.bB(av, J.bearing);
		 if (dR && pan) { 
			 pan.setPosition(av);
 			pan.setPov({ heading: J.bearing, pitch: 0, zoom: 0 });
 		} 
	} 
 };
 
 DMap.prototype.dB = function (bz, pt) { 
	var J = this.V[bz];
 	var pt1 = J.start;
 	var pt2 = J.end;
 	var dm = pt1 + " to " + pt2;
 	var aS = Point2PointDistance(pt1, pt2);
 	var dF = 0;
 	while (aS > 1) { 
 		var mx = (pt1.lng() + pt2.lng()) / 2;
 		var my = (pt1.lat() + pt2.lat()) / 2;
 		var pt0 = new google.maps.LatLng(my, mx);
 		var cT = Point2PointDistance(pt1, pt);
 		var cC = Point2PointDistance(pt2, pt);
 		if (cT < cC) {
 			pt2 = pt0;
 		} else { 
 			pt1 = pt0;
 		} 
 		aS = Point2PointDistance(pt1, pt2);
 		dF++;
 	} 
 	var bZ = Point2PointDistance(J.start, J.end);
 	var aJ = Point2PointDistance(J.start, pt1);
 	var ds = aJ * 100 / bZ;
 	this.aI = bz;
 	this.dP = J;
 	this.ae = aJ;
 	return pt1;
 };
 
 DMap.prototype.initInputForm = function () { 
	// this.aG.innerHTML = "<table cellspacing=0 cellpadding=2 style='background-color:gainsboro;text-align:center;width:100%;font-size:14px' ><tr><td style='width:100%'>" + "<div style='font-size:18px;padding:2px;font-weight:bold'>Directions Map</div>(with Animated Street View)" + "</td></tr></table>" + "<table cellspacing=0 cellpadding=4 style='width:100%;font-size:12px' ><tr><td style='width:50px;color:gray'>" + "From" + "</td><td>" + "<input id='fromText' type='text' style='width:100%' />" + "</td></tr><tr><td style='color:gray'>" + "To" + "</td><td>" + "<input id='toText' type='text' style='width:100%' />" + "</td></tr><tr><td style='color:gray'>" + "Via" + "</td><td>" + "<input id='viaText' type='text' style='width:100%' />" + "</td></tr><tr><td style='color:gray'>" + "Mode" + "</td><td>" + "<select id='travelModeSelect' style='width:150px'>" + "<option value='0'>Driving</option>" + "<option value='1'>Walking</option>" + "<option value='2'>Bicycle</option>" + "</select> &nbsp;&nbsp;&nbsp;" + "<a href='javascript:swapDirections()' title='Reverse To and From addresses'>reverse</a>" + "</td></tr><tr><td style='color:gray'>" + "Options" + "</td><td>" + "<input id='avoidHighwaysBox' type='checkbox' /> Avoid Highways &nbsp;" + "<input id='avoidTollsBox' type='checkbox' /> Avoid Toll Roads" + "</td></tr><tr><td colspan='2'>" + "<input type='button' value='Get Directions' style='width:100%;font-size:24px;color:green' onclick='getDirections()'>" + "</td></tr></table>" + "<table cellspacing=0 cellpadding=4 style='width:100%;font-size:11px;color:gray'><tr><td colspan=2>" + "<b>Animation Options</b>" + "</td></tr><tr><td style='width:100px'>" + "Step Distance</td><td><input id='stepText' type='text' style='width:80px' /> metres" + "</td></tr><tr><td>" + "Refresh Interval</td><td><input id='intervalText' type='text' style='width:80px' /> ms" + "</td></tr><tr><td>" + "Zoom (1 - 20)</td><td><input id='zoomText' type='text' style='width:80px' />" + "</td></tr></table>" + "<p>" + "<a target='_blank' href='http://www.tripgeo.com/DirectionsMap.aspx' title='TripGeo - Create Your Directions Map' style='color:green'>TripGeo - Create Your Directions Map</a>" + "</p>" + "";
	 this.startPanel.innerHTML = "<table cellspacing=0 cellpadding=2 style='background-color:gainsboro;text-align:center;width:100%;font-size:14px' ><tr><td style='width:100%'>" 
	 + "<div style='font-size:18px;padding:2px;font-weight:bold'>Picycle Map</div>" 
	 + "</td></tr></table>" 
	 + "<table cellspacing=0 cellpadding=4 style='width:100%;font-size:12px' ><tr><td style='width:50px;color:gray'>" 
	 + "From" + "</td><td>" 
	 + "<input id='fromText' type='text' style='width:50%' />" 
	 + "</td></tr><tr><td style='color:gray'>" 
	 + "To" + "</td><td>" 
	 + "<input id='toText' type='text' style='width:50%' />" 
	 + "</td></tr><tr><td style='color:gray'>" 
	 + "Via" + "</td><td>" 
	 + "<input id='viaText' type='text' style='width:50%' />"
	 + "</td></tr><tr><td style='color:gray'>" 
	
	 + "Region" + "</td><td>" 
	 + "<input id='regionText' type='text' style='width:50%' />" 
	 + "</td></tr><tr><td style='color:gray'>" 
	 
	 + "Map Type" + "</td><td>" 
	 + "<select id='mapTypeSelect' style='width:150px'>" 
	 + "<option value='0' selected>Road</option>" 
	 + "<option value='1'>Satellite</option>" 
	 + "<option value='2'>Hybrid</option>" 
	 + "<option value='3'>Terrain</option>" 
	 + "</select> &nbsp;&nbsp;&nbsp;" 
	 + "</td></tr><tr><td style='color:gray'>" 
	 
	 
	 + "Mode" + "</td><td>" 
	 + "<select id='travelModeSelect' style='width:150px'>" 
	 + "<option value='0'>Driving</option>" 
	 + "<option value='1'>Walking</option>" + "<option value='2'>Bicycle</option>" 
	 + "</select> &nbsp;&nbsp;&nbsp;" 
	 
	 + "<a href='javascript:swapDirections()' title='Reverse To and From addresses'>reverse</a>" 
	 + "</td></tr><tr><td style='color:gray'>" 
	 + "Options" 
	 + "</td><td>" 
	 + "<input id='avoidHighwaysBox' type='checkbox' /> Avoid Highways &nbsp;" 
	 + "<input id='avoidTollsBox' type='checkbox' /> Avoid Toll Roads" 
	 + "<input id='draggableRouteBox' type='checkbox' /> Draggable Route"
	 + "</td></tr><tr><td colspan='2'>" 
	 
	 + "</td></tr></table>" 
	 + "<table cellspacing=0 cellpadding=4 style='width:100%;font-size:11px;color:gray'><tr><td colspan=2>" 
	 + "<b>Animation Options</b>" 
	 + "</td></tr><tr><td style='width:100px'>" 
	 + "Step Distance</td><td><input id='stepText' type='text' style='width:80px' /> metres" 
	 + "</td></tr><tr><td>" 
	 + "Refresh Interval</td><td><input id='intervalText' type='text' style='width:80px' /> ms" 
	 + "</td></tr><tr><td>" 
	 + "Zoom (1 - 20)</td><td><input id='zoomText' type='text' style='width:80px' />" 
	 + "</td></tr></table>" 
	 + "</p>" + "<input type='button' value='Get Directions' style='width:20%;font-size:24px' onclick='getDirections()'>" ;
	
	eid("fromText").value 	= this.startLoc;
 	eid("toText").value 	= this.endLoc;
 	eid("viaText").value 	= this.viaLoc;
 	eid("regionText").value = this.region;
 	eid("travelModeSelect").value = this.travelMode.toString();
 	eid("mapTypeSelect").value = this.mapType.toString();
 	
 	if (this.avoidHighways) {
 		eid("avoidHighwaysBox").checked = true;
 	} 

 	if (this.avoidTolls) {
 		eid("avoidTollsBox").checked = true;
 	} 
 	
 	if (this.draggableRoute) {
 		eid("draggableRouteBox").checked = true;
 	}
 	
 	eid("stepText").value 	= this.bq;
 	eid("intervalText").value = this.animationDelay;
 	eid("zoomText").value 	= this.mapZoom;
 };
 
 DMap.prototype.dA = function () { 
	 
 
	var ca = { addressControl: false, linksControl: false, navigationControl: true };
 	pan = new google.maps.StreetViewPanorama(this.streetViewPanel, ca);
 	pan.setPosition(this.l);
 	pan.setPov({ heading: 135, pitch: 0, zoom: 1 });
 	google.maps.event.addListener(pan, "pov_changed", function (a) { 
 		if (!dmap.F) { 
 			var cO = pan.getPov();
 			var az = cO.heading;
 			while (az < 0) { 
 				az += 360;
 			} 
 			while (az > 360) { 
 				az -= 360;
 			} 
 			dmap.bB(dmap.l, az);
 		} 
 	});
 };
 
 function eid(id) { 
	 return document.getElementById(id);
 };
 
 function formatFloat(n, d) { 
	 	var m = Math.pow(10, d);
 		return parseInt(n * m, 10) / m;
 };
 
function glog(a) { 
	 if (typeof (console) != "undefined" && console && console.log) { 
		 console.log(a);
 	} 
};

function initRandom() { 
	var seed = new Date().getTime();
 	var x = Math.random(seed);
 };
 
 function getRandomInt(bV) { 
	 var aN = Math.floor(Math.random() * bV);
 	return aN;
 };
 
 function getRandomFloat(bV) { 
	 var aN = Math.random() * bV;
 	return aN;
 };
 
 function initGeoUtils() { 
	 if (typeof (String.prototype.toRad) === "undefined") { 
		 Number.prototype.toRad = function () { 
			 return this * Math.PI / 180;
 		} 
	} 
	if (typeof (String.prototype.toDeg) === "undefined") { 
		Number.prototype.toDeg = function () { 
			return this * 180 / Math.PI;
 		} 
	} 
};

 function Point2PointDistance(pt1, pt2) { 
	var lat1 = pt1.lat();
 	var lat2 = pt2.lat();
 	var lon1 = pt1.lng();
 	var lon2 = pt2.lng();
	var R = 6371000;
 	var dLat = (lat2 - lat1).toRad();
 	var dLon = (lon2 - lon1).toRad();
 	var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1.toRad()) * Math.cos(lat2.toRad()) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
 	var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 	var d = R * c;
 	return d;
 };
 
 function Point2PointBearing(pt1, pt2) { 
	 var angle = 0;
 	if (pt1 != null && pt2 != null && !pt1.equals(pt2)) { 
 		var y1 = pt1.lat().toRad();
 		var x1 = pt1.lng().toRad();
 		var y2 = pt2.lat().toRad();
 		var x2 = pt2.lng().toRad();
 		var a = Math.sin(x1 - x2) * Math.cos(y2);
 		var b = Math.cos(y1) * Math.sin(y2) - Math.sin(y1) * Math.cos(y2) * Math.cos(x1 - x2);
 		angle = -(Math.atan2(a, b));
 		if (angle < 0.0) { 
 			angle += Math.PI * 2.0;
 		} 
 		angle = parseInt(angle * 180.0 / Math.PI);
 	} 
 	return angle;
 };

window.onunload=unloadPage;

window.onresize=resizePage;