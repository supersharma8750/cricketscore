package com.example.cricketscore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

        String teamAName = teamA.getText().toString().trim();
        String teamBName = teamB.getText().toString().trim();
        String oversValue = overs.getText().toString().trim();

        if (teamAName.isEmpty() || teamBName.isEmpty() || oversValue.isEmpty()) {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        // 👉 OPEN TOSS SCREEN (NOT scoring)
        Intent i = new Intent(this, TossActivity.class);

        i.putExtra("teamA", teamAName);
        i.putExtra("teamB", teamBName);
        i.putExtra("overs", oversValue);

        startActivity(i);
    }
    }
