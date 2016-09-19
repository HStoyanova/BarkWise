package com.hsstoyanova.barkwise.data;

public class Suggestion {

	public String id;
	public String user;
	public String content;
	public String date;
	
	public Suggestion(String _id, String _user, String _content, String _date)
	{
		this.id = _id;
		this.user = _user;
		this.content = _content;
		this.date = _date;
	}
}
