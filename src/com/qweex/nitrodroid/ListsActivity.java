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

import java.io.InputStream;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ViewFlipper;

public class ListsActivity extends Activity
{
	public int themeID;
	
	public String SERVICE, OATH_TOKEN_SECRET, OATH_TOKEN, UID,
			      STATS__UID, STATS__OS, STATS__LANGUAGE, STATS__VERSION;
	
	public static String listHash; 
	ListView mainListView;
	TasksActivity ta;
	public static ViewFlipper flip;
	public static boolean isTablet = false;
	public static SyncHelper syncHelper;
	public static float DP;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		SERVICE = "dropbox";
		OATH_TOKEN_SECRET = "k34znqvh8cgftb4";
		OATH_TOKEN = "5bnt7mpm6sgoprb";
		UID = "336890";
		STATS__UID = "notbryant@gmail.com";
		STATS__OS = "android";
		STATS__LANGUAGE = "english";
		STATS__VERSION = "1.5";
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		DP = this.getResources().getDisplayMetrics().density;
	}
	
	
	//WHY THE FUCK WON'T THESE WORK
	//http://stackoverflow.com/questions/5832368/tablet-or-phone-android
	@TargetApi(4)
	public boolean isTablet2(android.content.Context context) {
	    boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
	    boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
	    return (xlarge || large);
	}
	
	public boolean isTablet(android.content.Context context) {
	    try {
	        // Compute screen size
	        android.util.DisplayMetrics dm = context.getResources().getDisplayMetrics();
	        float screenWidth  = dm.widthPixels / dm.xdpi;
	        float screenHeight = dm.heightPixels / dm.ydpi;
	        double size = Math.sqrt(Math.pow(screenWidth, 2) +
	                                Math.pow(screenHeight, 2));
	        // Tablet devices should have a screen size greater than 6 inches
	        return size >= 6;
	    } catch(Throwable t) {
	        return false;
	    }
	} 
	
	@TargetApi(4)
	public static boolean isTabletDevice(android.content.Context activityContext) {
	    // Verifies if the Generalized Size of the device is XLARGE to be
	    // considered a Tablet
	    boolean xlarge = ((activityContext.getResources().getConfiguration().screenLayout & 
	                        Configuration.SCREENLAYOUT_SIZE_MASK) >=	//Changed this from == to >= because my tablet was returning 8 instead of 4. 
	                        Configuration.SCREENLAYOUT_SIZE_LARGE);
	    
	    
	    // If XLarge, checks if the Generalized Density is at least MDPI (160dpi)
	    if (xlarge) {
	        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
	        Activity activity = (Activity) activityContext;
	        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
	        
	        System.out.println(metrics.densityDpi + "adsadsa" + android.util.DisplayMetrics.DENSITY_MEDIUM);
	        
	        // MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160, DENSITY_TV=213, DENSITY_XHIGH=320
	        if (metrics.densityDpi == android.util.DisplayMetrics.DENSITY_DEFAULT
	                || metrics.densityDpi == android.util.DisplayMetrics.DENSITY_HIGH
	                || metrics.densityDpi == android.util.DisplayMetrics.DENSITY_MEDIUM
	                //|| metrics.densityDpi == android.util.DisplayMetrics.DENSITY_TV
	                || metrics.densityDpi == android.util.DisplayMetrics.DENSITY_XHIGH) {

	            return true;
	        }
	    }

	    return false;
	}
	
	
	public void doCreateStuff()
	{
		setTheme(themeID);
		
		if(isTabletDevice(this))
		{
		    isTablet = true;
		    setContentView(R.layout.tablet);
		    findViewById(R.id.taskTitlebar).setVisibility(View.GONE);
		}
		else
		{
			isTablet = false;
			setContentView(R.layout.phone);
		}
		
		System.out.println("isTablet: " + isTablet);
		
		
		flip = (ViewFlipper) findViewById(R.id.FLIP);
		mainListView = (ListView) findViewById(android.R.id.list);
		((ImageButton) findViewById(R.id.settings)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent x = new Intent(ListsActivity.this, QuickPrefsActivity.class);
				startActivity(x);
			}
         });
         mainListView.setOnItemClickListener(selectList);
         mainListView.setEmptyView(findViewById(R.id.empty1));
         
         
         syncHelper = new SyncHelper(this);
         try {
         	InputStream input = getAssets().open("nitro_data.json");
             
         	 syncHelper.db.clearEverything(this);
         	
             int size = input.available();
             byte[] buffer = new byte[size];
             input.read(buffer);
             input.close();
             

             // byte buffer into a string
             String text = new String(buffer);
             syncHelper.readJSONtoSQL(text, this);
     		} catch(Exception e) {
     			e.printStackTrace();
     		}
         
         Cursor r = syncHelper.db.getAllLists();
         System.out.println(r.getCount());
         mainListView.setAdapter(new ListAdapter(ListsActivity.this, R.layout.list_item, r));
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		String new_theme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "Default");
		int new_themeID = getResources().getIdentifier(new_theme, "style", getApplicationContext().getPackageName());
		if(new_themeID!=themeID)
		{
			themeID = new_themeID;
			doCreateStuff();
		}
	}
	
	OnItemClickListener selectList = new OnItemClickListener() 
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
    	  //String name = (String) ((TextView)view.findViewById(R.id.listName)).getText();
    	  //((TextView)findViewById(R.id.taskTitlebar)).setText(name);
    	  if(ta==null)
    		  ta = new TasksActivity();
    	  ta.listHash = (String)((TextView)view.findViewById(R.id.listId)).getText();
    	  System.out.println("Selected: " + ta.listHash);
    	  ta.context = (Activity) view.getContext();
    	  ta.onCreate(null);
    	  if(!isTablet && flip!=null)
          {
    		  flip.setInAnimation(view.getContext(), R.anim.slide_in_right);
    		  flip.setOutAnimation(view.getContext(), R.anim.slide_out_left);
    		  flip.showNext();
          }
    	  /*
    	  Intent viewList = new Intent(ListsActivity.this, TasksActivity.class);
    	  startActivity(viewList);
    	  */
      }
    };
    
    @TargetApi(5)
	public void onBackPressed()
    {
    	doBackThings();
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            doBackThings();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    void doBackThings()
    {
    	if(ta==null || isTablet)
    		finish();
    	else
    	{
    		if(!ta.doBackThings())
    			return;
            flip.setInAnimation(this, android.R.anim.slide_in_left);
            flip.setOutAnimation(this, android.R.anim.slide_out_right);
            ta = null;
    		flip.showPrevious();
    	}
    	
    }
    
}