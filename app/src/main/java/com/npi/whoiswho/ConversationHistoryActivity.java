package com.npi.whoiswho;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ConversationHistoryActivity extends AppCompatActivity {

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar_history);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        myToolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener(){

            @Override
            public  void onClick(View v) {

                onBackPressed();
            }
        });

        Intent intent = getIntent();
        ArrayList<String> conversation = intent.getStringArrayListExtra("historyArray");

        listView = (ListView) findViewById(R.id.conversationList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_expandable_list_item_1,conversation);
        listView.setAdapter(arrayAdapter);
    }
}
