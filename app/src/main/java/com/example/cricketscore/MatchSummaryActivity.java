package com.example.cricketscore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MatchSummaryActivity extends AppCompatActivity {

    TextView resultText, scoreText, matchText, tossText;
    Button viewScorecardBtn;
    Button newMatchBtn;
    LinearLayout scorecardContainer;
    ScrollView scorecardScroll;

    ArrayList<String> teamABatsmen, teamABowlers;
    ArrayList<String> teamBBatsmen, teamBBowlers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // 🔹 UI
        newMatchBtn = findViewById(R.id.newMatchBtn);
        resultText = findViewById(R.id.resultText);
        scoreText = findViewById(R.id.scoreText);
        matchText = findViewById(R.id.matchText);
        tossText = findViewById(R.id.tossText);
        viewScorecardBtn = findViewById(R.id.viewScorecardBtn);
        scorecardContainer = findViewById(R.id.scorecardContainer);
        scorecardScroll = findViewById(R.id.scorecardScroll);

        // 🔹 DATA
        String teamA = getIntent().getStringExtra("teamA");
        String teamB = getIntent().getStringExtra("teamB");
        String result = getIntent().getStringExtra("result");
        String score = getIntent().getStringExtra("score");
        String tossWinner = getIntent().getStringExtra("tossWinner");
        String decision = getIntent().getStringExtra("decision");

        teamABatsmen = getIntent().getStringArrayListExtra("teamABatsmen");
        teamABowlers = getIntent().getStringArrayListExtra("teamABowlers");

        teamBBatsmen = getIntent().getStringArrayListExtra("teamBBatsmen");
        teamBBowlers = getIntent().getStringArrayListExtra("teamBBowlers");

        // 🔹 SET UI
        matchText.setText(teamA + " vs " + teamB);
        resultText.setText(result);
        scoreText.setText("Final Score: " + score);

        if (tossWinner != null && decision != null) {
            tossText.setText(tossWinner + " won toss and chose to " + decision);
        }

        // 🔥 BUTTON CLICK
        viewScorecardBtn.setOnClickListener(v -> showScorecard());
    }

    // ================= SCORECARD =================

    void showScorecard() {

        scorecardScroll.setVisibility(View.VISIBLE);
        scorecardContainer.removeAllViews();

        // 🟢 TEAM A
        addSectionTitle("TEAM A BATTING");
        addBattingTable(teamABatsmen);

        addSectionTitle("TEAM A BOWLING");
        addBowlingTable(teamABowlers);

        // 🔵 TEAM B
        addSectionTitle("TEAM B BATTING");
        addBattingTable(teamBBatsmen);

        addSectionTitle("TEAM B BOWLING");
        addBowlingTable(teamBBowlers);
    }

    // ================= BATTING TABLE =================

    void addBattingTable(ArrayList<String> data) {

        if (data == null) return;

        LinearLayout header = createRow(true);
        header.addView(createCell("Name", 2, true));
        header.addView(createCell("R", 1, true));
        header.addView(createCell("B", 1, true));
        header.addView(createCell("SR", 1, true));
        scorecardContainer.addView(header);

        for (String s : data) {

            String name = s.split(" - ")[0];

            String runs = "0", balls = "0", sr = "0";

            try {
                String stats = s.split(" - ")[1];
                runs = stats.substring(0, stats.indexOf("("));
                balls = stats.substring(stats.indexOf("(") + 1, stats.indexOf(")"));
                sr = stats.substring(stats.indexOf("SR:") + 3);
            } catch (Exception e) {}

            LinearLayout row = createRow(false);

            row.addView(createCell(name, 2, false));
            row.addView(createCell(runs, 1, false));
            row.addView(createCell(balls, 1, false));
            row.addView(createCell(sr, 1, false));

            scorecardContainer.addView(row);
        }
    }

    // ================= BOWLING TABLE =================

    void addBowlingTable(ArrayList<String> data) {

        if (data == null) return;

        LinearLayout header = createRow(true);
        header.addView(createCell("Name", 2, true));
        header.addView(createCell("O", 1, true));
        header.addView(createCell("R", 1, true));
        header.addView(createCell("W", 1, true));
        header.addView(createCell("Eco", 1, true));
        scorecardContainer.addView(header);

        for (String s : data) {

            String name = s.split(" - ")[0];

            String overs = "0", runs = "0", wickets = "0", eco = "0";

            try {
                String stats = s.split(" - ")[1];

                wickets = stats.substring(0, stats.indexOf("/"));
                runs = stats.substring(stats.indexOf("/") + 1, stats.indexOf(" "));

                overs = stats.substring(stats.indexOf("(") + 1, stats.indexOf(")"));
                eco = stats.substring(stats.indexOf("Eco:") + 4);

            } catch (Exception e) {}

            LinearLayout row = createRow(false);

            row.addView(createCell(name, 2, false));
            row.addView(createCell(overs, 1, false));
            row.addView(createCell(runs, 1, false));
            row.addView(createCell(wickets, 1, false));
            row.addView(createCell(eco, 1, false));

            scorecardContainer.addView(row);
        }
    }

    // ================= UI HELPERS =================

    LinearLayout createRow(boolean isHeader) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(8, 8, 8, 8);
        newMatchBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);

            // 🔥 Clears old match completely
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(i);
        });
        if (isHeader) {
            row.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        }

        return row;
    }

    TextView createCell(String text, int weight, boolean isHeader) {
        TextView tv = new TextView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);

        tv.setLayoutParams(params);
        tv.setText(text);
        tv.setPadding(8, 4, 8, 4);

        tv.setTextColor(getResources().getColor(
                isHeader ? android.R.color.black : android.R.color.white));

        if (isHeader) {
            tv.setTextSize(14);
        }

        return tv;
    }

    void addSectionTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText("\n" + text);
        tv.setTextSize(18);
        tv.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        scorecardContainer.addView(tv);
    }
}