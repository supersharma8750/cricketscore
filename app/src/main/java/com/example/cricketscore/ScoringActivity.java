package com.example.cricketscore;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Stack;

public class ScoringActivity extends AppCompatActivity {

    TextView scoreText, overText, teamName, wicketText, targetText, crrText, rrrText;
    LinearLayout playerContainer;
    Spinner bowlerSpinner;
    // 🔥 STORE BOTH INNINGS DATA
    ArrayList<String> teamABatsmen = new ArrayList<>();
    ArrayList<String> teamABowlers = new ArrayList<>();

    ArrayList<String> teamBBatsmen = new ArrayList<>();
    ArrayList<String> teamBBowlers = new ArrayList<>();
    ArrayList<Player> players = new ArrayList<>();
    ArrayList<Bowler> bowlers = new ArrayList<>();

    Player striker, nonStriker;
    Bowler currentBowler;

    int runs = 0, wickets = 0, balls = 0, oversLimit = 0;
    String teamA, teamB, tossWinner, decision;

    boolean isSecondInnings = false;
    boolean matchOver = false;
    int target = 0;

    Stack<MatchState> history = new Stack<>();

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
    public void addWicket(View v) {
        if (matchOver) return;

        wickets++;

        // ✅ Safe check
        if (striker != null) {
            striker.balls++;
        }

        if (currentBowler != null) {
            currentBowler.wickets++;
        }

        addBall();

        showInput("New Batsman", name -> {
            Player p = new Player(name);
            players.add(p);
            striker = p;
            updateUI();
        });

        if (wickets >= 10) endMatch();

        updateUI();
        checkWin();
    }
    // 🔥 INIT PLAYERS
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

    // 🔥 INPUT DIALOG
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

    // 🔥 SPINNER
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

    // 🔥 SAVE STATE (UNDO)
    void saveState() {
        history.push(new MatchState(runs, wickets, balls, striker, nonStriker, currentBowler));
    }

    public void undo(View v) {
        if (history.isEmpty() || matchOver) return;

        MatchState prev = history.pop();

        runs = prev.runs;
        wickets = prev.wickets;
        balls = prev.balls;
        striker = prev.striker;
        nonStriker = prev.nonStriker;
        currentBowler = prev.bowler;

        updateUI();
    }

    // 🔥 BALL LOGIC
    void addBall() {
        if (matchOver) return;

        balls++;
        currentBowler.balls++;

        if (balls % 6 == 0) {
            swapStrike();
            if (balls < oversLimit * 6) showBowlerSelectionDialog();
        }

        if (balls >= oversLimit * 6) endMatch();
    }

    void swapStrike() {
        Player t = striker;
        striker = nonStriker;
        nonStriker = t;
    }

    // 🔥 SCORING
    public void addOne(View v) {
        if (matchOver) return;
        saveState();

        runs++; striker.addRuns(1); currentBowler.runs++;
        addBall(); swapStrike();
        updateUI(); checkWin();
    }

    public void addTwo(View v) {
        if (matchOver) return;
        saveState();

        runs += 2; striker.addRuns(2); currentBowler.runs += 2;
        addBall();
        updateUI(); checkWin();
    }

    public void addFour(View v) {
        if (matchOver) return;
        saveState();

        runs += 4; striker.addRuns(4); currentBowler.runs += 4;
        addBall();
        updateUI(); checkWin();
    }

    public void addSix(View v) {
        if (matchOver) return;
        saveState();

        runs += 6; striker.addRuns(6); currentBowler.runs += 6;
        addBall();
        updateUI(); checkWin();
    }

    public void addWide(View v) {
        if (matchOver) return;
        saveState();

        runs++; currentBowler.runs++;
        updateUI(); checkWin();
    }

    public void addNoBall(View v) {
        if (matchOver) return;
        saveState();

        runs++; currentBowler.runs++;
        updateUI(); checkWin();
    }

