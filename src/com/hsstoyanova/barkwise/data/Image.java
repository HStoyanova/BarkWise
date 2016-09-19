package com.hsstoyanova.barkwise.data;


public class Image {

	public int id;
	public String filePath;
	public String uploadDate;
	public int userId;
	public boolean isProfilePicture;
	
	public Image(int _id, String _filePath, String _uploadDate, int _userId, boolean _isProfilePic)
	{
		this.id = _id;
		this.filePath = _filePath;
		this.uploadDate = _uploadDate;
		this.userId = _userId;
		this.isProfilePicture = _isProfilePic;
	}
}
