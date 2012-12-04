package com.qweex.nitrodroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class sync {

	public sync()
	{
		postData("ASDASDSA", "dsada", "dropbox");
	}
	
	public void postData(String JSONdata, String access, String service) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://qweex.com/sync.php");

	    try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("tasks", JSONdata));
	        nameValuePairs.add(new BasicNameValuePair("lists", JSONdata));
	        nameValuePairs.add(new BasicNameValuePair("stats[uid]", "notbryant@gmail.com"));
	        nameValuePairs.add(new BasicNameValuePair("stats[os]", "python/android"));
	        nameValuePairs.add(new BasicNameValuePair("stats[language]", "English"));
	        nameValuePairs.add(new BasicNameValuePair("stats[version]", "1.5"));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    }
	} 
}
