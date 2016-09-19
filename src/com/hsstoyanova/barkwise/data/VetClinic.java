package com.hsstoyanova.barkwise.data;

import java.text.DecimalFormat;

import com.hsstoyanova.barkwise.common.Utils.OpenNowFlag;

public class VetClinic {

	public String name;
	public String longtitude;
	public String latitude;
	public String openNow;
	public String icon;
	
	public VetClinic(String _name, String _icon,
			String _long, String _lat, String _isOpenNow)
	{
		this.name = _name;
		this.longtitude = _long;
		this.latitude = _lat;
		this.openNow = _isOpenNow;
		this.icon = _icon;
	}
}
