package com.stockholmsgator.stockholmsgator.Classes;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;

public class SaveLoadManager {

    private SharedPreferences sp;

    public SaveLoadManager(Context context) {
        sp = context.getSharedPreferences("savedList",Context.MODE_PRIVATE);
    }

    public ArrayList<String> loadSearchList(){
        ArrayList<String> searches = new ArrayList<>();
        String list = sp.getString("recentsList", "");
        if (!list.isEmpty()){
            String[] strings = list.split(";;");
            for(int i = 0; i < strings.length; i++){
                searches.add(strings[i]);
            }
        }

        return searches;
    }

    public void save(ArrayList<String> recentSearches){
        StringBuilder sb = new StringBuilder();
        for (String s : recentSearches){
            sb.append(s).append(";;");
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("recentsList",sb.toString());
        editor.apply();
    }
}
