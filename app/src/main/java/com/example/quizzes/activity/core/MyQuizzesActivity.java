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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.adapter.MyQuizzesAdapter;
import com.example.quizzes.model.Quiz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MyQuizzesActivity extends AppCompatActivity {

    private FirebaseFirestore database;
    private ArrayList<Quiz> quizzes = new ArrayList<>();
    private MyQuizzesAdapter adapter;
    private ProgressDialog progressDialog;
    public static LinearLayout shadeLL, moreLL;
    public static TextView deleteTV, cancelTV, editTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_quizzes);
        setActionBarTitle("My Quizzes");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        database = FirebaseFirestore.getInstance();
        findViewsByIds();
        setRecyclerView();
        getMyQuizzes();
    }
    private void findViewsByIds(){
        shadeLL = findViewById(R.id.shadeLL);
        moreLL = findViewById(R.id.moreLL);
        deleteTV = findViewById(R.id.deleteTV);
        cancelTV = findViewById(R.id.cancelTV);
        editTV = findViewById(R.id.editTV);
    }
    private void getMyQuizzes(){
        database.collection("quizzes")
                .whereEqualTo("poster",
                        getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE)
                                .getString(StaticClass.EMAIL, " "))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                if(document.exists()){
                                    Quiz quiz = new Quiz();
                                    quiz.setId(document.getId());
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
                                    quiz.setUserDefinedHardness((Long) document.get("hardness-user-defined"));
                                    quiz.setPosterDefinedHardness((Long) document.get("hardness-poster-defined"));
                                    quizzes.add(quiz);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(),"Error getting documents: "+ task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }
    private void setRecyclerView(){
        adapter = new MyQuizzesAdapter(getApplicationContext(), quizzes);
        RecyclerView myQuizzesRV = findViewById(R.id.myQuizzesRV);
        myQuizzesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        myQuizzesRV.setAdapter(adapter);
    }
    public static void setMoreVisibility(boolean visibility){
        shadeLL.setVisibility(visibility ? View.VISIBLE : View.GONE);
        moreLL.setVisibility(visibility ? View.VISIBLE : View.GONE);
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
        if(shadeLL.getVisibility()== View.VISIBLE){
            setMoreVisibility(false);
        }else{
            startActivity(new Intent(getApplicationContext(), CoreActivity.class)
                    .putExtra(StaticClass.TO, StaticClass.PROFILE_FRAGMENT));
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
