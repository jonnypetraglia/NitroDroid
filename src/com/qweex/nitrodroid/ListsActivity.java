package com.qweex.nitrodroid;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ListsActivity extends ListActivity
{
	public static JSONObject jObject, jLists, jListDetails;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.lists);
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
	         
	         getListView().setOnItemClickListener(selectList);
	         
	         ArrayList<String> listContents = new ArrayList<String>(listIDs.length());
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
	         
	         
	         
	         getListView().setAdapter(new ListsAdapter(this, R.layout.list_item, listContents));
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}
	
	
	
	
	OnItemClickListener selectList = new OnItemClickListener() 
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
    	  Intent viewList = new Intent(ListsActivity.this, TasksActivity.class);
    	  String content = (String)((TextView)view.findViewById(R.id.listId)).getText();
    	  viewList.putExtra("list", content);
    	  startActivity(viewList);
      }
    };
	
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