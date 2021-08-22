package com.rozin.donateyourfood.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;

import androidx.core.app.NotificationCompat;

import com.rozin.donateyourfood.MainActivity;
import com.rozin.donateyourfood.R;
import com.rozin.donateyourfood.models.NotificationVO;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NotificationUtils {
    private static final int NOTIFICATION_ID = 200;
    public static final int ID_BIG_NOTIFICATION = 234;
    public static final int ID_SMALL_NOTIFICATION = 235;
    private static final String PUSH_NOTIFICATION = "pushNotification";
    private static final String CHANNEL_ID = "myChannel";
    private static final String CHANNEL_NAME = "myChannelName";
    private static final String URL = "url";
    private static final String ACTIVITY = "activity";
    Map<String, Class> activityMap = new HashMap<>();
    private Context mContext;

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
        //Populate activity map
        activityMap.put("MainActivity", MainActivity.class);
//        activityMap.put("SecondActivity", SecondActivity.class);
    }

    /**
     * Displays notification based on parameters
     *
     * @param notificationVO
     * @param resultIntent
     */
    public void displayNotification(NotificationVO notificationVO, Intent resultIntent) {
        {
            String message = notificationVO.getMessage();
            String title = notificationVO.getTitle();
            String iconUrl = notificationVO.getIconUrl();
            String action = notificationVO.getAction();
            String destination = notificationVO.getActionDestination();
            Bitmap iconBitMap = null;
            if (iconUrl != null) {
                iconBitMap = getBitmapFromURL(iconUrl);
            }
            final int icon = R.mipmap.ic_launcher;

            PendingIntent resultPendingIntent;

            if (URL.equals(action)) {
                Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(destination));

                resultPendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
            } else if (ACTIVITY.equals(action) && activityMap.containsKey(destination)) {
                resultIntent = new Intent(mContext, activityMap.get(destination));

                resultPendingIntent =
                        PendingIntent.getActivity(
                                mContext,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT
                        );
            } else {
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                resultPendingIntent =
                        PendingIntent.getActivity(
                                mContext,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT
                        );
            }


            final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    mContext, CHANNEL_ID);

            Notification notification;

            if (iconBitMap == null) {
                //When Inbox Style is applied, user can expand the notification
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                inboxStyle.addLine(message);
                notification = mBuilder
                        .setSmallIcon(icon)
                        .setTicker(title)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setContentIntent(resultPendingIntent)
                        .setStyle(inboxStyle)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                        .setContentText(message)
                        .setWhen(System.currentTimeMillis())
                        .setPriority(Notification.PRIORITY_MAX)
                        .build();

            } else {
                //If Bitmap is created from URL, show big icon
                NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
                bigPictureStyle.setBigContentTitle(title);
                bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
                bigPictureStyle.bigPicture(iconBitMap);
                notification = mBuilder
                        .setSmallIcon(icon)
                        .setTicker(title)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setContentIntent(resultPendingIntent)
                        .setStyle(bigPictureStyle)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                        .setContentText(message)
                        .setWhen(System.currentTimeMillis())
                        .setPriority(Notification.PRIORITY_MAX)
                        .build();
            }

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            //All notifications should go through NotificationChannel on Android 26 & above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

                channel.enableLights(true);
                channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);

            }
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    /**
     * Downloads push notification image before displaying it in
     * the notification tray
     *
     * @param strURL : URL of the notification Image
     * @return : BitMap representation of notification Image
     */
    private Bitmap getBitmapFromURL(String strURL) {
        try {
            java.net.URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Playing notification sound
     */
    public void playNotificationSound() {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/notification");
            Ringtone r = RingtoneManager.getRingtone(mContext, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    private void getImage(final RemoteMessage remoteMessage) {
//
//        Map<String, String> data = remoteMessage.getData();
////        Bitmap.Config.title = data.get("title");
////        Bitmap.Config.content = data.get("content");
////        Config.imageUrl = data.get("imageUrl");
////        Config.gameUrl = data.get("gameUrl");
//        //Create thread to fetch image from notification
//        if(remoteMessage.getData()!=null){
//
//            Handler uiHandler = new Handler(Looper.getMainLooper());
//            uiHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    // Get image from data Notification
//                    Picasso.get()
//                            .load(imageUrl)
//                            .into(target);
//                }
//            }) ;
//        }
//    }

    public void showBigNotification(String title, String message, String url, Intent intent) {
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        ID_BIG_NOTIFICATION,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(getBitmapFromURL(url));
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setStyle(bigPictureStyle)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_BIG_NOTIFICATION, notification);
    }

    //the method will show a small notification
    //parameters are title for message title, message for message text and an intent that will open
    //when you will tap on the notification
    public void showSmallNotification(String title, String message, Intent intent) {
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        mContext,
                        ID_SMALL_NOTIFICATION,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_SMALL_NOTIFICATION, notification);
    }



}