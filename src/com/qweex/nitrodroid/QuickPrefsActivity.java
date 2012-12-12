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
import android.widget.PopupWindow;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;


/** The activity that allows the user to change the preferences.
 * @author MrQweex
 */

public class QuickPrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{    
	public final static String DONATION_APP = "com.qweex.donation";
	PopupWindow aboutWindow;
	String[] themes;
	
	/** Called when the activity is created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        ((Preference) findPreference("clear")).setOnPreferenceClickListener(ClickReset);
        ((Preference) findPreference("about")).setOnPreferenceClickListener(ClickAbout);
        
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
    	aboutWindow.setAnimationStyle(R.style.AboutShow);
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
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // Override back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (aboutWindow.isShowing()) {
                aboutWindow.dismiss();
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