package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.talentica.wifiindoorpositioning.wifiindoorpositioning.R;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.adapter.NearbyReadingsAdapter;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.core.Algorithms;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.core.WifiService;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.LocDistance;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.LocationWithNearbyPlaces;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.model.WifiData;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.utils.AppContants;
import com.talentica.wifiindoorpositioning.wifiindoorpositioning.utils.Utils;

import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by suyashg on 10/09/17.
 */

public class LocateMeActivity extends AppCompatActivity {
    //지도,시간표
    private ImageView map;
    private Button mapButton;
    private String LocationMapValue;

    private ImageView tt;
    private Button ttButton;


    private WifiData mWifiData;
    private Algorithms algorithms = new Algorithms();
    private String projectId, defaultAlgo;
    private IndoorProject project;
    private MainActivityReceiver mReceiver = new MainActivityReceiver();
    private Intent wifiServiceIntent;
    private TextView tvLocation, tvNearestLocation, tvDistance;
    private RecyclerView rvPoints;
    private LinearLayoutManager layoutManager;
    private NearbyReadingsAdapter readingsAdapter = new NearbyReadingsAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mWifiData = null;

        // set receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(AppContants.INTENT_FILTER));

        // launch WiFi service
        wifiServiceIntent = new Intent(this, WifiService.class);
        startService(wifiServiceIntent);

        // recover retained object
        mWifiData = (WifiData) getLastNonConfigurationInstance();

        // set layout
        setContentView(R.layout.activity_locate_me);
        initUI();

        defaultAlgo = Utils.getDefaultAlgo(this);
        projectId = getIntent().getStringExtra("projectId");
        if (projectId == null) {
            Toast.makeText(getApplicationContext(), "Project Not Found", Toast.LENGTH_LONG).show();
            this.finish();
        }
        Realm realm = Realm.getDefaultInstance();
        project = realm.where(IndoorProject.class).equalTo("id", projectId).findFirst();
        Log.v("LocateMeActivity", "onCreate");

        //맵, 시간표
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibleMap(LocationMapValue);
            }
        });

        ttButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibleTimeTable(LocationMapValue);
            }
        });

    }

    private void initUI() {
        layoutManager = new LinearLayoutManager(this);
        tvLocation = findViewById(R.id.tv_location);
        tvNearestLocation = findViewById(R.id.tv_nearest_location);
        tvDistance = findViewById(R.id.tv_distance_origin);
        rvPoints = findViewById(R.id.rv_nearby_points);
        rvPoints.setLayoutManager(layoutManager);
        rvPoints.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvPoints.setAdapter(readingsAdapter);

        map = findViewById(R.id.map);
        mapButton = findViewById(R.id.MapButton);
        ttButton = findViewById(R.id.TimeTable);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mWifiData;
    }

    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LocateMeActivity", "MainActivityReceiver");
            mWifiData = (WifiData) intent.getParcelableExtra(AppContants.WIFI_DATA);

            if (mWifiData != null) {
                LocationWithNearbyPlaces loc = Algorithms.processingAlgorithms(mWifiData.getNetworks(), project, Integer.parseInt(defaultAlgo));
                Log.v("LocateMeActivity", "loc:" + loc);
                if (loc == null) {
                    tvLocation.setText("Location: NA\nNote:Please switch on your wifi and location services with permission provided to App");
                } else {
                    //일단 여기 located me  밑에 사진 넣고 if 문으로 theNearestPoint.getName()=ㅁㄴㅇㄹ
                    String locationValue = Utils.reduceDecimalPlaces(loc.getLocation());
                    tvLocation.setText("Location: " + locationValue);
                    String theDistancefromOrigin = Utils.getTheDistancefromOrigin(loc.getLocation());
                    tvDistance.setText("The distance from stage area is: " + theDistancefromOrigin + "m");
                    LocDistance theNearestPoint = Utils.getTheNearestPoint(loc);
                    if (theNearestPoint != null) {
                        tvNearestLocation.setText("You are near to: " + theNearestPoint.getName());
                        LocationMapValue = theNearestPoint.getName();
                    }
                    readingsAdapter.setReadings(loc.getPlaces());
                    readingsAdapter.notifyDataSetChanged();
                    
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopService(wifiServiceIntent);
    }

    private void visibleMap(String locationValue){
        StringBuilder fileName = new StringBuilder("map_");
        fileName.append( locationValue );
        int resID = getResources().getIdentifier(fileName.toString(), "drawable", getPackageName());
        try {
            map.setImageResource(resID);
        }catch (Exception e){
            Toast.makeText(this, "데이터가 없음.", Toast.LENGTH_SHORT).show();
            map.setImageResource(R.drawable.ic_launcher_background);
        }
        map.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }
    private void visibleTimeTable(String locationValue){
        StringBuilder fileName = new StringBuilder("tt_");
        fileName.append( locationValue );
        int resID = getResources().getIdentifier(fileName.toString(), "drawable", getPackageName());
        try {
            map.setImageResource(resID);
        }catch (Exception e){
            Toast.makeText(this, "데이터가 없음.", Toast.LENGTH_SHORT).show();
            map.setImageResource(R.drawable.ic_launcher_background);
        }
        map.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }
}
