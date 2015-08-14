package com.khackett.runmate;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by KHackett on 10/08/15.
 */

public class AdapterNavigation extends RecyclerView.Adapter<AdapterNavigation.ViewHolder> {

    // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // String Array to store the passed titles Value from MainActivity.java
    // Int Array to store the passed icons resource value from MainActivity.java
    private String mNavTitles[];
    private int mIcons[];

    //String Resource for header View Name
    private String name;
    //int Resource for header view profile picture
    private int profile;
    //String Resource for header view email
    private String email;

    // Creating a ViewHolder which extends the RecyclerView View Holder
    // ViewHolder are used to to store the inflated views in order to recycle them
    public static class ViewHolder extends RecyclerView.ViewHolder {
        int holderId;
        TextView textView;
        ImageView imageView;
        ImageView profile;
        TextView Name;
        TextView email;

        // Creating ViewHolder Constructor with View and viewType As a parameter
        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created
            if (ViewType == TYPE_ITEM) {
                // Creating TextView object with the id of textView from item_row.xml
                textView = (TextView) itemView.findViewById(R.id.rowText);
                // Creating ImageView object with the id of ImageView from item_row.xml
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                // setting holder id as 1 as the object being populated are of type item row
                holderId = 1;
            } else {
                // Creating Text View object from header.xml for name
                Name = (TextView) itemView.findViewById(R.id.name);
                // Creating Text View object from header.xml for email
                email = (TextView) itemView.findViewById(R.id.email);
                // Creating Image view object from header.xml for profile pic
                profile = (ImageView) itemView.findViewById(R.id.circleView);
                // Setting holder id = 0 as the object being populated are of type header view
                holderId = 0;
            }
        }
    }

    // AdapterNavigation Constructor with titles and icons parameter titles, icons, name, email, profile pic
    // are passed from the main activity as we have seen earlier
    AdapterNavigation(String Titles[], int Icons[], String Name, String Email, int Profile) {
        //here we assign those passed values to the values we declared here in adapter
        mNavTitles = Titles;
        mIcons = Icons;
        name = Name;
        email = Email;
        profile = Profile;
    }

    //Below first we override the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER and pass it to the view holder
    @Override
    public AdapterNavigation.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_item_row, parent, false); //Inflating the layout
            //Creating ViewHolder and passing the object of type view
            ViewHolder vhItem = new ViewHolder(v, viewType);
            return vhItem; // Returning the created object
            //inflate your layout and pass it to view holder
        } else if (viewType == TYPE_HEADER) {
            //Inflating the layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_header, parent, false);
            //Creating ViewHolder and passing the object of type view
            ViewHolder vhHeader = new ViewHolder(v, viewType);
            //returning the object created
            return vhHeader;
        }
        return null;
    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(AdapterNavigation.ViewHolder holder, int position) {
        // as the list view is going to be called after the header view so we decrement the
        if (holder.holderId == 1) {
            // position by 1 and pass it to the holder while setting the text and image
            // Setting the Text with the array of our Titles
            holder.textView.setText(mNavTitles[position - 1]);
            // Setting the image with array of our icons
            holder.imageView.setImageResource(mIcons[position - 1]);
        } else {
            // Similarly we set the resources for header view
            holder.profile.setImageResource(profile);
            holder.Name.setText(name);
            holder.email.setText(email);
        }
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        // the number of items in the list will be +1 the titles including the header view.
        return mNavTitles.length + 1;
    }

    // With the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }
}