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

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TasksActivity
{
	private static final int ID_MAGIC     = 1;
	private static final int ID_HAND   = 2;
	private static final int ID_TITLE = 3;
	private static final int ID_DATE   = 4;
	private static final int ID_PRIORITY  = 5;	
	
	ListView lv;
	boolean allTasks = false;
	View lastClicked = null, editingTags = null;
	public static String lastClickedID;
	View separator;
	TextView tag_bubble;
	String listName;
	public String listHash;
	ArrayList<String> tasksContents;
	public Activity context;
	
	//@Overload
	public void onCreate(Bundle savedInstanceState)
	{
		//super.onCreate(savedInstanceState)
		//context.requestWindowFeature(Window.FEATURE_NO_TITLE);

		
		
		
		
        sortPopup = new QuickAction(context, QuickAction.VERTICAL);
		
        sortPopup.addActionItem(new ActionItem(ID_MAGIC, "Magic", context.getResources().getDrawable(R.drawable.magic)));
        sortPopup.addActionItem(new ActionItem(ID_HAND, "By hand", context.getResources().getDrawable(R.drawable.hand)));
        sortPopup.addActionItem(new ActionItem(ID_TITLE, "By title", createTitleDrawable()));
        sortPopup.addActionItem(new ActionItem(ID_DATE, "By date", context.getResources().getDrawable(R.drawable.date)));
        sortPopup.addActionItem(new ActionItem(ID_PRIORITY, "By priority", context.getResources().getDrawable(R.drawable.priority)));
        doCreateStuff();
	}
	
	
	public Drawable createTitleDrawable()
	{
		final int DIM = 24;

		Bitmap canvasBitmap = Bitmap.createBitmap(DIM, 
		                                          DIM, 
		                                          Bitmap.Config.ARGB_8888);
		Canvas imageCanvas = new Canvas(canvasBitmap);
		
		Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		imagePaint.setTypeface(Typeface.SERIF);
		imagePaint.setTextAlign(Align.CENTER);
		imagePaint.setTextSize(30f);
		imagePaint.setAntiAlias(true);
		imagePaint.setColor(0xffffffff);

		imageCanvas.drawText("A", 
		                         DIM / 2, 
		                         DIM, 
		                         imagePaint); 
		BitmapDrawable finalImage = new BitmapDrawable(canvasBitmap);
		
		return finalImage.getCurrent();
	}
	
	
	
	QuickAction sortPopup;
	
	//@Override
	/*
	public void onResume()
	{
		//super.onResume();
		String new_theme = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString("theme", "Default");
		int new_themeID = context.getResources().getIdentifier(new_theme, "style", context.getApplicationContext().getPackageName());
		System.out.println(new_themeID + "!=" + ListsActivity.themeID);
		if(new_themeID!=ListsActivity.themeID)
		{
			ListsActivity.themeID = new_themeID;
			doCreateStuff();
		}
	}
	*/
	
	public void doCreateStuff()
	{
		System.out.println("HERE");
		//context.setTheme(ListsActivity.themeID);
		//context.setContentView(R.layout.tasks);
		
		
		ImageButton sortButton = ((ImageButton)context.findViewById(R.id.sortbutton));
        sortButton.setOnClickListener(new OnClickListener()
    	{
    		@Override
    		public void onClick(View v)
    		{
    			sortPopup.show(v);
    		}
        });
        
		lv = (ListView) ((Activity) context).findViewById(R.id.tasksListView);
		lv.setEmptyView(context.findViewById(android.R.id.empty));
		((TextView)context.findViewById(R.id.showTitle)).setText(listName);		
		lv.setOnItemClickListener(selectTask);
        
		Cursor r = ListsActivity.syncHelper.db.getTasksOfList(listHash);
		System.out.println("COUNT: " + r.getCount());
        lv.setAdapter(new TaskAdapter(context, R.layout.task_item, r));
		
	}
	
    
	QuickAction.OnActionItemClickListener selectSort = new QuickAction.OnActionItemClickListener() {			
		@Override
		public void onItemClick(QuickAction source, int pos, int actionId) {				
			ActionItem actionItem = sortPopup.getActionItem(pos);
             
			switch(actionId)
			{
			case ID_MAGIC:
			case ID_HAND:
			case ID_TITLE:
			case ID_DATE:
			case ID_PRIORITY:
			}
		}
	};
    
	
	static void expand(View view)
	{
		if(view.findViewById(R.id.taskInfo).getVisibility()==View.GONE)
		{
		  view.findViewById(R.id.taskName).setVisibility(View.GONE);
		  view.findViewById(R.id.taskTime).setVisibility(View.GONE);
		  view.findViewById(R.id.taskName_edit).setVisibility(View.VISIBLE);
		  ((TextView)view.findViewById(R.id.taskName_edit)).setText(((TextView)view.findViewById(R.id.taskName)).getText());
		  
		  view.findViewById(R.id.taskInfo).setVisibility(View.VISIBLE);
		}
	}
	
	static void collapse(View view)
	{
		if(view.findViewById(R.id.taskInfo).getVisibility()!=View.GONE)
		{
		  view.findViewById(R.id.taskName).setVisibility(View.VISIBLE);
		  view.findViewById(R.id.taskTime).setVisibility(View.VISIBLE);
		  view.findViewById(R.id.taskName_edit).setVisibility(View.GONE);
		  ((TextView)view.findViewById(R.id.taskName)).setText(((TextView)view.findViewById(R.id.taskName_edit)).getText());
		  
		  view.findViewById(R.id.taskInfo).setVisibility(View.GONE);
		}
	}
	
	OnItemClickListener selectTask = new OnItemClickListener() 
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
    	  if(//lastClicked==view &&
    			  view.findViewById(R.id.taskInfo).getVisibility()==View.GONE)
    		  expand(view);
    	  else
    	  {
    		  //lastClicked = view;
    		  //return;
    		  collapse(view);
    	  }
    	  
    	  
    	  if(lastClicked!=view)
    	  {
    		  if(lastClicked!=null)
    			  lastClicked.findViewById(R.id.taskInfo).setVisibility(View.GONE);
			  lastClicked = view;
			  lastClickedID = (String) ((TextView)lastClicked.findViewById(R.id.taskId)).getText();
    	  }
    	  else
    	  {
    		  lastClickedID = "";
    		  lastClicked = null;
    	  }
      }
    };
    
    //@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            doBackThings();
        }
        return true;
        //return super.onKeyDown(keyCode, event);
    }
    
    @TargetApi(5)
	public void onBackPressed()
    {
    	doBackThings();
    }
    
    boolean doBackThings()
    {
    	if(editingTags!=null)
    	{
    		System.out.println("Finished Editing Tags");
    		editingTags.setVisibility(View.GONE);
    		((LinearLayout)editingTags.getParent()).getChildAt(1).setVisibility(View.VISIBLE);
    		editingTags = null;
    	}
    	else if(lastClicked!=null)
    	{
	    	collapse(lastClicked);
	    	lastClicked = null;
	    	lastClickedID = "";
    	}
    	else
    		return true;
    		//context.finish();
    	return false;
    }
    
	
	
	
	static OnLongClickListener pressTag = new OnLongClickListener()
	{
		@Override
		public boolean onLongClick(View v)
		{
			LinearLayout tagparent = (LinearLayout) ((View)v.getParent());
			android.widget.HorizontalScrollView s = (android.widget.HorizontalScrollView) tagparent.getParent();
			LinearLayout sParent = (LinearLayout) s.getParent();
			EditText e = (EditText) sParent.findViewById(R.id.tags_edit);
			for(int i=0; i<(tagparent.getChildCount()); i=i+2)
			{
				if(i>0)
					e.append(", ");
				e.append(((TextView) (tagparent.getChildAt(i))).getText());
			}
			e.setVisibility(View.VISIBLE);
			int n = e.getText().toString().indexOf((String) ((TextView)v).getText());
			e.setSelection(n, n + ((TextView)v).getText().length());
			e.requestFocus();
			s.setVisibility(View.GONE);
			//editingTags = e;
			return true;
		}
	};
}
