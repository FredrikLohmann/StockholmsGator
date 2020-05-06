package com.stockholmsgator.stockholmsgator.Classes;

import android.content.Context;

import java.util.ArrayList;

public class RecentSearchManager {

    private static SaveLoadManager slm;
    private  ArrayList<String> recentSearches = new ArrayList<>();
    public RecentSearchManager(Context context) {
        slm = new SaveLoadManager(context);
        for (String s: slm.loadSearchList()) {
            addLast(s);
        }
    }

    private void addLast(String item){
        recentSearches.remove(item);
        recentSearches.add(item);

        if(recentSearches.size() > 20)
            recentSearches.remove(recentSearches.size()-1);
    }

    public void add(String item){
        recentSearches.remove(item);
        recentSearches.add(0,item);

        if(recentSearches.size() > 20)
            recentSearches.remove(recentSearches.size()-1);
        saveSearchList();
    }

    public ArrayList<String> getRecentSearches(){
        return recentSearches;
    }
    public void saveSearchList(){
        if (slm != null)
            slm.save(recentSearches);
    }
}
