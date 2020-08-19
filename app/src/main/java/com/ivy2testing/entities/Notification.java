package com.ivy2testing.entities;

public class Notification {

    String id;
    Integer type;
    String visual;
    Long timestamp;
    String notification_receiver_id;
    String notification_sender_name;
    String notification_origin_name;
    String notification_origin_id;


    public Notification(String id, Integer type, String visual, Long time, String notification_receiver_id, String notification_sender_name, String notification_origin_name, String notification_origin_id){
        this.id = id;
        this.type = type;
        this.visual = visual;
        this.timestamp = time;
        this.notification_receiver_id = notification_receiver_id;
        this.notification_sender_name = notification_sender_name;
        this.notification_origin_name = notification_origin_name;
        this.notification_origin_id = notification_origin_id;
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

    public String getNotification_receiver_id() {
        return notification_receiver_id;
    }

    public void setNotification_receiver_id(String notification_receiver_id) {
        this.notification_receiver_id = notification_receiver_id;
    }

    public String getNotification_sender_name() {
        return notification_sender_name;
    }

    public void setNotification_sender_name(String notification_sender_name) {
        this.notification_sender_name = notification_sender_name;
    }

    public String getNotification_origin_name() {
        return notification_origin_name;
    }

    public void setNotification_origin_name(String notification_receiver_name) {
        this.notification_origin_name = notification_receiver_name;
    }

    public String getNotification_origin_id() {
        return notification_origin_id;
    }

    public void setNotification_origin_id(String notification_origin_id) {
        this.notification_origin_id = notification_origin_id;
    }
}
