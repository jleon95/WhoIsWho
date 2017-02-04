package com.npi.whoiswho;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar_help);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        myToolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener(){

            @Override
            public  void onClick(View v) {

                onBackPressed();
            }
        });
    }
}
