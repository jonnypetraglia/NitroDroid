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
    int todayCount = 0;
    int totalCount = 0;

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
			if(ListsActivity.isTablet)
			{
				((TextView)row.findViewById(R.id.listName)).setTextSize(ListsActivity.DP*20);
				((TextView)row.findViewById(R.id.listNumber)).setTextSize(ListsActivity.DP*12);
			}
		}
		this.c = getCursor();
        this.c.moveToPosition(pos);
		
		
		String hash = this.c.getString(this.c.getColumnIndex("hash"));
		String name = this.c.getString(this.c.getColumnIndex("name"));
        String tasks_in_order = this.c.getString(this.c.getColumnIndex("tasks_in_order"));
		
        ((TextView)row.findViewById(R.id.listId)).setText(hash);
        ((TextView)row.findViewById(R.id.listName)).setText(name);
        if(hash.equals("all"))
        	((TextView)row.findViewById(R.id.listNumber)).setText(Integer.toString(totalCount));
        else if(hash.equals("today")) //Today
        	((TextView)row.findViewById(R.id.listNumber)).setText(Integer.toString(todayCount));
        else
        	((TextView)row.findViewById(R.id.listNumber)).setText(Integer.toString(numberOfTags(tasks_in_order)));
		
		return row;
	}
}