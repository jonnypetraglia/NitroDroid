package com.qweex.nitrodroid;

public class NitroUtils {

	static String bit()
	{
		
		return Integer.toString((int)Math.floor(Math.random() *36), 36);
	}
	
	static String part()
	{
		return bit() + bit() + bit() + bit();
	}
	
	static String getID()
	{
		return part() + "-" + part() + "-" + part();
	}
}
