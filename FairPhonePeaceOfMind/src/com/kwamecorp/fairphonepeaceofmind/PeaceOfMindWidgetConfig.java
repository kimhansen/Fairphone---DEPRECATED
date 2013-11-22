package com.kwamecorp.fairphonepeaceofmind;

import java.util.ArrayList;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.kwamecorp.fairphonepeaceofmind.components.ListItem;
import com.kwamecorp.fairphonepeaceofmind.managers.PeaceOfMindListAdapter;

public class PeaceOfMindWidgetConfig extends Activity
{
    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    public static final String APPWIDGET_SETTINGS = "com.kwamecorp.fairphonepeaceofmind.WidgetSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.widget_config_layout);

        setupLayout();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            finish();
        }

        Log.d("KW", "config");
    }

    private void setupLayout()
    {
        final PeaceOfMindListAdapter listAdapter = new PeaceOfMindListAdapter(this, getListItems());
        ListView servicesList = (ListView) findViewById(R.id.servicesList);
        servicesList.setAdapter(listAdapter);
        servicesList.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View view, int itemIndex, long id)
            {
                ListItem item = (ListItem) listAdapter.getItem(itemIndex);
                item.toggleChecked();
            }
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                completedConfiguration();
            }
        });
    }

    private ArrayList<ListItem> getListItems()
    {
        ArrayList<ListItem> listItems = new ArrayList<ListItem>();
        listItems.add(new ListItem(getResources().getString(R.string.calls_sms), true));
        listItems.add(new ListItem(getResources().getString(R.string.internet_conn), true));
        listItems.add(new ListItem(getResources().getString(R.string.led_lights), true));
        listItems.add(new ListItem(getResources().getString(R.string.audio), true));

        return listItems;
    }

    private void completedConfiguration()
    {
        boolean[] widgetSettings = new boolean[4];
        widgetSettings[0] = true;
        widgetSettings[1] = false;
        widgetSettings[2] = true;
        widgetSettings[3] = true;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sp.edit();
        editor.putBoolean(APPWIDGET_SETTINGS + "hasWifi", true);
        editor.putBoolean(APPWIDGET_SETTINGS + "hasMobileData", false);
        editor.putBoolean(APPWIDGET_SETTINGS + "hasAudioMode", true);
        editor.putBoolean(APPWIDGET_SETTINGS + "hasLED", true);
        editor.commit();

        Intent result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        setResult(RESULT_OK, result);
        finish();
    }
}