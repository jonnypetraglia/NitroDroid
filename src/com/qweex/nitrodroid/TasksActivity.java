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

import android.view.Gravity;
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import com.qweex.nitrodroid.TaskAdapter.Separator;
import com.qweex.nitrodroid.TaskAdapter.TagView;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ExpandableListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

public class TasksActivity
{
	private static final int ID_MAGIC     = 1;
	private static final int ID_HAND   = 2;
	private static final int ID_TITLE = 3;
	private static final int ID_DATE   = 4;
	private static final int ID_PRIORITY  = 5;	
	
	ExpandableListView lv;
    TaskAdapter adapter;
	static View lastClicked = null;
	static EditText editingTags = null;
	public static String lastClickedID;
	String listName;
	public String listHash;
	public Activity context;
	QuickAction sortPopup;
	Drawable selectedDrawable, normalDrawable;
	AlertDialog.Builder deleteDialog;
	PopupWindow datePickerDialog; 
	LinearLayout popupLayout;
	View datePicker;
    int lastDate = 0;
	
	//@Overload
	public void onCreate(Bundle savedInstanceState)
	{
		//super.onCreate(savedInstanceState)
		//context.requestWindowFeature(Window.FEATURE_NO_TITLE);
        sortPopup = new QuickAction(context, QuickAction.VERTICAL);
		
        sortPopup.addActionItem(new ActionItem(ID_MAGIC, context.getResources().getString(R.string.by_magic), context.getResources().getDrawable(R.drawable.sort_magic)));
        sortPopup.addActionItem(new ActionItem(ID_HAND, context.getResources().getString(R.string.by_hand), context.getResources().getDrawable(R.drawable.sort_hand)));
        sortPopup.addActionItem(new ActionItem(ID_TITLE, context.getResources().getString(R.string.by_title), createTitleDrawable()));
        sortPopup.addActionItem(new ActionItem(ID_DATE, context.getResources().getString(R.string.by_date), context.getResources().getDrawable(R.drawable.sort_date)));
        sortPopup.addActionItem(new ActionItem(ID_PRIORITY, context.getResources().getString(R.string.by_priority), context.getResources().getDrawable(R.drawable.sort_priority)));
        sortPopup.setOnActionItemClickListener(selectSort);
        
        TypedArray a = context.getTheme().obtainStyledAttributes(ListsActivity.themeID, new int[] {R.attr.tasks_selector});     
        int attributeResourceId = a.getResourceId(0, 0);
        normalDrawable = context.getResources().getDrawable(attributeResourceId);
        selectedDrawable = context.getResources().getDrawable(R.drawable.button_low);
        
        deleteDialog = new AlertDialog.Builder(context);
		deleteDialog.setTitle(R.string.warning);
		deleteDialog.setMessage(R.string.delete_task);
		deleteDialog.setPositiveButton(R.string.yes, confirmDelete);
		deleteDialog.setNegativeButton(R.string.no, confirmDelete);



		popupLayout = new LinearLayout(context);
		popupLayout.setOrientation(LinearLayout.VERTICAL);
		popupLayout.setBackgroundColor(0x00AAAAAA);
		popupLayout.setPadding((int)ListsActivity.DP, (int)ListsActivity.DP, (int)ListsActivity.DP, (int)ListsActivity.DP);


		datePickerDialog = new PopupWindow(context);
		datePickerDialog.setContentView(popupLayout);
		datePickerDialog.setBackgroundDrawable(new BitmapDrawable());
		datePickerDialog.setAnimationStyle(R.style.CalendarShow);
		createCalendar();

        doCreateStuff();
	}


