package com.cprasmu.picycle.model.chart;

import java.util.ArrayList;

public class DataSet<T> {
	
	private ArrayList<T> xData;
	private ArrayList<ChartSeries> datasets = new ArrayList<ChartSeries>();
	
	public DataSet (ArrayList<T> xData){
		this.xData = xData;
	}

	public ArrayList<T> getxData() {
		return xData;
	}

	public void setxData(ArrayList<T> xData) {
		this.xData = xData;
	}

	public ArrayList<ChartSeries> getDatasets() {
		return datasets;
	}

	public void setDatasets(ArrayList<ChartSeries> datasets) {
		this.datasets = datasets;
	}
	
}