package com.stockholmsgator.stockholmsgator.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stockholmsgator.stockholmsgator.R;

import java.io.File;

public class StreetActivity extends AppCompatActivity {

    TextView textView;
    TextView textView2;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street);

        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        imageView = findViewById(R.id.imageView);

        String streetInfo = getIntent().getStringExtra("streetInfo");
        String title = getIntent().getStringExtra("title");
        Uri image = getIntent().getExtras().getParcelable("image");

        if (streetInfo != null && !streetInfo.isEmpty()){
            textView.setText(android.text.Html.fromHtml(streetInfo));
        }
        if (title != null && !title.isEmpty()){
            textView2.setText(android.text.Html.fromHtml(title));
            if (title.equals("Portal:Huvudsida")){
                textView2.setText("Ingen träff");
            }
        }
        if(image != null){
            imageView.setImageURI(image);
            File f = new File(getFilePath(image));
            if (f.exists()){
                f.delete();
            }
        }
    }

    private String getFilePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null
            cursor.close();
            return picturePath;
        }
        return null;
    }
}
