package com.talentica.wifiindoorpositioning.wifiindoorpositioning.ui;

import static com.talentica.wifiindoorpositioning.wifiindoorpositioning.R.layout.activity_select;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.talentica.wifiindoorpositioning.wifiindoorpositioning.R;

public class SelectUser extends AppCompatActivity {

    EditText edPW;

    @Override
    protected  void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        setContentView(activity_select);

        Button admin = findViewById(R.id.Admin);
        Button guest = findViewById(R.id.Geust);
        edPW = (EditText) findViewById(R.id.PassWord);


            admin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String temp = edPW.getText().toString();
                    if (temp.equals("Team3")) {
                        Intent intent = new Intent();
                        intent.putExtra("user", 1);
                        setResult(200, intent);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Input the correct password.", Toast.LENGTH_SHORT).show();
                    }
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
