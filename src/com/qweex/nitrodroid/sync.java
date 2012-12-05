package com.qweex.nitrodroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

public class sync {

	HashMap<String, String> C_chart = new HashMap<String, String>();
	private JSONObject compress(JSONObject obj)
	{
		if(C_chart.isEmpty())
		{
			C_chart.put("name", "a");
			C_chart.put("tasks", "b");
			C_chart.put("content", "c");
			C_chart.put("priority", "d");
			C_chart.put("date", "e");
			C_chart.put("today", "f");
			C_chart.put("showInToday", "g");
			C_chart.put("list", "h");
			C_chart.put("lists", "i");
			C_chart.put("logged", "j");
			C_chart.put("time", "k");
			C_chart.put("sync", "l");
			C_chart.put("synced", "m");
			C_chart.put("order", "n");
			C_chart.put("queue", "o");
			C_chart.put("length", "p");
			C_chart.put("notes", "q");
			C_chart.put("items", "r");
			C_chart.put("next", "s");
			C_chart.put("someday", "t");
			C_chart.put("deleted", "u");
			C_chart.put("logbook", "v");
			C_chart.put("scheduled", "w");
			C_chart.put("version", "x");
			C_chart.put("tags", "y");
		}
			
			JSONObject out = new JSONObject();
			Iterator<?> keys = obj.keys();
			try {
			while(keys.hasNext())
			{
				String key = (String)keys.next();
				if(C_chart.containsKey(key))
				{
					out.put(C_chart.get(key), obj.get(key));
					if( obj.get(key) instanceof JSONObject)
					{
						out.put(C_chart.get(key), compress(out.getJSONObject(C_chart.get(key))));
					}
				} else {
					out.put(key, obj.get(key));
					if( obj.get(key) instanceof JSONObject)
					{
						out.put(key, compress(out.getJSONObject(key)));
					}
				}
			}
			} catch(Exception e) {}
			System.out.println("GRAAAAAHHHHH: " + out.toString());
			return out;
	}
	
	
	
	public void postData(JSONObject JSONdata, String service,
			String access_oathS, String access_oath, String access_uid,
			String stats_uid, String stats_os, String stats_language, String stats_version)
	{
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://qweex.com/sync.php");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        
	        //data w/ sites
	        JSONObject stats = new JSONObject();
	        stats.put("uid", stats_uid);
	        stats.put("os", stats_os);
	        stats.put("language", stats_language);
	        stats.put("version", stats_version);
	        JSONdata.put("stats", stats);
	        String x = compress(JSONdata).toString();
	        System.out.println(x);
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
	        
	        System.out.println(response.toString());
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    	e.printStackTrace();
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    	e.printStackTrace();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	} 
}
