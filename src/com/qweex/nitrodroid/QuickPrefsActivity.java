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
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;


/** The activity that allows the user to change the preferences.
 * @author MrQweex
 */

public class QuickPrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{    
	public final static String DONATION_APP = "com.qweex.donation";
	
	/** Called when the activity is created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {        
        super.onCreate(savedInstanceState);        
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        Preference myPref = (Preference) findPreference("clear");
        myPref.setOnPreferenceClickListener(ClickReset);
        
    }
    
    OnPreferenceClickListener ClickReset = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
       	 DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
       		    @Override
       		    public void onClick(DialogInterface dialog, int which) {
       		        switch (which){
       		        case DialogInterface.BUTTON_POSITIVE:
       		            //Yes button clicked
       		            break;

       		        case DialogInterface.BUTTON_NEGATIVE:
       		            //No button clicked
       		            break;
       		        }
       		    }
       		};

       		AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
       		builder.setTitle(getResources().getString(R.string.warning));
       		builder.setMessage(getResources().getString(R.string.warning_msg));
       		builder.setPositiveButton(getResources().getString(R.string.yes), dialogClickListener);
       		builder.setNegativeButton(getResources().getString(R.string.no), dialogClickListener);
       		builder.show();
       		return true;
        }
    };
    
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