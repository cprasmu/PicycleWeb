package com.cprasmu.picycle;

public class Constants {
	public static final int STATE_STOP = 0x0;
    public static final int STATE_START = 0x1;

    public static final int IMPERIAL = 0x0;
    public static final int METRIC = 0x1;
    public static final int NAUTICAL_IMPERIAL = 0x2;
    public static final int NAUTICAL_METRIC = 0x3;
    public static final int RUNNING_IMPERIAL = 0x4;
    public static final int RUNNING_METRIC = 0x5;

    public static final int REFRESH_INTERVAL_DEFAULT = 1000;

    public static final float MS_TO_KPH = 3.6f;
    public static final float MS_TO_MPH = 2.23693629f;
    public static final float MS_TO_KNOT = 1.943844f;
    public static final float M_TO_KM = 0.001f;
    public static final float M_TO_MILES = 0.000621371192f;
    public static final float M_TO_NM = 0.000539957f;
    public static final float M_TO_M = 1f;
    public static final float M_TO_FEET = 3.2808399f;
}
