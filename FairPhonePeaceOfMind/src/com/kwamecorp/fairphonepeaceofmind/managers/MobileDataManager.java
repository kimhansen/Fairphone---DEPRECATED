package com.kwamecorp.fairphonepeaceofmind.managers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MobileDataManager
{
    public static final String MOBILE_DATA_STATE_CHANGED = "MobileDataStateChanging";
    public static final int MOBILE_DATA_CHANGING = 1;
    public static final int MOBILE_DATA_ENABLED = 2;
    public static final int MOBILE_DATA_DISABLED = 3;

    private Context context;

    private int currentStatus = -1;

    // NETWORK MANAGERS
    private ConnectivityManager mobileDataManager;

    public MobileDataManager(Context context)
    {
        this.context = context;

        setManagers();
    }

    private void setManagers()
    {
        mobileDataManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    // GETTERS / SETTERS
    public boolean isMobileDataEnabled()
    {
        boolean mobileDataStatus = false;

        try
        {
            Class<?> cmClass = Class.forName(mobileDataManager.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            mobileDataStatus = (Boolean) method.invoke(mobileDataManager);

        } catch (Exception e)
        {
            Toast.makeText(context, "Error detecting Mobile Data Status", Toast.LENGTH_SHORT).show();
        }

        return mobileDataStatus;
    }

    public void setMobileDataNetworkEnabled(boolean status)
    {
        int bv = Build.VERSION.SDK_INT;

        currentStatus = MOBILE_DATA_CHANGING;

        try
        {
            if (bv == Build.VERSION_CODES.FROYO)
            {
                Method dataConnSwitchmethod;
                Class<?> telephonyManagerClass;
                Object ITelephonyStub;
                Class<?> ITelephonyClass;

                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
                Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
                ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());

                if (status)
                {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");
                }
                else
                {
                    dataConnSwitchmethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
                }
                dataConnSwitchmethod.setAccessible(true);
                dataConnSwitchmethod.invoke(ITelephonyStub);
            }
            else
            {
                final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final Class<?> conmanClass = Class.forName(conman.getClass().getName());
                final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
                iConnectivityManagerField.setAccessible(true);
                final Object iConnectivityManager = iConnectivityManagerField.get(conman);
                final Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
                final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                setMobileDataEnabledMethod.setAccessible(true);
                setMobileDataEnabledMethod.invoke(iConnectivityManager, status);
            }

        } catch (Exception e)
        {
            Toast.makeText(context, "error turning on/off data: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        if (isMobileDataEnabled())
        {
            currentStatus = MOBILE_DATA_ENABLED;
        }
        else
        {
            currentStatus = MOBILE_DATA_DISABLED;
        }
        sendBroadcast();
    }

    private void sendBroadcast()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.kwamecorp.fairphonepeaceofmind.MOBILE_DATA_STATE_CHANGED");
        broadcastIntent.putExtra(MOBILE_DATA_STATE_CHANGED, currentStatus);
        // broadcastIntent.addCategory("nl.vu.contextframework.CONTEXT");
        context.sendBroadcast(broadcastIntent);
    }

    public int getMobileDataState()
    {
        // TODO Auto-generated method stub
        return currentStatus;
    }

    public void destroy()
    {

    }
}
