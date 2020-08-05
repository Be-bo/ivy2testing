package com.ivy2testing.entities;

public class Notification {

    String id;
    Integer type;
    String visual;
    Long timestamp;
    String target_id;
    String target_name;
    String author_name;


    public Notification(String id, Integer type, String visual, Long time, String targetId, String author_name, String targetName){
        this.id = id;
        this.type = type;
        this.visual = visual;
        this.timestamp = time;
        this.target_id = targetId;
        this.author_name = author_name;
        this.target_name = targetName;
    }
    public Notification(){

    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getVisual() {
        return visual;
    }

    public void setVisual(String visual) {
        this.visual = visual;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTarget_id() {
        return target_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public String getTarget_name() {
        return target_name;
    }

    public void setTarget_name(String target_name) {
        this.target_name = target_name;
    }
}
