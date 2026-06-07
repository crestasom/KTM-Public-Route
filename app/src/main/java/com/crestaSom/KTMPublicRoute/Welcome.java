package com.crestaSom.KTMPublicRoute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crestaSom.KTMPublicRoute.data.JSONParser;
import com.crestaSom.database.Database;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.appbar.MaterialToolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crestaSom.KTMPublicRoute.util.Labels;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.crestaSom.viewPageAdapter.ViewPagerAdapter;

public class Welcome extends AppCompatActivity implements OnClickListener {

    MaterialToolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter mViewPagerAdapter;

    JSONParser jsonParser = new JSONParser();
    private static String urlCheck = "http://shresthasom.com.np/collegeProjectDatabaseNew/admin.php?url=version/checkNew";
    private static String url = "http://shresthasom.com.np/collegeProjectDatabaseNew/admin.php?url=route/";
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String KEY = "flag";
    public static final String DB_KEY = "dbFlag";
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    int startFlag;
    SharedPreferences prefs;
    private ProgressDialog pDialog;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private void initLayout() {
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.iconktmlogo);
        getSupportActionBar().setTitle(" KTM Public Route");
        invalidateOptionsMenu();
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragments(new SearchRouteFragment(), " Search");
        mViewPagerAdapter.addFragments(new ViewRouteFragment(), " View");
        viewPager.setAdapter(mViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        initLayout();
        sharedPref = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        startFlag = sharedPref.getInt(KEY, -1);
        if (startFlag == -1) {
            copyMap();
            editor = sharedPref.edit();
            editor.putInt(KEY, 1);
            editor.putInt(DB_KEY, 2);
            editor.apply();
            startActivityForResult(new Intent(this, DisclaimerActivity.class), 100);
        } else if (startFlag == 1) {
            startActivity(new Intent(this, LanguageSelection.class));
        }
        if (isNetworkAvailable()) {
            checkNewRecord();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        startFlag = sharedPref.getInt(KEY, -1);
        if (startFlag == 1) {
            startActivity(new Intent(this, LanguageSelection.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auto_complete, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean animOn = prefs.getBoolean("animEnabled", true);
        boolean fareOn = prefs.getBoolean("showFare", false);
        menu.findItem(R.id.menu_toggle_animation).setTitle(Labels.menuAnimation(this, animOn));
        menu.findItem(R.id.menu_toggle_fare).setTitle(Labels.menuFare(this, fareOn));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_guide) {
            startActivity(new Intent(getApplicationContext(), HelpActivity.class));
        } else if (id == R.id.menu_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        } else if (id == R.id.menu_disclaimer) {
            startActivity(new Intent(getApplicationContext(), DisclaimerActivity.class));
        } else if (id == R.id.menu_feedback) {
            startActivity(new Intent(getApplicationContext(), FeedBackActivity.class));
        } else if (id == R.id.menu_toggle_animation) {
            boolean current = prefs.getBoolean("animEnabled", true);
            prefs.edit().putBoolean("animEnabled", !current).apply();
            item.setTitle(Labels.menuAnimation(this, !current));
        } else if (id == R.id.menu_toggle_fare) {
            boolean current = prefs.getBoolean("showFare", false);
            prefs.edit().putBoolean("showFare", !current).apply();
            item.setTitle(Labels.menuFare(this, !current));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    static public boolean isURLReachable(Context context) {
        try {
            URL u = new URL(urlCheck);
            HttpURLConnection urlc = (HttpURLConnection) u.openConnection();
            urlc.setConnectTimeout(10_000);
            urlc.connect();
            return urlc.getResponseCode() == 200;
        } catch (MalformedURLException e1) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private void checkNewRecord() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
        executor.execute(() -> {
            String result = "0";
            if (isURLReachable(getApplicationContext())) {
                try {
                    JSONObject json = jsonParser.makeHttpRequest(urlCheck, "POST");
                    int serverVer = json.getInt("dbVersion");
                    int clientVer = sharedPref.getInt(DB_KEY, -1);
                    result = clientVer < serverVer ? "1" : "0";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            final String res = result;
            mainHandler.post(() -> {
                if ("1".equals(res)) {
                    builder.setTitle(getString(R.string.update_available_title));
                    builder.setMessage(getString(R.string.update_available_msg));
                    builder.setNegativeButton(getString(R.string.btn_update), (d, w) -> {
                        if (isNetworkAvailable()) getNewRecord();
                        else Toast.makeText(getApplicationContext(), getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    });
                    builder.setPositiveButton(getString(R.string.btn_cancel), (d, w) -> {});
                    builder.create().show();
                }
            });
        });
    }

    private void getNewRecord() {
        pDialog = new ProgressDialog(Welcome.this);
        pDialog.setMessage(getString(R.string.getting_new_records));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        executor.execute(() -> {
            String result = null;
            Database db = new Database(getApplicationContext());
            if (isURLReachable(getApplicationContext())) {
                try {
                    JSONObject json = jsonParser.makeHttpRequest(urlCheck, "POST");
                    int serverVer = json.getInt("dbVersion");
                    int clientVer = sharedPref.getInt(DB_KEY, -1);
                    if (clientVer < serverVer) {
                        json = jsonParser.makeHttpRequest(url + "findNewRecords", "GET");
                        Log.d("json data", json.toString());
                        JSONArray edgeNew = json.getJSONArray("Edge");
                        JSONArray fareNew = json.getJSONArray("Fare");
                        json = jsonParser.makeHttpRequest(url + "findNewRecords1", "GET");
                        JSONArray vertexNew = json.getJSONArray("Vertex");
                        JSONArray routeNew = json.getJSONArray("Route");
                        String message = json.getString("message");
                        db.addNewRecords(vertexNew, edgeNew, routeNew, fareNew);
                        editor = sharedPref.edit();
                        editor.putInt(DB_KEY, serverVer);
                        editor.apply();
                        result = message;
                    } else {
                        result = "0";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                result = "-1";
            }
            final String res = result;
            mainHandler.post(() -> {
                pDialog.dismiss();
                if ("0".equals(res)) {
                    Toast.makeText(Welcome.this, getString(R.string.no_new_records), Toast.LENGTH_LONG).show();
                } else if ("-1".equals(res)) {
                    Toast.makeText(Welcome.this, getString(R.string.server_not_available), Toast.LENGTH_LONG).show();
                } else if (res != null) {
                    Toast.makeText(Welcome.this, res + getString(R.string.restarting_app), Toast.LENGTH_LONG).show();
                }
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            });
        });
    }

    private void copyMap() {
        pDialog = new ProgressDialog(Welcome.this);
        pDialog.setMessage(getString(R.string.initializing_map));
        pDialog.setCancelable(false);
        pDialog.setIndeterminate(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.show();

        executor.execute(() -> {
            try {
                InputStream in = getAssets().open("tiles.zip");
                // Use app-specific external dir (no permission needed on API 29+)
                File dir = new File(getExternalFilesDir(null), "osmdroid");
                if (!dir.exists()) dir.mkdirs();
                OutputStream out = new FileOutputStream(new File(dir, "tiles.zip"));
                byte[] buffer = new byte[1024];
                int read, i = 0, count = 12319;
                while ((read = in.read(buffer)) != -1) {
                    final int progress = (100 * i++) / count;
                    mainHandler.post(() -> pDialog.setProgress(progress));
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                Log.e("CopyMap", "Failed to copy asset: " + e.getMessage());
            }
            mainHandler.post(() -> pDialog.dismiss());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
