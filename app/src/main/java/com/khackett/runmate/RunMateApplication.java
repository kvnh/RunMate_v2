package com.khackett.runmate;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by KHackett on 06/08/15.
 */
public class RunMateApplication extends Application {

    @Override
    public void onCreate() {
        // since we are overriding the method from the parent, add super.onCreate() to make
        // sure all of the code in the base class gets called too
        super.onCreate();
        Parse.enableLocalDatastore(this);
        // the 2 parameters are the application ID and the client ID that we need to access our backend in parse.com
        Parse.initialize(this, "x85NrHETZkoNMOgxiPQIFCJ27iqOPnOeul8P0KA7", "cDcbwrY4pr0yBk9HFvjLt84wQF7EIRgVb9wRd0nk");

        // use to test that objects are being saved to parse backend
        // the name of the class is TestObject as can be seen in the core in parse.com
        // in terms of relational databases, TestObject is a table and the row of data is a row in the table
        // ... so first line creates a parse object with a class name
        // ParseObject testObject = new ParseObject("TestObject");
        // this line sets the value 'bar' for 'foo'
        // testObject.put("foo", "bar");
        // this line is the save method which saves the object in the background as a background thread, not in our main thread for the app
        // testObject.saveInBackground();
    }
}
