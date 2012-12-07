package com.qweex.nitrodroid;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class ListAdapter extends SimpleCursorAdapter
{
    private Cursor c;
    private Context context;

	public ListAdapter(Context context, int layout, Cursor c)
	{
		super(context, layout, c, new String[] {}, new int[] {});
		this.c = c;
		this.context = context;
	}
	
	private int numberOfTags(String s)
	{
		int res = -1, last = 0;
		do
		{
			res++;
			last = s.indexOf('|', last+1);
		} while(last!=-1);
		return res;
	}

	public View getView(int pos, View inView, ViewGroup parent)
	{
		View row = inView;
		if(row==null)
		{
			LayoutInflater inflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			row=inflater.inflate(R.layout.list_item, parent, false);
		}
		this.c = getCursor();
        this.c.moveToPosition(pos);
		
		
		String hash = this.c.getString(this.c.getColumnIndex("hash"));
		String name = this.c.getString(this.c.getColumnIndex("name"));
        String tasks_in_order = this.c.getString(this.c.getColumnIndex("tasks_in_order"));
		
        ((TextView)row.findViewById(R.id.listId)).setText(hash);
        ((TextView)row.findViewById(R.id.listName)).setText(name);
        ((TextView)row.findViewById(R.id.listNumber)).setText(Integer.toString(numberOfTags(tasks_in_order)));
		
		return row;
	}
}