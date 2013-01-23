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


import java.text.DateFormat;
import java.util.Calendar;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.database.DataSetObserver;

public class TaskAdapter extends BaseExpandableListAdapter
{
    private Cursor c;
    private TasksActivity.taskObject T;
    private Context context;
    public int lastClicked = -1;
    ArrayList<TasksActivity.taskObject> a;
    public boolean isMagic;
    protected static int ID_TAG = 11111;
    private String days[];

    //God I'm fucking lazy
    public static int[] drawsB = {R.drawable.button_none, R.drawable.button_low, R.drawable.button_med, R.drawable.button_high};
    public static int[] drawsC = {R.drawable.check_none, R.drawable.check_low, R.drawable.check_med, R.drawable.check_high};
    public static int[] drawsS = {R.string.None, R.string.Low, R.string.Medium, R.string.High};
    public static int[] v2_clrs = {R.color.priority_none, R.color.priority_low, R.color.priority_medium, R.color.priority_high};
    //public static java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy");
    public static DateFormat sdf = DateFormat.getDateInstance(DateFormat.LONG);

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

            ((EditText)view.findViewById(R.id.taskName_edit)).removeTextChangedListener(TasksActivity.writeName);

            View child = ((ViewGroup)((ViewGroup)view.getParent()).getParent()).getChildAt(1);   //This is the root view of task_item_details
            ((EditText)child.findViewById(R.id.notes)).removeTextChangedListener(TasksActivity.writeNotes);
            ((android.widget.Button)child.findViewById(R.id.priority)).setOnClickListener(null);
            ((android.widget.Button)child.findViewById(R.id.timeButton)).setOnClickListener(null); //*/
        }
    }

    // Return a group view. You can load your custom layout here.
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View row, ViewGroup parent)
    {
        Log.d("HERP", "Getting parent view");
        if(row==null)
        {
            LayoutInflater inflater=(LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            row=inflater.inflate(R.layout.task_item, parent, false);
        }
        if(isMagic)
            T = this.a.get(groupPosition);
        else
            this.c.moveToPosition(groupPosition);

        View id= row.findViewById(R.id.taskId);
        TextView name=(TextView)row.findViewById(R.id.taskName);
        EditText name_edit=(EditText)row.findViewById(R.id.taskName_edit);
        TextView time=(TextView)row.findViewById(R.id.taskTime);
        CheckBox done=(CheckBox)row.findViewById(R.id.taskDone);
        String hash = (isMagic ? T.hash :  c.getString(c.getColumnIndex("hash")));

        //Typeface
        name.setTypeface(ListsActivity.theTypeface);
        name_edit.setTypeface(ListsActivity.theTypeface);
        time.setTypeface(ListsActivity.theTypeface);

        //------ID------
		if(hash.equals(TasksActivity.lastClickedID))
		{
			TasksActivity.lastClicked = row;
		}

        id.setTag(hash);
        id.setTag(R.id.priority, isMagic ? T.priority : c.getString(c.getColumnIndex("priority")));
        //------Name & Done checkmark------
        name.setText((isMagic ? T.name : c.getString(c.getColumnIndex("name"))));
        name_edit.setText((isMagic ? T.name : c.getString(c.getColumnIndex("name"))));
        boolean isDone = (isMagic ? T.logged : c.getLong(c.getColumnIndex("logged")))>0;
        done.setChecked(isDone);
        int pri = (isMagic ? T.priority : c.getInt(c.getColumnIndex("priority")));
        if(ListsActivity.v2)
        {
            row.findViewById(R.id.taskId).setBackgroundColor(row.getContext().getResources().getColor(v2_clrs[isDone ? 0 : pri]));
            id.setVisibility(View.VISIBLE);
        }
        else
            done.setButtonDrawable(drawsC[pri]);
        done.setOnClickListener(TasksActivity.checkTask);

//        ((EditText)row.findViewById(R.id.taskName_edit)).addTextChangedListener(TasksActivity.writeName);

        long dat = (isMagic ? T.date : c.getLong(c.getColumnIndex("date")));
        String timeString = getTimeString(dat, name.getContext());
        time.setText(timeString);

        //This essentially takes the place of onGroupExpanded
        if(isExpanded && TasksActivity.lastClicked!=null)
        {
            TasksActivity.lastClicked.findViewById(R.id.taskName).setVisibility(View.GONE);
            TasksActivity.lastClicked.findViewById(R.id.taskTime).setVisibility(View.GONE);
            TasksActivity.lastClicked.findViewById(R.id.taskName_edit).setVisibility(View.VISIBLE);
            ((TextView)TasksActivity.lastClicked.findViewById(R.id.taskName_edit)).setText(((TextView)TasksActivity.lastClicked.findViewById(R.id.taskName)).getText());
            ((EditText)TasksActivity.lastClicked.findViewById(R.id.taskName_edit)).addTextChangedListener(TasksActivity.writeName);
            //onGroupExpanded(groupPosition);
        }
        //else
        //    onGroupCollapsed(groupPosition);

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

        //Typeface
        timeButton.setTypeface(ListsActivity.theTypeface);
        priority.setTypeface(ListsActivity.theTypeface);
        notes.setTypeface(ListsActivity.theTypeface);


		//------Tags------
        if(ListsActivity.v2)
        {
            row.findViewById(R.id.tag_scroller).setVisibility(View.GONE);
        }
        else
        {
		    ((EditText)row.findViewById(R.id.tags_edit)).setText(c.getString(c.getColumnIndex("tags")));
            LinearLayout tag_cont = (LinearLayout)row.findViewById(R.id.tag_container);
            tag_cont.setOnLongClickListener(ListsActivity.ta.longPressTag);

            if(!((isMagic ? T.tags : c.getString(c.getColumnIndex("tags"))).equals("")))
            {
                TasksActivity.getThemTagsSon(tag_cont, (isMagic ? T.tags : c.getString(c.getColumnIndex("tags"))) );
            }
        }

		//------Priority
		int pri = (isMagic ? T.priority : c.getInt(c.getColumnIndex("priority")));
        boolean isDone = (isMagic ? T.logged : c.getLong(c.getColumnIndex("logged")))>0;
        if(ListsActivity.v2)
        {
            View tid = row.findViewById(R.id.taskId2);
            tid.setBackgroundColor(row.getContext().getResources().getColor(v2_clrs[isDone ? 0 : pri]));
            tid.setVisibility(View.VISIBLE);
        }

        priority.setBackgroundResource(drawsB[pri]);
		priority.setTag(pri);
		priority.setText(context.getString(R.string.priority) + ": " + context.getString(drawsS[pri]));
        priority.setOnClickListener(ListsActivity.ta.pressPriority);
		//------Date button
        long dat = (isMagic ? T.date : c.getLong(c.getColumnIndex("date")));
		if(dat>0)
		{
            Calendar d = Calendar.getInstance();
            d.setTimeInMillis(dat);
			timeButton.setText(days[d.get(Calendar.DAY_OF_WEEK)-1] + ", " + sdf.format(d.getTime()));
		}
		else
			timeButton.setText(R.string.no_date_set);
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
        days = context.getResources().getStringArray(R.array.weekdays);
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
		long d = (new java.util.Date()).getTime();
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
	
	
	
	static public class TagView extends TextView
    {
        boolean isEmpty = false;

		public TagView(Context context, String s) {
			super(context);
            setId(ID_TAG);
            create(context, s);
		}
        public TagView(Context context, boolean b)
        {
            super(context);
            isEmpty = b;
            setId(ID_TAG+1);
            create(context, context.getResources().getString(R.string.no_tags));
        }

        private void  create(Context context, String s)
        {
            float DP = ListsActivity.DP;
            setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
            setPadding((int)(10*DP), (int)(10*DP), (int)(10*DP), (int)(10*DP));
            setTextSize(20*DP);
            setTextColor(0xFF1C759C);
            setOnLongClickListener(ListsActivity.ta.longPressTag);
            setOnClickListener(ListsActivity.ta.pressTag);
            setText(s);
            setTypeface(ListsActivity.theTypeface);
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