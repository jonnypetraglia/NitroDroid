package com.qweex.nitrodroid;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TasksActivity extends Activity
{
	ListView lv;
	boolean allTasks = false;
	View lastClicked = null, editingTags = null;
	String lastClickedID;
	JSONArray tasks;
	JSONObject ALLthetasks;
	View separator;
	TextView tag_bubble;
	float DP;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tasks);
		lv = (ListView) findViewById(R.id.tasksListView);
		lv.setEmptyView(findViewById(android.R.id.empty));
		try {
			JSONObject list = null;
			String listID = getIntent().getExtras().getString("list");
			try {
				list = ListsActivity.jListDetails.getJSONObject(listID);
				allTasks = false;
			} catch(Exception e) {
				allTasks = true;
			}
			String listName;
			if(listID.equals("f"))
				listName = getResources().getString(R.string.Today);
			else if(listID.equals("s"))
				listName = getResources().getString(R.string.Next);
			else if(listID.equals("v"))
				listName = getResources().getString(R.string.Logbook);
			else if(allTasks)
				listName = getResources().getString(R.string.AllTasks);
			else
				listName = list.getString("a");
			setTitle(listName);
			((TextView)findViewById(R.id.showTitle)).setText(listName);
			
			lv.setOnItemClickListener(selectTask);
			
			ALLthetasks = ListsActivity.jObject.getJSONObject("b");
			tasks = allTasks ? ALLthetasks.names()
					: list.getJSONArray("n");
			
			
			ArrayList<String> tasksContents = new ArrayList<String>(tasks.length());
			for(int i=0; i<tasks.length(); i++)
			{
				String id = tasks.getString(i);
				tasksContents.add(id);
			}
			
			lv.setAdapter(new TasksAdapter(this, R.layout.list_item, tasksContents));
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		DP = getResources().getDisplayMetrics().density;
	}
	
	void expand(View view)
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
	
	void collapse(View view)
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
    	  if(view.findViewById(R.id.taskInfo).getVisibility()==View.GONE)
    		  expand(view);
    	  else
    		  collapse(view);
    	  
    	  
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
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            doBackThings();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @TargetApi(5)
	public void onBackPressed()
    {
    	doBackThings();
    }
    
    void doBackThings()
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
    		finish();
    }
	
	public class TasksAdapter extends ArrayAdapter<String> {

		ArrayList<String> lists;
		public TasksAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
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
				row=inflater.inflate(R.layout.task_item, parent, false);
			}
			
			TextView id=(TextView)row.findViewById(R.id.taskId);
			TextView name=(TextView)row.findViewById(R.id.taskName);
			TextView time=(TextView)row.findViewById(R.id.taskTime);
			CheckBox done=(CheckBox)row.findViewById(R.id.taskDone);
			Button timeButton=(Button)row.findViewById(R.id.timeButton);
			Button priority=(Button)row.findViewById(R.id.priority);
			EditText notes=(EditText)row.findViewById(R.id.notes);
			
			row.findViewById(R.id.taskInfo).setVisibility(View.GONE);
			
			
			String data = lists.get(position);
			
			//------ID------
			if(data.equals(lastClickedID))
				expand(row);
			else
				collapse(row);
			id.setText(data);
			try {
			JSONObject item = ALLthetasks.getJSONObject(data);
			
			//------Name------
			name.setText(item.getString("c"));
			
			//------Done Checkmark------
			done.setChecked(!item.getString("j").equals("false"));
			
	        /*
        	j=Finished date ("false" if it's not done yet)
        	c=Name
        	
        	y=Tags
        	d=Priority
        	e=Due date
        	q=Notes
	         */
			
			//------Tags
			JSONArray tgs = item.getJSONArray("y");
			if(tgs.length()>0)
			{
				LinearLayout tag_cont = (LinearLayout)row.findViewById(R.id.tag_container);
				tag_cont.removeAllViews();
				int j=0;
				for(int i=0; i<tgs.length(); i++)
				{
					if(i>0)
					{
						tag_cont.addView(new Separator(tag_cont.getContext()));
						j++;
					}
					tag_cont.addView(new TagView(tag_cont.getContext(), tgs.getString(i)));
				}
			}
			
			//------Priority
			String pri = item.getString("d");
			if(pri.equals("low"))
			{
				done.setButtonDrawable(R.drawable.low_check);
				priority.setBackgroundResource(R.drawable.low_button);
				priority.setText(getResources().getString(R.string.Low));
			}else if(pri.equals("medium"))
			{
				done.setButtonDrawable(R.drawable.med_check);
				priority.setBackgroundResource(R.drawable.med_button);
				priority.setText(getResources().getString(R.string.Medium));
			} else if(pri.equals("high"))
			{
				done.setButtonDrawable(R.drawable.hi_check);
				priority.setBackgroundResource(R.drawable.hi_button);
				priority.setText(getResources().getString(R.string.High));
			} else
			{
				done.setButtonDrawable(R.drawable.none_check);
				priority.setBackgroundResource(R.drawable.none_button);
				priority.setText("none");
			}
			
			//------Date button
			if(!item.getString("e").equals(""))
			{
				long x = Long.parseLong(item.getString("e"));
				Date d = new Date(x);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("E, MMM dd y");
				timeButton.setText(sdf.format(d));
			}
			
			//------Notes
			notes.setText(item.getString("q"));
			
			//------Time for collapsed
			String timeString = "";
			try {
			long c = Long.parseLong(item.getString("e"));
			long d = (new Date()).getTime();
			
			if(c<d)
			{
				long days = (d - c) / 1000 / 60 / 60 / 24;
				if(days==0)
					timeString = "due today";
				else if(days==1)
					timeString = "due yesterday";
				else
					timeString = Long.toString(days) + " days overdue";
			} else
			{
				long days = (c - d) / 1000 / 60 / 60 / 24;
				if(days==0)
					timeString = "due today";
				else if(days==1)
					timeString = "due tomorrow";
				else
					timeString = Long.toString(days) + " days left";
			}
			} catch(Exception e) {}
			
			time.setText(timeString);
			}catch(Exception E) {}
			
			
			return row;
		}
	}
	
	
	
	OnLongClickListener pressTag = new OnLongClickListener()
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
			editingTags = e;
			return true;
		}
	};
	
	private class TagView extends TextView {

		public TagView(Context context, String s) {
			super(context);
			setId( //42
					R.id.tag
					);
			setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
			setPadding((int)(10*DP), (int)(10*DP), (int)(10*DP), (int)(10*DP));
			setTextSize(20*DP);
			setTextColor(0xFF1C759C);
			setOnLongClickListener(pressTag);
			setText(s);
		}
	}
	
	private class Separator extends View {

		public android.widget.LinearLayout.LayoutParams params;
		public Separator(Context context) {
			super(context);
			setBackgroundColor(0xFFe6e6e6);
			params = new android.widget.LinearLayout.LayoutParams((int) DP, android.widget.LinearLayout.LayoutParams.FILL_PARENT);
			this.setLayoutParams(params);
		}
		
	}
}
