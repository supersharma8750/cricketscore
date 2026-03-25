package com.example.cricketscore;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MatchSummaryActivity extends AppCompatActivity {

    TextView resultText, scoreText, matchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        resultText = findViewById(R.id.resultText);
        scoreText = findViewById(R.id.scoreText);
        matchText = findViewById(R.id.matchText);

        // ✅ GET DATA FROM INTENT
        String teamA = getIntent().getStringExtra("teamA");
        String teamB = getIntent().getStringExtra("teamB");
        String result = getIntent().getStringExtra("result");
        String score = getIntent().getStringExtra("score");

        // ✅ FIX NULL VALUES (IMPORTANT)
        if (teamA == null) teamA = "Team A";
        if (teamB == null) teamB = "Team B";
        if (result == null) result = "Match Result";
        if (score == null) score = "0/0";

        // ✅ SET UI
        matchText.setText(teamA + " vs " + teamB);
        resultText.setText(result);
        scoreText.setText("Final Score: " + score);
    }
}