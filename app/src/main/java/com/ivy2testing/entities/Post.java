package com.ivy2testing.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/** @author Clyde
 * @author Zahra Ghavasieh
 * Overview: Class to store a Firebase Post document
 * Features: firebase compatible, Parcelable (can pass as intent Extra)
 */
public class Post implements Parcelable {

    // Fields
    protected String id;
    protected String uni_domain;
    protected String author_id;
    protected String author_name;
    protected boolean author_is_organization = false;
    protected boolean is_event = false;
    protected boolean main_feed_visible = true;
    protected long creation_millis = 0;
    protected String creation_platform = "Android";
    protected String text;
    protected String visual;
    protected String pinned_id;
    protected String pinned_name;
    protected List<String> views_id;


/* Constructors
***************************************************************************************************/

    // Requirement for FireStore
    public Post(){}

    // Constructor mainly used by children
    public Post(boolean is_event){
        this.is_event = is_event;
    }

    // Use for creating a new Post in code
    public Post(String id, String uni_domain, String author_id, String author_name,
                boolean main_feed_visible, String pinned_id, String pinned_name, String visual){
        this.id = id;
        this.uni_domain = uni_domain;
        this.author_id = author_id;
        this.author_name = author_name;
        this.main_feed_visible = main_feed_visible;
        this.pinned_id = pinned_id;
        this.pinned_name = pinned_name;

        this.visual = visual;

        this.views_id = new ArrayList<>();
        creation_millis = System.currentTimeMillis();
    }

    // Make Post from Event
    public Post(Event event){
        this(event.getId(), event.getUni_domain(), event.getAuthor_id(), event.getAuthor_name(),
                event.isMain_feed_visible(), event.getPinned_id(), event.getPinned_name(), event.getVisual());
        this.author_is_organization = event.getAuthor_is_organization();
    }


/* Setters and Getters
***************************************************************************************************/

    // Don't write ID in database! (redundant)
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id != null && !id.isEmpty()) this.id = id;
    }

    public String getUni_domain() {
        return uni_domain;
    }

    public void setUni_domain(String uni_domain) {
        if (uni_domain != null && !uni_domain.isEmpty())
            this.uni_domain = uni_domain;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        if (author_id != null && !author_id.isEmpty())
            this.author_id = author_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        if (author_name != null && !author_name.isEmpty())
            this.author_name = author_name;
    }

    public boolean getAuthor_is_organization() {
        return author_is_organization;
    }

    public void setAuthor_is_organization(boolean author_is_organization) {
        this.author_is_organization = author_is_organization;
    }

    public Boolean getIs_event() {
        return is_event;
    }

    public Boolean isMain_feed_visible() {
        return main_feed_visible;
    }

    public void setMain_feed_visible(Boolean main_feed_visible) {
        this.main_feed_visible = main_feed_visible;
    }

    public long getCreation_millis() {
        return creation_millis;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVisual() {
        return visual;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    public String getPinned_id() {
        return pinned_id;
    }

    public void setPinned_id(String pinned_id) {
        this.pinned_id = pinned_id;
    }

    public String getPinned_name() {
        return pinned_name;
    }

    public void setPinned_name(String pinned_name) {
        this.pinned_name = pinned_name;
    }

    public String getCreation_platform() {
        return creation_platform;
    }

    public void setCreation_platform(String creation_platform) {
        this.creation_platform = creation_platform;
    }

    public List<String> getViews_id(){
        if (views_id == null) return new ArrayList<>();
        else return new ArrayList<>(views_id);
    }

    public void addViewIdToList(String userId){
        if (views_id == null) views_id = new ArrayList<>();
        if (userId != null && !userId.isEmpty()) views_id.add(userId);
    }

    public void deleteViewIdFromList(String userId){
        views_id.remove(userId);
    }

/* Parcelable Override Methods
***************************************************************************************************/

    protected Post(Parcel in) {
        id = in.readString();
        uni_domain = in.readString();
        author_id = in.readString();
        author_name = in.readString();
        author_is_organization = in.readByte() != 0;
        is_event = in.readByte() != 0;
        main_feed_visible = in.readByte() != 0;
        creation_millis = in.readLong();
        creation_platform = in.readString();
        text = in.readString();
        visual = in.readString();
        pinned_id = in.readString();
        pinned_name = in.readString();
        views_id = in.createStringArrayList();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
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
        dest.writeByte((byte) (author_is_organization ? 1 : 0));
        dest.writeByte((byte) (is_event ? 1 : 0));
        dest.writeByte((byte) (main_feed_visible ? 1 : 0));
        dest.writeLong(creation_millis);
        dest.writeString(creation_platform);
        dest.writeString(text);
        dest.writeString(visual);
        dest.writeString(pinned_id);
        dest.writeString(pinned_name);
        dest.writeStringList(views_id);
    }
}
