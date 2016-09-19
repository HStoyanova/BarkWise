package com.hsstoyanova.barkwise.data;

import java.text.SimpleDateFormat;

public class Pet {
	
	public int id;
	public String name;
	public int ownerId;
	public String breed;
	public double weight;
	public String dob;
	public String userName;
	public String chip;
	
	
	public Pet(int _id, String _name, int _ownerId, String _breed, double _weight, String _dob, String _userName, String _chip)
	{
		this.id = _id;
		this.name = _name;
		this.ownerId = _ownerId;
		this.breed = _breed;
		this.weight = _weight;
		this.dob = _dob;
		this.userName = _userName;
		this.chip = _chip;
	}
	
}
