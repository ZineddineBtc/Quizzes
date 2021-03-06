package com.example.quizzes.activity.core.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import com.example.quizzes.activity.core.CreateQuizActivity;
import com.example.quizzes.adapter.TimelineAdapter;
import com.example.quizzes.model.Quiz;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class TimelineFragment extends Fragment {

    private View fragmentView;
    private RecyclerView timelineRV;
    private TimelineAdapter adapter;
    private ArrayList<Quiz> quizList = new ArrayList<>();
    private FirebaseFirestore database;
    private ArrayList<String> userInterests;
    private ProgressDialog progressDialog;
    private Context context;
    private SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_timeline, container, false);
        context = fragmentView.getContext();
        sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE);
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        database = FirebaseFirestore.getInstance();
        userInterests = new ArrayList<>(Objects.requireNonNull(context
                .getSharedPreferences(StaticClass.SHARED_PREFERENCES, MODE_PRIVATE)
                .getStringSet(StaticClass.INTERESTS, null)));
        findViewsByIds();
        getUserDocument();
        return fragmentView;
    }
    private void findViewsByIds(){
        timelineRV = fragmentView.findViewById(R.id.timelineRV);
        FloatingActionButton createFAB = fragmentView.findViewById(R.id.createFAB);
        createFAB.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(fragmentView.getContext(), CreateQuizActivity.class)); }});
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
        adapter = new TimelineAdapter(fragmentView.getContext(), quizList, userDocument);
        timelineRV.setLayoutManager(new LinearLayoutManager(fragmentView.getContext(), LinearLayoutManager.VERTICAL, false));
        timelineRV.setAdapter(adapter);
        getTimelineQuizzes();
    }
    private void getTimelineQuizzes(){
        database.collection("quizzes")
                .whereArrayContainsAny("interests", userInterests)
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
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            progressDialog.dismiss();
                        } else {
                            Toast.makeText(fragmentView.getContext(),
                                    "Error getting documents: "+ task.getException(),
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }
}