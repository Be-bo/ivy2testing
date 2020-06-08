package com.ivy2testing;

public class Post {
    // this class is just getters and setters...

    // shall these vars be public? or private...

    // first half pre initialized
    public String id;
    public String uni_domain;
    public String author_id;
    public String author_name;

    public Boolean is_event;

    // second half finalized through createpost
    public Boolean main_feed_visible;

    public Long creation_millis;

    public String text;
    public String visual;
    public String pinned_id;
    public String views_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUni_domain() {
        return uni_domain;
    }

    public void setUni_domain(String uni_domain) {
        this.uni_domain = uni_domain;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public Boolean getIs_event() {
        return is_event;
    }

    public void setIs_event(Boolean is_event) {
        this.is_event = is_event;
    }

    public Boolean getMain_feed_visible() {
        return main_feed_visible;
    }

    public void setMain_feed_visible(Boolean main_feed_visible) {
        this.main_feed_visible = main_feed_visible;
    }

    public Long getCreation_millis() {
        return creation_millis;
    }

    public void setCreation_millis(Long creation_millis) {
        this.creation_millis = creation_millis;
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

    public String getViews_id() {
        return views_id;
    }

    public void setViews_id(String views_id) {
        this.views_id = views_id;
    }


}
