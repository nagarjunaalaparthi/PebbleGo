package com.pebblego;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.pebblego.sleep.SleepDataFragment;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private String[] pageTitles = new String[]{"Steps", "Sleep", "Heart Rate"};
    private UUID appUuid;
    private PebbleKit.PebbleDataReceiver pebbleDataReceiver;
    private StepCountReciever stepsReciever;
    private StepDataFragment stepsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = new Intent(MainActivity.this, DataSynchService.class);
        startService(intent);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        appUuid = UUID.fromString("30a113fd-7937-4ca3-8b18-dce66da2979f");
        registerReciever();
//        timer.start();
        Log.i("isDataLogEnabled", "" + PebbleKit.isDataLoggingSupported(MainActivity.this));
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivity(new Intent(MainActivity.this, SettingsAtivity.class));
            }
        });
    }

    private void registerReciever() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.pebblego.steps");
        stepsReciever = new StepCountReciever();
        registerReceiver(stepsReciever, filter);
    }

//    CountDownTimer timer = new CountDownTimer(60*1000,1000) {
//        @Override
//        public void onTick(long l) {
//            PebbleDictionary dic = new PebbleDictionary();
//            dic.addString(0, "values");
//            PebbleKit.sendDataToPebble(MainActivity.this, appUuid, dic);
//        }
//
//        @Override
//        public void onFinish() {
//
//        }
//    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stepsReciever != null) {
            unregisterReceiver(stepsReciever);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pebbleDataReceiver != null) {
            try {
                unregisterReceiver(pebbleDataReceiver);
                pebbleDataReceiver = null;
            } catch (Exception e) {
                Log.e("device", "Error unregistering receiver: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isConnected = PebbleKit.isWatchConnected(this);
//        Toast.makeText(this, "Pebble " + (isConnected ? "is" : "is not") + " connected!", Toast.LENGTH_LONG).show();
//        if(pebbleDataReceiver == null){
//            pebbleDataReceiver = new PebbleKit.PebbleDataReceiver(appUuid) {
//                @Override
//                public void receiveData(Context context, int id, PebbleDictionary data) {
//                    // Message received, over!
//                    PebbleKit.sendAckToPebble(context, id);
//                    Toast.makeText(MainActivity.this,"data "+data.toJsonString(),Toast.LENGTH_LONG).show();
//                    Log.i("datais", "int: " + id + " dic : " + data.toString());
//                    try {
//                        JSONArray array = new JSONArray(data.toJsonString());
//                        for(int i=0;i<array.length();i++){
//                            JSONObject object = array.getJSONObject(0);
//                            if(object.has("key")){
//                                if(object.optInt("key") == 1){
//                                    Toast.makeText(MainActivity.this,object.getInt("value")+" steps",Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
////            PebbleKit.sendAckToPebble(context, transactionId);
//                }
//
//
//            };
//        }
//        PebbleKit.registerReceivedDataHandler(MainActivity.this, pebbleDataReceiver);
    }


    class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    stepsFragment = new StepDataFragment();
                    return stepsFragment;
                case 1:
                case 2:
                    return new SleepDataFragment();
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public class StepCountReciever extends BroadcastReceiver {

        public StepCountReciever() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.hasExtra("value")) {
                    if(stepsFragment!=null){
                        stepsFragment.refreshData();
                    }
                }
            }
        }
    }
}
