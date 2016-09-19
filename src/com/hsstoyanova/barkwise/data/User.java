package com.hsstoyanova.barkwise.data;

import java.util.ArrayList;

public class User {

	public int id;
	public String name;
	public String email;
	//public String password;
	public int genderId;
	
	public ArrayList<Pet> pets;
	
	public ArrayList<Image> images;
	
	public ArrayList<Tag> tags;
	
	public User(){}
	
	public User(int _id, String _name, String _email, /*String _password, */int _genderId)
	{
		this.id = _id;
		this.name = _name;
		this.email = _email;
		//this.password = _password;
		this.genderId = _genderId;
		pets = new ArrayList<Pet>();
		images = new ArrayList<Image>();
		tags = new ArrayList<Tag>();
	}
}
