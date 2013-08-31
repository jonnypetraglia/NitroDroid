/*
Copyright (c) 2012-2013 Qweex
Copyright (c) 2012-2013 Jon Petraglia

This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.

    2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
 */
package com.qweex.nitrodroid;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qweex.utils.QweexUtils;
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
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;
import android.annotation.SuppressLint;
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
    private final int SYNC_ID = 1, IMAGE_ID = 2;
	PopupWindow aboutWindow, syncWindow;
	String[] themes;
	static String authorize_url, oauth_token, oauth_token_secret, service;
	Preference sync, notsync, bg, notbg;


	/** Called when the activity is created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        String TAG= QweexUtils.TAG();
    	Log.d(TAG, "Creating the Prefs Activity");
        super.onCreate(savedInstanceState);
        Locale.setDefault(new java.util.Locale(ListsActivity.locale));
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        ((Preference) findPreference("clear")).setOnPreferenceClickListener(ClickReset);
        ((Preference) findPreference("about")).setOnPreferenceClickListener(ClickAbout);
        bg = ((Preference) findPreference("background"));
        notbg = ((Preference) findPreference("default_background"));
        sync = ((Preference) findPreference("sync"));
        notsync = ((Preference) findPreference("logout"));
        bg.setOnPreferenceClickListener(ClickBG);
        notbg.setOnPreferenceClickListener(ClickBG);
        sync.setOnPreferenceClickListener(ClickSync);
        notsync.setOnPreferenceClickListener(ClickLogout);
        ((Preference) findPreference("donate")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String TAG= QweexUtils.TAG();
                Log.d(TAG, "Donate");
                Uri uri = Uri.parse("market://details?id=com.qweex.donation");
                Intent intent = new Intent (Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;
            }
        });
        updateSyncPrefs();
        
        //Force Phone preference
        if(!ListsActivity.forcePhone && !ListsActivity.isTablet)
        {
        	Log.d(TAG, "Updating the pref for forcePhone");
        	((PreferenceCategory)findPreference("advanced")).removePreference(findPreference("force_phone"));
/*        	if(((PreferenceCategory)findPreference("advanced")).getPreferenceCount()==0)
        		getPreferenceScreen().removePreference(findPreference("advanced"));*/
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

        try
        {
            Class.forName("android.widget.CalendarView", false, getClassLoader());
        } catch(Exception e)
        {
            ((PreferenceCategory)findPreference("advanced")).removePreference(findPreference("force_datepicker"));
            if(((PreferenceCategory)findPreference("advanced"))!=null &&
                    ((PreferenceCategory)findPreference("advanced")).getPreferenceCount()==0)
                getPreferenceScreen().removePreference(findPreference("advanced"));
        }
        
        //Create AboutWindow
    	aboutWindow = new PopupWindow(this);
        View cv = getLayoutInflater().inflate(R.layout.about, null, false);
        //LinearLayout cv2 = (LinearLayout) cv.findViewById(R.id.aboutMain);
        LinearLayout cv2 = new LinearLayout(this);
        for(int i=0; i<cv2.getChildCount(); i++)
        {
            if(cv2.getChildAt(i).getClass()!=TextView.class)
                continue;
            ((TextView)cv2.getChildAt(i)).setTypeface(ListsActivity.theTypeface);
        }
    	aboutWindow.setContentView(cv);
    	aboutWindow.setBackgroundDrawable(new BitmapDrawable());
    	aboutWindow.setAnimationStyle(android.R.style.Animation_Dialog);
    	aboutWindow.setOutsideTouchable(true);
    	
    	//Create SyncWindow
    	syncWindow = new PopupWindow(this);
    	syncWindow.setContentView(getLayoutInflater().inflate(R.layout.sync_setup, null, false));
    	syncWindow.setBackgroundDrawable(new BitmapDrawable());
    	syncWindow.setAnimationStyle(android.R.style.Animation_Dialog);
    	syncWindow.setOutsideTouchable(true);
    	
    	//Set OnClickListeners
    	((android.widget.Button)(syncWindow.getContentView().findViewById(R.id.dropbox_button))).setOnClickListener(pressAuth);
    	((android.widget.Button)(syncWindow.getContentView().findViewById(R.id.ubuntu_button))).setOnClickListener(pressAuth);
    	
    	//If the user pressed the sync button without it set up, show the dialog immediately
    	// We "post" it because it must be run after the Activity is completely done loading.
    	if(getIntent().getExtras().getBoolean("show_popup"))
	    	getListView().post(new Runnable() {
	    		public void run()
	    		{
	            		ClickSync.onPreferenceClick(sync);
	    		}});
    }
    
    
	@SuppressLint("NewApi")
	void updateSyncPrefs()
    {
        String TAG= QweexUtils.TAG();

	    if(getPreferenceScreen().getSharedPreferences().getString("service", null)!=null)
	    {
	    	Log.d(TAG, "Updating sync prefs: is set up");
	    	sync.setEnabled(false);
	    	sync.setTitle(capitalize(ListsActivity.syncHelper.SERVICE) + ": " + ListsActivity.syncHelper.STATS__EMAIL);
	    	if(QweexUtils.androidAPIover(11))
	    	{
	    		if("dropbox".equals(ListsActivity.syncHelper.SERVICE))
                    setIcon(sync, R.drawable.dropbox_mini);
	    		else
                    setIcon(sync, R.drawable.ubuntu_mini);
	    		
	    	}
	    	notsync.setEnabled(true);
	    }
	    else
	    {
	    	Log.d(TAG, "Updating sync prefs: is not set up");
	    	sync.setEnabled(true);
	    	sync.setTitle("Set Up");        //LOCALE
	    	if(QweexUtils.androidAPIover(11))
                setIcon(sync, (android.graphics.drawable.Drawable)null);
	    		//sync.setIcon((android.graphics.drawable.Drawable)null);

	    	notsync.setEnabled(false);
	    }
    }

    private void setIcon(Preference p, int i)
    {
        try {
        Method m = p.getClass().getMethod("setIcon", int.class);
        m.invoke(sync, i);
        } catch(Exception e) {}
    }

    private void setIcon(Preference p, android.graphics.drawable.Drawable d)
    {
        try {
            Method m = p.getClass().getMethod("setIcon", d.getClass());
            m.invoke(sync, d);
        } catch(Exception e) {}
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
        String TAG= QweexUtils.TAG();
    	Log.d(TAG, "Yo dawg setting you up with some auth");
        service = serv;
        Intent auth = new Intent(QuickPrefsActivity.this, AuthorizeActivity.class);
        auth.putExtra("service", serv);
        startActivityForResult(auth, SYNC_ID);
    }
    
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String TAG= QweexUtils.TAG();
    	Log.d(TAG, "Got result from activity");
        if(requestCode==SYNC_ID)
        {
            if(resultCode==RESULT_OK)
            {
                syncWindow.dismiss();
                //Write auth codes and whatnot
                try {
                    //Build the POST arguments
                    JSONObject result = new JSONObject();
                    if("dropbox".equals(service))
                        result.put("oauth_token_secret", oauth_token_secret);
                    else
                        result.put("oauth_secret", oauth_token_secret);
                    result.put("oauth_token", oauth_token);
                    result.put("authorize_url", authorize_url);
                    String arg1[] = {"token", result.toString()};
                    String arg2[] = {"service", service};

                    Log.d(TAG, "Posting data...");
                    String berg = postData(AuthorizeActivity.AUTH_URL, arg1, arg2);
                    Log.d(TAG, "Result: " + berg);
                    result = new JSONObject(berg);

                    //Extract the result
                    JSONObject access = result.getJSONObject("access");
                    String email = result.getString("email");
                    String uid = null;
                    if("dropbox".equals(service))
                    {
                        uid = access.getString("uid");
                        oauth_token_secret = access.getString("oauth_token_secret");
                    }
                    else
                        oauth_token_secret = access.getString("oauth_secret");
                    oauth_token = access.getString("oauth_token");

                    //Save the settings to the current SyncHelper & write them
                    ListsActivity.syncHelper.OATH_TOKEN = oauth_token;
                    ListsActivity.syncHelper.OATH_TOKEN_SECRET = oauth_token_secret;
                    ListsActivity.syncHelper.SERVICE = service;
                    ListsActivity.syncHelper.STATS__EMAIL = email;
                    Editor e = getPreferenceScreen().getSharedPreferences().edit();
                    e.putString("service", service);
                    e.putString("oauth_token", oauth_token);
                    e.putString("oauth_token_secret", oauth_token_secret);
                    if("dropbox".equals(service))
                        e.putString("uid", uid);
                    e.putString("stats_email", email);
                    e.commit();

                    updateSyncPrefs();
                    oauth_token = oauth_token_secret = authorize_url = service = null;

                } catch(Exception e)
                {
                    Log.e(TAG + "::Exception", "SERIOUSLY fucked. Something happened in requesting the auth information.");
                    e.printStackTrace();
                }
            }
        } else if(requestCode==IMAGE_ID)
        {
            if(resultCode==RESULT_OK)
            {
                String selectedImagePath = getPath(data.getData());
                Log.d(TAG, "..." + selectedImagePath);
                Editor e = getPreferenceScreen().getSharedPreferences().edit();
                e.putString("background", selectedImagePath);
                e.commit();
            }
        }
    }
    //http://stackoverflow.com/questions/2169649/open-an-image-in-androids-built-in-gallery-app-programmatically
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    
	public static String postData(String URL, String first[], String second[]) throws ClientProtocolException, IOException, org.json.JSONException
	{
        String TAG= QweexUtils.TAG();
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(URL);

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair(first[0], first[1]));
        nameValuePairs.add(new BasicNameValuePair(second[0], second[1]));


        Log.d(TAG, "Posting data..." + URL);
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        HttpResponse response = httpclient.execute(httppost);

        return SyncHelper.convertStreamToString(response.getEntity().getContent());
	}

    OnPreferenceClickListener ClickBG = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            if(preference.equals(notbg))
            {
                Editor e = getPreferenceScreen().getSharedPreferences().edit();
                e.remove("background");
                e.commit();
                return true;
            }
            Intent img = new Intent(Intent.ACTION_GET_CONTENT);
            img.setType("image/*");
            startActivityForResult(img, IMAGE_ID);
            return true;
        }
    };
    
    
    OnPreferenceClickListener ClickReset = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            String TAG= QweexUtils.TAG();
       	 DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
       		    @Override
       		    public void onClick(DialogInterface dialog, int which) {
                    String TAG= QweexUtils.TAG();
       		        switch (which){
       		        case DialogInterface.BUTTON_POSITIVE:
       		        	Log.d(TAG, "WARNING: Resetting data!");
       		        	ListsActivity.syncHelper.db.clearEverything(QuickPrefsActivity.this);
       		        	ListsActivity.listAdapter.changeCursor(ListsActivity.syncHelper.db.getAllLists());
       		        case DialogInterface.BUTTON_NEGATIVE:
       		        }
       		    }
       		};
       		Log.d(TAG, "User has clicked reset");
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
            String TAG= QweexUtils.TAG();
        	Log.d(TAG, "User has clicked About");
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
            String TAG= QweexUtils.TAG();
        	Log.d(TAG, "User has clicked Sync");
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
            String TAG= QweexUtils.TAG();
        	Log.d(TAG, "User has clicked Logout");
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
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
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
    
	public String capitalize(String word)
	{
		return word.substring(0,1).toUpperCase() + word.substring(1);
	}
}