    public void addByes(View v) {
        if (matchOver) return;
        saveState();

        runs++; currentBowler.runs++;
        addBall(); swapStrike();
        updateUI(); checkWin();
    }

    public void addRunOut(View v) {
        if (matchOver) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Run Out - Runs scored?");

        String[] runOptions = {"0", "1", "2", "3"};

        builder.setItems(runOptions, (dialog, which) -> {

            int run = Integer.parseInt(runOptions[which]);

            // 👉 STEP 2: Ask which batsman is OUT
            AlertDialog.Builder outBuilder = new AlertDialog.Builder(this);
            outBuilder.setTitle("Who is OUT?");

            String[] batsmanOptions = {
                    "Striker (" + striker.name + ")",
                    "Non-Striker (" + nonStriker.name + ")"
            };

            outBuilder.setItems(batsmanOptions, (d, w) -> {

                // ✅ Add runs
                runs += run;
                striker.runs += run;
                striker.balls++;

                currentBowler.runs += run;

                // ✅ Wicket
                wickets++;
                currentBowler.wickets++;

                // ✅ Ball counted
                addBall();

                // ✅ Strike change (based on runs)
                if (run % 2 == 1) {
                    swapStrike();
                }

                // ✅ Handle which player is out
                if (w == 0) {
                    // 🔴 Striker out
                    showInput("New Batsman", name -> {
                        Player p = new Player(name);
                        players.add(p);
                        striker = p;
                        updateUI();
                    });

                } else {
                    // 🔴 Non-striker out
                    showInput("New Batsman", name -> {
                        Player p = new Player(name);
                        players.add(p);
                        nonStriker = p;
                        updateUI();
                    });
                }

                if (wickets >= 10) endMatch();

                updateUI();
                checkWin();
            });

            outBuilder.show();
        });

        builder.show();
    }
    public void manualSwap(View v) {
        if (matchOver) return;
        saveState();

        striker.balls++;
        currentBowler.balls++;
        balls++;

        swapStrike();

        if (balls % 6 == 0) {
            swapStrike();
            if (balls < oversLimit * 6) showBowlerSelectionDialog();
        }

        if (balls >= oversLimit * 6) endMatch();

        updateUI();
    }

    // 🔥 UI
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
            double rrr = ballsLeft > 0 ? runsNeeded / (ballsLeft / 6.0) : 0;
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

    // 🔥 MATCH FLOW
    void checkWin() {
        if (isSecondInnings && runs >= target && !matchOver) {
            endMatch();
        }
    }

