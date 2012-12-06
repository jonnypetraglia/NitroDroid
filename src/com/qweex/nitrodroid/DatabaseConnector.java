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
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/** A tool for communicating with the SQlite database
 *  It has 3 different tables in it: episodes, queue, and calendar
 * @author MrQweex
 */

public class DatabaseConnector 
{
	//------Basic Functions
	private static final String TASKS_TABLE = "TASKS.db", TASKS_TIME_TABLE = "TTIMES.db",
								LISTS_TABLE = "LISTS.db", LISTS_TIME_TABLE = "LTIMES.db";
	private SQLiteDatabase database;
	private DatabaseOpenHelper databaseOpenHelper = null;
	
	/** Constructor for the class.
	 * @param context The context to associate with the connector.
	 * */
	public DatabaseConnector(Context context) 
	{
	    databaseOpenHelper = new DatabaseOpenHelper(context, TASKS_TABLE, null, 1);
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
	   newTask.put("logged", logged);
	
	   database.insert(TASKS_TABLE, null, newTask);
	}
/** Helper open class for DatabaseConnector */
	private class DatabaseOpenHelper extends SQLiteOpenHelper 
	{
	   public DatabaseOpenHelper(Context context, String name, CursorFactory factory, int version) 
	   {
	      super(context, name, factory, version);
	   }
	
	   @Override
	   public void onCreate(SQLiteDatabase db) 
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
	      createQuery = "CREATE TABLE " + TASKS_TIME_TABLE + " " + 
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
	      createQuery = "CREATE TABLE " + LISTS_TABLE + " " + 
		 	         "(_id integer primary key autoincrement, " +
		 	         	"hash TEXT, " +
		 	         	"name TEXT, " +
		 	         	"tasks_in_order TEXT);"; //Actually an array; probably pipe delimted
	      db.execSQL(createQuery);
	      createQuery = "CREATE TABLE " + LISTS_TIME_TABLE + " " + 
		 	         "(_id integer primary key autoincrement, " +
		 	         	"hash TEXT, " +
		 	         	"name TEXT, " +
		 	         	"tasks_in_order TEXT);";
	      db.execSQL(createQuery);
	      
	   }
	
	   //FEATURE: Need to expect onUpgrade
	   @Override
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	   {
	   }
	}
}
