package com.example.quizzes.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.adapter.AllInterestsAdapter;
import com.example.quizzes.adapter.InterestsIncludedAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class EditQuizActivity extends AppCompatActivity {

    ImageView photoIV;
    TextView usernameTV, errorTV, updatedTV;
    EditText descriptionET, correctAnswerET, wrongAnswerET0, wrongAnswerET1;
    SeekBar hardnessSB;
    RecyclerView allInterestsRV, interestsIncludedRV;
    SharedPreferences sharedPreferences;
    FirebaseFirestore database;
    ProgressDialog progressDialog;
    HashMap<String, Object> quizReference = new HashMap<>();
    AllInterestsAdapter allInterestsAdapter;
    public static InterestsIncludedAdapter interestsIncludedAdapter;
    public static ArrayList<String> interestsIncluded = new ArrayList<>();
    private String quizID, description, correctAnswer, wrongAnswer0, wrongAnswer1;
    private boolean descriptionEdited, correctAnswerEdited,
            wrongAnswer0Edited, wrongAnswer1Edited, hardnessEdited;
    public static boolean interestsEdited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quiz);
        setActionBarTitle("Edit Quiz");
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.show();
        quizID = getIntent().getStringExtra(StaticClass.QUIZ_ID);
        database = FirebaseFirestore.getInstance();
        findViewsByIds();
        setQuizUI();
    }
    private void setQuizUI(){
        database.collection("quizzes")
                .document(quizID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            descriptionET.setText(String.valueOf(document.get("description")));
                            correctAnswerET.setText(String.valueOf(document.get("correct")));
                            wrongAnswerET0.setText(String.valueOf(document.get("wrong0")));
                            wrongAnswerET1.setText(String.valueOf(document.get("wrong1")));
                            long progress = (long) document.get("hardness-poster-defined");
                            hardnessSB.setProgress((int) progress);
                            interestsIncluded = (ArrayList<String>) document.get("interests");
                            setRecyclerViews();
                            setChangeDetectors();
                            progressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        usernameTV = findViewById(R.id.usernameTV);
        usernameTV.setText(sharedPreferences.getString(StaticClass.USERNAME, "no username"));
        errorTV = findViewById(R.id.errorTV);
        updatedTV = findViewById(R.id.updatedTV);
        descriptionET = findViewById(R.id.descriptionET);
        correctAnswerET = findViewById(R.id.correctAnswerET);
        wrongAnswerET0 = findViewById(R.id.wrongAnswerET0);
        wrongAnswerET1 = findViewById(R.id.wrongAnswerET1);
        hardnessSB = findViewById(R.id.hardnessSB);
        allInterestsRV = findViewById(R.id.allInterestsRV);
        interestsIncludedRV = findViewById(R.id.includedInterestsRV);
    }
    private void setRecyclerViews(){
        interestsIncludedAdapter = new InterestsIncludedAdapter(getApplicationContext(),
                interestsIncluded);
        interestsIncludedRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        interestsIncludedRV.setAdapter(interestsIncludedAdapter);
        allInterestsAdapter = new AllInterestsAdapter(getApplicationContext(),
                StaticClass.allInterests);
        allInterestsRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        allInterestsRV.setAdapter(allInterestsAdapter);
    }
    private void setChangeDetectors(){
        descriptionET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!descriptionEdited){
                    descriptionEdited = true;
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}@Override public void afterTextChanged(Editable s) {}
        });
        correctAnswerET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!correctAnswerEdited){
                    correctAnswerEdited = true;
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}@Override public void afterTextChanged(Editable s) {}
        });
        wrongAnswerET0.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!wrongAnswer0Edited){
                    wrongAnswer0Edited = true;
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}@Override public void afterTextChanged(Editable s) {}
        });
        wrongAnswerET1.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!wrongAnswer1Edited){
                    wrongAnswer1Edited = true;
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}@Override public void afterTextChanged(Editable s) {}
        });
        hardnessSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!hardnessEdited){
                    hardnessEdited = true;
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}@Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_quiz_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.edit){
            edit();
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean isInputValid(){
        description = descriptionET.getText().toString().trim();
        if(description.isEmpty()){
            displayErrorTV(R.string.unspecified_description);
            return false;
        }
        correctAnswer = correctAnswerET.getText().toString().trim();
        if(correctAnswer.isEmpty()){
            displayErrorTV(R.string.unspecified_choices);
            return false;
        }
        wrongAnswer0 = wrongAnswerET0.getText().toString().trim();
        if(wrongAnswer0.isEmpty()){
            displayErrorTV(R.string.unspecified_choices);
            return false;
        }
        wrongAnswer1 = wrongAnswerET1.getText().toString().trim();
        if(wrongAnswer1.isEmpty()){
            displayErrorTV(R.string.unspecified_choices);
            return false;
        }
        if(interestsIncluded.isEmpty()){
            displayErrorTV(R.string.interests_unincluded);
            return false;
        }
        if(hardnessSB.getProgress() == 0){
            displayErrorTV(R.string.unspecified_hardness);
            return false;
        }
        return true;
    }
    private void edit(){
        if(isInputValid()){
            progressDialog.setMessage("Uploading...");
            progressDialog.show();
            putQuizReference();
            editQuiz();
        }
    }
    private void putQuizReference(){
        if(descriptionEdited) {
            quizReference.put("description", description);
        }
        if(correctAnswerEdited) {
            quizReference.put("correct", correctAnswer);
        }
        if(wrongAnswer0Edited) {
            quizReference.put("wrong0", wrongAnswer0);
        }
        if(wrongAnswer1Edited){
            quizReference.put("wrong1", wrongAnswer1);
        }
        if(interestsEdited) {
            quizReference.put("interests", interestsIncluded);
        }
        if(hardnessEdited) {
            quizReference.put("hardness-poster-defined", hardnessSB.getProgress());
        }
        quizReference.put("edited", descriptionEdited  || correctAnswerEdited ||
                                    wrongAnswer1Edited || wrongAnswer0Edited  ||
                                    interestsEdited    || hardnessEdited);
    }
    private void editQuiz(){
        database.collection("quizzes")
                .document(quizID)
                .update(quizReference)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            displayUpdatedTV();
                        }
                    }
                });
    }
    private void displayErrorTV(int resourceID) {
        errorTV.setText(resourceID);
        errorTV.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                errorTV.setVisibility(View.GONE);
            }
        }, 1500);
    }
    private void displayUpdatedTV() {
        updatedTV.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updatedTV.setVisibility(View.GONE);
                startActivity(new Intent(getApplicationContext(), MyQuizzesActivity.class));
            }
        }, 600);
    }
    public void setActionBarTitle(String title){
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(
                Html.fromHtml("<font color=\"#ffffff\"> "+title+" </font>")
        );
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("Are you sure you want to discard the edit?")
                .setPositiveButton(
                        Html.fromHtml("<font color=\"#770000\"> Discard </font>"),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getApplicationContext(), MyQuizzesActivity.class));
                            }
                        })
                .setNegativeButton(
                        Html.fromHtml("<font color=\"#3b7f8d\"> Cancel </font>"),
                        null)
                .show();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
