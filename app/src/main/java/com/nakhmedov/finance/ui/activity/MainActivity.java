package com.nakhmedov.finance.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.nakhmedov.finance.BuildConfig;
import com.nakhmedov.finance.R;
import com.nakhmedov.finance.ui.adapter.MainMenuAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created with Android Studio
 * User: navruz
 * Date: 3/30/17
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates
 */

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getCanonicalName();

    @BindView(R.id.gridview) GridView gridView;
    @BindView(R.id.adView) AdView mAdView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.search_view) MaterialSearchView searchView;

    // Remote Config keys
    private static final String BANNER_ADS_KEY = "banner_ads_enabled";
    private static final String INSERTIAL_ADS_KEY = "insertial_ads_enabled";
    private static final String APP_LAST_VERSION_KEY = "app_last_version_code";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("9955D816375FF5AF7DDE1FAA0B2B0413")
                .build();
        mAdView.loadAd(adRequest);


        /*Firebase*/

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings mRemoteConfigSettings = new FirebaseRemoteConfigSettings
                .Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        mFirebaseRemoteConfig.setConfigSettings(mRemoteConfigSettings);
//        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        /*Fetch remote config*/

        fetchRemoteConfig();

        MainMenuAdapter menuAdapter = new MainMenuAdapter(MainActivity.this);
        gridView.setAdapter(menuAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Clicked position = " + position, Toast.LENGTH_SHORT).show();

                switch (position) {
                    case 0: {
                        Intent categoryIntent = new Intent(MainActivity.this, CategoryActivity.class);
                        categoryIntent.putExtra(CategoryActivity.EXTRA_VIEW_POSITION, CategoryActivity.CATEGORY_POSITION);
                        startActivity(categoryIntent);
                        break;
                    }
                    case 1: {
                        Intent quizIntent = new Intent(MainActivity.this, CategoryActivity.class);
                        quizIntent.putExtra(CategoryActivity.EXTRA_VIEW_POSITION, CategoryActivity.QUIZ_POSITION);
                        startActivity(quizIntent);
                        break;
                    }
                    case 2: {

                        break;
                    }
                    case 3: {

                        break;
                    }
                    case 4: {
                        Intent starredIntent = new Intent(MainActivity.this, CategoryActivity.class);
                        starredIntent.putExtra(CategoryActivity.EXTRA_VIEW_POSITION, CategoryActivity.STARRED_POSITION);
                        startActivity(starredIntent);
                        break;
                    }
                    case 5: {

                        break;
                    }
                }
            }
        });

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        searchView.setVoiceSearch(true); //or false


    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (mAdView != null) {
            mAdView.resume();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fetchRemoteConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that
        // each fetch goes to the server. This should not be used in release
        // builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings()
                .isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.w(TAG, "Success onComplete fetching config");
                            mFirebaseRemoteConfig.activateFetched();
                            applyConfigs();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error fetching config: " +
                                e.getMessage());
                        applyConfigs();
                    }
                });
    }

    private void applyConfigs() {
        Log.i(TAG, mFirebaseRemoteConfig.getBoolean(BANNER_ADS_KEY) +"");
        Log.i(TAG, mFirebaseRemoteConfig.getBoolean(INSERTIAL_ADS_KEY) + "");
        Log.i(TAG, mFirebaseRemoteConfig.getLong(APP_LAST_VERSION_KEY) +"");
    }
}
