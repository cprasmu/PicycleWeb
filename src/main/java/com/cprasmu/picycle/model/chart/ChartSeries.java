package com.cprasmu.picycle.model.chart;

import java.util.ArrayList;

public class ChartSeries<T> {
	
	private String name;
	private String unit;
	private String type;
	private Integer valueDecimals;
	private ArrayList<T>data;
	
	public ChartSeries(ArrayList<T>data,String name,String unit,String type,Integer valueDecimals ) {
		this.data = data;
		this.name = name;
		this.type = type;
		this.unit = unit;
		this.valueDecimals = valueDecimals;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getValueDecimals() {
		return valueDecimals;
	}

	public void setValueDecimals(Integer valueDecimals) {
		this.valueDecimals = valueDecimals;
	}

	public ArrayList<T> getData() {
		return data;
	}

	public void setData(ArrayList<T> data) {
		this.data = data;
	}
	
}