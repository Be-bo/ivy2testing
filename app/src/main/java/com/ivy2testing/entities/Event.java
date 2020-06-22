package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Class to store a Firebase Event document
 * Features: firebase compatible, Parcelable (can pass as intent Extra)
 */
public class Event extends Post {

    // Extra Fields (Events only!)
    private long end_millis;
    private List<String> going_ids;
    private boolean is_active = true;
    private boolean is_featured = false;
    private String link;
    private String location;
    private String name;
    private long start_millis;


/* Constructors
***************************************************************************************************/

    // Requirement for FireStore
    public Event(){
        super(true);
    }

    // Use for creating a new Event in code TODO change parameters depending on createEvent page
    public Event(String id, String uni_domain, String author_id, String author_name,
                boolean main_feed_visible, String pinned_id, String pinned_name, String visual){
        super(id, uni_domain, author_id, author_name, main_feed_visible, pinned_id, pinned_name, visual);

        // going IDs needs to be instantiated for it to go in the database with the constructor...
        this.going_ids = new ArrayList<>();
    }

    // Make Event from Post
    public Event (Post post){
        this(post.getId(), post.getUni_domain(), post.getAuthor_id(), post.getAuthor_name(),
                post.isMain_feed_visible(), post.getPinned_id(), post.getPinned_name(), post.getVisual());
        is_event = true;
    }

/* Getters
***************************************************************************************************/

    public String getName() {
        return name;
    }

    public long getStart_millis() {
        return start_millis;
    }

    public long getEnd_millis() {
        return end_millis;
    }

    public boolean isIs_featured() {
        return is_featured;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public String getLink() {
        return link;
    }

    public String getLocation() {
        return location;
    }

    // getters are required for pushing to firebase. This constructor can be simplified...
    public List<String> getGoing_ids() {
        if (going_ids == null) return new ArrayList<>();
        else return new ArrayList<>(going_ids);
    }

/* Setters
***************************************************************************************************/

    public void setName(String name) {
        this.name = name;
    }

    public void setStart_millis(long start_millis) {
        this.start_millis = start_millis;
    }

    public void setEnd_millis(long end_millis) {
        this.end_millis = end_millis;
    }

    public void setIs_featured(boolean is_featured) {
        this.is_featured = is_featured;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void addGoingIdToList(String userId){
        if (userId != null && !userId.isEmpty()) going_ids.add(userId);
    }

    public void deleteGoingIdFromList(String userId){
        going_ids.remove(userId);
    }


/* Parcelable Override Methods
***************************************************************************************************/

    protected Event(Parcel in) {
        super(in);
        name = in.readString();
        start_millis = in.readLong();
        end_millis = in.readLong();
        is_featured = in.readByte() != 0;
        is_active = in.readByte() != 0;
        link = in.readString();
        location = in.readString();
        going_ids = in.createStringArrayList();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(name);
        dest.writeLong(start_millis);
        dest.writeLong(end_millis);
        dest.writeByte((byte) (is_featured ? 1 : 0));
        dest.writeByte((byte) (is_active ? 1 : 0));
        dest.writeString(link);
        dest.writeString(location);
        dest.writeStringList(going_ids);
    }
}