	public Drawable createTitleDrawable()
	{
		final int DIM = 24;

		Bitmap canvasBitmap = Bitmap.createBitmap(DIM, 
		                                          DIM, 
		                                          Bitmap.Config.ARGB_8888);
		Canvas imageCanvas = new Canvas(canvasBitmap);
		
		Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		imagePaint.setTypeface(Typeface.SERIF);
		imagePaint.setTextAlign(Align.CENTER);
		imagePaint.setTextSize(30f);
		imagePaint.setAntiAlias(true);
		imagePaint.setColor(0xffffffff);

		imageCanvas.drawText("A", 
		                         DIM / 2, 
		                         DIM, 
		                         imagePaint); 
		BitmapDrawable finalImage = new BitmapDrawable(canvasBitmap);
		
		return finalImage.getCurrent();
	}
	
	
	public void doCreateStuff()
	{
		ImageButton sortButton = ((ImageButton)context.findViewById(R.id.sortbutton));
        sortButton.setOnClickListener(new OnClickListener()
    	{
    		@Override
    		public void onClick(View v)
    		{
    			sortPopup.show(v);
    		}
        });
        ((ImageButton)context.findViewById(R.id.addbutton)).setOnClickListener(clickAdd);
        ((ImageButton)context.findViewById(R.id.deletebutton)).setOnClickListener(clickDelete);
        
		lv = (ExpandableListView) ((Activity) context).findViewById(R.id.tasksListView);
		lv.setEmptyView(context.findViewById(R.id.empty2));
		((TextView)context.findViewById(R.id.taskTitlebar)).setText(listName);
		lv.setOnGroupClickListener(selectTask);
		lv.post(new Runnable(){
			public void run()
			{
				createTheAdapterYouSillyGoose();
			}
		});
		
	}
	
	void createTheAdapterYouSillyGoose()
	{
		Cursor r;
		if(listHash==null)
			return;
		if(listHash.equals("today"))		//Today
		{
			listHash = null;
			System.out.println("Time: " + getBeginningOfDayInSeconds());
			r = ListsActivity.syncHelper.db.getTodayTasks(getBeginningOfDayInSeconds());
            adapter = new TaskAdapter(context, R.layout.task_item, r);
			lv.setAdapter(adapter);
			return;
		}
		else if(listHash.equals("all"))			//All
			listHash = null;
		
		r = ListsActivity.syncHelper.db.getTasksOfList(listHash, "order_num");
        adapter = new TaskAdapter(context, R.layout.task_item, r);
        lv.setAdapter(adapter);
        System.out.println("Dudewtf: " + adapter.getGroupCount());
        lv.setDescendantFocusability(ExpandableListView.FOCUS_AFTER_DESCENDANTS);
	}
	
	
	OnClickListener clickAdd = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(listHash==null || "logbook".equals(listHash) || "today".equals(listHash) || "next".equals(listHash))
			{
				Toast.makeText(v.getContext(), R.string.long_winded_reprimand, Toast.LENGTH_LONG).show();
				return;
			}
			
			lastClickedID = SyncHelper.getID();
			
			int order = ListsActivity.syncHelper.db.getTasksOfList(listHash, "order_num").getCount();
			ListsActivity.syncHelper.db.insertTask(lastClickedID, v.getContext().getResources().getString(R.string.default_task),
					0, 0, "", listHash, 0, "", order);
			ListsActivity.syncHelper.db.insertTaskTimes(lastClickedID, (new Date()).getTime(),
					0, 0, 0, 0, 0, 0);
			
			createTheAdapterYouSillyGoose();
			
			try {
				Method func = ExpandableListView.class.getMethod("smoothScrollToPosition", Integer.TYPE);
				func.invoke(lv, lv.getCount() - 1);
			}catch(Exception e)
			{
				lv.setSelection(lv.getCount() - 1);
			}
			