    public void nextInnings(View v) {
        if (!isSecondInnings) {

            target = runs + 1;

            // ✅ SAVE TEAM A DATA
            for (Player p : players) {
                double sr = p.balls > 0 ? (p.runs * 100.0 / p.balls) : 0;
                teamABatsmen.add(p.name + " - " + p.runs + "(" + p.balls + ") SR:" + String.format("%.1f", sr));
            }

            for (Bowler b : bowlers) {
                double overs = b.balls / 6.0;
                double eco = overs > 0 ? b.runs / overs : 0;
                teamABowlers.add(b.name + " - " + b.wickets + "/" + b.runs +
                        " (" + String.format("%.1f", overs) + ") Eco:" +
                        String.format("%.2f", eco));
            }

            // 🔄 RESET FOR TEAM B
            runs = 0;
            wickets = 0;
            balls = 0;

            players.clear();
            bowlers.clear();

            isSecondInnings = true;
            teamName.setText(teamB);

            initPlayers();
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
            int wicketsLeft = 10 - wickets;
            int ballsLeft = (oversLimit * 6) - balls;

            result = teamB + " won by " + wicketsLeft + " wickets";
            if (ballsLeft > 0) result += " (" + ballsLeft + " balls left)";
        } else {
            int runsDefended = target - runs - 1;
            result = teamA + " won by " + runsDefended + " runs";
        }
// ✅ SAVE TEAM B DATA
        for (Player p : players) {
            double sr = p.balls > 0 ? (p.runs * 100.0 / p.balls) : 0;
            teamBBatsmen.add(p.name + " - " + p.runs + "(" + p.balls + ") SR:" + String.format("%.1f", sr));
        }

        for (Bowler b : bowlers) {
            double overs = b.balls / 6.0;
            double eco = overs > 0 ? b.runs / overs : 0;
            teamBBowlers.add(b.name + " - " + b.wickets + "/" + b.runs +
                    " (" + String.format("%.1f", overs) + ") Eco:" +
                    String.format("%.2f", eco));
        }
        // ✅ CREATE ONLY ONE INTENT
        Intent i = new Intent(this, MatchSummaryActivity.class);
        i.putStringArrayListExtra("teamABatsmen", teamABatsmen);
        i.putStringArrayListExtra("teamABowlers", teamABowlers);

        i.putStringArrayListExtra("teamBBatsmen", teamBBatsmen);
        i.putStringArrayListExtra("teamBBowlers", teamBBowlers);
        i.putExtra("teamA", teamA);
        i.putExtra("teamB", teamB);
        i.putExtra("result", result);
        i.putExtra("score", runs + "/" + wickets);
        i.putExtra("overs", (balls / 6) + "." + (balls % 6));
        i.putExtra("tossWinner", tossWinner);
        i.putExtra("decision", decision);

        // ✅ BATSMAN STATS
        ArrayList<String> batsmanStats = new ArrayList<>();
        for (Player p : players) {
            double sr = p.balls > 0 ? (p.runs * 100.0 / p.balls) : 0;
            batsmanStats.add(p.name + " - " + p.runs + "(" + p.balls + ") SR:" + String.format("%.1f", sr));
        }
        i.putStringArrayListExtra("batsmen", batsmanStats);

        // ✅ BOWLER STATS
        ArrayList<String> bowlerStats = new ArrayList<>();
        for (Bowler b : bowlers) {
            double overs = b.balls / 6.0;
            double eco = overs > 0 ? b.runs / overs : 0;
            bowlerStats.add(b.name + " - " + b.wickets + "/" + b.runs +
                    " (" + String.format("%.1f", overs) + ") Eco:" +
                    String.format("%.2f", eco));
        }
        i.putStringArrayListExtra("bowlers", bowlerStats);

        // ✅ START ACTIVITY ONCE
        startActivity(i);
        finish();
    }
    void showBowlerSelectionDialog() {
        String[] options = new String[bowlers.size() + 1];
        for (int i = 0; i < bowlers.size(); i++) options[i] = bowlers.get(i).name;
        options[bowlers.size()] = "➕ New Bowler";

        new AlertDialog.Builder(this)
                .setTitle("Select Bowler")
                .setItems(options, (d, which) -> {
                    if (which < bowlers.size()) {
                        currentBowler = bowlers.get(which);
                        bowlerSpinner.setSelection(which);
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

    // 🔥 DATA CLASSES
    static class Player {
        String name;
        int runs = 0, balls = 0;
        Player(String n) { name = n; }
        void addRuns(int r) { runs += r; balls++; }
        String getStats() { return name + " " + runs + "(" + balls + ")"; }
    }

    static class Bowler {
        String name;
        int balls = 0, runs = 0, wickets = 0;
        Bowler(String n) { name = n; }
        String getStats() {
            double o = balls / 6.0;
            double eco = o > 0 ? runs / o : 0;
            return name + " " + wickets + "/" + runs +
                    " (" + String.format("%.1f", o) + ") Eco:" +
                    String.format("%.2f", eco);
        }
    }

    class MatchState {
        int runs, wickets, balls;
        Player striker, nonStriker;
        Bowler bowler;

        MatchState(int r, int w, int b, Player s, Player ns, Bowler bl) {
            runs = r;
            wickets = w;
            balls = b;
            striker = s;
            nonStriker = ns;
            bowler = bl;
        }
    }
}