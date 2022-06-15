package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
    //기압계
    float Press=0;
    private SensorManager mSM;
    private Sensor myPress;
    double F4=999.53, F2=1000.65, F5=999.05;


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
        //기압계
        mSM=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        myPress = mSM.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSM.registerListener(mySensorListener, myPress, SensorManager.SENSOR_DELAY_NORMAL);
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
//기압계
    @Override
    protected void onStart() {
        super.onStart();
        mSM.registerListener(mySensorListener, myPress, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSM.unregisterListener(mySensorListener);
    }

    public SensorEventListener mySensorListener = new SensorEventListener(){
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType()==Sensor.TYPE_PRESSURE){
                Press=sensorEvent.values[0];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


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
                        int F=F_calulation(Press);
                        tvNearestLocation.setText("You are near to: " + theNearestPoint.getName()+"\n현재 층수: "+F+"\n기압: "+Press);
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

    private int F_calulation(float Cpress){
        int F;

        double f2=F2-Cpress;
        double f4=F4-Cpress;
        double f5=F5-Cpress;
        if(f2<0)
            f2=f2*(-1);
        if(f4<0)
            f4=f4*(-1);
        if(f5<0)
            f5=f5*(-1);
        F=2;
        if(f4<f2&&f4<f5) {
            F = 4;
        }
        if(f5<f2&&f5<f4) {
            F = 5;
        }

        return F;
    }
}

