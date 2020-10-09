package com.example.quizzes.activity.core.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.activity.core.MyQuizzesActivity;
import com.example.quizzes.activity.entry.LoginActivity;
import com.example.quizzes.adapter.CheckInterestsAdapter;
import com.example.quizzes.adapter.StringRVAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private View fragmentView;
    private LinearLayout myQuizzesLL;
    private ImageView photoIV, editUsernameIV, editBioIV, editInterestsIV;
    private TextView usernameTV, bioTV, emailTV, signOutTV, errorTV;
    private EditText usernameET, bioET;
    private RecyclerView userInterestsRV, allInterestsRV;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseFirestore database;
    private String username, bio, email;
    private boolean editing;
    private boolean usernameEdited, bioEdited;
    public static boolean interestsEdited;
    public static ArrayList<String> userInterests;
    private HashSet<String> interestsSet;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        sharedPreferences = fragmentView.getContext().getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        database = FirebaseFirestore.getInstance();
        setHasOptionsMenu(true);
        findViewsByIds();
        setUserData();
        return fragmentView;
    }
    private void findViewsByIds(){
        photoIV = fragmentView.findViewById(R.id.photoIV);
        myQuizzesLL = fragmentView.findViewById(R.id.myQuizzesLL);
        signOutTV = fragmentView.findViewById(R.id.signOutTV);
        errorTV = fragmentView.findViewById(R.id.errorTV);
        usernameTV = fragmentView.findViewById(R.id.usernameTV);
        usernameET = fragmentView.findViewById(R.id.usernameET);
        bioTV = fragmentView.findViewById(R.id.bioTV);
        bioET = fragmentView.findViewById(R.id.bioET);
        emailTV = fragmentView.findViewById(R.id.emailTV);
        userInterestsRV = fragmentView.findViewById(R.id.userInterestsRV);
        allInterestsRV = fragmentView.findViewById(R.id.allInterestsRV);
        editInterestsIV = fragmentView.findViewById(R.id.editInterestsIV);
        editUsernameIV = fragmentView.findViewById(R.id.editUsernameIV);
        editBioIV = fragmentView.findViewById(R.id.editBioIV);
    }
    private void setUserData(){
        username = sharedPreferences.getString(StaticClass.USERNAME, "no username");
        usernameTV.setText(username);
        usernameET.setText(username);
        usernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameEdited = true;
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }@Override public void afterTextChanged(Editable s) {}
        });
        bio = sharedPreferences.getString(StaticClass.BIO, "no bio");
        bioTV.setText(bio);
        bioET.setText(bio);
        bioET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bioEdited = true;
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }@Override public void afterTextChanged(Editable s) {}
        });
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
        emailTV.setText(email);
        setRecyclerViews();
        signOutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        editUsernameIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUsername();
            }
        });
        editBioIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBio();
            }
        });
        editInterestsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editInterests();
            }
        });
        myQuizzesLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(fragmentView.getContext(), MyQuizzesActivity.class));
            }
        });
    }
    private void setRecyclerViews(){
        userInterests = new ArrayList<>(sharedPreferences.getStringSet(StaticClass.INTERESTS, new HashSet<String>()));
        CheckInterestsAdapter allInterestsAdapter = new CheckInterestsAdapter(fragmentView.getContext(),
                StaticClass.allInterests, userInterests);
        allInterestsRV.setLayoutManager(new LinearLayoutManager(fragmentView.getContext(), LinearLayoutManager.VERTICAL, false));
        allInterestsRV.setAdapter(allInterestsAdapter);
        StringRVAdapter userInterestsAdapter = new StringRVAdapter(fragmentView.getContext(), userInterests);
        userInterestsRV.setLayoutManager(new LinearLayoutManager(fragmentView.getContext(), LinearLayoutManager.VERTICAL, false));
        userInterestsRV.setAdapter(userInterestsAdapter);
    }
    private void editUsername(){
        if(editing){
            if(usernameET.getText().toString().length()>4) {
                if (usernameEdited && !username.equals(usernameET.getText().toString())) {
                    checkUsername();
                } else {
                    toggleUsername(false);
                }
            }else{
                displayErrorTV(R.string.insufficient_username);
            }
        }else{
            toggleUsername(true);
        }
    }
    private void toggleUsername(boolean toEdit){
        if(toEdit){
            usernameTV.setVisibility(View.GONE);
            usernameET.setVisibility(View.VISIBLE);
            usernameET.requestFocus();
            editUsernameIV.setImageDrawable(fragmentView.getContext()
                    .getDrawable(R.drawable.ic_check));
            editing = true;
        }else{
            usernameTV.setVisibility(View.VISIBLE);
            usernameET.setVisibility(View.GONE);
            editUsernameIV.setImageDrawable(fragmentView.getContext()
                    .getDrawable(R.drawable.ic_edit));
            editing = false;
        }
    }
    private void checkUsername(){
        database.collection("app-data")
                .document("usernames").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> usernames = (ArrayList<String>) document.get("usernames");
                        if(usernames.contains(usernameET.getText().toString().trim())){
                            displayErrorTV(R.string.username_taken);
                        }else{
                            adjustUsernameList(username, usernameET.getText().toString().trim());
                            username = usernameET.getText().toString().trim();
                            writeUsername();
                        }
                    }
                } else {
                    Toast.makeText(fragmentView.getContext(),
                            "get failed with " + task.getException(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void adjustUsernameList(String oldUsername, String newUsername){
        database.collection("app-data")
                .document("usernames")
                .update("usernames", FieldValue.arrayRemove(oldUsername),
                        "usernames", FieldValue.arrayUnion(newUsername));
    }
    private void writeUsername(){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("username", username);
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        editor.putString(StaticClass.USERNAME, username);
                        editor.apply();
                        toggleUsername(false);
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "Username updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing user",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void editBio(){
        if(editing){
            if (bioEdited && !bio.equals(bioET.getText().toString())) {
                bio = bioET.getText().toString();
                writeBio();
            } else {
                toggleBio(false);
            }
        }else{
            toggleBio(true);
        }
    }
    private void toggleBio(boolean toEdit){
        if(toEdit){
            bioTV.setVisibility(View.GONE);
            bioET.setVisibility(View.VISIBLE);
            bioET.requestFocus();
            editBioIV.setImageDrawable(fragmentView.getContext()
                    .getDrawable(R.drawable.ic_check));
            editing = true;
        }else{
            bioTV.setVisibility(View.VISIBLE);
            bioET.setVisibility(View.GONE);
            editBioIV.setImageDrawable(fragmentView.getContext()
                    .getDrawable(R.drawable.ic_edit));
            editing = false;
        }
    }
    private void writeBio(){
        Map<String, Object> userReference = new HashMap<>();
        userReference.put("bio", bio);
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        editor.putString(StaticClass.BIO, bio);
                        editor.apply();
                        toggleBio(false);
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "Bio updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing user",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void editInterests(){
        if(editing){
            if(!userInterests.isEmpty()){
                if(interestsEdited){
                    writeInterests();
                }else{
                    toggleInterests(false);
                }
            }else{
                displayErrorTV(R.string.unspecified_interests);
            }
        }else{
            toggleInterests(true);
        }
    }
    private void toggleInterests(boolean toEdit){
        if(toEdit){
            userInterestsRV.setVisibility(View.GONE);
            allInterestsRV.setVisibility(View.VISIBLE);
            editInterestsIV.setImageDrawable(fragmentView.getContext()
                    .getDrawable(R.drawable.ic_check));
            editing = true;
        }else{
            userInterestsRV.setVisibility(View.VISIBLE);
            allInterestsRV.setVisibility(View.GONE);
            editInterestsIV.setImageDrawable(fragmentView.getContext()
                    .getDrawable(R.drawable.ic_edit));
            editing = false;
        }
    }
    private void writeInterests(){
        Map<String, Object> userReference = new HashMap<>();
        interestsSet = new HashSet<>(userInterests);
        userReference.put("interests", new ArrayList<>(interestsSet));
        database.collection("users")
                .document(email)
                .update(userReference)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        editor.putStringSet(StaticClass.INTERESTS, interestsSet);
                        editor.apply();
                        toggleInterests(false);
                        setUserData();
                        Snackbar.make(fragmentView.findViewById(R.id.parentLayout),
                                "Interests updated", 1000)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(fragmentView.getContext(),
                                "Error writing user",
                                Toast.LENGTH_SHORT).show();
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
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(fragmentView.getContext(), LoginActivity.class));
    }
}