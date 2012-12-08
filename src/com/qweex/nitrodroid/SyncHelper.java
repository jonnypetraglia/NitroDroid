/*
Copyright (c) 2012 Qweex
Copyright (c) 2012 Jon Petraglia

This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.

    2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
 */
package com.qweex.nitrodroid;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

public class SyncHelper {
	
	public JSONObject jObject, jLists, jListDetails, jTasks;
	public DatabaseConnector db;
	
	public SyncHelper(Context c)
	{
		db = new DatabaseConnector(c);
	}
	
	
	public String[] parseTasksString(JSONArray jArray)
	{
		String[] result = new String[jArray.length()];
		for(int i=0; i<jArray.length(); i++)
		{
			try {
				result[i] = jArray.getString(i);
			} catch(Exception e) {}
		}
		return result;
	}
	
	public void readJSONtoSQL(String JSONstring, Context c)
	{
	    try {
	         
	         
	         jObject = new JSONObject(JSONstring);
	         jTasks = jObject.getJSONObject("b");
	         jLists = jObject.getJSONObject("i");
	         JSONArray listIDs = jLists.getJSONArray("n");
	         jListDetails = jLists.getJSONObject("r");
	         
	         String hash, name;
	         String[] tasksString;
	         
	         db.open();
	         
	         //Today
	         try
	         {
	        	 hash = "f";
	        	 name = c.getResources().getString(R.string.Today);
	        	 JSONObject item = jListDetails.getJSONObject(hash);
	        	 tasksString = parseTasksString(item.getJSONArray("n"));
	        	 
	        	 //Tasks
	        	 for(int i=0; i<item.getJSONArray("n").length(); i++)
	        		 try {
	        		 readAndInsertTask(item.getJSONArray("n").getString(i), i);
			    	 } catch(Exception e) {
			    		 System.err.println("Error in tasks");
			    	 }
	        	 
	        	 db.insertList(hash, name, tasksString);
	         } catch(Exception e) {
	        	 System.err.println("Error in today");
	         }
	         //Next
	         try {
	        	 hash = "f";
	        	 name = c.getResources().getString(R.string.Next);
	        	 JSONObject item = jListDetails.getJSONObject(hash);
	        	 tasksString = parseTasksString(item.getJSONArray("n")); 
	        	 
	        	 //Tasks
	        	 for(int i=0; i<item.getJSONArray("n").length(); i++)
	        		 try {
	        		 readAndInsertTask(item.getJSONArray("n").getString(i), i);
			    	 } catch(Exception e) {
			    		 System.err.println("Error in tasks");
			    	 }
	        	 
	        	 db.insertList(hash, name, tasksString);
	         } catch(Exception e) {
	        	 System.err.println("Error in Next");
	         }
	         //Logbook
	         try {
	        	 hash = "v";
	        	 name = c.getResources().getString(R.string.Logbook);
	        	 JSONObject item = jListDetails.getJSONObject(hash);
	        	 tasksString = parseTasksString(item.getJSONArray("n")); 
	        	 
	        	 //Tasks
	        	 for(int i=0; i<item.getJSONArray("n").length(); i++)
	        		 try {
	        		 readAndInsertTask(item.getJSONArray("n").getString(i), i);
			    	 } catch(Exception e) {
			    		 System.err.println("Error in tasks");
			    	 }
	        	 
	        	 db.insertList(hash, name, tasksString);
	         } catch(Exception e) {
	        	 System.err.println("Error in Log");
	         }
	         //All
	         try {
	        	 hash = "b";
	        	 name = c.getResources().getString(R.string.AllTasks);
	        	 JSONObject item = jObject.getJSONObject("b");	//NOTE: This is different!
	        	 tasksString = new String[0];					// <-   So is this!
	        	 
	        	 db.insertList(hash, name, tasksString);
	         } catch(Exception e) {
	        	 System.err.println("Error in All");
	         }
	         
	         
	         
	         //Misc.
	         try {
	         for (int j = 0; j < listIDs.length(); j++)
	         {
	        	 JSONObject item = jListDetails.getJSONObject(listIDs.getString(j));

	        	 hash = listIDs.getString(j);
	        	 name = item.getString("a");
	        	 tasksString = parseTasksString(item.getJSONArray("n"));
	        	 
	        	 //Tasks
	        	 for(int i=0; i<item.getJSONArray("n").length(); i++)
	        		 try {
	        		 readAndInsertTask(item.getJSONArray("n").getString(i), i);
			    	 } catch(Exception e) {
			    		 System.err.println("Error in tasks");
			    	 }
	        	 
	        	 db.insertList(hash, name, tasksString);
	         }
	         } catch(Exception e) {
	        	 System.err.println("Error in misc");
	         }
	         
	         db.close();
	         
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	
	void readAndInsertTask(String hash, int order) throws org.json.JSONException
	{
		JSONObject item = jTasks.getJSONObject(hash);
		String name, notes, list, tags, priority_;
		int priority;
		long date, logged;
		
		name = item.getString("c");
		notes = item.getString("q");
		list = item.getString("h");
		priority_ = item.getString("d");
		if(priority_.equals("high"))
			priority = 3;
		else if(priority_.equals("medium"))
			priority = 2;
		else if(priority_.equals("low"))
			priority = 1;
		else
			priority = 0;
		try {
		date = item.getLong("e");
		} catch(org.json.JSONException e){
			date = 0;
		}
		
		try {
			logged = item.getLong("j");
		} catch(org.json.JSONException e) {
			logged = 0;
		}
		JSONArray tags_ = item.getJSONArray("y");
		tags = "";
		for(int i=0; i<tags_.length(); i++)
		{
			if(i>0)
				tags = tags.concat(",");
			tags = tags.concat(tags_.getString(i));
		}
		
		db.insertTask(hash, name, priority, date, notes, list, logged, tags, order);
		
	}
}
