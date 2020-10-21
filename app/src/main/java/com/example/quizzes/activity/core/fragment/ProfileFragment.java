package com.example.quizzes.activity.core.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.activity.core.BookmarkActivity;
import com.example.quizzes.activity.core.MyQuizzesActivity;
import com.example.quizzes.activity.entry.LoginActivity;
import com.example.quizzes.adapter.CheckInterestsAdapter;
import com.example.quizzes.adapter.NetworkAdapter;
import com.example.quizzes.adapter.StringRVAdapter;
import com.example.quizzes.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private View fragmentView;
    private LinearLayout myQuizzesLL, bookmarkLL, networkCountLL,
            photoOptionsLL, viewPhotoLL, uploadPhotoLL, deletePhotoLL;
    public  static LinearLayout shadeLL, networkLL;
    private ImageView photoIV, editUsernameIV, editBioIV, editInterestsIV;
    public static ImageView photoFullScreenIV;
    private TextView usernameTV, bioTV, emailTV, scoreTV, signOutTV, errorTV,
                     followersCountTV, followingCountTV, emptyFollowersTV, emptyFollowingTV;
    private EditText usernameET, bioET;
    private RecyclerView userInterestsRV, allInterestsRV, followingRV, followersRV;
    private NetworkAdapter followersAdapter, followingAdapter;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String photoString, username, bio, email;
    private boolean editing, usernameEdited, bioEdited, networkSet;
    public  static boolean interestsEdited, networkShown,
            photoOptionsShown, photoFullScreen;
    private ArrayList<String> following, followers;
    private ArrayList<User> followingUsers = new ArrayList<>(),
                            followersUsers = new ArrayList<>();
    public  static ArrayList<String> userInterests;
    private HashSet<String> interestsSet;
    private Context context;
    private ProgressDialog progressDialog;
    private byte[] data;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);
        context = fragmentView.getContext();
        sharedPreferences = context.getSharedPreferences(StaticClass.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        setHasOptionsMenu(true);
        progressDialog = new ProgressDialog(context);
        findViewsByIds();
        setUserData();

        return fragmentView;
    }
    private void findViewsByIds(){
        photoFullScreenIV = fragmentView.findViewById(R.id.photoFullScreenIV);
        photoOptionsLL = fragmentView.findViewById(R.id.photoOptionsLL);
        viewPhotoLL = fragmentView.findViewById(R.id.viewPhotoLL);
        uploadPhotoLL = fragmentView.findViewById(R.id.uploadPhotoLL);
        deletePhotoLL = fragmentView.findViewById(R.id.deletePhotoLL);
        emptyFollowingTV = fragmentView.findViewById(R.id.emptyFollowingTV);
        emptyFollowersTV = fragmentView.findViewById(R.id.emptyFollowersTV);
        shadeLL = fragmentView.findViewById(R.id.shadeLL);
        networkLL = fragmentView.findViewById(R.id.networkLL);
        followersRV = fragmentView.findViewById(R.id.followersRV);
        followingRV = fragmentView.findViewById(R.id.followingRV);
        networkCountLL = fragmentView.findViewById(R.id.networkCountLL);
        followersCountTV = fragmentView.findViewById(R.id.followersCountTV);
        followingCountTV = fragmentView.findViewById(R.id.followingCountTV);
        photoIV = fragmentView.findViewById(R.id.photoIV);
        myQuizzesLL = fragmentView.findViewById(R.id.myQuizzesLL);
        bookmarkLL = fragmentView.findViewById(R.id.bookmarkLL);
        signOutTV = fragmentView.findViewById(R.id.signOutTV);
        errorTV = fragmentView.findViewById(R.id.errorTV);
        usernameTV = fragmentView.findViewById(R.id.usernameTV);
        usernameET = fragmentView.findViewById(R.id.usernameET);
        bioTV = fragmentView.findViewById(R.id.bioTV);
        bioET = fragmentView.findViewById(R.id.bioET);
        emailTV = fragmentView.findViewById(R.id.emailTV);
        scoreTV = fragmentView.findViewById(R.id.scoreTV);
        userInterestsRV = fragmentView.findViewById(R.id.userInterestsRV);
        allInterestsRV = fragmentView.findViewById(R.id.allInterestsRV);
        editInterestsIV = fragmentView.findViewById(R.id.editInterestsIV);
        editUsernameIV = fragmentView.findViewById(R.id.editUsernameIV);
        editBioIV = fragmentView.findViewById(R.id.editBioIV);
    }
    private void setUserData(){
        photoIV.setDrawingCacheEnabled(true);
        photoIV.buildDrawingCache();
        email = sharedPreferences.getString(StaticClass.EMAIL, "no email");
        getPhoto();
        username = sharedPreferences.getString(StaticClass.USERNAME, "no username");
        usernameTV.setText(username);
        usernameET.setText(username);
        bio = sharedPreferences.getString(StaticClass.BIO, "no bio");
        bioTV.setText(bio);
        bioET.setText(bio);
        emailTV.setText(email);
        long score = sharedPreferences.getLong(StaticClass.SCORE, 0);
        scoreTV.setText(String.valueOf(score));
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
        setListeners();
        getNetwork();
    }
    private void getPhoto(){
        final long ONE_MEGABYTE = 1024 * 1024;
        storage.getReference(email)
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                setBytesToPhoto(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();
            }
        });
    }
    private void setBytesToPhoto(byte[] bytes){
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        photoIV.setImageBitmap(Bitmap.createScaledBitmap(bmp, photoIV.getWidth(),
                photoIV.getHeight(), false));
    }
    private void setListeners(){
        photoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoOptions();
            }
        });
        usernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameEdited = true;
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }@Override public void afterTextChanged(Editable s) {}
        });
        bioET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bioEdited = true;
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }@Override public void afterTextChanged(Editable s) {}
        });
        myQuizzesLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(fragmentView.getContext(), MyQuizzesActivity.class));
            }
        });
        bookmarkLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(fragmentView.getContext(), BookmarkActivity.class));
            }
        });
        networkCountLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNetwork();
            }
        });
    }
    private void getNetwork(){
        database.collection("users")
                .document(email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            followersCountTV.setText(String.valueOf(document.get("followers-count")));
                            followingCountTV.setText(String.valueOf(document.get("following-count")));
                        }
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
    private void photoOptions(){
        if (!(networkShown || photoOptionsShown || editing)){
            shadeLL.setVisibility(View.VISIBLE);
            photoOptionsLL.setVisibility(View.VISIBLE);
            photoOptionsShown = true;
            viewPhotoLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPhoto();
                }
            });
            uploadPhotoLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    importImage();
                }
            });
            deletePhotoLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletePhoto();
                }
            });
        }else{
            Toast.makeText(context, "Pending Actions",
                    Toast.LENGTH_LONG).show();
        }
    }
    private void viewPhoto(){
        if(photoIV.getDrawable() != context.getDrawable(R.drawable.ic_account_circle_grey)) {
            photoOptionsLL.setVisibility(View.GONE);
            photoFullScreenIV.setImageDrawable(photoIV.getDrawable());
            photoFullScreenIV.setVisibility(View.VISIBLE);
            photoFullScreen = true;
        }
    }
    private void importImage(){
        Intent intent;
        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select Images"),
                StaticClass.PICK_SINGLE_IMAGE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StaticClass.PICK_SINGLE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = data.getData();
            if(uri != null){
                final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                ContentResolver resolver = context.getContentResolver();
                resolver.takePersistableUriPermission(uri, takeFlags);

                Bitmap imageBitmap = null;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(
                            context.getContentResolver(), uri);
                } catch (IOException e) {
                    Toast.makeText(context, "IO Exception when selecting a profile image",
                            Toast.LENGTH_LONG).show();
                }
                photoIV.setImageBitmap(imageBitmap);
                uploadPhoto();
                photoString = uri.toString();
            }
        }
    }
    private byte[] getPhotoData(){
        Bitmap bitmap = ((BitmapDrawable) photoIV.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
    private void newPhoto(){
        storage.getReference().child(email)
                .putBytes(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show();
                        shadeLL.setVisibility(View.GONE);
                        photoOptionsLL.setVisibility(View.GONE);
                        photoOptionsShown = false;
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(context, "Uploaded!", Toast.LENGTH_LONG).show();
                shadeLL.setVisibility(View.GONE);
                photoOptionsLL.setVisibility(View.GONE);
                photoOptionsShown = false;
            }
        });
    }
    private void changePhoto(){
        storage.getReference().child(email)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        newPhoto();
                    }
        });
    }
    private void uploadPhoto(){
        data = getPhotoData();
        progressDialog.setMessage("Uploading");
        progressDialog.show();
        if(photoIV.getDrawable()!=context.getDrawable(R.drawable.ic_account_circle_grey)) {
            changePhoto();
        }else{
            newPhoto();
        }
    }
    private void deletePhoto(){
        progressDialog.setMessage("Deleting");
        progressDialog.show();
        storage.getReference().child(email)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                Toast.makeText(context, "Uploaded!", Toast.LENGTH_LONG).show();
                shadeLL.setVisibility(View.GONE);
                photoOptionsLL.setVisibility(View.GONE);
                photoIV.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle_grey));
                photoOptionsShown = false;
            }
        });
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
    private void showNetwork(){
        if(!networkSet){
            getNetworkLists();
        }
        shadeLL.setVisibility(View.VISIBLE);
        networkLL.setVisibility(View.VISIBLE);
        networkShown = true;
    }
    private void getNetworkLists(){
        database.collection("users")
                .document(email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            followers = (ArrayList<String>) document.get("followers");
                            following = (ArrayList<String>) document.get("following");
                            setNetworkRVs();
                        }
                    }
                });
    }
    private void setNetworkRVs(){
        getFollowers();
        followersAdapter = new NetworkAdapter(context, followersUsers, email);
        followersRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        followersRV.setAdapter(followersAdapter);
        getFollowing();
        followingAdapter = new NetworkAdapter(context, followingUsers, email);
        followingRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        followingRV.setAdapter(followingAdapter);
        networkSet = true;
        checkEmptyLists();
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
    private void checkEmptyLists(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(followers.isEmpty()){
                    emptyFollowersTV.setVisibility(View.VISIBLE);
                }
                if(following.isEmpty()){
                    emptyFollowingTV.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);
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