package com.example.quizzes.activity.core;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.adapter.NetworkAdapter;
import com.example.quizzes.adapter.TimelineAdapter;
import com.example.quizzes.model.Quiz;
import com.example.quizzes.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    LinearLayout shadeLL, networkLL;
    ImageView photoIV;
    TextView usernameTV, bioTV, followersCount, followingCount, scoreTV;
    Button followButton, unfollowButton;
    RecyclerView quizzesRV, followersRV, followingRV;
    TimelineAdapter adapter;
    NetworkAdapter followersAdapter, followingAdapter;
    ArrayList<Quiz> quizList = new ArrayList<>();
    ArrayList<String> followers, following;
    ArrayList<User> followersUsers = new ArrayList<>(),
                    followingUsers = new ArrayList<>();
    FirebaseFirestore database;
    SharedPreferences sharedPreferences;
    ProgressBar progressBar;
    String profileID, userID, backToID;
    boolean networkShown, networkSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViewsByIds();
        backToID = getIntent().getStringExtra(StaticClass.BACK_TO_ID);
        database = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        userID = getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE).getString(StaticClass.EMAIL, " ");
        profileID = getIntent().getStringExtra(StaticClass.PROFILE_ID);
        setProfileUI(profileID);
        getUserDocument();
    }
    private void findViewsByIds(){
        photoIV = findViewById(R.id.photoIV);
        usernameTV = findViewById(R.id.usernameTV);
        bioTV = findViewById(R.id.bioTV);
        followersCount = findViewById(R.id.followersCountTV);
        followingCount = findViewById(R.id.followingCountTV);
        scoreTV = findViewById(R.id.scoreTV);
        followButton = findViewById(R.id.followButton);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follow();
            }
        });
        unfollowButton = findViewById(R.id.unfollowButton);
        unfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unfollow();
            }
        });
        quizzesRV = findViewById(R.id.profileQuizzesRV);
        progressBar = findViewById(R.id.progressBar);
        shadeLL = findViewById(R.id.shadeLL);
        networkLL = findViewById(R.id.networkLL);
        followersRV = findViewById(R.id.followersRV);
        followingRV = findViewById(R.id.followingRV);
    }
    private void setProfileUI(final String userID){
        database.collection("users")
                .document(userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                String username = String.valueOf(document.get("username"));
                                usernameTV.setText(username);
                                setActionBarTitle(username);
                                bioTV.setText(String.valueOf(document.get("bio")));
                                scoreTV.setText(String.valueOf(document.get("score")));
                                followersCount.setText(String.valueOf(document.get("followers-count")));
                                followingCount.setText(String.valueOf(document.get("following-count")));
                                followers = (ArrayList<String>) document.get("followers");
                                setFollower();
                                following = (ArrayList<String>) document.get("following");
                                setFollowingUser();
                            }
                        }
                    }
                });
    }
    private void setFollower(){
        if(followers != null) {
            if (followers.contains(userID)) {
                followButton.setVisibility(View.GONE);
                unfollowButton.setVisibility(View.VISIBLE);
            }
        }
    }
    private void setFollowingUser(){
        if (following != null) {
            if (following.contains(userID)) {
                followButton.setText(R.string.follow_back);
            }
        }
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
                                setRecyclerView(document);
                            }
                        }
                    }
                });
    }
    private void setRecyclerView(DocumentSnapshot userDocument){
        adapter = new TimelineAdapter(getApplicationContext(), quizList, userDocument);
        quizzesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        quizzesRV.setAdapter(adapter);
        getQuizzes();
    }
    private void getQuizzes(){
        database.collection("quizzes")
                .whereEqualTo("poster", profileID)
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
                                    quiz.setUserDefinedHardness((Long) document.get("hardness-user-defined"));
                                    quiz.setPosterDefinedHardness((Long) document.get("hardness-poster-defined"));
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
    private void follow(){
        database.collection("users")
                .document(profileID)
                .update("followers", FieldValue.arrayUnion(userID))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        incrementFollowersCount();
                    }
                });
        followButton.setVisibility(View.GONE);
        unfollowButton.setVisibility(View.VISIBLE);
    }
    private void incrementFollowersCount(){
        int count = Integer.valueOf(followersCount.getText().toString()) + 1;
        followersCount.setText(String.valueOf(count));
        database.collection("users")
                .document(profileID)
                .update("followers-count", FieldValue.increment(1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        addUserFollowing();
                    }
                });
    }
    private void addUserFollowing(){
        database.collection("users")
                .document(userID)
                .update("following", FieldValue.arrayUnion(profileID))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        database.collection("users")
                                .document(userID)
                                .update("following-count", FieldValue.increment(1));
                    }
                });
    }
    private void unfollow(){
        database.collection("users")
                .document(profileID)
                .update("followers", FieldValue.arrayRemove(userID))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        decrementFollowersCount();
                    }
                });
        followButton.setVisibility(View.VISIBLE);
        unfollowButton.setVisibility(View.GONE);
    }
    private void decrementFollowersCount(){
        int count = Integer.valueOf(followersCount.getText().toString()) - 1;
        followersCount.setText(String.valueOf(count));
        database.collection("users")
                .document(profileID)
                .update("followers-count", FieldValue.increment(-1))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        removeUserFollowing();
                    }
                });
    }
    private void removeUserFollowing(){
        database.collection("users")
                .document(userID)
                .update("following", FieldValue.arrayRemove(profileID))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        database.collection("users")
                                .document(userID)
                                .update("following-count", FieldValue.increment(-1));
                    }
                });
    }
    public void showNetwork(View view){
        if(!networkSet){
            setNetworkRVs();
        }
        shadeLL.setVisibility(View.VISIBLE);
        networkLL.setVisibility(View.VISIBLE);
        networkShown = true;
    }
    private void setNetworkRVs(){
        getFollowers();
        Toast.makeText(getApplicationContext(), String.valueOf(followersUsers.size()), Toast.LENGTH_LONG).show();
        followersAdapter = new NetworkAdapter(getApplicationContext(), followersUsers, profileID);
        followersRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        followersRV.setAdapter(followersAdapter);
        getFollowing();
        followingAdapter = new NetworkAdapter(getApplicationContext(), followingUsers, profileID);
        followingRV.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        followingRV.setAdapter(followingAdapter);
        networkSet = true;
    }
    private void getFollowers(){
        for (String s: followers){
            database.collection("users")
                    .document(s)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){
                                    User user = new User();
                                    user.setId(document.getId());
                                    user.setUsername(String.valueOf(document.get("username")));
                                    followersUsers.add(user);
                                    followersAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
    }
    private void getFollowing(){
        for (String s: following){
            database.collection("users")
                    .document(s)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){
                                    User user = new User();
                                    user.setId(document.getId());
                                    user.setUsername(String.valueOf(document.get("username")));
                                    followingUsers.add(user);
                                    followingAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
        }
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
        if (!networkShown) {
            if(backToID != null){
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class)
                        .putExtra(StaticClass.BACK_TO_ID, backToID));
            }else {
                startActivity(new Intent(getApplicationContext(), CoreActivity.class)
                        .putExtra(StaticClass.TO, StaticClass.TIMELINE));
            }
        }else{
            shadeLL.setVisibility(View.GONE);
            networkLL.setVisibility(View.GONE);
            networkShown = false;
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
