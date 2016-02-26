
function loadPage()
{
    createMap();
}

function createMap()
{
    var from 		= eid("fromText").value;
    var to 			= eid("toText").value;
    var via 		= eid("viaText").value;
    var mapType 	= eid("mapTypeSelect").value;
    var travelMode 	= eid("travelModeSelect").value;
    var avoidHighways = eid("avoidHighwaysSelect").value;
    var avoidTolls 	= eid("avoidTollsSelect").value;
    var region 		= eid("regionText").value;
    var units 		= eid("unitsSelect").value;
    var initial 	= eid("initialSelect").value;
    var draggable 	= eid("draggableSelect").value;
    var zoom 		= eid("zoomText").value;
    var interval 	= eid("intervalText").value;
    var step 		= eid("stepText").value;
    var width 		= eid("widthText").value;
    var height 		= eid("heightText").value;
    var linkUrl 	= mapurl + "?from=" + from + "&to=" + to;

    if (via) {
        linkUrl += "&via=" + via;
    }
    
    if (mapType != "0") {
        linkUrl += "&maptype=" + mapType;
    }

    if (travelMode != "0") {
        linkUrl += "&mode=" + travelMode;
    }
    
    if (avoidHighways != "0") {
        linkUrl += "&ah=1";
    }
    
    if (avoidTolls != "0") {
        linkUrl += "&at=1";
    }
    
    if (draggable != "0") {
        linkUrl += "&draggable=1";
    }
    
    if (region != "") {
        linkUrl += "&region=" + region;
    }
    
    if (units != "0") {
        linkUrl += "&units=" + units;
    }
    
    if (initial != "2") {
        linkUrl += "&initial=" + initial;
    }
    
    if (zoom != "15") {
        linkUrl += "&z=" + zoom;
    }
    
    if (interval != "1000") {
        linkUrl += "&fi=" + interval;
    }
    
    if (step != "20") {
        linkUrl += "&fs=" + step;
    }
    
    var embedHTML = "<iframe src='" + linkUrl + "' width='" + width + "' height='" + height + "' style='padding:0px' marginwidth='0' marginheight='0' frameborder='0' scrolling='no'></iframe>";    
    
    eid("previewDiv").innerHTML = embedHTML;
}

function eid(id) {
    return document.getElementById(id); 
}