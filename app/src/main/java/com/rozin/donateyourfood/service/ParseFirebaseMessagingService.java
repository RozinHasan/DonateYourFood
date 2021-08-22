package com.rozin.donateyourfood.service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.PLog;
import com.parse.PushRouter;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "ParseFirebaseMessagingService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        ParseFCM.register(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        PLog.d(TAG, "onMessageReceived");

        String pushId = remoteMessage.getData().get("push_id");
        String timestamp = remoteMessage.getData().get("time");
        String dataString = remoteMessage.getData().get("data");
        String channel = remoteMessage.getData().get("channel");

        JSONObject data = null;
        if (dataString != null) {
            try {
                data = new JSONObject(dataString);
            } catch (JSONException e) {
                PLog.e(TAG, "Ignoring push because of JSON exception while processing: " + dataString, e);
                return;
            }
        }

        PushRouter.getInstance().handlePush(pushId, timestamp, channel, data);
    }
}