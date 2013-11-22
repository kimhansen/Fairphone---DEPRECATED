package com.kwamecorp.fairphonepeaceofmind;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.kwamecorp.fairphonepeaceofmind.managers.MobileDataManager;

/**
 * TODO Add configuration settings to the widget through SharedPreferences
 * 
 * !!ATTENTION!! The refresh rate (ALARM_ACTION_RATE) is low for development
 * purposes. increase the time rate upon release!
 */

public class PeaceOfMindWidget extends AppWidgetProvider
{
    // private static final boolean ANIMATIONS_ON = false;

    public static final String ALARM_ACTION = "com.kwamecorp.fairphonepeaceofmind.ALARM_ACTION";
    public static final String APPWIDGET_UPDATE_OPTIONS = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";
    private static final long ALARM_ACTION_RATE = 60000;

    private static final String PACKAGE = "com.kwamecorp.fairphonepeaceofmind.";
    private static final String ACTION_WIDGET_CLICK = "ActionWidgetClick";

    private static boolean isPeaceOfMind = false;

    // private static PeaceOfMindWidgetServices services;

    @Override
    public void onEnabled(Context context)
    {
        Log.d("KW", "enabled");
        enableAlarm(context);
        setInitialTimmings(context);

        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (getWidgetChangingStatus(context))
        {
            return;
        }

        String action = intent.getAction();
        if (action.equals(ACTION_WIDGET_CLICK))
        {
            isPeaceOfMind = !isPeaceOfMind;

            updateWidgetLayout(context);

            if (isPeaceOfMind)
            {
                setWidgetServicesPreviousModes(context);
            }

            updateWidgetServices(context);
        }
        else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
        {
            if (isPeaceOfMind && getPhoneNetworkStatus(context))
            {
                isPeaceOfMind = false;
                updateWidgetLayout(context);
                Toast.makeText(context, "Leaving Peace Of Mind mode...", Toast.LENGTH_SHORT).show();
            }
        }
        else if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION))
        {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (isPeaceOfMind && audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT)
            {
                isPeaceOfMind = false;
                updateWidgetLayout(context);
                Toast.makeText(context, "Leaving Peace Of Mind mode...", Toast.LENGTH_SHORT).show();
            }
        }
        else if (action.equals(ALARM_ACTION))
        {
            updateTimmings(context);
        }
        else if (action.equals(APPWIDGET_UPDATE_OPTIONS))
        {

            updateTimmings(context);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(new ComponentName(context, PeaceOfMindWidget.class)));
        }
        else
        {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(new ComponentName(context, PeaceOfMindWidget.class)));
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        Log.d("KW", "onUpdate");
        for (int i = 0; i < appWidgetIds.length; i++)
        {
            int appWidgetId = appWidgetIds[i];

            Intent active = new Intent(context, PeaceOfMindWidget.class);
            active.setAction(ACTION_WIDGET_CLICK);
            PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.toggleButton, actionPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onDisabled(Context context)
    {
        Log.d("KW", "disabled");
        disableAlarm(context);
        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * Updates widgets services (WIFI, MobileNetwork, audio)
     */
    private void updateWidgetServices(Context context)
    {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        MobileDataManager mobileDataManager = new MobileDataManager(context);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (isPeaceOfMind)
        {
            wifiManager.setWifiEnabled(false);
            mobileDataManager.setMobileDataNetworkEnabled(false);
            audioManager.setRingerMode(0);

            Notification notification = new Notification();
            notification.ledARGB = 0xff000000;
            notification.flags = Notification.FLAG_SHOW_LIGHTS;
            notificationManager.notify(0, notification);
        }
        else
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            wifiManager.setWifiEnabled(sp.getBoolean(PACKAGE + "wasWifi", false));
            mobileDataManager.setMobileDataNetworkEnabled(sp.getBoolean(PACKAGE + "wasMobileData", false));
            audioManager.setRingerMode(sp.getInt(PACKAGE + "wasAudioMode", -1));
            notificationManager.cancel(0);
        }

    }

    /**
     * sets widgets services (WIFI, MobileNetwork, audio) to retrieve when Piece
     * Of is off.
     */
    private void setWidgetServicesPreviousModes(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        MobileDataManager mobileDataManager = new MobileDataManager(context);
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putBoolean(PACKAGE + "wasWifi", wifiManager.isWifiEnabled());
        editor.putBoolean(PACKAGE + "wasMobileData", mobileDataManager.isMobileDataEnabled());
        editor.putInt(PACKAGE + "wasAudioMode", audioManager.getRingerMode());
        editor.commit();
    }

    private void updateWidgetLayout(Context context)
    {
        int toggleButtonImageID;
        int activeIndicatorImageID;
        int inactiveIndicatorImageID;

        int activeLabelTextColor;
        int inactiveLabelTextColor;

        if (isPeaceOfMind)
        {
            toggleButtonImageID = R.drawable.peace_of_mind_btn_on;
            activeIndicatorImageID = R.drawable.active_inactive_background;
            inactiveIndicatorImageID = R.drawable.inactive_active_background;
            activeLabelTextColor = context.getResources().getColor(R.color.white);
            inactiveLabelTextColor = context.getResources().getColor(R.color.lightgreen);
        }
        else
        {
            toggleButtonImageID = R.drawable.peace_of_mind_btn_off;
            activeIndicatorImageID = R.drawable.active_active_background;
            inactiveIndicatorImageID = R.drawable.inactive_inactive_background;
            activeLabelTextColor = context.getResources().getColor(R.color.lightgreen);
            inactiveLabelTextColor = context.getResources().getColor(R.color.white);
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, PeaceOfMindWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int i = 0; i < appWidgetIds.length; i++)
        {
            views.setTextColor(R.id.activeIndicatorLabel, activeLabelTextColor);
            views.setTextColor(R.id.inactiveIndicatorLabel, inactiveLabelTextColor);

            views.setImageViewResource(R.id.toggleButton, toggleButtonImageID);

            views.setImageViewResource(R.id.activeIndicator, activeIndicatorImageID);
            views.setImageViewResource(R.id.inactiveIndicator, inactiveIndicatorImageID);

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
    }

    private boolean getWidgetChangingStatus(Context context)
    {
        boolean changing = false;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        int state = wifiManager.getWifiState();
        if (state == WifiManager.WIFI_STATE_DISABLING || state == WifiManager.WIFI_STATE_ENABLING)
        {
            changing = true;

            Toast.makeText(context, "WIFI is changing", Toast.LENGTH_SHORT).show();
        }

        return changing;
    }

    private boolean getPhoneNetworkStatus(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void enableAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(PeaceOfMindWidget.ALARM_ACTION), 0);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, PeaceOfMindWidget.ALARM_ACTION_RATE, PeaceOfMindWidget.ALARM_ACTION_RATE, pi);
    }

    private void disableAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(PeaceOfMindWidget.ALARM_ACTION), 0);
        alarmManager.cancel(pi);
    }

    private void updateTimmings(Context context)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, PeaceOfMindWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        float peaceOfMindTime = sp.getFloat(PACKAGE + "peaceOfMindTime", 0);
        float workingTime = sp.getFloat(PACKAGE + "workingTime", 0);
        float peaceOfMindPercent = sp.getFloat("peaceOfTimePercent", 0);
        float workingPercent = sp.getFloat("workingPercent", 0);
        if (isPeaceOfMind)
        {
            peaceOfMindTime++;

        }
        else
        {
            workingTime++;
        }
        float totalTime = peaceOfMindTime + workingTime;
        if (totalTime == 0)
        {
            totalTime = 1;
        }

        if (peaceOfMindTime > 0)
        {
            peaceOfMindPercent = (peaceOfMindTime / totalTime) * 100f;
        }
        if (workingTime > 0)
        {
            workingPercent = (workingTime / totalTime) * 100f;
        }

        for (int i = 0; i < appWidgetIds.length; i++)
        {
            views.setTextViewText(R.id.inactiveIndicatorLabel, "" + (int) Math.floor(peaceOfMindPercent) + "%");
            views.setTextViewText(R.id.activeIndicatorLabel, "" + (int) Math.ceil(workingPercent) + "%");

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }

        Editor editor = sp.edit();
        editor.putFloat(PACKAGE + "peaceOfMindTime", peaceOfMindTime);
        editor.putFloat(PACKAGE + "workingTime", workingTime);
        editor.putFloat(PACKAGE + "peaceOfTimePercent", peaceOfMindPercent);
        editor.putFloat(PACKAGE + "workingPercent", workingPercent);
        editor.commit();
    }

    private void setInitialTimmings(Context context)
    {
        Calendar cal = Calendar.getInstance();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putInt(PACKAGE + "initialMinutes", cal.get(Calendar.MINUTE));
        editor.putFloat(PACKAGE + "peaceOfMindTime", 0);
        editor.putFloat(PACKAGE + "workingTime", 0);
        editor.commit();
    }
}
