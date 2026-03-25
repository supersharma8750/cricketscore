package com.example.cricketscore;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ScoringActivity extends AppCompatActivity {

    TextView scoreText, overText, teamName, wicketText, targetText, crrText, rrrText;
    LinearLayout playerContainer;
    Spinner bowlerSpinner;

    ArrayList<Player> players = new ArrayList<>();
    ArrayList<Bowler> bowlers = new ArrayList<>();
    String tossWinner, decision;
    Player striker, nonStriker;
    Bowler currentBowler;

    int runs = 0, wickets = 0, balls = 0, oversLimit = 0;
    String teamA, teamB;

    boolean isSecondInnings = false;
    boolean matchOver = false;
    int target = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoring);

        scoreText = findViewById(R.id.scoreText);
        overText = findViewById(R.id.overText);
        teamName = findViewById(R.id.teamName);
        wicketText = findViewById(R.id.wicketText);
        targetText = findViewById(R.id.targetText);
        crrText = findViewById(R.id.crrText);
        rrrText = findViewById(R.id.rrrText);
        playerContainer = findViewById(R.id.playerContainer);
        bowlerSpinner = findViewById(R.id.bowlerSpinner);
        teamA = getIntent().getStringExtra("teamA");
        teamB = getIntent().getStringExtra("teamB");
        oversLimit = Integer.parseInt(getIntent().getStringExtra("overs"));
        tossWinner = getIntent().getStringExtra("tossWinner");
        decision = getIntent().getStringExtra("decision");
        teamName.setText(teamA);

        initPlayers();
    }

    // 🔥 INPUT FLOW
    void initPlayers() {
        showInput("Striker", s -> {
            striker = new Player(s);
            players.add(striker);

            showInput("Non-Striker", ns -> {
                nonStriker = new Player(ns);
                players.add(nonStriker);

                showInput("Bowler", b -> {
                    currentBowler = new Bowler(b);
                    bowlers.add(currentBowler);
                    setupSpinner();
                    updateUI();
                });
            });
        });
    }

    void showInput(String title, InputCallback cb) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(title);

        EditText e = new EditText(this);
        b.setView(e);

        b.setPositiveButton("OK", (d, w) -> {
            String t = e.getText().toString();
            if (t.isEmpty()) t = "Player";
            cb.onInput(t);
        });

        b.setCancelable(false);
        b.show();
    }

    interface InputCallback {
        void onInput(String text);
    }

    void setupSpinner() {
        ArrayList<String> names = new ArrayList<>();
        for (Bowler b : bowlers) names.add(b.name);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, names);

        bowlerSpinner.setAdapter(adapter);

        bowlerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                currentBowler = bowlers.get(pos);
            }
            public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    // 🔥 GAME LOGIC
    void addBall() {
        if (matchOver) return;

        balls++;
        currentBowler.balls++;

        if (balls % 6 == 0 && balls < oversLimit * 6) {
            swapStrike();
            askBowler();
        }

        if (balls >= oversLimit * 6) endMatch();
    }

    void askBowler() {
        if (matchOver) return;

        String[] options = new String[bowlers.size() + 1];

        for (int i = 0; i < bowlers.size(); i++)
            options[i] = bowlers.get(i).name;

        options[bowlers.size()] = "New Bowler";

        new AlertDialog.Builder(this)
                .setTitle("Select Bowler")
                .setItems(options, (d, which) -> {
                    if (which < bowlers.size()) {
                        currentBowler = bowlers.get(which);
                    } else {
                        showInput("New Bowler", name -> {
                            Bowler b = new Bowler(name);
                            bowlers.add(b);
                            setupSpinner();
                            currentBowler = b;
                        });
                    }
                }).show();
    }

    void swapStrike() {
        Player t = striker;
        striker = nonStriker;
        nonStriker = t;
    }

    public void addOne(View v) {
        runs++; striker.addRuns(1); currentBowler.runs++;
        addBall(); swapStrike(); updateUI(); checkWin();
    }

    public void addTwo(View v) {
        runs += 2; striker.addRuns(2); currentBowler.runs += 2;
        addBall(); updateUI(); checkWin();
    }

    public void addFour(View v) {
        runs += 4; striker.addRuns(4); currentBowler.runs += 4;
        addBall(); updateUI(); checkWin();
    }

    public void addSix(View v) {
        runs += 6; striker.addRuns(6); currentBowler.runs += 6;
        addBall(); updateUI(); checkWin();
    }

    public void addWicket(View v) {
        wickets++; striker.balls++; currentBowler.wickets++;
        addBall();

        showInput("New Batsman", name -> {
            Player p = new Player(name);
            players.add(p);
            striker = p;
            updateUI();
        });

        if (wickets >= 10) endMatch();
    }

    public void addWide(View v) {
        runs++; currentBowler.runs++;
        updateUI(); checkWin();
    }

    public void nextInnings(View v) {
        if (!isSecondInnings) {
            target = runs + 1;

            runs = wickets = balls = 0;
            players.clear();
            bowlers.clear();

            isSecondInnings = true;
            teamName.setText(teamB);

            initPlayers();
        }
    }

    void checkWin() {
        if (isSecondInnings && runs >= target && !matchOver) {
            endMatch();
        }
    }

    void endMatch() {

        if (!isSecondInnings) {
            nextInnings(null);
            return;
        }

        matchOver = true;

        String result;

        if (runs >= target) {
            // ✅ Team B wins by wickets
            int wicketsLeft = 10 - wickets;

            // 🔥 Balls left calculation
            int ballsLeft = (oversLimit * 6) - balls;

            result = teamB + " won by " + wicketsLeft + " wickets";

            // ✅ Add balls left only if match finished early
            if (ballsLeft > 0) {
                result += " (" + ballsLeft + " balls left)";
            }

            result += " 🎉";

        } else {
            // ✅ Team A wins by runs
            int runsDefended = target - runs - 1;

            result = teamA + " won by " + runsDefended + " runs 🏆";
        }

        Intent i = new Intent(this, MatchSummaryActivity.class);
        i.putExtra("teamA", teamA);
        i.putExtra("teamB", teamB);
        i.putExtra("result", result);
        i.putExtra("score", runs + "/" + wickets);

        startActivity(i);
        finish();
    }

    void updateUI() {
        scoreText.setText(runs + "/" + wickets);
        wicketText.setText("Wickets: " + wickets);
        overText.setText("Overs: " + (balls / 6) + "." + (balls % 6));

        double crr = balls > 0 ? (runs * 6.0) / balls : 0;
        crrText.setText("CRR: " + String.format("%.2f", crr));

        if (isSecondInnings) {
            targetText.setText("Target: " + target);

            int ballsLeft = (oversLimit * 6) - balls;
            int runsNeeded = target - runs;

            double rrr = ballsLeft > 0 ? (runsNeeded * 6.0) / ballsLeft : 0;
            rrrText.setText("RRR: " + String.format("%.2f", rrr));
        }

        showPlayers();
    }

    void showPlayers() {
        playerContainer.removeAllViews();

        TextView s = new TextView(this);
        s.setText("⭐ " + striker.getStats());
        s.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        playerContainer.addView(s);

        TextView ns = new TextView(this);
        ns.setText("• " + nonStriker.getStats());
        ns.setTextColor(getResources().getColor(android.R.color.white));
        playerContainer.addView(ns);

        TextView b = new TextView(this);
        b.setText("Bowler: " + currentBowler.getStats());
        b.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        playerContainer.addView(b);
    }

    // 🔥 CLASSES
    static class Player {
        String name;
        int runs = 0, balls = 0;

        Player(String n) { name = n; }

        void addRuns(int r) { runs += r; balls++; }

        String getStats() {
            return name + " " + runs + "(" + balls + ")";
        }
    }

    static class Bowler {
        String name;
        int balls = 0, runs = 0, wickets = 0;

        Bowler(String n) { name = n; }

        String getStats() {
            double overs = balls / 6.0;
            double eco = overs > 0 ? runs / overs : 0;
            return name + " " + wickets + "/" + runs +
                    " (" + String.format("%.1f", overs) + ") Eco:" +
                    String.format("%.2f", eco);
        }
    }
}