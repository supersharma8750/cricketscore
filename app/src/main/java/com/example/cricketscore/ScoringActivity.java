package com.example.cricketscore;

import android.app.AlertDialog;
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

        teamName.setText(teamA);

        initPlayers();
    }

    void initPlayers() {
        showInput("Striker Name", s -> {
            striker = new Player(s);
            players.add(striker);

            showInput("Non-Striker Name", ns -> {
                nonStriker = new Player(ns);
                players.add(nonStriker);

                showInput("Bowler Name", b -> {
                    currentBowler = new Bowler(b);
                    bowlers.add(currentBowler);
                    setupSpinner();
                    updateUI();
                });
            });
        });
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

    void updateUI() {
        scoreText.setText(runs + "/" + wickets);
        wicketText.setText("Wickets: " + wickets);
        overText.setText("Overs: " + (balls / 6) + "." + (balls % 6));

        double oversPlayed = balls / 6.0;
        double crr = oversPlayed > 0 ? runs / oversPlayed : 0;
        crrText.setText("CRR: " + String.format("%.2f", crr));

        if (isSecondInnings) {
            targetText.setText("Target: " + target);

            int ballsLeft = (oversLimit * 6) - balls;
            int runsNeeded = target - runs;

            double oversLeft = ballsLeft / 6.0;
            double rrr = oversLeft > 0 ? runsNeeded / oversLeft : 0;

            rrrText.setText("RRR: " + String.format("%.2f", rrr));
        }

        showPlayers();
    }

    void showPlayers() {
        playerContainer.removeAllViews();

        TextView s = new TextView(this);
        s.setText("⭐ " + striker.getStats());
        playerContainer.addView(s);

        TextView ns = new TextView(this);
        ns.setText("• " + nonStriker.getStats());
        playerContainer.addView(ns);

        TextView b = new TextView(this);
        b.setText("Bowler: " + currentBowler.getStats());
        playerContainer.addView(b);
    }

    void addBall() {
        if (matchOver) return;

        balls++;
        currentBowler.balls++;

        // 🔥 End of over
        if (balls % 6 == 0) {
            swapStrike();

            // ❗ Ask for next bowler ONLY if match not over
            if (!matchOver && balls < oversLimit * 6) {

                showBowlerSelectionDialog();
            }
        }

        if (balls >= oversLimit * 6) {
            endMatch();
        }
    }

    void swapStrike() {
        Player t = striker;
        striker = nonStriker;
        nonStriker = t;
    }

    public void addOne(View v) {
        if (matchOver) return;
        runs++; striker.addRuns(1); currentBowler.runs++;
        addBall(); swapStrike(); updateUI(); checkWin();
    }

    public void addTwo(View v) {
        if (matchOver) return;
        runs += 2; striker.addRuns(2); currentBowler.runs += 2;
        addBall(); updateUI(); checkWin();
    }

    public void addFour(View v) {
        if (matchOver) return;
        runs += 4; striker.addRuns(4); currentBowler.runs += 4;
        addBall(); updateUI(); checkWin();
    }

    public void addSix(View v) {
        if (matchOver) return;
        runs += 6; striker.addRuns(6); currentBowler.runs += 6;
        addBall(); updateUI(); checkWin();
    }

    public void addWicket(View v) {
        if (matchOver) return;

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
        if (matchOver) return;
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
        if (isSecondInnings && runs >= target) {
            matchOver = true;
            scoreText.setText(teamB + " WON 🎉");
        }
    }
    void showBowlerSelectionDialog() {

        String[] options = new String[bowlers.size() + 1];

        // Existing bowlers
        for (int i = 0; i < bowlers.size(); i++) {
            options[i] = bowlers.get(i).name;
        }

        // Add new option
        options[bowlers.size()] = "➕ New Bowler";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Bowler");

        builder.setItems(options, (dialog, which) -> {

            // Existing bowler selected
            if (which < bowlers.size()) {
                currentBowler = bowlers.get(which);
                bowlerSpinner.setSelection(which);
            }
            // New bowler
            else {
                showInput("Enter Bowler Name", name -> {
                    Bowler b = new Bowler(name);
                    bowlers.add(b);
                    setupSpinner();
                    currentBowler = b;
                });
            }
        });

        builder.setCancelable(false);
        builder.show();
    }
    void endMatch() {
        if (!isSecondInnings) nextInnings(null);
        else {
            matchOver = true;
            scoreText.setText((runs >= target ? teamB : teamA) + " WON");
        }
    }

    static class Player {
        String name; int runs = 0, balls = 0;
        Player(String n) { name = n; }
        void addRuns(int r) { runs += r; balls++; }
        String getStats() { return name + " " + runs + "(" + balls + ")"; }
    }

    static class Bowler {
        String name; int balls = 0, runs = 0, wickets = 0;
        Bowler(String n) { name = n; }
        String getStats() {
            double o = balls / 6.0;
            double eco = o > 0 ? runs / o : 0;
            return name + " " + wickets + "/" + runs +
                    " (" + String.format("%.1f", o) + ") Eco:" +
                    String.format("%.2f", eco);
        }
    }
}