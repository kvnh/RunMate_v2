package com.khackett.runmate.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.khackett.runmate.R;
import com.khackett.runmate.adapters.UserAdapter;
import com.khackett.runmate.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class EditFriendsActivity extends Activity {

    // TAG to represent the EditFriendsActivity class
    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    // set up a reference to the current user
    protected ParseUser mCurrentUser;
    // set up a member variable to store a list of users returned from the parse user query
    protected List<ParseUser> mUsers;
    // set up a ParseRelation member to hold ParseUsers
    protected ParseRelation<ParseUser> mFriendsRelation;
    // Create a variable for the GridView
    protected GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set up progress bar
        // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // Set the GridView layout
        setContentView(R.layout.user_grid);

        // line to ensure the action bar displays in the layout
        // getActionBar().setDisplayHomeAsUpEnabled(true);

        // Show the Up button in the action bar.
        // setupActionBar();

        // Set the GridView in the layout
        mGridView = (GridView) findViewById(R.id.friendsGrid);
        // get the default grid view associated with this activity and set it to allow multiple items/friends to be checked
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);

        // Add a listener to detect when items on the grid view have been tapped
        mGridView.setOnItemClickListener(mOnItemClickListener);

        // Check that there are friends to display - if not, display a message
        TextView emptyFriendsList = (TextView) findViewById(android.R.id.empty);
        // Attach this as the empty text view for the GridView
        mGridView.setEmptyView(emptyFriendsList);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get the current user using the getCurrentUser() method
        mCurrentUser = ParseUser.getCurrentUser();
        // for the relation, from this user we want to call a method called getRelation()
        mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

        // set progress bar to visible
        // setProgressBarIndeterminateVisibility(true);

        // the query is going to return a list of ParseUser objects
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        // sort the results of the query in ascending order by username (using the "username" key)
        query.orderByAscending(ParseConstants.KEY_USERNAME);
        // set limits for our queries to 1000 users
        query.setLimit(1000);
        // execute the query
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> users, ParseException e) {

                // set progress bar to invisible
                // setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    // successful query - we have users to display
                    mUsers = users;
                    // create an array of strings to store the usernames and set the size equal to that of the list returned
                    String[] usernames = new String[mUsers.size()];
                    // enhanced for loop to go through the list of parse users and extract the usernames
                    int i = 0;
                    for (ParseUser user : mUsers) {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    // Use the custom user adapter
                    // Get the adapter associated with the GridView and check to see if it is null
                    if (mGridView.getAdapter() == null) {
                        // Use the custom UserAdapter to display the users in the GridView
                        UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                        // Call setAdapter for this activity to set the items in the GridView
                        mGridView.setAdapter(adapter);
                    } else {
                        // GridView is not available - refill with the list of friends
                        ((UserAdapter) mGridView.getAdapter()).refill(mUsers);
                    }

                    addFriendCheckMarks();

                } else {
                    // there was an error - log the message
                    Log.e(TAG, e.getMessage());
                    // display an alert to the user
                    // if there is a parse exception then...
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                    // set the message from the exception
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    //    /**
//     * Set up the {@link android.app.ActionBar}.
//     */
//    private void setupActionBar() {
//        getActionBar().setDisplayHomeAsUpEnabled(true);
//    }


//    // can remove this as there is no need for the options menu on this screen
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_edit_friends, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button,
        // so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void addFriendCheckMarks() {
        // the first thing we need is a list of the users friends
        // we have the friend relation, but this doesn't give us a list of users to work with
        // the list itself is still on the back end, we need to use the ParseRelation to retrieve it
        // use the build in query to retrieve it - this gets us the query associated with this ParseRelation
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                // We are working with asynchronous tasks so we need to think about how our app will behave if the asynchronous
                // task takes longer than anticipated...
                // We start this activity by getting the list of all users in the onResume() method;
                // then when that is complete, we come down here and kick off this second task which then runs asynchronously itself...
                // When this comes back, the check marks get updated
                if (e == null) {
                    // query successful and list is returned - look for a match by looping through the entire list of users...
                    // for each one we'll check it in our list of friends
                    // (this shouldn't be too bad, because at most our list of users will be 1000
                    for (int i = 0; i < mUsers.size(); i++) {
                        // store the user in a ParseUser variable
                        ParseUser user = mUsers.get(i);

                        // write a separate for loop to loop through the list of friends that is returned in this done() method
                        for (ParseUser friend : friends) {
                            // compare the object id of each friend (the object id is the unique id created each time a new user signs up)
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                // set the check mark
                                mGridView.setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    // Show or hide the image for the check mark overlay
    // The item that is tapped on, gets passed in as the view parameter
    // The view parameter is the relative layout of the user_item.xml
    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Get a reference to the ImageView
            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkFriendImageView);

            if (mGridView.isItemChecked(position)) {
                // Add a friend if checked
                // Pass in the user that was tapped on as the parameter (the position of the item that is tapped on)
                // Map this to our list of users stored in the variable mUsers
                // For a list variable (mUsers), we use the get() method
                // Now the user at the current position will be added to the friends relation
                mFriendsRelation.add(mUsers.get(position));

                // Manipulate the image view for the check mark - set to visible when selected
                checkImageView.setVisibility(View.VISIBLE);
            } else {
                // remove the friend by calling the remove() method of ParseRelation
                mFriendsRelation.remove(mUsers.get(position));

                // Manipulate the image view for the check mark - set to invisible when selected
                checkImageView.setVisibility(View.INVISIBLE);
            }

            // the user is added/removed locally, but we also need to save this relation to the backend
            // choose the asynchronous method saveInBackground() and for the callback, use new SaveCallback()
            mCurrentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    // Don't do anything if successful
                    // If it fails, let the user retry deleting them as friends again and log the exception
                    if (e != null) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        }
    };
}

