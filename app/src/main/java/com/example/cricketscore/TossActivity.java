package com.example.cricketscore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TossActivity extends AppCompatActivity {

    TextView matchTitle;
    RadioButton teamARadio, teamBRadio, batRadio, bowlRadio;

    String teamA, teamB, overs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toss);

        matchTitle = findViewById(R.id.matchTitle);
        teamARadio = findViewById(R.id.teamARadio);
        teamBRadio = findViewById(R.id.teamBRadio);
        batRadio = findViewById(R.id.batRadio);
        bowlRadio = findViewById(R.id.bowlRadio);

        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        overs = getIntent().getStringExtra("overs");

        matchTitle.setText(teamA + " vs " + teamB);

        teamARadio.setText(teamA);
        teamBRadio.setText(teamB);
    }

    public void startMatch(View v) {

        if (!teamARadio.isChecked() && !teamBRadio.isChecked()) return;
        if (!batRadio.isChecked() && !bowlRadio.isChecked()) return;

        String tossWinner = teamARadio.isChecked() ? teamA : teamB;
        String decision = batRadio.isChecked() ? "bat" : "bowl";

        Intent i = new Intent(this, ScoringActivity.class);

        i.putExtra("teamA", teamA);
        i.putExtra("teamB", teamB);
        i.putExtra("overs", overs);
        i.putExtra("tossWinner", tossWinner);
        i.putExtra("decision", decision);

        startActivity(i);
        finish();
    }
}