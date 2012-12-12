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


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/** A tool for communicating with the SQlite database
 *  It has 3 different tables in it: episodes, queue, and calendar
 * @author MrQweex
 */

public class DatabaseConnector 
{
	//------Basic Functions
	private static final String DATABASE_NAME = "Nitro.db", TASKS_TABLE = "tasks", TASKS_TIME_TABLE = "ttimes",
								LISTS_TABLE = "lists", LISTS_TIME_TABLE = "ltimes";
	private SQLiteDatabase database;
	private DatabaseOpenHelper databaseOpenHelper = null;
	
	/** Constructor for the class.
	 * @param context The context to associate with the connector.
	 * */
	public DatabaseConnector(Context context) 
	{
	    databaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
	}
	
	/** Opens the database so that it can be read or written. */
	public void open() throws SQLException 
	{
	   database = databaseOpenHelper.getWritableDatabase();
	}
	
	/** Closes the database when you are done with it. */
	public void close() 
	{
	   if (database != null)
	      database.close();
	}
	
	public void insertTask(String hash,
							  String name,
							  long priority,
							  long date,
							  String notes,
							  String list,
							  long logged,
							  String tags,
							  int order)
	{
	   ContentValues newTask = new ContentValues();
	   newTask.put("hash", hash);
	   newTask.put("name", name);
	   newTask.put("priority", priority);
	   newTask.put("date", date);
	   newTask.put("notes", notes);
	   newTask.put("list", list);
	   newTask.put("logged", logged);
	   newTask.put("tags", tags);
	   newTask.put("order_num", order);
	
	   database.insert(TASKS_TABLE, null, newTask);
	}
	
	public boolean deleteTask(String hash) 
	{
	    return database.delete(TASKS_TABLE, "hash='" + hash + "'", null) > 0;
	}
	
	//OVERLOAD _ALL_ THE FUNCTIONS!
	
	public boolean modifyTask(String hash, String[] columns, String[] new_vals)
	{
		ContentValues args = new ContentValues();
		for(int i=0; i<columns.length; i++)
			args.put(columns[i], new_vals[i]);
	    return database.update(TASKS_TABLE, args, "hash='" + hash + "'", null)>0;
	}
	
	public boolean modifyTask(String hash, String column, int new_val)
	{
		ContentValues args = new ContentValues();
	    args.put(column, new_val);
	    return database.update(TASKS_TABLE, args, "hash='" + hash + "'", null)>0;
	}
	
	public boolean modifyTask(String hash, String column, long new_val)
	{
		ContentValues args = new ContentValues();
	    args.put(column, new_val);
	    return database.update(TASKS_TABLE, args, "hash='" + hash + "'", null)>0;
	}
	
	public boolean modifyTask(String hash, String column, String new_val)
	{
		ContentValues args = new ContentValues();
		System.out.println("MODIFYTASK : " + column);
	    args.put(column, new_val);
	    return database.update(TASKS_TABLE, args, "hash='" + hash + "'", null)>0;
	}
	
	
	public void modifyOrder(String hash, int new_order)
	{
		modifyTask(hash, "order_num", new_order);
	}
	
	public void modifyListOrder(String hash, String new_order)
	{
	 	ContentValues args = new ContentValues();
	    args.put("tasks_in_order", new_order);
	    database.update(LISTS_TABLE, args, "hash='" + hash + "'", null);
	}
	
	public void insertList(String hash,
			  String name,
			  String[] tasks_in_order)
	{
		ContentValues newList = new ContentValues();
		newList.put("hash", hash);
		newList.put("name", name);
		String tasksString = "";
		if(tasks_in_order!=null && tasks_in_order.length>0)
		{
			tasksString = tasks_in_order[0];
			for(int i=0; i<tasks_in_order.length; i++)
				tasksString = tasksString.concat("|" + tasks_in_order[i]);
		}
		newList.put("tasks_in_order", tasksString);
		
		database.insert(LISTS_TABLE, null, newList);
	}
	
