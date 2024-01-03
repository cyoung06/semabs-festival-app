package kr.semicolon.bsfestival;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActionBar;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DevicePolicyManager manager =
                    (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);


        ComponentName deviceAdmin = new ComponentName(this, DeviceOwnerReceiver.class);

        if (manager.isDeviceOwnerApp(getPackageName())) {
            manager.setLockTaskPackages(deviceAdmin,
                    new String[] { getPackageName() });
        } else {
            showToast("This app is not the device owner!");
        }

        if (manager.isLockTaskPermitted(this.getPackageName())) {
            startLockTask();
        } else {
            showToast("Kiosk Mode not permitted");
        }
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (sharedPreferences.getString("url", null) == null) {
            setContentView(R.layout.activity_main2);

            findViewById(R.id.button).setOnClickListener((e) -> {
                sharedPreferences.edit().putString("url", "http://172.16.0.3/vote/"+((EditText)findViewById(R.id.name)).getText().toString()).apply();
                runKiosk();
            });
        } else {
            runKiosk();
        }

//        if (manager.isDeviceOwnerApp(getApplicationContext().getPackageName())) {
//            // This app is set up as the device owner. Show the main features.
//            Log.d(TAG, "The app is the device owner.");


//        } else {
//            // This app is not set up as the device owner. Show instructions.
//            Log.d(TAG, "The app is not the device owner.");
//            showFragment(InstructionFragment.newInstance());
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);


        if (sharedPreferences.getString("url", null) == null) {
            setContentView(R.layout.activity_main2);

            findViewById(R.id.button).setOnClickListener((e) -> {
                sharedPreferences.edit().putString("url", "http://172.16.0.3/vote/" + ((EditText) findViewById(R.id.name)).getText().toString()).apply();
                runKiosk();
            });
        } else {
            runKiosk();
        }

    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }
    private void runKiosk() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Hide the status bar.
        int uiOptions =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;

        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();


        WebView.setWebContentsDebuggingEnabled(false);
        WebView myWebView = findViewById(R.id.webview);
        myWebView.getSettings().setAppCacheEnabled(false);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        myWebView.getSettings().setBlockNetworkLoads (false);
        myWebView.getSettings().setJavaScriptEnabled(true);

        myWebView.loadUrl(sharedPreferences.getString("url", "https://google.com/"));

        SwipeRefreshLayout swipe = findViewById(R.id.swiperefresh);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myWebView.reload();
                swipe.setRefreshing(false); // Call this at the end of loading
            }
        });
    }

    @Override
    public void onBackPressed() {}

}
