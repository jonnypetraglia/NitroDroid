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
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ViewFlipper;

public class ListsActivity extends Activity
{
	public static JSONObject jObject, jLists, jListDetails;
	public static int themeID;
	
	public String SERVICE, OATH_TOKEN_SECRET, OATH_TOKEN, UID,
			      STATS__UID, STATS__OS, STATS__LANGUAGE, STATS__VERSION;
	
	ArrayList<String> listContents;
	public static String listID; 
	ListView mainListView;
	TasksActivity ta;
	public static ViewFlipper flip;
	public static boolean isTablet = false; 
	
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
		try {
		if((getResources().getConfiguration().getClass().getDeclaredField("screenLayout").getInt(getResources().getConfiguration())
				& Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
		{
		    isTablet = true;
		    setContentView(R.layout.lists);

		}
		} catch(Exception e)
		{
			isTablet = false;
			setContentView(R.layout.phone);
		}
		
		
	    try {
	    	InputStream input = getAssets().open("nitro_data.json");
	         
	         int size = input.available();
	         byte[] buffer = new byte[size];
	         input.read(buffer);
	         input.close();
	         
	
	         // byte buffer into a string
	         String text = new String(buffer);
	         
	         
	         jObject = new JSONObject(text);
	         jLists = jObject.getJSONObject("i");
	         JSONArray listIDs = jLists.getJSONArray("n");
	         jListDetails = jLists.getJSONObject("r");
	         
	         
	         listContents = new ArrayList<String>(listIDs.length());
	         //Today
	         {
	        	 JSONObject item = jListDetails.getJSONObject("f");
	        	 int count = item.getJSONArray("n").length();
	        	 listContents.add("f\n" + getResources().getString(R.string.Today) + "\r" + Integer.toString(count));
	         }
	         //Next
	         {
	        	 JSONObject item = jListDetails.getJSONObject("s");
	        	 int count = item.getJSONArray("n").length();
	        	 listContents.add("s\n" + getResources().getString(R.string.Next) + "\r" + Integer.toString(count));
	         }
	         //Logbook
	         {
	        	 JSONObject item = jListDetails.getJSONObject("v");
	        	 int count = item.getJSONArray("n").length();
	        	 listContents.add("v\n" + getResources().getString(R.string.Logbook) + "\r" + Integer.toString(count));
	         }
	         //All
	         {
	        	 JSONObject item = ListsActivity.jObject.getJSONObject("b");
	        	 JSONArray j = item.names();
	        	 int count = 0;
	        	 for(int i=0; i<j.length(); i++)
	        	 {
	        		 JSONObject x = item.getJSONObject(j.getString(i));
	        		 try {
	        			 if(x.getString("j").equals("false"))
	        				 count++;
	        		 } catch(Exception e){};
	        	 }
	        	 listContents.add(" \n" + getResources().getString(R.string.AllTasks) + "\r" + Integer.toString(count));
	         }
	         for (int i = 0; i < listIDs.length(); i++)
	         {
	        	 JSONObject item = jListDetails.getJSONObject(listIDs.getString(i));
	        	 int count = item.getJSONArray("n").length();
	        	 
	        	 listContents.add(listIDs.getString(i) + "\n" + item.getString("a") + "\r" + Integer.toString(count));
	         }
	         
	         String new_theme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "Default");
	         themeID = getResources().getIdentifier(new_theme, "style", getApplicationContext().getPackageName());
	         setTheme(themeID);
	         doCreateStuff();
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	
	public void doCreateStuff()
	{
		setTheme(themeID);
		setContentView(R.layout.lists);
		mainListView = (ListView) findViewById(android.R.id.list);
		((ImageButton) findViewById(R.id.settings)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent x = new Intent(ListsActivity.this, QuickPrefsActivity.class);
				startActivity(x);
			}
         });
         mainListView.setOnItemClickListener(selectList);
         mainListView.setAdapter(new ListsAdapter(this, R.layout.list_item, listContents));
         
         
         flip = (ViewFlipper) findViewById(R.id.FLIP);
         //flip.setFlipInterval(100);
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
    	  
    	  ta = new TasksActivity();
    	  System.out.println("DSADS");
    	  ta.listID = (String)((TextView)view.findViewById(R.id.listId)).getText();
    	  System.out.println("DSADSSSS");
    	  ta.context = (Activity) view.getContext();
    	  System.out.println("DSADSASDSA");
    	  ta.onCreate(null);
    	  System.out.println("DSADSDDDDD");
          flip.setInAnimation(view.getContext(), R.anim.slide_in_right);
          flip.setOutAnimation(view.getContext(), R.anim.slide_out_left);
          System.out.println("DSADSAAAAAA");
    	  flip.showNext();
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
    
    
	
	public class ListsAdapter extends ArrayAdapter<String> {

		ArrayList<String> lists;
		public ListsAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
			super(context, textViewResourceId, objects);
			lists = objects;
		}

		@Override
		public View getView(int position, View inView, ViewGroup parent)
		{
			View row = inView;
			if(row==null)
			{
				LayoutInflater inflater=getLayoutInflater();
				row=inflater.inflate(R.layout.list_item, parent, false);
			}
			
			TextView id=(TextView)row.findViewById(R.id.listId);
			TextView name=(TextView)row.findViewById(R.id.listName);
			TextView count=(TextView)row.findViewById(R.id.listNumber);
			String data = lists.get(position);
			id.setText(data.substring(0, data.indexOf('\n')));
			name.setText(data.substring(data.indexOf('\n')+1, data.indexOf('\r')));
			count.setText(data.substring(data.indexOf('\r')+1));
	
			return row;
		}
	}
}