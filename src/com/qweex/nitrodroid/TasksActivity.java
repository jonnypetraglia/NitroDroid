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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TasksActivity extends Activity
{
	ListView lv;
	boolean allTasks = false;
	View lastClicked = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasks);
		lv = (ListView) findViewById(R.id.tasksListView);
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
			
			JSONObject ALLthetasks = ListsActivity.jObject.getJSONObject("b");
			JSONArray tasks = allTasks ? ALLthetasks.names()
					: list.getJSONArray("n");
			
			
			ArrayList<String> tasksContents = new ArrayList<String>(tasks.length());
			for(int i=0; i<tasks.length(); i++)
			{
				String id = tasks.getString(i);
				JSONObject item = ALLthetasks.getJSONObject(id);
				try {
				String name = item.getString("c");
				String time = item.getString("e");
				boolean done = !item.getString("j").equals("false");
				tasksContents.add(id + "\n" + name + "\r" + time + "\n" + done);
				} catch(Exception e) {}
			}
			
			lv.setAdapter(new TasksAdapter(this, R.layout.list_item, tasksContents));
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	OnItemClickListener selectTask = new OnItemClickListener() 
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
    	  if(view.findViewById(R.id.taskInfo).getVisibility()==View.GONE)
    	  {
    		  view.findViewById(R.id.taskName).setVisibility(View.GONE);
    		  view.findViewById(R.id.taskTime).setVisibility(View.GONE);
    		  view.findViewById(R.id.taskName_edit).setVisibility(View.VISIBLE);
    		  ((TextView)view.findViewById(R.id.taskName_edit)).setText(((TextView)view.findViewById(R.id.taskName)).getText());
    		  
    		  view.findViewById(R.id.taskInfo).setVisibility(View.VISIBLE);
    	  }
    	  else
    	  {
    		  view.findViewById(R.id.taskName).setVisibility(View.VISIBLE);
    		  view.findViewById(R.id.taskTime).setVisibility(View.VISIBLE);
    		  view.findViewById(R.id.taskName_edit).setVisibility(View.GONE);
    		  ((TextView)view.findViewById(R.id.taskName)).setText(((TextView)view.findViewById(R.id.taskName_edit)).getText());
    		  
    		  view.findViewById(R.id.taskInfo).setVisibility(View.GONE);
    	  }
    	  
    	  
    	  if(lastClicked!=view)
    	  {
    		  if(lastClicked!=null)
    			  lastClicked.findViewById(R.id.taskInfo).setVisibility(View.GONE);
			  lastClicked = view;
    	  }
    	  else
    		  lastClicked = null;
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
    	if(lastClicked!=null)
    	{
	    	View view = lastClicked;
	 		  view.findViewById(R.id.taskName).setVisibility(View.VISIBLE);
			  view.findViewById(R.id.taskTime).setVisibility(View.VISIBLE);
			  view.findViewById(R.id.taskName_edit).setVisibility(View.GONE);
			  ((TextView)view.findViewById(R.id.taskName)).setText(((TextView)view.findViewById(R.id.taskName_edit)).getText());
			  
			  view.findViewById(R.id.taskInfo).setVisibility(View.GONE);
			  lastClicked = null;
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
			String data = lists.get(position);
			int n1 = data.indexOf('\n');
			id.setText(data.substring(0, n1));
			name.setText(data.substring(n1+1, data.indexOf('\r')));
			done.setChecked(
					!data.substring(data.indexOf('\n',n1+1)+1).equals("false")
					);
			
			String timeString = "";
			try {
				int r1=data.indexOf('\r')+1;
			long c = Long.parseLong(data.substring(r1, data.indexOf('\n',r1)));
			long d = (new Date()).getTime();
			System.out.println("c=" + c);
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
			
			return row;
		}
	}
}
