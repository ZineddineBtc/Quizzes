package com.example.quizzes.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.adapter.InterestsIncludedAdapter;
import com.example.quizzes.adapter.IncludingInterestsAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class CreateQuizActivity extends AppCompatActivity {

    ImageView photoIV;
    TextView usernameTV, errorTV, postedTV;
    EditText descriptionET, correctAnswerET, wrongAnswerET0, wrongAnswerET1;
    SeekBar hardnessSB;
    RecyclerView allInterestsRV, interestsIncludedRV;
    SharedPreferences sharedPreferences;
    FirebaseFirestore database;
    IncludingInterestsAdapter includingInterestsAdapter;
    public static InterestsIncludedAdapter interestsIncludedAdapter;
    public static ArrayList<String> interestsIncluded = new ArrayList<>();
    String description, correctAnswer, wrongAnswer0, wrongAnswer1;
    long index;
    ProgressDialog progressDialog;
    HashMap<String, Object> quizReference = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);
        database = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        findViewsByIds();
        progressDialog = new ProgressDialog(this);
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        usernameTV = findViewById(R.id.usernameTV);
        usernameTV.setText(sharedPreferences.getString(StaticClass.USERNAME, "no username"));
        errorTV = findViewById(R.id.errorTV);
        postedTV = findViewById(R.id.postedTV);
        descriptionET = findViewById(R.id.descriptionET);
        correctAnswerET = findViewById(R.id.correctAnswerET);
        wrongAnswerET0 = findViewById(R.id.wrongAnswerET0);
        wrongAnswerET1 = findViewById(R.id.wrongAnswerET1);
        hardnessSB = findViewById(R.id.hardnessSB);
        allInterestsRV = findViewById(R.id.allInterestsRV);
        interestsIncludedRV = findViewById(R.id.includedInterestsRV);
        setRecyclerViews();
    }
    private void setRecyclerViews(){
        includingInterestsAdapter = new IncludingInterestsAdapter(getApplicationContext(),
                StaticClass.allInterests);
        allInterestsRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));
        allInterestsRV.setAdapter(includingInterestsAdapter);
        interestsIncludedAdapter = new InterestsIncludedAdapter(getApplicationContext(),
                interestsIncluded);
        interestsIncludedRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false));
        interestsIncludedRV.setAdapter(interestsIncludedAdapter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_quiz_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.post){
            post();
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
    private void post(){
        if(isInputValid()){
            progressDialog.setMessage("Uploading...");
            progressDialog.show();
            putQuizReference();
            writeQuiz();
        }
    }
    private void putQuizReference(){
        quizReference.put("poster", sharedPreferences.getString(StaticClass.EMAIL, " "));
        quizReference.put("description", description);
        quizReference.put("correct", correctAnswer);
        quizReference.put("wrong0", wrongAnswer0);
        quizReference.put("wrong1", wrongAnswer1);
        quizReference.put("correct-count", 0);
        quizReference.put("wrong-count", 0);
        quizReference.put("likes-count", 0);
        quizReference.put("dislikes-count", 0);
        index = new Random().nextInt(3);
        quizReference.put("correct-index", index);
        quizReference.put("hardness-user-defined", 0);
        quizReference.put("hardness-poster-defined", hardnessSB.getProgress());
        quizReference.put("time", StaticClass.getCurrentTime());
        quizReference.put("likes-users", new ArrayList<String>());
        quizReference.put("dislikes-users", new ArrayList<String>());
        quizReference.put("answers-users", new ArrayList<String>());
    }
    private void arrayUnionQuizToUserDocument(DocumentReference doc){
        database.collection("users")
                .document(sharedPreferences.getString(StaticClass.EMAIL, " "))
                .update("quizzes", FieldValue.arrayUnion(doc))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        displayPostedTV();
                    }
                });
    }
    private void writeQuiz(){
        final DocumentReference documentReference = database.collection("quizzes").document();
        documentReference.set(quizReference)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            arrayUnionQuizToUserDocument(documentReference);
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
    private void displayPostedTV() {
        postedTV.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                postedTV.setVisibility(View.GONE);
                startActivity(new Intent(getApplicationContext(), CoreActivity.class));
            }
        }, 600);
    }
}
