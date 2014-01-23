/*
Copyright (c) 2012-2014 Jon Petraglia of Qweex

This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.

    2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
 */
package com.qweex.nitrodroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: notbryant
 * Date: 1/16/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class SyncHelper_v2
{
    public DatabaseConnector db;
    Context context;
    SocketIO socket;
    final String url = "http://10.0.2.2",
                 port = "5000";
    private boolean isOnline = true, isEnabled = true;
    LinkedList<Triple> queue = new LinkedList<Triple>();
    String username;

    private class Triple {
        public String name; public JSONObject args; public long time;
        public Triple(String n, JSONObject a, long t) { name=n; args=a; time=t;}
    }
    /*
    public SyncHelper_v2(Context c)
    {

        db = new DatabaseConnector(c);
        this.context = c;

        //get user information
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());

        System.out.println("DERP: Creating SyncHelper");
        try {
        socket = new SocketIO(url + ":" + port);
        }catch(Exception e) {}

        collection = new Collection(this);
        socket.connect(collection);
        socket.send("DERP");
    }


    // Send data to the server using Socket.IO
    public void emit(String name, JSONObject args, IOAcknowledge fn)
    {
        // If the user is not online...
        if(!isOnline)
        {
            addToQueue(name, args);
            return;
        }
        if(!isEnabled) return;
        socket.emit(name, fn, args);
    }

    public void sync()
    {
        //Send queue to server
        emit("sync", queue, new IOAcknowledge() {
            @Override
            public void ack(Object... objects) {
                //Update records
                queue = null;
                saveQueue();
            }
        });
        //Update Records?
    }

    public void disable(IOCallback callback)
    {
        if(isEnabled)
        {
            isEnabled = false;
            //do callback
            //finally
            {
            isEnabled = true;
            }
        }
        else
        {
            //do callback
        }
    }

    // Check an event, and if it is a model update add it to the queue
    // TODO: Add optimization for duplicate events with timestamps
    public void addToQueue(String name, JSONObject args)
    {
        if("create".equals(name) || "update".equals(name) || "destroy".equals(name))
        {
            Date now = new Date();
            long time;
            if("update".equals(name))
            {
                time = 0;
                //for own key of args[1]
                //  continue if key is "id"
                //  time[key] = now
            }
            else
                time = now.getTime();
            queue.push(new Triple(name, args, time));
            optimizeQueue();
        }

    }

    private void optimizeQueue()
    {
        //TODO: TBD
    }

    private void saveQueue()
    {
        //TODO: TBD
    }

    private void goOffline()
    {
        isOnline = false;
    }

    private void login()
    {
        socket.emit("login", username);
    }

    Collection collection;
    class Collection implements IOCallback
    {
        Object model;
        public Collection(Object model)
        {
            this.model = model;
        }

        @Override
        public void onMessage(JSONObject json, IOAcknowledge ack) {
            try {
                System.out.println("DERP: Server said:" + json.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(String data, IOAcknowledge ack) {
            System.out.println("DERP: Server said: " + data);
        }

        @Override
        public void onError(SocketIOException socketIOException) {
            System.out.println("DERP: an Error occured");
            socketIOException.printStackTrace();
        }

        @Override
        public void onDisconnect() {
            System.out.println("DERP: Connection terminated.");
        }

        @Override
        public void onConnect() {
            System.out.println("DERP: Connection established");
        }

        @Override
        public void on(String event, IOAcknowledge ack, Object... data)
        {
             if(event.equals("create"))
             {
                 //[className, item] = data
                 //if classname is @model.className
                 //   Sync.disable =>
                 //     @model.create item
             }
             else if(event.equals("update"))
             {
                 //[className, item] = data
                 //if classname is @model.className
                 //   Sync.disable =>
                 //     @model.find(item.id).updateAttributes(item)
             }
             else if(event.equals("destroy"))
             {
                 //[className, item] = data
                 //if classname is @model.className
                 //   Sync.disable =>
                 //     @model.find(id).destroy()
             }
     // ## Handle offline modes
             else if(event.equals("error") || event.equals("disconnect") || event.equals("connect_failed"))
                 goOffline();
             System.out.println("DERP: Server triggered event '" + event + "'");
        }

        public void all(Object params, IOCallback callback)
        {
            emit("fetch", @model.className, new IOAcknowledge(){
            @Override
            public void ack(Object... data) {
                recordsResponse(data);
                callback.onMessage(data, null);
            }
        });
        }

        public void fetch(Object params, Object options)
        {
            all(params, new IOCallback() {
                @Override
                public void onDisconnect() {}
                @Override
                public void onConnect() {}
                @Override
                public void onMessage(String records, IOAcknowledge ioAcknowledge)
                {
                    @model.refresh(records, options);
                }
                @Override
                public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {}
                @Override
                public void on(String s, IOAcknowledge ioAcknowledge, Object... objects) {}
                @Override
                public void onError(SocketIOException e) {}
            });
        }

        public void recordsResponse(Object data)
        {
            @model.trigger("syncSuccess", data);
        }

    };



    //1. in "sync" when you call "emit" with "queue", what is that supposed to do?
    //2. What exactly does "disable" do and what does "do callback" mean? wait for the result, or pretent like there was a result?
    //3. need to understand what "for own key" does in "addToQueue"
    //4. Need to figure out "optimizeQueue". Have not even tried.
    //5. What does "saveQueue" mean for a SQLite database?
    //6. does "login" need a callback?
    //7. is "Collection" what we are supposed to do, since there is no "setOn" for SocketIO?
    //8. wtf is "all" and "fetch"? callbacks? should they be regular functions, or IOCallbacks?
    //9. WTF is "recordsResponse"? UI-specific?
    //10. What the fuck is the "Singleton" class? as well as "Include" and "Extend"
    //11. Have NO clue what the fuck "Model.Sync" is
    //12. Have no clue what the fuck "Model.Sync.Methods" is
    //13. Have no clue what the fuck the Globals are/do
    //14. For "all": what is: @model.className, callback data
    //15. For "fetch": what method in the callback should be called?
    //16. For "fetch": what is @model.refresh
    //17. For recordsResponse: what is @model.trigger



    /****************************** MISC ******************************/

    //SEND: login, fetch, sync, create, destroy, update
    //RECEIVE: create, update, destroy


    /* SAMPLE DATA:

    login "username"

    fetch "Task"

    fetch "List"

    sync [["create",["List",{"name":"Inbox","tasks":["c-0"],"permanent":true,"id":"inbox"}],1358394828603],["create",["Task",{"name":"abcde","completed":false,"priority":1,"list":"inbox","id":"c-0"}],1358394907077]]
    create ["Task",{"name":"xyz","completed":false,"priority":1,"list":"inbox","id":"c-1"}]

    destroy ["Task","c-0"]

    update ["Task",{"id":"c-1","notes":"Ragnarok"}]

    create ["List",{"name":"Ra","tasks":[],"id":"c-1"}]

    create ["List",{"name":"Ra","tasks":[],"id":"c-1"}]

    sync [["create",["List",{"name":"Inbox","tasks":["c-0"],"permanent":true,"id":"inbox"}],1358394828603],["create",["Task",{"name":"abcde","completed":false,"priority":1,"list":"inbox","id":"c-0"}],1358394907077]]
    create ["Task",{"name":"dsa","completed":false,"priority":1,"list":"c-1","id":"c-3"}]
     */
}
