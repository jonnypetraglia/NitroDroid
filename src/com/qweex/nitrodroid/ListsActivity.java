/*
Copyright (c) 2012-2013 Qweex
Copyright (c) 2012-2013 Jon Petraglia

This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.

    2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
 */
package com.qweex.nitrodroid;

import java.lang.reflect.Field;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.qweex.utils.QweexUtils;

//TODO: Sometimes the counts for today and total are spotty

public class ListsActivity extends Activity
{

    public static boolean v2 = false;

	/** Static variables concerning the preferences of the program **/
	public static int themeID;
	public static boolean forcePhone;
	public static String locale = null, backgroundPath = null;
    public static Locale realLocale = null;
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
	public static Context context;
	private boolean loadingApp  = true, loadingOnCreate = true;
	private View splash;
	private ListView mainListView;
	public static ListAdapter listAdapter;
	public static ImageButton syncLoading;
    static ImageView arrow;
    public static Typeface theTypeface;


    void changeLocale()
    {
        locale = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("locale", "en");
        realLocale = new Locale("ca");
        Locale.setDefault(realLocale);
        Configuration config = new Configuration();
        config.locale = realLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public boolean onSearchRequested()
    {
        String TAG= QweexUtils.TAG();
        Log.d(TAG, "SearchRequested " + ta);
        startSearch(null, false, null, false);
        return SyncHelper.isSyncing;
    }

    static public void newSearch(String term)
    {
        String TAG= QweexUtils.TAG();
        Log.d(TAG, "SEARCHING: " + term + "\\" + ta);
        if(ta==null)
        {
            Log.d(TAG, "Instanciating TaskActivity: " + term);
            ta = new TasksActivity();
            ta.context = (Activity) ListsActivity.context;
            ta.searchTerm = term;
            ta.onCreate(null);
        }else
        {
            Log.d(TAG, "Updating TaskActivity: " + term + "\\" + ta.listHash);
            ta.searchTerm = term;
            ((Activity) context).findViewById(R.id.tasksListView).post(new Runnable(){
                public void run()
                {
                    ta.onCreate(null);
                    //ta.createTheAdapterYouSillyGoose();
                }
            });
        }

        //Show the animation & flip the flipper if it is a phone
        if(!isTablet)
        {
            Log.d(TAG, "Flipping to that TaskActivity");
            if(flip.getCurrentView()!=flip.getChildAt(1))
            {
                flip.setInAnimation(inFromRightAnimation());
                flip.setOutAnimation(outToLeftAnimation());
                flip.showNext();
            }
            else
            {
                //TODO: Change this animation [1 of 3]
                ((Activity) ListsActivity.context).findViewById(R.id.tasks_fade).startAnimation(AnimationUtils.loadAnimation(ListsActivity.context, android.R.anim.fade_out));
                ((Activity) ListsActivity.context).findViewById(R.id.tasks_fade).startAnimation(AnimationUtils.loadAnimation(ListsActivity.context, android.R.anim.fade_in));
            }
        }
    }

    Handler derp = new Handler();
    String searchTerm;
    private void handleIntent(Intent intent) {
        searchTerm = intent.getStringExtra(SearchManager.QUERY);
        derp.post(new Runnable(){

            @Override
            public void run() {
                newSearch(searchTerm);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }


    /************************** Activity Lifecycle methods **************************/

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
        String TAG= QweexUtils.TAG();
        Log.d(TAG, "Oncreate");
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            handleIntent(getIntent());
            return;
        }



        changeLocale();
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Creating ListsActivity");
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//Load preferences
        boolean fixLocale = (locale==null);
		String new_theme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("theme", "Default");
		themeID = getResources().getIdentifier(new_theme, "style", getApplicationContext().getPackageName());
		forcePhone = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("force_phone", false);
		locale = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("locale", "en");
		lastList = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("last_list", "today");
        backgroundPath = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("background", null);


        doViewStuff();
        changeLocale();
		
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
    public void onPause()
    {
        super.onPause();
        hideAddButton();
    }
		
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    { 
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
        String TAG= QweexUtils.TAG();
		super.onResume();
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) return;
		Log.d(TAG, "Resuming main activity");
		
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
            isTablet = QweexUtils.isTabletDevice(this) && !forcePhone;
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
        String TAG=QweexUtils.TAG();
        if (!QweexUtils.androidAPIover(5)
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            doBackThings();
            Log.i(TAG, "keydown " + flip + " " + loadingApp + "   " + isTablet + " " + flip.getCurrentView() + "==" + flip.getChildAt(1));
            return flip!=null &&
                   !loadingApp &&
                   !isTablet &&
                   flip.getCurrentView()==flip.getChildAt(1);


        } else if(keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0)
        {
            if(isTablet)
                return true;

            View add = findViewById(R.id.frame);
            if(add.getVisibility()==View.VISIBLE)
            {
                hideAddButton();
            } else
            {
                //add.setAnimation(inFromBottomAnimation());
                add.setVisibility(View.VISIBLE);
                add.startAnimation(inFromBottomAnimation());
            }

            return flip==null ||                    //To avoid a null pointer
                   loadingApp ||       //If the splash is shown
                   (!isTablet &&                    //Always show it if it is a tablet
                    flip.getCurrentView()==flip.getChildAt(1));
        }

        return super.onKeyDown(keyCode, event);
    }
    static void hideAddButton()
    {
        if(((Activity)ListsActivity.context).findViewById(R.id.frame).getVisibility()==View.VISIBLE)
        {
            ((Activity)ListsActivity.context).findViewById(R.id.frame).setVisibility(View.GONE);
            ((Activity)ListsActivity.context).findViewById(R.id.frame).startAnimation(outToBottomAnimation());
        }
    }
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        String TAG= QweexUtils.TAG();
        Configuration config = getBaseContext().getResources().getConfiguration();
        // refresh your views here
        Locale.setDefault(realLocale);
        config.locale = realLocale;
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Herp");
    }
	
   /************************** Yoda methods **************************/
   //"Do or do not, there is no try"
   // These methods are essentially the Activity Lifecycle methods, but
   // they have been split off so that they can be called in other methods
	
	public void doViewStuff()
	{
        String TAG= QweexUtils.TAG();
		Log.d(TAG, "Doing view things");
		//Theme
        if(v2)
            setTheme(R.style.Version2);
        else
		    setTheme(themeID);
		
		//Force Phone
		forcePhone = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("force_phone", false);
		if(!forcePhone && QweexUtils.isTabletDevice(this))
		{
			Log.d(TAG, "Setting tablet");
		    isTablet = true;
		    if(themeID==R.style.Wunderlist || themeID==R.style.Right || v2)
		    	setContentView(R.layout.tablet_right);
		    else
		    	setContentView(R.layout.tablet);
		    findViewById(R.id.taskTitlebar).setVisibility(v2 ? View.INVISIBLE : View.GONE);
		}
		else
		{
			Log.d(TAG, "Setting phone");
			isTablet = false;
			setContentView(R.layout.phone);
			findViewById(R.id.taskTitlebar).setVisibility(View.VISIBLE);
		}

        //Time to set the typeface
        theTypeface = Typeface.createFromAsset(getAssets(), "Lato-Regular.ttf");
        //((TextView)findViewById(R.id.sweetFlatteryWillGetYouEverywhere)).setTypeface(theTypeface);
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
            if(isTablet)
                findViewById(R.id.sync).setVisibility(View.INVISIBLE);
            else
                findViewById(R.id.sync).setVisibility(View.GONE);
            //findViewById(R.id.settings).setVisibility(View.GONE); //Derp
            findViewById(R.id.settings).setPadding(0,0,10,0);
        } else
        {
            findViewById(R.id.optionsbutton).setVisibility(View.GONE);
            //New List button
            if(isTablet)
            {
                Log.d(TAG, "View.VISIBLE");
                findViewById(R.id.backbutton).setVisibility(View.GONE);
                findViewById(R.id.frame).setVisibility(View.VISIBLE);
            }
            ((View)findViewById(R.id.newTask).getParent()).setVisibility(View.GONE);
            ((View)findViewById(R.id.newList).getParent()).setVisibility(View.GONE);
        }

        ((android.widget.Button)findViewById(R.id.add_list)).setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v)
            {
                hideAddButton();
                pressCreateList();
            }
        });

        //Background
        if(backgroundPath!=null)
        {
            try {
            android.graphics.drawable.BitmapDrawable backgroundImage = new android.graphics.drawable.BitmapDrawable(android.graphics.BitmapFactory.decodeFile(backgroundPath));
            backgroundImage.setTileModeXY(android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT);
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
        String TAG= QweexUtils.TAG();
		Log.d(TAG, "Doing create things");
        ta = null;
		if(loadingOnCreate)
			return;
		if(!noSetMainView)
			doViewStuff();
		
		flip = (ViewFlipper) findViewById(R.id.FLIP);

        //Add the New List editbox
		mainListView = (ListView) findViewById(R.id.list);
        if(v2)
        {
            FrameLayout fl = new FrameLayout(this);
            newList = (EditText) findViewById(R.id.newList);
            newList.setTypeface(theTypeface);
            newList.setOnEditorActionListener(newListListener);

            newList.setOnTouchListener(new RightDrawableOnTouchListener(newList) {
                @Override
                public boolean onDrawableTouch(final MotionEvent event) {
                    String TAG= QweexUtils.TAG();
                    Log.d(TAG, "Pressed drawable");
                    String newListName = newList.getText().toString();
                    reallyCreateList(newListName);
                    return true;
                }
            });
        }


		syncLoading = ((ImageButton) findViewById(R.id.sync));
		
		//Add them onClickListeners
		((android.widget.Button)findViewById(R.id.add_list)).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				newListDialog.show();
			}
		});
		((ImageButton) findViewById(R.id.settings)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
                hideAddButton();
                Intent x = new Intent(ListsActivity.this, QuickPrefsActivity.class);
				x.putExtra("show_popup", false);
				startActivity(x);
			}
         });
		syncLoading.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                hideAddButton();
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
            Log.d(TAG, "Total count=" + listAdapter.totalCount);
         
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
					//flip.setInAnimation(context, R.anim.slide_in_right);
                    flip.setInAnimation(inFromRightAnimation());
					doCreateThingsHandler.sendEmptyMessage(1);
				}
				@Override
				public void onAnimationRepeat(Animation animation) {}
				@Override
				public void onAnimationStart(Animation animation) {}
			});
			splash.startAnimation(animation);
         }
         flip.setInAnimation(inFromRightAnimation());
         flip.setOutAnimation(outToLeftAnimation());
         loadingApp = false;
	}
	
    void doBackThings()
    {
        String TAG = QweexUtils.TAG();
        Log.i(TAG, "doing back things");
        if(!isTablet && findViewById(R.id.frame).getVisibility()==View.VISIBLE)
        {
            hideAddButton();
            return;
        }
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
	            //flip.setInAnimation(this, android.R.anim.slide_in_left);
	            //flip.setOutAnimation(this, android.R.anim.slide_out_right);
                flip.setInAnimation(inFromLeftAnimation());
                flip.setOutAnimation(outToRightAnimation());
	            ta = null;
	    		flip.showPrevious();
    		}
    	}
    }

    AsyncTask<Void, Void, Void> doCreateAsyncronously = new AsyncTask<Void, Void, Void>()
	{

		@Override
	    protected Void doInBackground(Void... params) {
            String TAG= QweexUtils.TAG();
			Log.d(TAG, "Launching asyncronously");
			syncHelper.db.open();
			DP = context.getResources().getDisplayMetrics().density;

			//Get the drawables for normal & selected according to the theme
			TypedArray a;
			a = context.getTheme().obtainStyledAttributes(ListsActivity.themeID, new int[] {R.attr.lists_selector});
        	list_normalDrawable = a.getResourceId(0, 0);
	        a = context.getTheme().obtainStyledAttributes(ListsActivity.themeID, new int[] {R.attr.lists_selected});
        	list_selectedDrawable = a.getResourceId(0, 0);

            try {
            Looper.prepare();   //This is to fix a problem with HTC. FUCK HTC.
            } catch(Exception e) {}
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
	        Log.d(TAG, "Done with the async stuff");
	        doCreateThingsHandler.sendEmptyMessage(0);
	        return null;
        }
   };

   Handler doCreateThingsHandler = new Handler()
   {
 	   @Override
 		public void handleMessage(android.os.Message msg) 
 		{
            String TAG = QweexUtils.TAG();
            Log.d(TAG, "Handling message: " + msg.what);
            if(msg.what==0)
               doCreateStuff(true);
            else
              flip.removeView(splash);
 		}
    };;
    
    
    /************************** Misc methods **************************/
   
	void pressCreateList()
	{
        newList.setTag("create");
        if(newList.getParent()!=null)
            ((ViewGroup)newList.getParent()).removeView(newList);
		newListDialog.show();
	}
	
	DialogInterface.OnClickListener createList = new DialogInterface.OnClickListener()
	{
        public void onClick(DialogInterface dialog, int whichButton) {
            String TAG= QweexUtils.TAG();
            String newListName = newList.getText().toString();
            if("".equals(newListName))
            	return;
            if(newList.getTag().equals("create"))
            {
                reallyCreateList(newListName);
            } else
            {
                Log.d(TAG, "Renaming a List " + newListName);
                syncHelper.db.modifyList((String) currentList.findViewById(R.id.listId).getTag(), "name", newListName);
               }
            
            listAdapter.changeCursor(syncHelper.db.getAllLists());
        }
    };


    void reallyCreateList(String newListName)
    {
        String TAG= QweexUtils.TAG();
        if("".equals(newListName))
            return;
        newList.setText("");
        String new_id = SyncHelper.getID();
        Log.d(TAG, "Creating a List " + newListName + " [" + new_id + "]");
        Toast.makeText(context, "Created list: " + newListName, Toast.LENGTH_SHORT).show(); //Locale
        syncHelper.db.insertList(new_id, newListName, null);
        syncHelper.db.insertListTimes(new_id, (new java.util.Date()).getTime(), 0);
        Log.d(TAG, newListName + " ");
        listAdapter.changeCursor(syncHelper.db.getAllLists());
        InputMethodManager imm = (InputMethodManager)newList.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(newList.getWindowToken(), 0);
    }

    DialogInterface.OnClickListener renameList = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int whichButton) {
            newList.setTag("rename");
            newList.setText(((TextView)currentList.findViewById(R.id.listName)).getText());
            if(newList.getParent()!=null)
                ((ViewGroup)newList.getParent()).removeView(newList);
            newListDialog.show();
        }
    };

    DialogInterface.OnClickListener deleteList = new DialogInterface.OnClickListener()
    {
        public void onClick(DialogInterface dialog, int whichButton) {
            new AlertDialog.Builder(context)
                    .setTitle("Really delete?") //Locale
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
            hideAddButton();
            String derp = (String) view.findViewById(R.id.listId).getTag();
            if(!v2 && false)
            {
                if(currentList!=null)
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
            if(tv.getParent()!=null)
                ((ViewGroup)tv.getParent()).removeView(tv);
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
          String TAG= QweexUtils.TAG();
          hideAddButton();
          if(SyncHelper.isSyncing)
    		  return;
    	  Log.d(TAG, "List Selected");
    	  
    	  //Get info
    	  String hash, name;
		  name=(String) ((TextView)view.findViewById(R.id.listName)).getText();
		  hash=(String) view.findViewById(R.id.listId).getTag();
		  Log.d(TAG, "List Selected is: " + name);
		  
		  //parent==null signifies that it is programatically selected so no need to update.
		  if(parent!=null)
		  {
			  Log.d(TAG, "Updating LastList to " + hash);
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
    	  if(ta==null)
    	  {
    		  Log.d(TAG, "Instanciating TaskActivity: " + hash);
    		  ta = new TasksActivity();
    		  ta.context = (Activity) view.getContext();
    		  ta.listHash = hash;
    		  ta.listName = name;
    		  ta.onCreate(null);	//I think I might get away with calling "doCreateThings" or even "sillygoose" but whatevs
    	  }else
    	  {
    		  Log.d(TAG, "Updating TaskActivity: " + hash);
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
    		  Log.d(TAG, "Flipping to that TaskActivity");
    		  //flip.setInAnimation(view.getContext(), R.anim.slide_in_right);
    		  //flip.setOutAnimation(view.getContext(), R.anim.slide_out_left);
              flip.setInAnimation(inFromRightAnimation());
              flip.setOutAnimation(outToLeftAnimation());
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
    
    public static final int ANIMATION_DURATION = 200;
    //http://smartandroidians.blogspot.com/2010/04/viewflipper-in-android.html
    public static Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    public static Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(ANIMATION_DURATION);
        outtoLeft.setInterpolator(new AccelerateInterpolator());
        return outtoLeft;
    }

    public static Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromLeft.setDuration(ANIMATION_DURATION);
        inFromLeft.setInterpolator(new AccelerateInterpolator());
        return inFromLeft;
    }

    public static Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoRight.setDuration(ANIMATION_DURATION);
        outtoRight.setInterpolator(new AccelerateInterpolator());
        return outtoRight;
    }



    public static Animation inFromBottomAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +0.1f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    public static Animation outToBottomAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +0.1f);
        inFromRight.setDuration(ANIMATION_DURATION);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }
    
}