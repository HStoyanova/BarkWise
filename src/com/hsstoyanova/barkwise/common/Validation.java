package com.hsstoyanova.barkwise.common;


public class Validation {


	public static String IsInputUserDataValid(String username, String password, 
			String email, String confirmPassword)
	{
		if(username == null || (username != null && (username.isEmpty() || !isUserPetNameValid(username))))
		{
			return Utils.ErrorMessage.missingUsername; 
		}
		
		if(email == null || (email != null && email.isEmpty()))
		{
			return Utils.ErrorMessage.missingEmail;
		}
			
		if(!isEmailValid(email))
		{
			return Utils.ErrorMessage.invalidEmail;
		}
		
		if(password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty())
		{
			return Utils.ErrorMessage.missingPassword;
		}
		
		if(isPasswordValid(password) || isPasswordValid(confirmPassword))
		{	
			return Utils.ErrorMessage.invalidPassword; 
		}
		

		if(!password.equals(confirmPassword))
		{
			return Utils.ErrorMessage.passwordsNotMatching;
		}
		
		return Utils.ErrorMessage.validInput;
	}
	
	
	public static String IsInputUserDataValid(String username, String email)
	{
		if(username == null || (username != null && (username.isEmpty() || !isUserPetNameValid(username))))
		{
			return Utils.ErrorMessage.missingUsername; 
		}
		
		if(email == null || (email != null && email.isEmpty()))
		{
			return Utils.ErrorMessage.missingEmail;
		}
			
		if(!isEmailValid(email))
		{
			return Utils.ErrorMessage.invalidEmail;
		}
		
		return "";
	}
	
	public static boolean isEmailValid(String email)
	{
		boolean result = false;
		
		if(email != null && !email.equals(""))
		{
			String regex = "([a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)";
			
			if(email.matches(regex))
			{
				result = true;
			}
		}
		return result;
	}
	
	public static boolean isPasswordValid(String password)
	{
		if(password == null || password.isEmpty())
		{
			return false;
		}
		
	  return password.matches("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#_$%]).{6,20})");
	}
	
	public static boolean isUserPetNameValid(String input)
	{
		if(input == null || input.isEmpty())
		{
			return false;
		}
		
		return input.matches("^([a-zA-Z0-9_ ]){4,15}");
	}
	
	public static boolean isInputTextValid(String input)
	{
		if(input == null)
		{
			return false;
		} 
		return input.matches("^[A-Za-z0-9 _]*[A-Za-z0-9][A-Za-z0-9 _.,?!\"]*$");
	}
	
}
