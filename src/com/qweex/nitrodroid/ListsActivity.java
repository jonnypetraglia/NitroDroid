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

import java.lang.reflect.Field;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ListsActivity extends Activity
{

    public static boolean v2 = false;

	/** Static variables concerning the preferences of the program **/
	public static int themeID;
	public static boolean forcePhone;
	public static String locale = null, backgroundPath = null;
	public static boolean isTablet = false;
	boolean splashEnabled = false;
	
	/** Static variables that are used across files **/
	public static String listHash;
	public static View currentList;
	public static TasksActivity ta;
	public static ViewFlipper flip;
	public static SyncHelper syncHelper;
    public static SyncHelper_v2 syncHelper2;
	public static float DP;
	public static String lastList;
	
	/** Local variables **/
	private static int list_normalDrawable, list_selectedDrawable;
	private EditText newList;
	private Builder newListDialog, editListDialog;
	private static Context context;
	private boolean loadingApp  = true, loadingOnCreate = true;
	private View splash;
	private ListView mainListView;
	public static ListAdapter listAdapter;
	public static ImageButton syncLoading;
    static ImageView arrow;
    public static Typeface theTypeface;

    /************************** Activity Lifecycle methods **************************/
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d("ListsActivity::()", "Creating ListsActivity");
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Load preferences
		String new_theme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "Default");
		themeID = getResources().getIdentifier(new_theme, "style", getApplicationContext().getPackageName());
		forcePhone = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("force_phone", false);
		locale = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("locale", "en");
		lastList = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("last_list", "today");
        backgroundPath = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("background", null);
		doViewStuff();
		
		//Create/set locals
		context = this;
		syncHelper = new SyncHelper(context);
        //syncHelper2 = new SyncHelper_v2(context);

        //Create arrow view
        arrow = new ImageView(context);
        arrow.setImageResource(R.drawable.arrow);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.gravity = Gravity.CENTER;
        arrow.setLayoutParams(ll);
		
		//Show Splash
		splash = findViewById(R.id.splash);
		Animation splashAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
		splashAnim.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation animation)
			{
				for(long i=0; splashEnabled && i<(procUtils.getCPUFreq()>0 ? procUtils.getCPUFreq()*10 : 10000l); i++);
				doCreateAsyncronously.execute();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) { System.out.println("nerts1"); }
		});
		splash.startAnimation(splashAnim);
	}
		
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    { 
		if(isTablet && !forcePhone && false)
		{
			Log.d("ListsActivity::onCreateOptionsMenu", "'s a tablet. Doing some shit that I forget why I did it.");
			findViewById(R.id.frame).setVisibility(View.VISIBLE);
			/*((android.widget.Button)findViewById(R.id.add_list)).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v)
				{
					pressCreateList();
				}
			});*/
			return false;
		}
        if(!v2)
		    menu.add(0, 42, 0, getResources().getString(R.string.add_list));
		return true;
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		pressCreateList();
		return true;
    }

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d("ListsActivity::onResume", "Resuming main activity");
		
		//Usually resumes after visiting Preferences.
		//Re-read preferences
		String new_theme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "Default");
        String new_background = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("background", null);
		int new_themeID = getResources().getIdentifier(new_theme, "style", getApplicationContext().getPackageName());
		boolean new_force = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("force_phone", false);
		String new_locale = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("language", "en");

		//If any of them have changed, rebuild the UI
		if(ta==null || new_themeID!=themeID || new_force!=forcePhone || !new_locale.equals(locale) || new_background==null || !new_background.equals(backgroundPath))
		{
			java.util.Locale derlocale = new java.util.Locale(new_locale);
			java.util.Locale.setDefault(derlocale);
			Configuration config = new Configuration();
			config.locale = derlocale;
			getBaseContext().getResources().updateConfiguration(config,
			      getBaseContext().getResources().getDisplayMetrics());
			locale = new_locale;
			themeID = new_themeID;
			forcePhone = new_force;
            backgroundPath = new_background;
			doCreateStuff();
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		syncHelper.db.close();
		Log.d("ListsActivity::onDestroy", "Herp");
        ta = null;
	}
	
    @TargetApi(5)
	public void onBackPressed()
    {
    	doBackThings();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            doBackThings();
        }

        return super.onKeyDown(keyCode, event);
    }
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("ListsActivity::onConfigurationChanged", "Herp");
    }
	
   /************************** Yoda methods **************************/
   //"Do or do not, there is no try"
   // These methods are essentially the Activity Lifecycle methods, but
   // they have been split off so that they can be called in other methods
	
	public void doViewStuff()
	{
		Log.d("ListsActivity::doViewStuff", "Doing view things");
		//Theme
        if(v2)
            setTheme(R.style.Version2);
        else
		    setTheme(themeID);
		
		//Force Phone
		forcePhone = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("force_phone", false);
		if(!forcePhone && isTabletDevice(this))
		{
			Log.d("ListsActivity::doViewStuff", "Setting tablet");
		    isTablet = true;
		    if(themeID==R.style.Wunderlist || themeID==R.style.Right || v2)
		    	setContentView(R.layout.tablet_right);
		    else
		    	setContentView(R.layout.tablet);
		    findViewById(R.id.taskTitlebar).setVisibility(v2 ? View.INVISIBLE : View.GONE);
		}
		else
		{
			Log.d("ListsActivity::doViewStuff", "Setting phone");
			isTablet = false;
			setContentView(R.layout.phone);
			findViewById(R.id.taskTitlebar).setVisibility(View.VISIBLE);
		}

        //Time to set the typeface
        theTypeface = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
        ((TextView)findViewById(R.id.sweetFlatteryWillGetYouEverywhere)).setTypeface(theTypeface);
        ((TextView)findViewById(R.id.appTitle)).setTypeface(theTypeface);
        ((TextView)findViewById(R.id.taskTitlebar)).setTypeface(theTypeface);
        ((TextView)findViewById(R.id.empty2)).setTypeface(theTypeface);
        //task_item_details: tags_edit
        //task_item_empty_tag
        //QuickPrefs: textView1, dropbox_button, ubuntu_button

        //Make alterations for version 2
        if(v2)
        {
            //List toolbar stuff
            findViewById(R.id.logo).setVisibility(View.GONE);
            findViewById(R.id.addbutton).setVisibility(View.GONE);
            findViewById(R.id.deletebutton).setVisibility(View.GONE);
            //Task toolbar stuff
            findViewById(R.id.sync).setVisibility(View.GONE);
            findViewById(R.id.sortbutton).setVisibility(View.GONE);

            ((ImageButton)findViewById(R.id.sync)).setImageResource(R.drawable.sort);
            if(!forcePhone && isTabletDevice(this))
                findViewById(R.id.sync).setVisibility(View.INVISIBLE);
            else
                findViewById(R.id.sync).setVisibility(View.GONE);
            //findViewById(R.id.settings).setVisibility(View.GONE); //Derp
            findViewById(R.id.settings).setPadding(0,0,10,0);
        } else
        {
            findViewById(R.id.optionsbutton).setVisibility(View.GONE);
            if(forcePhone || !isTabletDevice(this))
                findViewById(R.id.backbutton).setVisibility(View.GONE);
            ((View)findViewById(R.id.newTask).getParent()).setVisibility(View.GONE);
            ((View)findViewById(R.id.newList).getParent()).setVisibility(View.GONE);
        }

        //Background
        if(backgroundPath!=null)
        {
            try {
            android.graphics.drawable.Drawable backgroundImage = new android.graphics.drawable.BitmapDrawable(android.graphics.BitmapFactory.decodeFile(backgroundPath));
            View v = (View)findViewById(R.id.background);
            if(v==null)
                v = findViewById(R.id.FLIP);
            v.setBackgroundDrawable(backgroundImage);
            } catch(Exception e) {}
        }
		
		//If we are NOT in the middle of the splash screen, remove the splash view
		if(!loadingApp)
		{
			((ViewFlipper)findViewById(R.id.FLIP)).removeViewAt(0);
		}
	}
	
	public void doCreateStuff() { doCreateStuff(false); }
	public void doCreateStuff(boolean noSetMainView)
	{
		Log.d("ListsActivity::doCreateStuff", "Doing create things");
        ta = null;
		if(loadingOnCreate)
			return;
		if(!noSetMainView)
			doViewStuff();
		
		flip = (ViewFlipper) findViewById(R.id.FLIP);

        //Add the New List editbox
		mainListView = (ListView) findViewById(android.R.id.list);
        if(v2)
        {
            FrameLayout fl = new FrameLayout(this);
            newList = (EditText) findViewById(R.id.newList);
            newList.setTypeface(theTypeface);
            newList.setOnEditorActionListener(newListListener);

            newList.setOnTouchListener(new RightDrawableOnTouchListener(newList) {
                @Override
                public boolean onDrawableTouch(final MotionEvent event) {
                    Log.d("HERP", "Pressed drawable");
                    String newListName = newList.getText().toString();
                    reallyCreateList(newListName);
                    return true;
                }
            });
        }


		syncLoading = ((ImageButton) findViewById(R.id.sync));
		
		//Add them onClickListeners
		/*((android.widget.Button)findViewById(R.id.add_list)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				newListDialog.show();
			}
		});*/
		((ImageButton) findViewById(R.id.settings)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent x = new Intent(ListsActivity.this, QuickPrefsActivity.class);
				x.putExtra("show_popup", false);
				startActivity(x);
			}
         });
		syncLoading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(SyncHelper.isSyncing)
					return;
				if(syncHelper.SERVICE==null)
				{
					Intent x = new Intent(ListsActivity.this, QuickPrefsActivity.class);
					x.putExtra("show_popup", true);
					startActivity(x);
					return;
				}
				((android.graphics.drawable.AnimationDrawable) ListsActivity.syncLoading.getDrawable()).start();
				syncHelper.new performSync().execute();
			}
		});
         mainListView.setOnItemClickListener(selectList);
         mainListView.setOnItemLongClickListener(longSelectList);
         
         //Create the ListAdapter & set it
         Cursor r = syncHelper.db.getAllLists();
         listAdapter = new ListAdapter(context, R.layout.list_item, r);
         listAdapter.todayCount = ListsActivity.syncHelper.db.getTodayTasks(TasksActivity.getBeginningOfDayInSeconds()).getCount();
         listAdapter.totalCount = ListsActivity.syncHelper.db.getTasksOfList(null, "order_num").getCount();
         
         if(r.getCount()<3)
         {
        	 syncHelper.readJSONtoSQL("", this);
        	 listAdapter.changeCursor(syncHelper.db.getAllLists());
         }
         mainListView.setAdapter(listAdapter);
         
         
         //Do animation things with the splash
         if(loadingApp)
         {
			Animation animation = AnimationUtils.loadAnimation(ListsActivity.this, android.R.anim.fade_out);
			animation.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation animation)
				{
					//TODO: WHAT THE FUCK IS THIS SHIT 
					flip.setInAnimation(context, R.anim.slide_in_right);
					doCreateThingsHandler.sendEmptyMessage(1);
				}
				@Override
				public void onAnimationRepeat(Animation animation) {}
				@Override
				public void onAnimationStart(Animation animation) {}
			});
			splash.startAnimation(animation);
         }
         
         loadingApp = false;
	}
	
    void doBackThings()
    {
    	if(ta==null)
    		finish();
    	else
    	{
    		if(!ta.doBackThings())
    			return;
    		
    		if(isTablet)
    			finish();
    		else
    		{
	            flip.setInAnimation(this, android.R.anim.slide_in_left);
	            flip.setOutAnimation(this, android.R.anim.slide_out_right);
	            ta = null;
	    		flip.showPrevious();
    		}
    	}
    }

    AsyncTask<Void, Void, Void> doCreateAsyncronously = new AsyncTask<Void, Void, Void>()
	{

		@Override
	    protected Void doInBackground(Void... params) {
			Log.d("ListsActivity::doCreateThingsAsyncronously", "Launching asyncronously");
			syncHelper.db.open();
			DP = context.getResources().getDisplayMetrics().density;

			//Get the drawables for normal & selected according to the theme
			TypedArray a;
			a = context.getTheme().obtainStyledAttributes(ListsActivity.themeID, new int[] {R.attr.lists_selector});
        	list_normalDrawable = a.getResourceId(0, 0);
	        a = context.getTheme().obtainStyledAttributes(ListsActivity.themeID, new int[] {R.attr.lists_selected});
        	list_selectedDrawable = a.getResourceId(0, 0);

            Looper.prepare();   //This is to fix a problem with HTC. FUCK HTC.
            newList = new EditText(context);
	        newList.setId(42);
	        newListDialog = new AlertDialog.Builder(context)
	        	.setTitle(R.string.add_list)
	        	.setView(newList)
	        	.setPositiveButton(android.R.string.ok, createList).setNegativeButton(android.R.string.cancel, null);
            editListDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.edit_list)
                .setPositiveButton(R.string.rename,renameList).setNegativeButton(R.string.delete,deleteList);

	        //Done creating
	        loadingOnCreate = false;
	        Log.d("ListsActivity::doCreateThingsAsyncronously", "Done with the async stuff");
	        doCreateThingsHandler.sendEmptyMessage(0);
	        return null;
        }
   };

   Handler doCreateThingsHandler = new Handler()
   {
 	   @Override
 		public void handleMessage(android.os.Message msg) 
 		{
 		   if(msg.what==0)
 			   doCreateStuff(true);
 		   else
 		   {
 			  flip.removeView(splash);
 		   }
 		}
    };;
    
    
    /************************** Misc methods **************************/
   
	void pressCreateList()
	{
        newList.setTag("create");
		newListDialog.show();
	}
	
	DialogInterface.OnClickListener createList = new DialogInterface.OnClickListener()
	{
        public void onClick(DialogInterface dialog, int whichButton) {
            String newListName = newList.getText().toString();
            if("".equals(newListName))
            	return;
            if(newList.getTag().equals("create"))
            {
                reallyCreateList(newListName);
            } else
            {
                Log.d("ListsActivity::createList", "Renaming a List " + newListName);
                syncHelper.db.modifyList((String) currentList.findViewById(R.id.listId).getTag(), "name", newListName);
               }
            
            listAdapter.changeCursor(syncHelper.db.getAllLists());
        }
    };


    void reallyCreateList(String newListName)
    {
        if("".equals(newListName))
            return;
        newList.setText("");
        String new_id = SyncHelper.getID();
        Log.d("ListsActivity::createList", "Creating a List " + newListName + " [" + new_id + "]");
        Toast.makeText(context, "Created list: " + newListName, Toast.LENGTH_SHORT).show();
        syncHelper.db.insertList(new_id, newListName, null);
        syncHelper.db.insertListTimes(new_id, (new java.util.Date()).getTime(), 0);
        Log.d("Dsadsadsa", newListName + " ");
        listAdapter.changeCursor(syncHelper.db.getAllLists());
        InputMethodManager imm = (InputMethodManager)newList.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(newList.getWindowToken(), 0);
    }

    DialogInterface.OnClickListener renameList = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int whichButton) {
            newList.setTag("rename");
            newList.setText(((TextView)currentList.findViewById(R.id.listName)).getText());
            newListDialog.show();
        }
    };

    DialogInterface.OnClickListener deleteList = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int whichButton) {
            new AlertDialog.Builder(context)
                    .setTitle("Really delete?")
                    .setPositiveButton(android.R.string.yes, reallyDelete).setNegativeButton(android.R.string.no, null).show();

        }
        DialogInterface.OnClickListener reallyDelete = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton) {
                String hash = (String) currentList.findViewById(R.id.listId).getTag();
                syncHelper.db.deleteList(hash);
                listAdapter.changeCursor(syncHelper.db.getAllLists());
            }
        };
    };


    OnItemLongClickListener longSelectList = new OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            String derp = (String) view.findViewById(R.id.listId).getTag();
            if(!v2)
            {
                currentList.setBackgroundResource(list_normalDrawable);
                view.setBackgroundResource(list_selectedDrawable);
            }
            currentList = view;
            if(derp.equals("today") || derp.equals("next") || derp.equals("logbook") || derp.equals("all"))
                return true;
            TextView tv = new TextView(view.getContext());
            tv.setText("     '" + ((TextView)currentList.findViewById(R.id.listName)).getText() + "'");
            tv.setTextColor(0xffffffff);
            tv.setTextSize(20);
            editListDialog.setView(tv);
            editListDialog.show();
            return true;
        }
    };

	static OnItemClickListener selectList = new OnItemClickListener() 
    {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id)
      {
    	  if(SyncHelper.isSyncing)
    		  return;
    	  Log.d("ListsActivity::selectList", "List Selected");
    	  
    	  //Get info
    	  String hash, name;
		  name=(String) ((TextView)view.findViewById(R.id.listName)).getText();
		  hash=(String) view.findViewById(R.id.listId).getTag();
		  Log.d("ListsActivity::selectList", "List Selected is: " + name);
		  
		  //parent==null signifies that it is programatically selected so no need to update.
		  if(parent!=null)
		  {
			  Log.d("ListsActivity::selectList", "Updating LastList to " + hash);
    		  Editor e = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
        	  e.putString("last_list", hash);
        	  e.commit();
		  }
    	  
		  //Set background of last selected to normal & the current to selected
    	  if(isTablet && currentList!=null && !v2)
    		  currentList.setBackgroundResource(list_normalDrawable);
    	  currentList = view;
    	  if(isTablet)
          {
              if(!v2)
                  currentList.setBackgroundResource(list_selectedDrawable);
              else
              {
                  if(arrow.getParent()!=null)
                      ((LinearLayout)arrow.getParent()).removeView(arrow);
                  ((LinearLayout)currentList).addView(arrow, 0);
              }
          }
    	  
    	  //Yay update shit
    	  TasksActivity.lastClicked = null;
		  TasksActivity.lastClickedID = null;
          System.out.println("Dude:::" + ta);
    	  if(ta==null)
    	  {
    		  Log.d("ListsActivity::selectList", "Instanciating TaskActivity");
    		  ta = new TasksActivity();
    		  ta.context = (Activity) view.getContext();
    		  ta.listHash = hash;
    		  ta.listName = name;
    		  ta.onCreate(null);	//I think I might get away with calling "doCreateThings" or even "sillygoose" but whatevs
    	  }else
    	  {
    		  Log.d("ListsActivity::selectList", "Updating TaskActivity");
    		  ta.listHash = hash;
    		  ta.listName = name;
    		  ((Activity) context).findViewById(R.id.tasksListView).post(new Runnable(){
    			 public void run()
    			 {
    				 ta.createTheAdapterYouSillyGoose();
    			 }
    		  });
    	  }
    		  
    	  //Show the animation & flip the flipper if it is a phone
    	  if(!isTablet)
          {
    		  Log.d("ListsActivity::selectList", "Flipping to that TaskActivity");
    		  flip.setInAnimation(view.getContext(), R.anim.slide_in_right);
    		  flip.setOutAnimation(view.getContext(), R.anim.slide_out_left);
    		  flip.showNext();
          }
      }
    };



    TextView.OnEditorActionListener newListListener = new TextView.OnEditorActionListener()
    {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL)
            {
                String newListName = v.getText().toString();
                reallyCreateList(newListName);
                return true;
            }
            return false;
        }
    };
    
    
    /************************** Utility methods **************************/
    
	//http://stackoverflow.com/a/9624844/1526210
	@TargetApi(4)
	public static boolean isTabletDevice(android.content.Context activityContext) {
	    // Verifies if the Generalized Size of the device is XLARGE to be
	    // considered a Tablet
	    boolean xlarge = ((activityContext.getResources().getConfiguration().screenLayout & 
	                        Configuration.SCREENLAYOUT_SIZE_MASK) >=	//Changed this from == to >= because my tablet was returning 8 instead of 4. 
	                        Configuration.SCREENLAYOUT_SIZE_LARGE);
	    
	    
	    // If XLarge, checks if the Generalized Density is at least MDPI (160dpi)
	    if (xlarge) {
	        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
	        Activity activity = (Activity) activityContext;
	        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
	        
	        //This next block lets us get constants that are not available in lower APIs.
	        // If they aren't available, it's safe to assume that the device is not a tablet.
	        // If you have a tablet or TV running Android 1.5, what the fuck is wrong with you.
	        int xhigh = -1, tv = -1;
	        try {
	        	Field f = android.util.DisplayMetrics.class.getDeclaredField("DENSITY_XHIGH");
	        	xhigh = (Integer) f.get(null);
	        	f = android.util.DisplayMetrics.class.getDeclaredField("DENSITY_TV");
	        	xhigh = (Integer) f.get(null);
	        }catch(Exception e){}
	        
	        // MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160, DENSITY_TV=213, DENSITY_XHIGH=320
	        if (metrics.densityDpi == android.util.DisplayMetrics.DENSITY_DEFAULT
	                || metrics.densityDpi == android.util.DisplayMetrics.DENSITY_HIGH
	                || metrics.densityDpi == android.util.DisplayMetrics.DENSITY_MEDIUM
	                || metrics.densityDpi == tv 
	                || metrics.densityDpi == xhigh
	                ) {

	            return true;
	        }
	    }
	    return false;
	}
    
}