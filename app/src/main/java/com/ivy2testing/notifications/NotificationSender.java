package com.ivy2testing.notifications;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationSender {

    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private static final String TAG = "NotificationSenderTag";
    private String targetToken = "";
    private Map<String, Object> notificationParams = new HashMap<>();
    private String title  = "";
    private String body = "";
    private String conversation_id = "";

    public NotificationSender(String token, String title, String body, String convId){ //constructor
        prepNotification(token, title, body, convId); //which also preps default notification
    }

    private void prepNotification(String token, String title, String body, String convId){ //if we wanna change the notification later we can
        this.targetToken = token;
        this.title = title;
        this.body = body;
        this.conversation_id = convId;
    }

    public void sendNotification(Context context){ //send the HTTP request

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        JSONObject notificationData = new JSONObject();
        try {
            notificationBody.put("title", title); //actual notification info to display
            notificationBody.put("body", body);

            notificationData.put("user", "test_id");
            notificationData.put("conversationID", conversation_id);

            notification.put("to", targetToken); //can't have spacing and must follow: /sdfsd/sdfs
            notification.put("notification", notificationBody);
            notification.put("data", notificationData);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage() );
        }


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST , FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: " + error.toString());
                        error.printStackTrace();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=AAAAnkRkLE8:APA91bHIsRG-JlN8SzFMgjhNFDYgfLMFz3EIl6FF3Urg-LmuE9iGcPPbaiQOQVvVpfBjL8aG27VDObBXBakaZP3j-vsRd1EhESey2e21FJt5N_Eb84pVo2x8MEvMc4mEto9gfL2BtUNT"); //Server Key
                params.put("Content-Type", "application/json"); //Content Type
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonObjectRequest);
    }


    public String getTargetToken() {
        return targetToken;
    }

    public void setTargetToken(String targetToken) {
        this.targetToken = targetToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
