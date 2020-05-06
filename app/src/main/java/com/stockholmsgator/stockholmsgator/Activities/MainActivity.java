package com.stockholmsgator.stockholmsgator.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.stockholmsgator.stockholmsgator.Classes.RecentSearchManager;
import com.stockholmsgator.stockholmsgator.Fragments.RecentSearchesFragment;
import com.stockholmsgator.stockholmsgator.R;
import com.stockholmsgator.stockholmsgator.Fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private Fragment searchFragment = new SearchFragment();
    private Fragment recentSearchFragment = new RecentSearchesFragment();
    public RecentSearchManager recentSearches;

    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
    }

    private void initComponents(){
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_search);
        recentSearches = new RecentSearchManager(this);
    }

    public void setBottomNavigationViewEnabled(boolean b){
        for (int i = 0; i<navigation.getMenu().size(); i++){
            navigation.getMenu().getItem(i).setEnabled(b);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_search:
                    fragment = searchFragment;
                    break;
                case R.id.navigation_list:
                    fragment = recentSearchFragment;
                    break;
            }
            setFragment(fragment);
            return true;
        }
    };

    private void setFragment(Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_container, fragment);
        ft.commit();
    }
}
