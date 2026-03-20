package com.example.cricketscore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText teamA, teamB, overs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        teamA = findViewById(R.id.teamA);
        teamB = findViewById(R.id.teamB);
        overs = findViewById(R.id.overs);
    }

    public void startMatch(View v) {
        Intent i = new Intent(this, ScoringActivity.class);
        i.putExtra("teamA", teamA.getText().toString());
        i.putExtra("teamB", teamB.getText().toString());
        i.putExtra("overs", overs.getText().toString());
        startActivity(i);
    }
}