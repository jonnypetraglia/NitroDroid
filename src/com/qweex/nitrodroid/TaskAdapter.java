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


import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class TaskAdapter extends SimpleCursorAdapter
{
    private Cursor c;
    private Context context;
    
    //God I'm fucking lazy
    int[] drawsB = {R.drawable.button_none, R.drawable.button_low, R.drawable.button_med, R.drawable.button_high};
	int[] drawsC = {R.drawable.check_none, R.drawable.check_low, R.drawable.check_med, R.drawable.check_high};
	int[] drawsS = {R.string.None, R.string.Low, R.string.Medium, R.string.High};

	public TaskAdapter(Context context, int layout, Cursor c)
	{
		super(context, layout, c, new String[] {}, new int[] {});
		this.c = c;
		this.context = context;
	}
	
	/*
	j=Finished date ("false" if it's not done yet)
	c=Name
	
	y=Tags
	d=Priority
	e=Due date
	q=Notes
     */
	public View getView(int pos, View inView, ViewGroup parent)
	{
		View row = inView;
		if(row==null)
		{
			LayoutInflater inflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			row=inflater.inflate(R.layout.task_item, parent, false);
		}
        this.c.moveToPosition(pos);
		
        
		TextView id=(TextView)row.findViewById(R.id.taskId);
		TextView name=(TextView)row.findViewById(R.id.taskName);
		TextView time=(TextView)row.findViewById(R.id.taskTime);
		CheckBox done=(CheckBox)row.findViewById(R.id.taskDone);
		Button timeButton=(Button)row.findViewById(R.id.timeButton);
		Button priority=(Button)row.findViewById(R.id.priority);
		EditText notes=(EditText)row.findViewById(R.id.notes);
		
		row.findViewById(R.id.taskInfo).setVisibility(View.GONE);
		
		String hash = c.getString(c.getColumnIndex("hash"));
		//------ID------
		if(hash.equals(TasksActivity.lastClickedID))
		{
			System.out.println("Urg.  id = " + hash);
			TasksActivity.expand(row);
			//TasksActivity.lastClicked = row;
		}
		else
		{
			System.out.println("Urg.  " + hash  + "!=" + TasksActivity.lastClickedID);
			TasksActivity.collapse(row);
		}
		
		id.setText(hash);
		//------Name & Done checkmark------
		name.setText(c.getString(c.getColumnIndex("name")));
		done.setChecked(c.getLong(c.getColumnIndex("logged"))>0);
		
		//------Tags------
		if(!c.getString(c.getColumnIndex("tags")).equals(""))
		{
			String[] tgs = c.getString(c.getColumnIndex("tags")).split(",");
			LinearLayout tag_cont = (LinearLayout)row.findViewById(R.id.tag_container);
			tag_cont.removeAllViews();
			for(int i=0; i<tgs.length; i++)
			{
				System.out.println("BLAAARGGGGG " + tgs[i]);
				if(i>0)
					tag_cont.addView(new Separator(tag_cont.getContext()));
				tag_cont.addView(new TagView(tag_cont.getContext(), tgs[i]));
			}
		}
		
		//------Priority
		int pri = c.getInt(c.getColumnIndex("priority"));
		
		done.setButtonDrawable(drawsC[pri]);
		priority.setBackgroundResource(drawsB[pri]);
		priority.setText(drawsS[pri]); //context.getResources().getString(R.string.Low));
		
		//------Date button
		long dat = c.getLong(c.getColumnIndex("date"));
		if(dat>0)
		{
			Date d = new Date(dat);
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("E, MMM dd y");
			timeButton.setText(sdf.format(d));
		}
		else
			timeButton.setText(R.string.None);
		
		//------Notes
		notes.setText(c.getString(c.getColumnIndex("notes")));
		
		//------Time for collapsed
		
		long d = (new Date()).getTime();
		String timeString;
		if(dat==0)
			timeString = "";
		else if(dat<d)
		{
			long days = (d - dat) / 1000 / 60 / 60 / 24;
			if(days==0)
				timeString = context.getResources().getString(R.string.due_today);
			else if(days==1)
				timeString = context.getResources().getString(R.string.due_yesterday);
			else
				timeString = Long.toString(days) + " " + context.getResources().getString(R.string.days_overdue);
		} else
		{
			long days = (dat - d) / 1000 / 60 / 60 / 24;
			if(days==0)
				timeString = context.getResources().getString(R.string.due_today);
			else if(days==1)
				timeString = context.getResources().getString(R.string.due_yesterday);
			else
				timeString = Long.toString(days) + " " + context.getResources().getString(R.string.days_left);
		}
		time.setText(timeString);
		return row;
	}
	
	
	
	private class TagView extends TextView {

		public TagView(Context context, String s) {
			super(context);
			float DP = ListsActivity.DP;
			setId(R.id.tag);
			setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
			setPadding((int)(10*DP), (int)(10*DP), (int)(10*DP), (int)(10*DP));
			setTextSize(20*DP);
			setTextColor(0xFF1C759C);
			setOnLongClickListener(TasksActivity.pressTag);
			setText(s);
		}
	}
	
	private class Separator extends View {

		public android.widget.LinearLayout.LayoutParams params;
		public Separator(Context context) {
			super(context);
			setBackgroundColor(0xFFe6e6e6);
			params = new android.widget.LinearLayout.LayoutParams((int) ListsActivity.DP, android.widget.LinearLayout.LayoutParams.FILL_PARENT);
			this.setLayoutParams(params);
		}
		
	}
}