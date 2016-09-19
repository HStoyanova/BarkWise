package com.hsstoyanova.barkwise.common;

import com.hsstoyanova.barkwise.data.Tag;
import android.app.PendingIntent;

public class Utils {

	public static final String REQUEST_RESULT= "success";
	public static final String TIME_SEPARATOR= ":";
	public static int RADIUS = 5;
	public static String VET="veterinar clinics";
	public static String GOOGLE_SEARCH_RADIUS="5000";
	
	
	public static enum RequestActions
	{
		POST,
		GET
	}
	
	public static class ErrorMessage
	{
		public static String somethingWentWrong = "Something went wrong!";
		public static String validInput = "Valid";
		public static String missingUsername = "Please, enter username!";
		public static String missingPassword = "Please, enter password!";
		public static String missingNameAndPass = "Please, enter password and user name!";
		public static String invalidPassword = "The entered password is not valid! Check menu help for more information!";
		public static String passwordsNotMatching = "The entered passwords don't match!";
		public static String missingEmail = "Please, enter email address!";
		public static String invalidEmail = "Please, enter valid email address!";
		public static String existingUser= "User with this username or email already exists!";
		public static String incorrectLoginInput = "Incorrect user name or password!";
		public static String username = "Please, enter valid username!";
		public static String invalidReminderData = "Selected reminder data is invalid! Please, select reminder type, date, time and pet!";
		public static String internetConnection = "The selected operation requires Internet connection!";
		public static String chipIdTaken = "This pet chip number has already been registered!";
		public static String noImageToScan = "No image for scanning is chosen!";
		public static String problemWithTrainedDataOCR = "We encountered a problem uploading trained data into your device.Please, try again!";
		public static String noImageWasLoaded = "No image was loaded!";
		public static String allFieldsAreRequired = "All fields are required!";
		public static String incorrectPassword = "The current password is incorrect!";
		public static String couldNotCreateFileForImage = "Creating file for image was unsuccessful";
		public static String couldnLocateYou = "Barkwise couldn't locate you..";
		public static String noPetsAreFound = "No pets are found!";
		public static String noRemindersFound = "No reminders are found!";
		public static String couldntScanImage = "Barkwise couldn't scan the image!";
		public static String noSuggestionsFound = "No suggestions are found!";
		public static String noTextForSuggestion = "Enter your suggestion in the text box.";
		public static String turnOnGps = "The selected operation requires GPS device to be turned on!";
		public static String turnOnGpsAndInternert = "The selected operation requires internet connection and GPS device to be turned on!";
		public static String invalidPetData = "Invalid pet data!";
		public static String invalidCaption = "Invalid caption data!";
	}
	
	public static class Message
	{
		public static String userCreated = "Successful registration!";
		public static String imageUploaded = "Successfully uploaded image!";
		public static String reminderAdded = "Successfully added reminder!";
		public static String reminderDeleted = "Reminder successfully deleted!";
		public static String tagDeleted  = "Tag successfully deleted!";
		public static String petDeleted  = "Pet successfully deleted!";
		public static String petDataUpdated  = "Pet data saved!";
		public static String userDataUpdated  = "User data saved!";
		public static String noImages = "No images were found.";
		public static String warningGoBackButton = "Press back again to leave.";
		public static String imageDeleted  = "Image successfully deleted!";
		public static String gspAllert = "Your GPS seems to be disabled, do you want to enable it?";
		public static String suggestionAdded = "Successfully added suggestion!";
		public static String changedParrword = "Successfully changed password!";
		
	}
	
	public static class InfoPair
	{
		public int id;
		public String name;
		
		public InfoPair(int _id, String _name)
		{
			this.id = _id;
			this.name = _name;
		}
	}
	
	public static class ReminderPair
	{
		public int id;
		public String type;
		public String time;
		public String date;
		public String note;
		public String pet;;
		public String rCode;
		
		
		public ReminderPair(int _id, String _type, String _time, String _date, String _note, String _pet, String _rCode)
		{
			this.id = _id;
			this.type = _type;

			this.time = _time;
			this.date = _date;

			this.note = _note;
			this.pet = _pet;
			this.rCode = _rCode;
		}
	}
	
	public static class ReminderInfoPair
	{
		public PendingIntent pIntent;
		public int id;
		
		public ReminderInfoPair(PendingIntent _pIntent, int _id)
		{
			this.pIntent = _pIntent;
			this.id = _id;
		}
	}
	
	public static class RecentTagInfoPair
	{
		public Tag tag;
		public String username;
		
		public RecentTagInfoPair(Tag tag2, String _name)
		{
			this.tag = tag2;
			this.username = _name;
		}
	}
	
	public static class OpenNowFlag
	{
		public static final String YES = "Yes";
		public static final String NO = "No";
		public static final String NA = "N/A";
	}

}
