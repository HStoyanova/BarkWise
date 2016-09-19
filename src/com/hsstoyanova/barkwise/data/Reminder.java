package com.hsstoyanova.barkwise.data;

import java.sql.Date;

public class Reminder {

	public int id;
	public int userId;
	public int petId;
	public int reminderId;
	public String note;
	public Date date; 
	public String time;
	public String rCode;
	private String userName = "";
	
	
	public Reminder(int _id, int _userId, int _petId, int _reminderId, String _note, Date _date, String _time, String _rCode)
	{
		this.id = _id;
		this.userId = _userId;
		this.petId = _petId;
		this.reminderId = _reminderId;
		this.note = _note;
		this.date = _date;
		this.time = time;
		this.rCode = _rCode;
	}
	
	public void SetUserName(String un)
	{
		this.userName = un;
	}
	
	public String GetUserName()
	{
		return this.userName;
	}
	
}
