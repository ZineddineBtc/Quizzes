package com.example.quizzes.activity.core.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.adapter.TimelineAdapter;
import com.example.quizzes.model.Quiz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class FollowingFragment extends Fragment {

    private View fragmentView;
    private TimelineAdapter adapter;
    private ArrayList<String> following, quizzesIDs;
    private ArrayList<Quiz> quizList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private FirebaseFirestore database;
    private SharedPreferences sharedPreferences;
    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_following, container, false);
        context = fragmentView.getContext();
        sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        database = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(context);
        //progressDialog.setMessage("Loading...");
        //progressDialog.show();
        getUserDocument();
        return fragmentView;
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
                                following = (ArrayList<String>) document.get("following");
                                setRecyclerView(document);
                            }
                        }
                    }
                });
    }
    private void setRecyclerView(DocumentSnapshot userDocument){
        adapter = new TimelineAdapter(fragmentView.getContext(), quizList, userDocument);
        RecyclerView followingRV = fragmentView.findViewById(R.id.followingRV);
        followingRV.setLayoutManager(new LinearLayoutManager(fragmentView.getContext(), LinearLayoutManager.VERTICAL, false));
        followingRV.setAdapter(adapter);
        getQuizzesIDs();
    }
    private void getQuizzesIDs(){
        if(following == null) return;
        for(String profile: following) {
            database.collection("users")
                    .document(profile)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()) {
                                    quizzesIDs = (ArrayList<String>) document.get("quizzes");
                                    getQuizzesData();
                                }
                            }
                        }
                    });
        }
    }
    private void getQuizzesData(){
        for(String quizID: quizzesIDs){
            database.collection("quizzes")
                    .document(quizID)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot document) {
                            if(document.exists()) {
                                appendQuiz(document);
                            }
                        }
            });
        }
    }
    private void appendQuiz(DocumentSnapshot document){
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
        adapter.notifyDataSetChanged();
    }
}