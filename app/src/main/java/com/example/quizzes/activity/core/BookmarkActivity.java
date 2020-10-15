package com.example.quizzes.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.adapter.TimelineAdapter;
import com.example.quizzes.model.Quiz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class BookmarkActivity extends AppCompatActivity {

    TextView emptyListTV;
    FirebaseFirestore database;
    SharedPreferences sharedPreferences;
    ArrayList<String> quizzesReferences = new ArrayList<>();
    ArrayList<Quiz> bookmarkQuizzes = new ArrayList<>();
    RecyclerView bookmarkQuizzesRV;
    TimelineAdapter adapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        setActionBarTitle("Bookmark");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        database = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        getUserDocument();
        getBookmark();
    }
    private void getUserDocument(){
        database.collection("users")
                .document(sharedPreferences.getString(StaticClass.EMAIL, " "))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                setViews(document);
                            }
                        }
                    }
                });
    }
    private void setViews(DocumentSnapshot userDocument){
        emptyListTV = findViewById(R.id.emptyListTV);
        adapter = new TimelineAdapter(getApplicationContext(), bookmarkQuizzes, userDocument);
        bookmarkQuizzesRV = findViewById(R.id.bookmarkQuizzesRV);
        bookmarkQuizzesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        bookmarkQuizzesRV.setAdapter(adapter);
    }
    private void getBookmark(){
        database.collection("users")
                .document(sharedPreferences.getString(StaticClass.EMAIL, " "))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                quizzesReferences = (ArrayList<String>)
                                        document.get("bookmark");
                                if(quizzesReferences.isEmpty()){
                                    emptyListTV.setVisibility(View.VISIBLE);
                                    progressDialog.dismiss();
                                }else {
                                    getQuizzes();
                                }
                            }
                        }
                    }
                });
    }
    private void getQuizzes() {
        for(String documentReference: quizzesReferences){
            database.collection("quizzes")
                    .document(documentReference)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){
                                    Quiz quiz = new Quiz();
                                    quiz.setId(document.getId());
                                    quiz.setPoster(String.valueOf(document.get("poster")));
                                    quiz.setDescription(String.valueOf(document.get("description")));
                                    quiz.setCorrect(String.valueOf(document.get("correct")));
                                    quiz.setWrong0(String.valueOf(document.get("wrong0")));
                                    quiz.setWrong1(String.valueOf(document.get("wrong1")));
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
                                    quiz.setUserDefinedHardness((Long) document.get("hardness-user-defined"));
                                    quiz.setPosterDefinedHardness((Long) document.get("hardness-poster-defined"));
                                    bookmarkQuizzes.add(quiz);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
        progressDialog.dismiss();
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
        startActivity(new Intent(getApplicationContext(), CoreActivity.class)
                .putExtra(StaticClass.TO, StaticClass.PROFILE_FRAGMENT));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
