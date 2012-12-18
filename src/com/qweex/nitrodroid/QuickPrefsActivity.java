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
import org.json.JSONObject;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;


/** The activity that allows the user to change the preferences.
 * @author MrQweex
 */

public class QuickPrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{    
	public final static String DONATION_APP = "com.qweex.donation";
	PopupWindow aboutWindow, syncWindow;
	String[] themes;
	final String POST_URL = "http://app.nitrotasks.com/request_url",
			     POST_URL2 = "http://app.nitrotasks.com/auth";
	String authorize_url, oauth_token, oauth_token_secret, service;
	Preference sync, notsync;
	
	public String capitalize(String word)
	{
		return word.substring(0,1).toUpperCase() + word.substring(1);
	}
	
	/** Called when the activity is created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        ((Preference) findPreference("clear")).setOnPreferenceClickListener(ClickReset);
        ((Preference) findPreference("about")).setOnPreferenceClickListener(ClickAbout);
        sync = ((Preference) findPreference("sync"));
        notsync = ((Preference) findPreference("logout"));
        sync.setOnPreferenceClickListener(ClickSync);
        notsync.setOnPreferenceClickListener(ClickLogout);
        updateSyncPrefs();
        
        
        
        if(!ListsActivity.forcePhone && !ListsActivity.isTablet)
        {
        	((PreferenceCategory)findPreference("advanced")).removePreference(findPreference("force_phone"));
        	if(((PreferenceCategory)findPreference("advanced")).getPreferenceCount()==0)
        		getPreferenceScreen().removePreference(findPreference("advanced"));
        	ListPreference l = (ListPreference) findPreference("theme");
        	String[] themes2 = getResources().getStringArray(R.array.themes);
        	themes = new String[themes2.length-1];
        	for(int i=0, j=0; i<themes.length; i++)
        	{
        		if(themes2[i].equals("Right"))
        			continue;
        		themes[j++] = themes2[i];
        	}
        	l.setEntries(themes);
        	l.setEntryValues(themes);
        	if(ListsActivity.themeID==R.style.Right)
        		l.setValue(getResources().getString(R.string.theme1));
        }
        
    	aboutWindow = new PopupWindow(this);
    	aboutWindow.setContentView(getLayoutInflater().inflate(R.layout.about, null, false));
    	aboutWindow.setBackgroundDrawable(new BitmapDrawable());
    	aboutWindow.setAnimationStyle(android.R.style.Animation_Dialog);
    	aboutWindow.setOutsideTouchable(true);
    	
    	syncWindow = new PopupWindow(this);
    	syncWindow.setContentView(getLayoutInflater().inflate(R.layout.sync_setup, null, false));
    	syncWindow.setBackgroundDrawable(new BitmapDrawable());
    	syncWindow.setAnimationStyle(android.R.style.Animation_Dialog);
    	syncWindow.setOutsideTouchable(true);
    	((android.widget.Button)(syncWindow.getContentView().findViewById(R.id.dropbox_button))).setOnClickListener(pressAuth);
    	((android.widget.Button)(syncWindow.getContentView().findViewById(R.id.ubuntu_button))).setOnClickListener(pressAuth);
    	
    	if(getIntent().getExtras().getBoolean("show_popup"))
    	getListView().post(new Runnable()
    	{
    		public void run()
    		{
            		ClickSync.onPreferenceClick(sync);
    		}
    	});
    }
    
    void updateSyncPrefs()
    {
	    if(getPreferenceScreen().getSharedPreferences().getString("service", null)!=null)
	    {
	    	sync.setEnabled(false);
	    	sync.setTitle(capitalize(ListsActivity.syncHelper.SERVICE) + ": " + ListsActivity.syncHelper.STATS__EMAIL);
//	    	sync.setIcon(R.drawable.dropbox);
	    	notsync.setEnabled(true);
	    }
	    else
	    {
	    	sync.setEnabled(true);
	    	sync.setTitle("Set Up");
	    	notsync.setEnabled(false);
	    }
    }
    
    OnClickListener pressAuth = new OnClickListener()
    {
		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.dropbox_button)
				getAuth("dropbox");
			else if(v.getId()==R.id.ubuntu_button)
				getAuth("ubuntu");
		}
    };
    
    
    public void getAuth(String serv)
    {
		try {
		service = serv;
		
		String arg1[] = {"service", serv};
		String arg2[] = {"app", "android"};
		
		JSONObject result = new JSONObject(postData(POST_URL, arg1, arg2));
		authorize_url = result.getString("authorize_url");
		oauth_token = result.getString("oauth_token");
		oauth_token_secret = result.getString("oauth_token_secret");
		
		Intent auth = new Intent(QuickPrefsActivity.this, AuthorizeActivity.class);
		auth.putExtra("authorize_url", authorize_url);
		startActivityForResult(auth, 1);
//		Intent i = new Intent(Intent.ACTION_VIEW);
//		i.setData(android.net.Uri.parse(result_url));
//		startActivity(i);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
    }
    
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	System.out.println("derp" + resultCode);
    	if(resultCode==RESULT_OK)
    	{
    		syncWindow.dismiss();
    		//Write auth codes and whatnot
    		try {
    			JSONObject result = new JSONObject();
    			result.put("oauth_token_secret", oauth_token_secret);
    			result.put("oauth_token", oauth_token);
    			result.put("authorize_url", authorize_url);
    			
    			String arg1[] = {"token", result.toString()};
    			String arg2[] = {"service", service};
    			
    			result = new JSONObject(postData(POST_URL2, arg1, arg2));
    			JSONObject access = result.getJSONObject("access");
    			System.out.println(result);
    			String email = result.getString("email");
    			String uid = access.getString("uid");
    			oauth_token = access.getString("oauth_token");
    			oauth_token_secret = access.getString("oauth_token_secret");
    			ListsActivity.syncHelper.OATH_TOKEN = oauth_token;
    			ListsActivity.syncHelper.OATH_TOKEN_SECRET = oauth_token_secret;
    			ListsActivity.syncHelper.SERVICE = service;
    			ListsActivity.syncHelper.STATS__EMAIL = email;
    			
    			Editor e = getPreferenceScreen().getSharedPreferences().edit();
        		e.putString("service", service);
        		e.putString("oauth_token", oauth_token);
        		e.putString("oauth_token_secret", oauth_token_secret);
        		e.putString("uid", uid);
        		e.putString("stats_email", email);
        		e.commit();
        		updateSyncPrefs();
    			oauth_token = oauth_token_secret = authorize_url = service = null;
            	
    		} catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    }
    
    //5. Set oath, oathsecret, uid, email, version?
    //6. Adjust views
    
    
	public String postData(String URL, String first[], String second[]) throws ClientProtocolException, IOException, org.json.JSONException 
	{
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(URL);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair(first[0], first[1]));
        nameValuePairs.add(new BasicNameValuePair(second[0], second[1]));
        

        System.out.println("POST:");
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = httpclient.execute(httppost);
        
        return SyncHelper.convertStreamToString(response.getEntity().getContent());
	} 
    
   
    
    
    OnPreferenceClickListener ClickReset = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
       	 DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
       		    @Override
       		    public void onClick(DialogInterface dialog, int which) {
       		        switch (which){
       		        case DialogInterface.BUTTON_POSITIVE:
       		            //Yes button clicked
       		        	ListsActivity.syncHelper.db.clearEverything(QuickPrefsActivity.this);
       		        	ListsActivity.listAdapter.changeCursor(ListsActivity.syncHelper.db.getAllLists());
       		            break;

       		        case DialogInterface.BUTTON_NEGATIVE:
       		            //No button clicked
       		            break;
       		        }
       		    }
       		};

       		AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
       		builder.setTitle(R.string.warning);
       		builder.setMessage(R.string.warning_msg);
       		builder.setPositiveButton(R.string.yes, dialogClickListener);
       		builder.setNegativeButton(R.string.no, dialogClickListener);
       		builder.show();
       		return true;
        }
    };
    
    OnPreferenceClickListener ClickAbout = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
        	
        	Display display = getWindowManager().getDefaultDisplay(); 
        	int width = display.getWidth();  // deprecated
        	int height = display.getHeight();  // deprecated
        	View v = getCurrentFocus();
        	aboutWindow.showAtLocation(v, Gravity.CENTER, 40, 40);
        	aboutWindow.update(0, 0, width-40, height-80);
        	return true;
        }
    };
    
    OnPreferenceClickListener ClickSync = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
        	Display display = getWindowManager().getDefaultDisplay();
        	int width = display.getWidth();
        	int height = display.getHeight();
        	View v = getCurrentFocus();
        	v = getListView();
        	syncWindow.showAtLocation(v, Gravity.CENTER, 40, width/2);
        	syncWindow.update(0, 0, width-40, height-80);
        	return true;
        }
    };
    
    OnPreferenceClickListener ClickLogout = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
			Editor e = getPreferenceScreen().getSharedPreferences().edit();
			e.remove("service");
			e.remove("oauth_token");
			e.remove("oauth_token_secret");
			e.remove("uid");
			e.remove("stats_email");
    		e.commit();
			ListsActivity.syncHelper.OATH_TOKEN = null;
			ListsActivity.syncHelper.OATH_TOKEN_SECRET = null;
			ListsActivity.syncHelper.SERVICE = null;
			ListsActivity.syncHelper.STATS__EMAIL = null;
			updateSyncPrefs();
    		return true;
        }
    };
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Override back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (aboutWindow.isShowing()) {
                aboutWindow.dismiss();
                return false;
            }
        	if(syncWindow.isShowing()) {
        		syncWindow.dismiss();
        		return false;
        	}
        }
        return super.onKeyDown(keyCode, event);
    } 
    
    /** Called when any of the preferences is changed. Used to perform actions on certain events. */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
    	/*
    	if("theme".equals(key))
    	{
    		ListsActivity.themeID = getResources().getIdentifier(
    				sharedPreferences.getString("theme",
    						PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "Default")
    						),
    				"style",
    				getApplicationContext().getPackageName());
    	}
    	//*/
    }
    
    /** Determines if a package (i.e. application) is installed on the device.
     * http://stackoverflow.com/questions/6758841/how-to-know-perticular-package-application-exist-in-the-device
     * @param targetPackage The target package to test
     * @param c The Context of the where to search? I dunno.
     * @return True of the package is installed, false otherwise
     */
    public static boolean packageExists(String targetPackage, Context c)
    {
	   PackageManager pm=c.getPackageManager();
	   try {
		   pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
	       } catch (NameNotFoundException e) {
	    	   return false;
	    }  
	    return true;
   }
}