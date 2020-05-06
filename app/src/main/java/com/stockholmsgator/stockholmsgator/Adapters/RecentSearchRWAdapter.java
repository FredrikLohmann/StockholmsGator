package com.stockholmsgator.stockholmsgator.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stockholmsgator.stockholmsgator.Activities.StreetActivity;
import com.stockholmsgator.stockholmsgator.Classes.WikipediaSearcher;
import com.stockholmsgator.stockholmsgator.R;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class RecentSearchRWAdapter extends RecyclerView.Adapter<RecentSearchRWAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private Activity activity;
    private Context context;
    private List<String> recentSearchList;

    public RecentSearchRWAdapter(Activity activity, List<String> recentSearchList){
        this.context = activity.getBaseContext();
        this.activity = activity;
        this.recentSearchList = recentSearchList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_recents, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int i) {
        Button streetName;

        streetName = holder.streetName;
        streetName.setText(recentSearchList.get(i));

        streetName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchOpenStreetActivityIntent(recentSearchList.get(i));
            }
        });
    }

    @Override
    public int getItemCount() {
        return recentSearchList.size();
    }

    private void dispatchOpenStreetActivityIntent(final String str) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WikipediaSearcher searcher = new WikipediaSearcher(str, "Mozilla/5.0");
                    String title = searcher.getTitle();
                    String streetInfo = searcher.getExtract();

                    Intent intent = new Intent(activity, StreetActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("streetInfo", streetInfo);

                    Bitmap bmp = searcher.getImage();
                    if (bmp != null)
                        intent.putExtra("image",getImageUri(bmp));
                    activity.startActivity(intent);
                }
                catch (Exception e){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Något gick fel", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public Button streetName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // initierar textvyerna
            streetName = itemView.findViewById(R.id.streetName);
        }
    }
}
