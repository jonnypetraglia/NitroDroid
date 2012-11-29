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