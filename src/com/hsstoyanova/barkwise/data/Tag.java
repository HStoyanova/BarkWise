package com.hsstoyanova.barkwise.data;

public class Tag {

	public int id;
	public int userId;
	public double longtitude;
	public double latitude;
	public String address;
	//public SimpleDateFormat date;
	public String date;
	public String caption;
	
	public String userName;
	
	public Tag(int _id, int _userId, double _longtitude, 
			double _latitude, String _date, String _caption, String _address)
	{
		this.id = _id;
		this.userId = _userId;
		this.longtitude = _longtitude;
		this.latitude = _latitude;
		this.address = _address;
		this.date = _date;
		this.caption = _caption;
	}
	
	public void setUserName(String uname)
	{
		if(uname != null && uname != "")
		{
			this.userName = uname;
		}
	}
}
