package com.npi.whoiswho;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class ConversationHistoryActivity extends AppCompatActivity {

    ArrayList<String> conversation = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        conversation = intent.getStringArrayListExtra("historyArray");
    }


}
