package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import static com.talentica.wifiindoorpositioning.wifiindoorpositioning.R.layout.activity_select;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.talentica.wifiindoorpositioning.wifiindoorpositioning.R;

public class SelectUser extends AppCompatActivity {

    @Override
    protected  void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        setContentView(activity_select);

        Button admin = findViewById(R.id.Admin);
        Button guest = findViewById(R.id.Geust);

        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("user", 1);
                setResult(200,intent);
                finish();
            }
        });

        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("user", 2);
                setResult(200,intent);
                finish();
            }
        });

    }

}
