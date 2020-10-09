package com.example.quizzes.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.adapter.ProfileQuizzesAdapter;
import com.example.quizzes.model.Quiz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    ImageView photoIV;
    TextView usernameTV, bioTV;
    RecyclerView quizzesRV;
    ProfileQuizzesAdapter adapter;
    ArrayList<Quiz> quizList = new ArrayList<>();
    FirebaseFirestore database;
    ProgressBar progressBar;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViewsByIds();
        database = FirebaseFirestore.getInstance();
        userID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        setProfileUI(userID);
        setRecyclerView();
        getQuizzes();
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        usernameTV = findViewById(R.id.usernameTV);
        bioTV = findViewById(R.id.bioTV);
        quizzesRV = findViewById(R.id.profileQuizzesRV);
        progressBar = findViewById(R.id.progressBar);
    }
    private void setProfileUI(String userID){
        database.collection("users")
                .document(userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                usernameTV.setText(document.get("username").toString());
                                bioTV.setText(document.get("bio").toString());
                            }
                        }
                    }
                });
    }
    private void setRecyclerView(){
        adapter = new ProfileQuizzesAdapter(getApplicationContext(), quizList);
        quizzesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        quizzesRV.setAdapter(adapter);
    }
    private void getQuizzes(){
        database.collection("quizzes")
                .whereEqualTo("poster", userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                if(document.exists()){
                                    Quiz quiz = new Quiz();
                                    quiz.setId(document.getId());
                                    quiz.setPoster(document.get("poster").toString());
                                    quiz.setDescription(document.get("description").toString());
                                    quiz.setCorrect(document.get("correct").toString());
                                    quiz.setWrong0(document.get("wrong0").toString());
                                    quiz.setWrong1(document.get("wrong1").toString());
                                    quiz.setCorrectCount((Long) document.get("correct-count"));
                                    quiz.setWrongCount((Long) document.get("wrong-count"));
                                    quiz.setLikesCount((Long) document.get("likes-count"));
                                    quiz.setDislikesCount((Long) document.get("dislikes-count"));
                                    quiz.setTime(document.get("time").toString());
                                    quiz.setInterestsIncluded((ArrayList<String>) document.get("interests"));
                                    quiz.setCorrectIndex((Long) document.get("correct-index"));
                                    quiz.setLikesUsers((ArrayList<String>) document.get("likes-users"));
                                    quiz.setDislikesUsers((ArrayList<String>) document.get("dislikes-users"));
                                    quiz.setAnswersUsers((ArrayList<String>) document.get("answers-users"));

                                    quizList.add(quiz);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(getApplicationContext(),"Error getting documents: "+ task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void setActionBarTitle(String title){
        Objects.requireNonNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(
                Html.fromHtml("<font color=\"#1976D2\"> "+title+" </font>")
        );
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), CoreActivity.class)
                .putExtra(StaticClass.TO, StaticClass.SETTINGS));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
