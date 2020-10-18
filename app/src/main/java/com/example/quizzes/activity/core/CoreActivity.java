package com.example.quizzes.activity.core;

import android.os.Bundle;
import android.view.View;

import com.example.quizzes.R;
import com.example.quizzes.StaticClass;
import com.example.quizzes.activity.core.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class CoreActivity extends AppCompatActivity {

    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_timeline, R.id.navigation_following, R.id.navigation_profile)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        if(getIntent().getStringExtra(StaticClass.TO) != null) {
            if(getIntent().getStringExtra(StaticClass.TO)
                    .equals(StaticClass.PROFILE_FRAGMENT)) {
                navView.setSelectedItemId(R.id.navigation_profile);
            }else if(getIntent().getStringExtra(StaticClass.TO)
                    .equals(StaticClass.TIMELINE)) {
                navView.setSelectedItemId(R.id.navigation_timeline);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(navView.getSelectedItemId()==R.id.navigation_profile){
            if(ProfileFragment.networkShown){
                ProfileFragment.networkLL.setVisibility(View.GONE);
                ProfileFragment.shadeLL.setVisibility(View.GONE);
                ProfileFragment.networkShown = false;
            }
        }else{
            moveTaskToBack(true);
        }

    }
}
