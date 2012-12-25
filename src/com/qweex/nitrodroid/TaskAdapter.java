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
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ExpandableListAdapter;
import android.database.DataSetObserver;

public class TaskAdapter extends BaseExpandableListAdapter
{
    private Cursor c;
    private TasksActivity.taskObject T;
    private Context context;
    public int lastClicked = -1;
    ArrayList<TasksActivity.taskObject> a;
    public boolean isMagic;

    //God I'm fucking lazy
    public static int[] drawsB = {R.drawable.button_none, R.drawable.button_low, R.drawable.button_med, R.drawable.button_high};
    public static int[] drawsC = {R.drawable.check_none, R.drawable.check_low, R.drawable.check_med, R.drawable.check_high};
    public static int[] drawsS = {R.string.None, R.string.Low, R.string.Medium, R.string.High};
    public static java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy");

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
        //Not implemented
    }
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer)
    {
        //Not implemented
    }
    @Override
    public boolean isEmpty()
    {
        return (isMagic ? a.size() : c.getCount())==0;
    }
    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }
    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }
    @Override
    public int getGroupCount()
    {
        return (isMagic ? a.size() : c.getCount());
    }
    @Override
    public int getChildrenCount(int groupPosition)
    {
        return 1;
    }
    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }
    @Override
    public boolean hasStableIds()
    {
        return true;
    }
    @Override
    public boolean isChildSelectable(int arg0, int arg1)
    {
        return false;
    }


    @Override
    public long getCombinedChildId(long groupId, long childId)
    {
        return 0;
    }
    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return 0;
        //return children.get(groupPosition).get(childPosition);
    }
    @Override
    public Object getGroup(int groupPosition)
    {
        return 0;
        //return groups.get(groupPosition);
    }

    @Override
    public void onGroupCollapsed(int groupPosition)
    {
        View view = TasksActivity.lastClicked;
        if(view!=null)
        {
            view.findViewById(R.id.taskName).setVisibility(View.VISIBLE);
            view.findViewById(R.id.taskTime).setVisibility(View.VISIBLE);
            view.findViewById(R.id.taskName_edit).setVisibility(View.GONE);
            ((TextView)view.findViewById(R.id.taskName)).setText(((TextView)view.findViewById(R.id.taskName_edit)).getText());

/*            View child = ((ViewGroup)view).getChildAt(((ViewGroup)view).getChildCount());
            ((EditText)view.findViewById(R.id.taskName_edit)).removeTextChangedListener(TasksActivity.writeName);
            ((EditText)child.findViewById(R.id.notes)).removeTextChangedListener(TasksActivity.writeNotes);
            ((android.widget.Button)child.findViewById(R.id.priority)).setOnClickListener(null);
            ((android.widget.Button)child.findViewById(R.id.timeButton)).setOnClickListener(null); */
        }
    }
    @Override
    public void onGroupExpanded(int groupPosition)
    {
        System.out.println("HERP");
        View view = TasksActivity.lastClicked;
        if(view!=null && false)
        {
            view.findViewById(R.id.taskName).setVisibility(View.GONE);
            view.findViewById(R.id.taskTime).setVisibility(View.GONE);
            view.findViewById(R.id.taskName_edit).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.taskName_edit)).setText(((TextView)view.findViewById(R.id.taskName)).getText());
        }
    }

    // Return a group view. You can load your custom layout here.
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View row, ViewGroup parent)
    {
        if(row==null)
        {
            LayoutInflater inflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            row=inflater.inflate(R.layout.task_item, parent, false);
        }
        if(isMagic)
            T = this.a.get(groupPosition);
        else
            this.c.moveToPosition(groupPosition);

        TextView id=(TextView)row.findViewById(R.id.taskId);
        TextView name=(TextView)row.findViewById(R.id.taskName);
        EditText name_edit=(EditText)row.findViewById(R.id.taskName_edit);
        TextView time=(TextView)row.findViewById(R.id.taskTime);
        CheckBox done=(CheckBox)row.findViewById(R.id.taskDone);
        String hash = (isMagic ? T.hash :  c.getString(c.getColumnIndex("hash")));
        //------ID------
		if(hash.equals(TasksActivity.lastClickedID))
		{
			TasksActivity.lastClicked = row;
		}

        id.setText(hash);
        //------Name & Done checkmark------
        name.setText((isMagic ? T.name : c.getString(c.getColumnIndex("name"))));
        name_edit.setText((isMagic ? T.name : c.getString(c.getColumnIndex("name"))));
        done.setChecked((isMagic ? T.logged : c.getLong(c.getColumnIndex("logged")))>0);
        int pri = (isMagic ? T.priority : c.getInt(c.getColumnIndex("priority")));
        done.setButtonDrawable(drawsC[pri]);
        ((EditText)row.findViewById(R.id.taskName_edit)).addTextChangedListener(TasksActivity.writeName);

        long dat = (isMagic ? T.date : c.getLong(c.getColumnIndex("date")));
        String timeString = getTimeString(dat, name.getContext());
        time.setText(timeString);
        return row;
    }
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View row, ViewGroup parent)
    {
        if(row==null)
        {
            LayoutInflater inflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            row=inflater.inflate(R.layout.task_item_details, parent, false);
        }
        this.c.moveToPosition(groupPosition);
        String hash = (isMagic ? T.hash : c.getString(c.getColumnIndex("hash")));
        Button timeButton=(Button)row.findViewById(R.id.timeButton);
        Button priority=(Button)row.findViewById(R.id.priority);
        EditText notes=(EditText)row.findViewById(R.id.notes);


		//------Tags------
		((EditText)row.findViewById(R.id.tags_edit)).setText(c.getString(c.getColumnIndex("tags")));

		if(!(isMagic ? T.tags : c.getString(c.getColumnIndex("tags"))).equals("") && false)
		{
			String[] tgs = (isMagic ? T.tags : c.getString(c.getColumnIndex("tags"))).split(",");
			LinearLayout tag_cont = (LinearLayout)row.findViewById(R.id.tag_container);
			tag_cont.removeAllViews();
			for(int i=0; i<tgs.length; i++)
			{
				if(i>0)
					tag_cont.addView(new Separator(tag_cont.getContext()));
				tag_cont.addView(new TagView(tag_cont.getContext(), tgs[i]));
			}
		}
        LinearLayout la = (LinearLayout)row.findViewById(R.id.tag_container);
        String s = ((EditText)row.findViewById(R.id.tags_edit)).getText().toString();
        TasksActivity.getThemTagsSon(la,s);

		//------Priority
		int pri = (isMagic ? T.priority : c.getInt(c.getColumnIndex("priority")));
		priority.setBackgroundResource(drawsB[pri]);
		priority.setTag(pri);
		priority.setText(drawsS[pri]); //context.getResources().getString(R.string.Low));
        priority.setOnClickListener(ListsActivity.ta.pressPriority);
		//------Date button
        long dat = (isMagic ? T.date : c.getLong(c.getColumnIndex("date")));
		if(dat>0)
		{
			Date d = new Date(dat);
			System.out.println(sdf.format(d));
			timeButton.setText(sdf.format(d));
		}
		else
			timeButton.setText(R.string.None);
		timeButton.setTag(dat);
        timeButton.setOnClickListener(ListsActivity.ta.pressDate);

		//------Notes
		notes.setText((isMagic ? T.notes : c.getString(c.getColumnIndex("notes"))));
        ((EditText)row.findViewById(R.id.notes)).addTextChangedListener(TasksActivity.writeNotes);
        return row;
    }



    public long getCombinedGroupId(long groupId) {return 0;}
	public TaskAdapter(Context context, int layout, Cursor c)
	{
		this.c = c;
		this.context = context;
        this.isMagic = false;
	}

    public TaskAdapter(Context context, int layout, ArrayList<TasksActivity.taskObject> objects)
    {
        this.a = objects;
        this.context = context;
        this.isMagic = true;
    }
	
	/*
	j=Finished date ("false" if it's not done yet)
	c=Name
	
	y=Tags
	d=Priority
	e=Due date
	q=Notes
     */

	static public String getTimeString(long dat, Context context)
	{
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
		return timeString;
	}
	
	
	
	static public class TagView extends TextView {

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
	
	static public class Separator extends View {

		public android.widget.LinearLayout.LayoutParams params;
		public Separator(Context context) {
			super(context);
			setBackgroundColor(0xFFe6e6e6);
			params = new android.widget.LinearLayout.LayoutParams((int) ListsActivity.DP, android.widget.LinearLayout.LayoutParams.FILL_PARENT);
			this.setLayoutParams(params);
		}
		
	}
}