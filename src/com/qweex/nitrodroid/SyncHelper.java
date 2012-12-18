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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SyncHelper {
	
	public String SERVICE, OATH_TOKEN_SECRET, OATH_TOKEN, UID,
    			  STATS__EMAIL, STATS__LANGUAGE;
	private final String STATS__OS = "android", STATS__VERSION = "0.99";
	public JSONObject jObject, jLists, jListDetails, jTasks;
	public DatabaseConnector db;
	Context context;
	final String POST_URL = "http://app.nitrotasks.com/sync/";
	public static boolean isSyncing = false;
	
	HashMap<String, String> localeToLanguage;
	
	
	public SyncHelper(Context c)
	{
		if(localeToLanguage==null)
		{
			 localeToLanguage = new HashMap<String, String>();
			 localeToLanguage.put("en", "english");
			 localeToLanguage.put("es", "spanish");
		}
		
		
		
		
		db = new DatabaseConnector(c);
		this.context = c;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
		
		SERVICE 		  = sp.getString("service", null);
		OATH_TOKEN_SECRET = sp.getString("oauth_token_secret", null);
		OATH_TOKEN 		  = sp.getString("oauth_token", null);
		UID 			  = sp.getString("uid", null);
		STATS__EMAIL 	  = sp.getString("stats_email", null);
		STATS__LANGUAGE   = localeToLanguage.get(ListsActivity.locale);
		
		/*
		SERVICE = "dropbox";
		OATH_TOKEN_SECRET = "k34znqvh8cgftb4";
		OATH_TOKEN = "5bnt7mpm6sgoprb";
		UID = "336890";
		STATS__EMAIL = "notbryant@gmail.com";
		STATS__LANGUAGE = "english";
		*/
	}
	
	
	public class performSync extends AsyncTask<Void, Void, Boolean>
	{

		@Override
	    protected Boolean doInBackground(Void... params) {
			isSyncing = true;
//			testRead();
			try {
				JSONObject local = writeSQLtoJSON();
				String result = postData(local,
						SERVICE,
						OATH_TOKEN_SECRET,
						OATH_TOKEN,
						UID,
						STATS__EMAIL,
						STATS__OS,
						STATS__LANGUAGE,
						STATS__VERSION);
				debugPrint(result);
				db.clearEverything(context);
				readJSONtoSQL(result, context);
			} catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}
	        return true;
        }
		
		@Override
		protected void onPostExecute(Boolean success)
		{
			((android.graphics.drawable.AnimationDrawable) ListsActivity.syncLoading.getDrawable()).stop();
			ListsActivity.syncLoading.setImageResource(R.drawable.loading_animation);
			if(success) {
				ListsActivity.listAdapter.changeCursor(db.getAllLists());
				if(ListsActivity.flip.getCurrentView() != ListsActivity.flip.getChildAt(0))
				{
					ListsActivity.flip.setInAnimation(context, android.R.anim.slide_in_left);
					ListsActivity.flip.setOutAnimation(context, android.R.anim.slide_out_right);
					ListsActivity.ta = null;
					ListsActivity.flip.showPrevious();
				}
			}
			else {
				Toast.makeText(context, R.string.sync_error, Toast.LENGTH_LONG).show();
			}
			isSyncing = false;
		}
		
   };
	
	public void testRead()
	{
        try {
         	InputStream input = context.getAssets().open("nitro_data.json");
             
         	 db.clearEverything(context);
         	
             int size = input.available();
             byte[] buffer = new byte[size];
             input.read(buffer);
             input.close();
             

             // byte buffer into a string
             String text = new String(buffer);
             readJSONtoSQL(text, context);
     		} catch(Exception e) {
     			e.printStackTrace();
     		}
	}
	
	
	public JSONObject writeSQLtoJSON()
	{
		try {
		jObject = new JSONObject();
		jTasks = new JSONObject();
		jLists = new JSONObject();
		jListDetails = new JSONObject();
		
		HashMap<String, String> chart = new HashMap<String, String>();
		chart.put("today","f");
		chart.put("next", "s");
		chart.put("logbook", "v");
		Cursor c;
		String hash, name;
		
		
		//------LISTS------
		boolean isSpecial = false;
		try {
		c = db.getAllLists();
		c.moveToNext();
		JSONArray N_1 = new JSONArray();
		while(!c.isAfterLast())
		{
			hash = c.getString(c.getColumnIndex("hash"));
			name = c.getString(c.getColumnIndex("name"));
			JSONObject curr = new JSONObject();
			isSpecial = !hash.contains("-");
			if(isSpecial)
			{
				hash = chart.get(hash);
				if(hash==null || hash.equals(""))
				{
					c.moveToNext();
					continue;
				}
			}
			else
			{
				N_1.put(hash);
				curr.put("a", name);
			}
			
			JSONArray N = new JSONArray();
			String tasks[] = c.getString(c.getColumnIndex("tasks_in_order")).split(",");
			for(String t : tasks)
				if(!t.trim().equals(""))
					N.put(t);
			curr.put("n", N);
			
			JSONObject K = new JSONObject();
			long timestampA = db.getListTime(hash, "name"),
				 timestampN = db.getListTime(hash, "tasks_in_order");	//TODO
			if(!isSpecial)
				K.put("a", timestampA);
			K.put("n", timestampN);
			curr.put("k", K);
			
			jListDetails.put(hash, curr);
			c.moveToNext();
		}
		jLists.put("n", N_1);
		jLists.put("r", jListDetails);
		} catch(Exception e)
		{
			System.err.println("No lists found");
		}
		
		//------TASKS------
		try {
		c = db.getTasksOfList("", null);
		c.moveToNext();
		String priorities[] = {"none", "low", "medium", "high"}; 
		while(!c.isAfterLast())
		{
			JSONObject curr = new JSONObject();
			hash = c.getString(c.getColumnIndex("hash"));
			
			curr.put("c", c.getString(c.getColumnIndex("name")));
			curr.put("d", priorities[c.getInt(c.getColumnIndex("priority"))]);
			if(c.getLong(c.getColumnIndex("date"))>0)
				curr.put("e", c.getLong(c.getColumnIndex("date")));
			else
				curr.put("e", "");
			curr.put("q", c.getString(c.getColumnIndex("notes")));
			curr.put("h", c.getString(c.getColumnIndex("list")));
			long x = c.getLong(c.getColumnIndex("logged"));
			if(x>0)
				curr.put("j", x);
			else
				curr.put("j", false);
			
			JSONArray tags = new JSONArray();
			String tagsA[] = c.getString(c.getColumnIndex("tags")).split(",");
			for(String t : tagsA)
				if(!t.equals(""))
					tags.put(t);
			curr.put("y", tags);
			JSONObject times = new JSONObject();
			times.put("c", db.getTaskTime(hash, "name"));
			times.put("d", db.getTaskTime(hash, "priority"));
			times.put("e", db.getTaskTime(hash, "date"));
			times.put("q", db.getTaskTime(hash, "notes"));
			times.put("h", db.getTaskTime(hash, "list"));
			times.put("j", db.getTaskTime(hash, "logged"));
			times.put("y", db.getTaskTime(hash, "tags"));
			
			curr.put("k", times);
			jTasks.put(hash, curr);
			c.moveToNext();
		}
		} catch(Exception e)
		{
			System.err.println("No tasks found");
		}
		//------DELETED------
		try {
		c = db.getAllDeleted();
		c.moveToNext();
		while(!c.isAfterLast())
		{
			JSONObject curr = new JSONObject();
			hash = c.getString(c.getColumnIndex("hash"));
			curr.put("u", c.getLong(c.getColumnIndex("date")));
			jTasks.put(hash, curr);
			c.moveToNext();
		}
		} catch(Exception e)
		{
			System.err.println("No deleted found");
		}

		jLists.put("k", (long)0);				//TODO FUCK MUFFINS
		jObject.put("i", jLists);
		jObject.put("b", jTasks);
		jObject.put("x", context.getResources().getString(R.string.version_for_JSON));
		} catch(Exception e) {
			e.printStackTrace();
		}		
		return jObject;
	}
	
	static void debugPrint(String butts)
	{
		int x = 900;
		for(int i=0; ; i = i+x)
		{
			if(i>butts.length())
				break;
			int j = (i+x) >= butts.length() ? butts.length() : (i+x);
			System.out.println(butts.substring(i, j));
		}
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
	         
	         
	         //Today
	         try
	         {
	        	 hash = "f";
	        	 name = c.getResources().getString(R.string.Today);
	        	 JSONObject item = jListDetails.getJSONObject(hash);
	        	 JSONObject times = item.getJSONObject("k");
	        	 
	        	 db.insertListTimes(hash, 0, times.getLong("n"));
	        	 tasksString = parseTasksString(item.getJSONArray("n"));
	        	 
	        	 hash = "today";
	        	 db.insertList(hash, name, tasksString);
	         } catch(Exception e) {
	        	 System.err.println("Error in today: " + e.getClass());
	         }
	         //Next
	         try {
	        	 hash = "s";
	        	 name = c.getResources().getString(R.string.Next);
	        	 JSONObject item = jListDetails.getJSONObject(hash);
	        	 JSONObject times = item.getJSONObject("k");
	        	 
	        	 db.insertListTimes(hash, 0, times.getLong("n"));
	        	 tasksString = parseTasksString(item.getJSONArray("n"));
	        	 
	        	 hash = "next";
	        	 db.insertList(hash, name, tasksString);
	         } catch(Exception e) {
	        	 System.err.println("Error in Next: " + e.getClass());
	         }
	         //Logbook
	         try {
	        	 hash = "v";
	        	 name = c.getResources().getString(R.string.Next);
	        	 JSONObject item = jListDetails.getJSONObject(hash);
	        	 JSONObject times = item.getJSONObject("k");
	        	 
	        	 db.insertListTimes(hash, 0, times.getLong("n"));
	        	 tasksString = parseTasksString(item.getJSONArray("n"));
	        	 
	        	 hash = "logbook";
	        	 db.insertList(hash, name, tasksString);
	        	
	         } catch(Exception e) {
	        	 System.err.println("Error in Log: " + e.getClass());
	         }
	         
	         //All
	         try {
	        	 name = c.getResources().getString(R.string.AllTasks);
	        	 hash = "all";
	        	 
	        	 db.insertList(hash, name, null);
	         } catch(Exception e) {
	        	 System.err.println("Error in All: " + e.getClass());
	         }
	         //*/
	         
	         
	         //Misc.
	         try {
	         for (int j = 0; j < listIDs.length(); j++)
	         {
	        	 JSONObject item = jListDetails.getJSONObject(listIDs.getString(j));

	        	 hash = listIDs.getString(j);
	        	 name = item.getString("a");
	        	 tasksString = parseTasksString(item.getJSONArray("n"));
	        	 
	        	 db.insertList(hash, name, tasksString);
	        	 JSONObject times = item.getJSONObject("k");
	        	 db.insertListTimes(hash, times.getLong("a"), times.getLong("n"));
	         }
	         } catch(Exception e) {
	        	 System.err.println("Error in misc: " + e.getClass());
	         }
	         
	         
	         //Tasks
	         jTasks.names().length();
	         for(int i=0; i<jTasks.names().length(); i++)
	         {
        		 try {
        			 readAndInsertTask(jTasks.names().getString(i), 0);	//FIX: Derp. Fucked up order
		    	 } catch(Exception e) {
		    		 System.err.println("Error in tasks");
		    	 }
	         }
	         
	         
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
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
	
	void readAndInsertTask(String hash, int order) throws org.json.JSONException
	{
		JSONObject item = jTasks.getJSONObject(hash);
		String name, notes, list, tags, priority_;
		int priority;
		long date, logged;
		
		try {
			long delorted = item.getLong("u");
			db.insertDeleted(hash, delorted);
			return;
		}catch(Exception e) {}
		
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
			tags = tags.concat(tags_.getString(i)).concat(",");
		}
		
		db.insertTask(hash, name, priority, date, notes, list, logged, tags, order);
		
		JSONObject times = item.getJSONObject("k");
		db.insertTaskTimes(
				hash,
				times.getLong("c"),	//name
				times.getLong("d"), //priority
				times.getLong("e"), //date
				times.getLong("q"), //notes
				times.getLong("h"), //list
				times.getLong("j"), //logged
				times.getLong("y")  //tags
				);
	}
	
	
	
	
	public String postData(JSONObject JSONdata, String service,
			String access_oathS, String access_oath, String access_uid,
			String stats_uid, String stats_os, String stats_language, String stats_version) throws ClientProtocolException, IOException, org.json.JSONException 
	{
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(POST_URL); //"http://qweex.com/sync.php");

        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        
        //data w/ sites
        JSONObject stats = new JSONObject();
        stats.put("uid", stats_uid);
        stats.put("os", stats_os);
        stats.put("language", stats_language);
        stats.put("version", stats_version);
        JSONdata.put("stats", stats);
        String x = JSONdata.toString();		//Compress?
        nameValuePairs.add(new BasicNameValuePair("data", x));

        //access
        nameValuePairs.add(new BasicNameValuePair("access[oauth_token_secret]", access_oathS));
        nameValuePairs.add(new BasicNameValuePair("access[oauth_token]", access_oath));
        nameValuePairs.add(new BasicNameValuePair("access[uid]", access_uid));
        
        //service
        nameValuePairs.add(new BasicNameValuePair("service", service));
        System.out.println("POST:");
        // Execute HTTP Post Request
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = httpclient.execute(httppost);
        
        return convertStreamToString(response.getEntity().getContent());
	} 
	
	
	public static String convertStreamToString(java.io.InputStream is) throws IOException {
		//read it with BufferedReader
    	BufferedReader br
        	= new BufferedReader(
        		new InputStreamReader(is));
 
    	StringBuilder sb = new StringBuilder();
 
    	String line;
    	while ((line = br.readLine()) != null) {
    		sb.append(line);
    	} 
 
    	return sb.toString();
	}
	
	
	
	static String bit() {
		return Integer.toString((int)Math.floor(Math.random() *36), 36);
	}
	
	static String part() {
		return bit() + bit() + bit() + bit();
	}
	
	static String getID() {
		return part() + "-" + part() + "-" + part();
	}
}
