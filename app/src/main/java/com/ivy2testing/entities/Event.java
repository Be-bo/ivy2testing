package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/** @author Zahra Ghavasieh
 * Overview: Class to store a Firebase Post document
 * Features: firebase compatible, Parcelable (can pass as intent Extra)
 */
public class Event implements Parcelable {

    // Fields
    private String id;
    private String uni_domain;
    private String author_id;
    private String author_name;
    private final boolean is_event = true;
    private boolean main_feed_visible = true;
    private long creation_millis = 0;
    private String name;
    private long start_millis;
    private long end_millis;
    private boolean is_featured = false;
    private boolean is_active = true;
    private String link;
    private String location;
    private String text;
    private String visual;
    private String pinned_id;
    private List<String> views_id;
    private List<String> going_ids;


/* Constructors
***************************************************************************************************/

    // Requirement for FireStore
    public Event(){}

    // Use for creating a new Post in code TODO change parameters depending on createEvent page
    public Event(String id, String uni_domain, String author_id, String author_name,
                boolean main_feed_visible, String pinned_id){
        this.id = id;
        this.uni_domain = uni_domain;
        this.author_id = author_id;
        this.author_name = author_name;
        this.main_feed_visible = main_feed_visible;
        this.pinned_id = pinned_id;

        this.views_id = new ArrayList<>();
        creation_millis = System.currentTimeMillis();
    }

    // Make Event from Post
    public Event (Post post){
        this(post.getId(), post.getUni_domain(), post.getAuthor_id(), post.getAuthor_name(),
                post.getMain_feed_visible(), post.getPinned_id());
    }

/* Getters
***************************************************************************************************/

    // Don't write ID in database! (redundant)
    @Exclude
    public String getId() {
        return id;
    }

    public String getUni_domain() {
        return uni_domain;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public boolean isIs_event() {
        return is_event;
    }

    public boolean isMain_feed_visible() {
        return main_feed_visible;
    }

    public long getCreation_millis() {
        return creation_millis;
    }

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

    public String getText() {
        return text;
    }

    public String getVisual() {
        return visual;
    }

    public String getPinned_id() {
        return pinned_id;
    }

    public List<String> getViews_id() {
        return views_id;
    }

    public List<String> getGoing_ids() {
        if (views_id == null) return new ArrayList<>();
        else return new ArrayList<>(going_ids);
    }

/* Setters
***************************************************************************************************/

    public void setUni_domain(String uni_domain) {
        this.uni_domain = uni_domain;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public void setMain_feed_visible(boolean main_feed_visible) {
        this.main_feed_visible = main_feed_visible;
    }

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

    public void setText(String text) {
        this.text = text;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    public void setPinned_id(String pinned_id) {
        this.pinned_id = pinned_id;
    }

    public void addViewIdToList(String userId){
        if (userId != null && !userId.isEmpty()) views_id.add(userId);
    }

    public void deleteViewIdFromList(String userId){
        views_id.remove(userId);
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
        id = in.readString();
        uni_domain = in.readString();
        author_id = in.readString();
        author_name = in.readString();
        main_feed_visible = in.readByte() != 0;
        creation_millis = in.readLong();
        name = in.readString();
        start_millis = in.readLong();
        end_millis = in.readLong();
        is_featured = in.readByte() != 0;
        is_active = in.readByte() != 0;
        link = in.readString();
        location = in.readString();
        text = in.readString();
        visual = in.readString();
        pinned_id = in.readString();
        views_id = in.createStringArrayList();
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
        dest.writeString(id);
        dest.writeString(uni_domain);
        dest.writeString(author_id);
        dest.writeString(author_name);
        dest.writeByte((byte) (main_feed_visible ? 1 : 0));
        dest.writeLong(creation_millis);
        dest.writeString(name);
        dest.writeLong(start_millis);
        dest.writeLong(end_millis);
        dest.writeByte((byte) (is_featured ? 1 : 0));
        dest.writeByte((byte) (is_active ? 1 : 0));
        dest.writeString(link);
        dest.writeString(location);
        dest.writeString(text);
        dest.writeString(visual);
        dest.writeString(pinned_id);
        dest.writeStringList(views_id);
        dest.writeStringList(going_ids);
    }
}
