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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class ListAdapter extends SimpleCursorAdapter
{
    int todayCount = 0;
    int totalCount = 0;
    View clickThisPlz;
	//I do not like the existence of these two things but I cbf to work around it
	android.os.Handler hd = new android.os.Handler();
    Runnable rd = new Runnable()
    {
    	@Override
		public void run()
    	{
    		ListsActivity.selectList.onItemClick(null, clickThisPlz, 0, 0);
    	}
    };
    private Cursor c;
    private Context context;

	public ListAdapter(Context context, int layout, Cursor c)
	{
		super(context, layout, c, new String[] {}, new int[] {});
		Log.d("ListAdapter", "Creating a new ListAdapter");
		this.c = c;
		this.context = context;
	}
	
	private int numberOfTags(String s)
	{
		return ListsActivity.syncHelper.db.getTasksOfList(s, null).getCount();
	}

	public View getView(int pos, View inView, ViewGroup parent)
	{
		View row = inView;
		//Inflate it if we are not reusing a view
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

		//Get the data
		String hash = this.c.getString(this.c.getColumnIndex("hash"));
		String name = this.c.getString(this.c.getColumnIndex("name"));
//        String tasks_in_order = this.c.getString(this.c.getColumnIndex("tasks_in_order"));

		//Set that shit!
        row.findViewById(R.id.listId).setTag(hash);
        ((TextView)row.findViewById(R.id.listName)).setText(name);
        if(hash.equals("all"))
        {
        	((TextView)row.findViewById(R.id.listNumber)).setText(Integer.toString(totalCount));
            if(ListsActivity.v2)
            {
                ((ImageView)row.findViewById(R.id.listId)).setVisibility(View.VISIBLE);
                ((ImageView)row.findViewById(R.id.listId)).setImageResource(R.drawable.all);
            }
        }
        else if(hash.equals("inbox"))
        {
            if(ListsActivity.v2)
            {
                ((ImageView)row.findViewById(R.id.listId)).setVisibility(View.VISIBLE);
                ((ImageView)row.findViewById(R.id.listId)).setImageResource(R.drawable.inbox);
            }
        }
        else if(hash.equals("today")) //Today
        {
        	((TextView)row.findViewById(R.id.listNumber)).setText(Integer.toString(todayCount));
        }
        else
        {
            if(hash.equals("logbook"))
            {
                if(ListsActivity.v2)
                {
                    ((ImageView)row.findViewById(R.id.listId)).setVisibility(View.VISIBLE);
                    ((ImageView)row.findViewById(R.id.listId)).setImageResource(R.drawable.completed);
                }
            }
            ((TextView)row.findViewById(R.id.listNumber)).setText(Integer.toString(numberOfTags(hash)));
        }

        //Select it if it is the last used List
        if(ListsActivity.isTablet && hash.equals(ListsActivity.lastList))
		{
			ListsActivity.lastList = null;
			clickThisPlz = row;
			hd.post(rd);
		}

		return row;
	}
}