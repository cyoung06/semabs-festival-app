package kr.semicolon.bsfestival;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DeviceOwnerReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        showToast(context, "[Device Admin enabled]");
        becomeHomeActivity(context);
    }

    static void becomeHomeActivity(Context c) {
        ComponentName deviceAdmin = new ComponentName(c, DeviceOwnerReceiver.class);
        DevicePolicyManager dpm = (DevicePolicyManager) c.getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (!dpm.isAdminActive(deviceAdmin)) {
            showToast(c, "This app is not a device admin!");
            return;
        }
        if (!dpm.isDeviceOwnerApp(c.getPackageName())) {
            showToast(c, "This app is not the device owner!");
            return;
        }
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName activity = new ComponentName(c, MainActivity.class);
        dpm.addPersistentPreferredActivity(deviceAdmin, intentFilter, activity);
        showToast(c, "Home activity: " + getHomeActivity(c));
    }

    static String getHomeActivity(Context c) {
        PackageManager pm = c.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ComponentName cn = intent.resolveActivity(pm);
        if (cn != null)
            return cn.flattenToShortString();
        else
            return "none";
    }


    static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceOwnerReceiver.class);
    }

    @Override
    public void onProfileProvisioningComplete(@NonNull Context context, @NonNull Intent intent) {
        DevicePolicyManager manager =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = getComponentName(context);
        manager.setProfileName(componentName, context.getString(R.string.profile_name));
        // Open the main screen

        PersistableBundle bundle = intent.getParcelableExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
        String url = bundle.getString("stuff.url");
        Log.d("DeviceOwnerReceiver", url);
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("url", url).apply();


//        final WifiManager wifiManager =
//                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

//        final String SSID = bundle.getString("stuff.ssid");
//        final String PWD = bundle.getString("stuff.pwd");
//        final WifiConfiguration configuration = new WifiConfiguration();
//        configuration.SSID = "\""+SSID+"\"";
//        configuration.preSharedKey = "\""+PWD+"\"";
//        configuration.priority = 999;
//        configuration.
//        int netid = wifiManager.addNetwork(configuration);
//        wifiManager.enableNetwork(netid, true);


        Intent launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Stop cheating";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "[Device Admin disabled]");
    }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent,
                                       String pkg) {
        showToast(context, "[Kiosk Mode enabled]");
    }

    @Override
    public void onLockTaskModeExiting(Context context, Intent intent) {
        showToast(context, "[Kiosk Mode disabled]");
    }
}