			TextView currentListCount = (TextView)ListsActivity.currentList.findViewById(R.id.listNumber);
			int i = Integer.parseInt((String) currentListCount.getText()) + 1;
			currentListCount.setText(Integer.toString(i));
			
		}
    };
    
    OnClickListener clickDelete = new OnClickListener()
	{
    	@Override
		public void onClick(View v)
		{
    		if(lastClicked==null)
    			return;
    		TasksActivity.this.deleteDialog.show();
		}
	};
	
	DialogInterface.OnClickListener confirmDelete = new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
	        switch (which){
	        case DialogInterface.BUTTON_POSITIVE:
	            if(!ListsActivity.syncHelper.db.deleteTask(lastClickedID))
	            	return;
	            
	            
	            createTheAdapterYouSillyGoose();
	            lastClicked = null;
	            lastClickedID = null;
	            
	            TextView currentListCount = (TextView)ListsActivity.currentList.findViewById(R.id.listNumber);
				int i = Integer.parseInt((String) currentListCount.getText()) - 1;
				currentListCount.setText(Integer.toString(i));
	            break;
	        }
	    }
	};
	
	public static long getBeginningOfDayInSeconds()
	{
		java.util.Calendar c = java.util.Calendar.getInstance(TimeZone.getDefault());
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}
	
    
	QuickAction.OnActionItemClickListener selectSort = new QuickAction.OnActionItemClickListener() {			
		@Override
		public void onItemClick(QuickAction source, int pos, int actionId) {				
			//ActionItem actionItem = sortPopup.getActionItem(pos);
             
			Cursor r;
			switch(actionId)
			{
			case ID_MAGIC:
				ArrayList<taskObject> nert = new ArrayList<taskObject>();
				r = ListsActivity.syncHelper.db.getTasksOfList(listHash, "order_num");
				if(r.getCount()>0)
					r.moveToFirst();
				for(int i=0; i<r.getCount(); i++)
				{
					nert.add(new taskObject(r));
					r.moveToNext();
				}
				Collections.sort(nert, new MagicComparator());
				adapter = new TaskAdapter(context, R.layout.task_item, nert);
				lv.setAdapter(adapter);
				adapter.notifyDataSetChanged();
				return;
			case ID_HAND:
				return;
			case ID_TITLE:
				r = ListsActivity.syncHelper.db.getTasksOfList(listHash, "name");
				changeSort(r);
				break;
			case ID_DATE:
				r = ListsActivity.syncHelper.db.getTasksOfList(listHash, "logged, date");
				changeSort(r);
				break;
			case ID_PRIORITY:
				r = ListsActivity.syncHelper.db.getTasksOfList(listHash, "priority");
				changeSort(r);
			default:
				return;
			}
			adapter = new TaskAdapter(context, R.layout.task_item, r);
			lv.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	};

	public class MagicComparator implements Comparator<taskObject> {
		@Override
		public int compare(taskObject a, taskObject b) {
			int ratingA = a.dateWorth, ratingB = b.dateWorth;
			ratingA += a.priority*2;
			ratingB += b.priority*2;
			
			if(a.logged>0 && b.logged==0) return 1;
			else if(a.logged==0 && b.logged>0) return -1;
			else if(a.logged>0 && a.logged>0) return 0;
			return ratingB - ratingA;
		}
	}
	
	/*
	case "magic":
	list.sort(function(a, b) {

		var rating = {					//Get the days between now and the due date
			a: getDateWorth(a.date),	//If it's >14 days, the value is 1
			b: getDateWorth(b.date)		//Otherwise, it's 14 - diff + 1.
		}								// 13 = 2, 12 = 3, 11 = 4, etc
										// Highest possible is today: 0 = 13
		var worth = { none: 0, low: 2, medium: 4, high: 6 }
										// Highest possible: 19
		rating.a += worth[a.priority]
		rating.b += worth[b.priority]

		if(a.logged && !b.logged) return 1
		else if(!a.logged && b.logged) return -1
		else if(a.logged && b.logged) return 0

		return rating.b - rating.a

	})
	break
	 */
	
	class taskObject {
		String hash, name, notes, list, tags;
		int priority;
		long logged, date;
		
		int dateWorth;
		
		public taskObject(Cursor c)
		{
			hash = c.getString(c.getColumnIndex("hash"));
			name = c.getString(c.getColumnIndex("name"));
			notes = c.getString(c.getColumnIndex("notes"));
			list = c.getString(c.getColumnIndex("list"));
			tags = c.getString(c.getColumnIndex("tags"));
			priority = c.getInt(c.getColumnIndex("priority"));
			logged = c.getLong(c.getColumnIndex("logged"));
			date = c.getLong(c.getColumnIndex("date"));
			dateWorth = getDateWorth();
		}
		
		private int getDateWorth()
		{
			if(date == 0)
				return 0;
			Date due = new Date(date);
			Date today = new Date();
			
			Date one = new Date(due.getYear(), due.getMonth(), due.getDate());
			Date two = new Date(today.getYear(), today.getMonth(), today.getDate());
			
			int millisecondsPerDay = 1000 * 60 * 60 * 24;
			long millisBetween = one.getTime() - two.getTime();
			double days = (double)millisBetween / millisecondsPerDay;
			int diff = (int) Math.floor(days);
			if(diff > 14)
				diff = 14;
			return 14 - diff + 1;
			
		}
	}
	

	
	void changeSort(Cursor r)
	{
		String tempHash;
		String tempHashString = "";
		if(r.getCount()>0)
			r.moveToFirst();
		for(int i=0; i<r.getCount(); i++)
		{
     			tempHash = r.getString(r.getColumnIndex("hash"));
			if(i>0)
				tempHashString = tempHashString.concat("|");
			tempHashString = tempHashString.concat(tempHash);
			ListsActivity.syncHelper.db.modifyOrder(tempHash, i);
			r.moveToNext();
		}
		ListsActivity.syncHelper.db.modifyListOrder(listHash, tempHashString);
	}

	OnClickListener pressPriority = new OnClickListener()
	{
    	@Override
		public void onClick(View v)
		{
    		TextView priority = (TextView) v;
    		int pri = (Integer) priority.getTag();
    		pri = (pri+1) % 4;
    		android.widget.CheckBox done = (android.widget.CheckBox)
    				((View)((View)v.getParent()).getParent())
    				.findViewById(R.id.taskDone); 
    		done.setButtonDrawable(TaskAdapter.drawsC[pri]);
    		priority.setBackgroundResource(TaskAdapter.drawsB[pri]);
    		priority.setTag(pri);
    		priority.setText(context.getString(R.string.priority) + ": " + context.getString(TaskAdapter.drawsS[pri]));
    		
    		ListsActivity.syncHelper.db.modifyTask(lastClickedID, "priority", pri);
		}
	};
	
	OnClickListener pressDate = new OnClickListener()
	{
		@TargetApi(11)
		@Override
		public void onClick(View v)
		{
			long dateL = (Long)v.getTag();
			Calendar date = Calendar.getInstance();
            if(dateL>0)
			    date.setTimeInMillis(dateL);
            lastDate = date.get(Calendar.YEAR)*10000+date.get(Calendar.MONTH)*100+date.get(Calendar.DAY_OF_MONTH);
			
			if(android.os.Build.VERSION.SDK_INT<11)
			{
/*				((android.widget.DatePicker)datePicker).updateDate(date.get(Calendar.YEAR),
																   date.get(Calendar.MONTH),
																   date.get(Calendar.DATE));*/
			}else
			{
				((android.widget.CalendarView)datePicker).setDate(dateL);
                int ix = 1;
                try {
                    ix = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("week_starts_on", "3"));
                }catch(Exception e) {}

				((android.widget.CalendarView)datePicker).setFirstDayOfWeek(ix);
			}

			datePickerDialog.showAtLocation(context.findViewById(R.id.FLIP), android.view.Gravity.CENTER, 0, 0);
            datePickerDialog.update(0, 0, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
	};
	

	void createCalendar()
	{
		if(android.os.Build.VERSION.SDK_INT<11)
		{
			android.widget.DatePicker datePicker_ = new android.widget.DatePicker(context);
			datePicker = datePicker_;
            ((android.widget.DatePicker)datePicker).setForegroundGravity(Gravity.CENTER);
			datePicker.setPadding((int)(10*ListsActivity.DP), (int)(10*ListsActivity.DP), (int)(10*ListsActivity.DP), (int)(10*ListsActivity.DP));
			Button confirm = new Button(context);
			confirm.setPadding((int) (10 * ListsActivity.DP), (int) (10 * ListsActivity.DP), (int) (10 * ListsActivity.DP), (int) (10 * ListsActivity.DP));
			confirm.setText(R.string.confirm);
			confirm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDate(((android.widget.DatePicker) datePicker).getYear(),
                            ((android.widget.DatePicker) datePicker).getMonth(),
                            ((android.widget.DatePicker) datePicker).getDayOfMonth());
                    datePickerDialog.dismiss();
                }
            });
			popupLayout.addView(datePicker);
			popupLayout.addView(confirm);
            popupLayout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.splash_bg));
			
			datePicker.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			confirm.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			datePickerDialog.setWidth(datePicker.getMeasuredWidth());
			datePickerDialog.setHeight((int) (datePicker.getMeasuredHeight() + 20*ListsActivity.DP + confirm.getMeasuredHeight()));
			datePickerDialog.setOutsideTouchable(true);
			return;
		}
		android.widget.CalendarView datePicker_ = new android.widget.CalendarView(context); // = (CalendarView) datePicker;
		datePicker = datePicker_;
		popupLayout.addView(datePicker_, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f));
		
		datePicker_.setBackgroundColor(0x00FFFFFF);
		datePicker_.setShowWeekNumber(false);
		datePicker_.setOnDateChangeListener(new android.widget.CalendarView.OnDateChangeListener() {

	        @Override
	        public void onSelectedDayChange(android.widget.CalendarView view, int year, int month, int dayOfMonth)
	        {
                if(lastDate!=(year*10000+month*100+dayOfMonth))
	        	    updateDate(year, month, dayOfMonth);
	        }
	    });
		
		datePicker_.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		datePickerDialog.setWidth(datePicker.getMeasuredWidth());
		datePickerDialog.setHeight((datePicker.getMeasuredHeight()));
		datePickerDialog.setOutsideTouchable(true);
	}
	
	void updateDate(int year, int month, int dayOfMonth)
	{
    	Calendar c = Calendar.getInstance();
    	c.set(year, month, dayOfMonth, 0, 0, 0);
    	c.set(Calendar.MILLISECOND, 0);
    	
    	((Button)context.findViewById(R.id.timeButton)).setText(
    			TaskAdapter.sdf.format(c.getTime())
    			);
    	ListsActivity.syncHelper.db.modifyTask(lastClickedID, "date", c.getTimeInMillis());
    	
    	((android.widget.TextView)lastClicked.findViewById(R.id.taskTime)).setText(TaskAdapter.getTimeString(c.getTimeInMillis(), context));
    	
        datePickerDialog.dismiss();
	}
	
    static String oldname, oldnotes;
    static TextWatcher writeName = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0)
		{
			String s = oldname;
			String new_val = arg0.toString(); //((EditText)lastClicked.findViewById(R.id.taskName_edit)).getText().toString();
			System.out.println(s + "==" + new_val);
			if(s.toString().equals(new_val))
				return;
	    	ListsActivity.syncHelper.db.modifyTask(lastClickedID, "name", s.toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{oldname = s.toString();}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count){}
    };
    
    static TextWatcher writeNotes = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0)
		{
			if(lastClicked==null || arg0==null)
				return;
			String new_val = arg0.toString();
			String s = oldnotes;
			if(s.toString().equals(new_val))
				return;
	    	ListsActivity.syncHelper.db.modifyTask(lastClickedID, "notes", s.toString());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after)
		{oldnotes = s.toString();}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count){}
    };
	
	OnGroupClickListener selectTask = new OnGroupClickListener()
    {
      @Override
      public boolean onGroupClick(ExpandableListView parent, View view, int position, long id)
      {
    	  if(SyncHelper.isSyncing)
    		  return false;

          if(adapter.lastClicked>0)
              ListsActivity.ta.lv.collapseGroup(adapter.lastClicked);
          adapter.lastClicked = position;

    	  if(lastClicked!=null && lastClicked==view)
    	  {
       		  lastClicked.setBackgroundDrawable(normalDrawable);
    		  //parent.expandGroup(position);
              return false;
    	  }
    	  else
    	  {
    		  parent.collapseGroup(position);
    		  if(lastClicked!=null)
    			  lastClicked.setBackgroundDrawable(normalDrawable);
    		  lastClicked = view;
    		  lastClicked.setBackgroundDrawable(selectedDrawable);
    		  lastClickedID = (String) ((TextView)lastClicked.findViewById(R.id.taskId)).getText();
              return true;
    	  }
      }
    };
    
    
    boolean doBackThings()
    {
    	if(datePickerDialog!=null && datePickerDialog.isShowing())
    	{
    		datePickerDialog.dismiss();
    	}
    	//Finish editing tasks
    	else if(editingTags!=null)
    	{
    		editingTags.setVisibility(View.GONE);
    		HorizontalScrollView hsv = (HorizontalScrollView) ((LinearLayout)editingTags.getParent()).getChildAt(1);
    		
    		getThemTagsSon((LinearLayout)hsv.getChildAt(0), editingTags.getText().toString());
    		
    		hsv.setVisibility(View.VISIBLE);
    		String newtags = editingTags.getText().toString();
    		
    		ArrayList<String> newtagsList = new ArrayList<String>(Arrays.asList(newtags.split(",")));

            newtags = "";
    		removeDuplicateWithOrder(newtagsList);
    		for(String S : newtagsList)
    		{
    			newtags = newtags.concat(S).concat(",");
    		}
    		ListsActivity.syncHelper.db.modifyTask(lastClickedID, "tags", newtags);
    		
    		editingTags = null;
    	}
    	//Deselect an item
    	else if(lastClicked!=null || lastClickedID!=null)
    	{
            lv.collapseGroup(adapter.lastClicked);
            adapter.lastClicked = -1;
	    	if(lastClicked!=null)
	    		lastClicked.setBackgroundDrawable(normalDrawable);
	    	lastClicked = null;
	    	lastClickedID = null;
    	}
    	else
    		return true;
    		//context.finish();
    	return false;
    }
    
    
    /** List order maintained **/
    public static void removeDuplicateWithOrder(ArrayList arlList)
    {
       java.util.Set set = new java.util.TreeSet(String.CASE_INSENSITIVE_ORDER);//java.util.HashSet();
       java.util.List newList = new ArrayList();
       for (java.util.Iterator iter = arlList.iterator(); iter.hasNext();)
       {
          Object element = iter.next();
          if (set.add(element))
             newList.add(element);
       }
       arlList.clear();
       arlList.addAll(newList);
    }
    
    static void getThemTagsSon(LinearLayout tag_cont, String tags)
    {
    	String[] tgs = tags.split(",");
    	for(int i=0; i<tgs.length; i++)
    		tgs[i] = tgs[i].trim();
    	ArrayList<String> arList = new ArrayList<String>(Arrays.asList(tgs));
    	removeDuplicateWithOrder(arList);

		for(int i=0; i<arList.size(); i++)
		{
            if(i==0)
                tag_cont.removeAllViews();
			else
				tag_cont.addView(new Separator(tag_cont.getContext()));
			tag_cont.addView(new TagView(tag_cont.getContext(), arList.get(i).trim()));
		}
    }
	
    static int n, m;
    static android.widget.HorizontalScrollView s;

	static OnLongClickListener pressTag = new OnLongClickListener()
	{
		
		@Override
		public boolean onLongClick(View v)
		{
            LinearLayout tagparent;
            if(v.getId()!=R.id.tag_container)
            {
			    tagparent = (LinearLayout) ((View)v.getParent());
                s = (android.widget.HorizontalScrollView) tagparent.getParent();
            }
            else
            {
                tagparent = (LinearLayout)v;
                s = (HorizontalScrollView)v.getParent();
            }
			LinearLayout sParent = (LinearLayout) s.getParent();
			editingTags = (EditText) sParent.findViewById(R.id.tags_edit);
			editingTags.setText("");
			
			editingTags.setOnEditorActionListener(new EditText.OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView tv, int actionId, KeyEvent event)
				{
					if(  actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                      || actionId == android.view.inputmethod.EditorInfo.IME_NULL )
					{
						LinearLayout ll = (LinearLayout) editingTags.getParent();
                        String x = tv.getText().toString();
                        ListsActivity.syncHelper.db.modifyTask(lastClickedID, "tags", x);
					    getThemTagsSon((LinearLayout)ll.findViewById(R.id.tag_container), x);
						  ((EditText)ll.findViewById(R.id.tags_edit)).getText().toString();
					    ll.findViewById(R.id.tag_scroller).setVisibility(View.VISIBLE);
						editingTags.setVisibility(View.GONE);
						tv.setVisibility(View.GONE);
					    return true;
				    }
				    return false;
				}
			});
			
			for(int i=0; i<(tagparent.getChildCount()); i=i+2)
			{
                if(tagparent.getChildAt(i).getId()!=TaskAdapter.ID_TAG)
                    continue;
				if(i>0)
					editingTags.append(", ");
				editingTags.append(((TextView) (tagparent.getChildAt(i))).getText());
			}
			try {
                if(v.getId()!=TaskAdapter.ID_TAG)
                    throw new Exception();
                String xyz = (String) ((TextView)v).getText();
                System.out.println("Derpy: " + xyz + " - " + editingTags.getText());
				n = editingTags.getText().toString().indexOf(xyz);
				m = ((TextView)v).getText().length();
			} catch(Exception e) {
				n = editingTags.getText().length();
				m = 0;
			}
            s.setVisibility(View.GONE);
            editingTags.setVisibility(View.VISIBLE);

            editingTags.setSelection(n+m); //, n + m);
            editingTags.requestFocus();
			return true;
		}
		
	};
}
