package com.qweex.nitrodroid;
/*
Copyright (C) 2012 Qweex
This file is a part of Callisto.

Callisto is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

Callisto is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Callisto; If not, see <http://www.gnu.org/licenses/>.
*/


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
							  String tags)
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
	
	   database.insert(TASKS_TABLE, null, newTask);
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
		open();
		Cursor c = database.query(LISTS_TABLE, new String[] {"_id", "hash", "name", "tasks_in_order"},
				null, null, null, null, null);
		c.getCount();		//I have no clue why the fuck this has to be here. If you don't call it, the cursor is empty.
		close();
		return c;
	}
	
	public Cursor getTasksOfList(String hash)
	{
		open();
		if(hash!=null)
			hash = "list = '" + hash + "'";
		Cursor c = database.query(TASKS_TABLE,
			    new String[] {"_id", "hash", "name", "priority", "date", "notes", "list", "logged", "tags"},
			    hash,
			   	null,
			   	null,
			   	null,
			   	null);
		c.getCount();		//I have no clue why the fuck this has to be here. If you don't call it, the cursor is empty.
		close();
		return c;
	}
	
	
	public void clearEverything(Context context)
	{
		open();
		database.execSQL("DROP TABLE IF EXISTS " + LISTS_TABLE);
		database.execSQL("DROP TABLE IF EXISTS " + TASKS_TABLE);
		database.execSQL("DROP TABLE IF EXISTS " + LISTS_TIME_TABLE);
		database.execSQL("DROP TABLE IF EXISTS " + TASKS_TIME_TABLE);
		createThemTables(database);
		close();
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
	 	         	"tags TEXT);";
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
