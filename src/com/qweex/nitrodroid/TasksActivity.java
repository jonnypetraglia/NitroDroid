package com.qweex.nitrodroid;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import com.qweex.nitrodroid.ListsActivity.ListsAdapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TasksActivity extends Activity
{
	ListView lv;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasks);
		lv = (ListView) findViewById(R.id.tasksListView);
		try {
			String listID = getIntent().getExtras().getString("list");
			JSONObject list = ListsActivity.jListDetails.getJSONObject(listID);
			String listName = list.getString("a");
			setTitle(listName);
			JSONArray tasks = list.getJSONArray("n");
			JSONObject allTasks = ListsActivity.jObject.getJSONObject("b");
			
			//lv.setOnItemClickListener(selectList);
			
			ArrayList<String> tasksContents = new ArrayList<String>(tasks.length());
			for(int i=0; i<tasks.length(); i++)
			{
				String id = tasks.getString(i);
				JSONObject item = allTasks.getJSONObject(id);
				String name = item.getString("c");
				String time = item.getString("e");
				System.out.println();
				tasksContents.add(id + "\n" + name + "\r" + time);
			}
			
			lv.setAdapter(new TasksAdapter(this, R.layout.list_item, tasksContents));
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
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
			String data = lists.get(position);
			id.setText(data.substring(0, data.indexOf('\n')));
			name.setText(data.substring(data.indexOf('\n')+1, data.indexOf('\r')));
			
			String timeString = "";
			try {
			long c = Long.parseLong(data.substring(data.indexOf('\r')+1));
			long d = (new Date()).getTime();
			System.out.println(c);
			System.out.println(d);
			
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