	public Cursor getAllLists()
	{
		System.out.println("HEY THERE");
		return database.query(LISTS_TABLE, new String[] {"_id", "hash", "name", "tasks_in_order"},
				null, null, null, null, null);
	}
	
	public Cursor getTodayTasks(long currentDay)
	{
		long msecondsInDay = 60 * 60 * 24 * 1000;
		String query = "SELECT * FROM " + TASKS_TABLE + " " + "WHERE date " + 
				"BETWEEN " + (currentDay-1) + " AND "  + (currentDay+msecondsInDay-1);
		return database.rawQuery(query, null);
	}
	
	public Cursor getTasksOfList(String hash, String sort)
	{
		if(hash!=null)
		{
			if(!hash.equals(""))
			{
				hash = "list = '" + hash + "'";
				if(!hash.equals("list = 'logbook'"))
					hash = hash + " AND logged='0'";
			}
		} else
			hash = "logged='0'";
		return database.query(TASKS_TABLE,
			    new String[] {"_id", "hash", "name", "priority", "date", "notes", "list", "logged", "tags"},
			    hash, null, null, null, sort);
	}
	
	
	public void clearEverything(Context context)
	{
		database.execSQL("DROP TABLE " + LISTS_TABLE);
		database.execSQL("DROP TABLE " + TASKS_TABLE);
		database.execSQL("DROP TABLE " + LISTS_TIME_TABLE);
		database.execSQL("DROP TABLE " + TASKS_TIME_TABLE);
		createThemTables(database);
		open();
		System.out.println("CELARD: " + getAllLists().getCount());
	}
	
	
	public void createThemTables(SQLiteDatabase db)
	{
	      String createQuery = "CREATE TABLE " + TASKS_TABLE + " " + 
	 	         "(_id integer primary key autoincrement, " +
	 	    		"hash TEXT, " +
	 	         	"name TEXT, " +
	 	         	"priority INTEGER, " + 
	 	         	"date INTEGER, " +
	 	         	"notes TEXT, " +
	 	         	"list TEXT, " +
	 	         	"logged INTEGER, " +
	 	         	"tags TEXT, " +
	 	         	"order_num INTEGER);";
	 	      db.execSQL(createQuery);
	 	      String createQuery2 = "CREATE TABLE " + TASKS_TIME_TABLE + " " + 
	 	 	         "(_id integer primary key autoincrement, " +
	 	 	         	"hash TEXT, " +
	 	 	         	"name TEXT, " +
	 	 	         	"priority INTEGER, " + 
	 	 	         	"date INTEGER, " +
	 	 	         	"notes TEXT, " +
	 	 	         	"list TEXT, " +
	 	 	         	"logged INTEGER, " +
	 	 	         	"tags TEXT);";
	 	      db.execSQL(createQuery2);
	 	      String createQuery3 = "CREATE TABLE " + LISTS_TABLE + " " + 
	 		 	         "(_id integer primary key autoincrement, " +
	 		 	         	"hash TEXT, " +
	 		 	         	"name TEXT, " +
	 		 	         	"tasks_in_order TEXT);"; //Actually an array; probably pipe delimted
	 	      db.execSQL(createQuery3);
	 	      String createQuery4 = "CREATE TABLE " + LISTS_TIME_TABLE + " " + 
	 		 	         "(_id integer primary key autoincrement, " +
	 		 	         	"hash TEXT, " +
	 		 	         	"name TEXT, " +
	 		 	         	"tasks_in_order TEXT);";
	 	      db.execSQL(createQuery4);
	}
	
/** Helper open class for DatabaseConnector */
	private class DatabaseOpenHelper extends SQLiteOpenHelper 
	{
	   public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) 
	   {
	      super(context, name, factory, version);
	      this.getWritableDatabase();
	   }
	
	   @Override
	   public void onCreate(SQLiteDatabase db) 
	   {
	      createThemTables(db);
	   }
	
	   //FEATURE: Need to expect onUpgrade
	   @Override
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	   {
	   }
	}
}
