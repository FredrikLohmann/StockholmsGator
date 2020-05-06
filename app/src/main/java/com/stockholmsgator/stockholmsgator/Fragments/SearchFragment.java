package com.stockholmsgator.stockholmsgator.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.TextServicesManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.stockholmsgator.stockholmsgator.Activities.InformationActivity;
import com.stockholmsgator.stockholmsgator.Activities.MainActivity;
import com.stockholmsgator.stockholmsgator.Activities.CameraActivity;
import com.stockholmsgator.stockholmsgator.R;
import com.stockholmsgator.stockholmsgator.Activities.StreetActivity;
import com.stockholmsgator.stockholmsgator.Classes.WikipediaSearcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Provider;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int CAMERA_REQUEST = 1;
    private static final int CROP_REQUEST = 2;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 3;
    private static final int ACCESS_FINE_LOCATION_REQUEST = 4;

    private EditText editText;
    private Button searchBtn;
    private Button openCameraBtn;
    private Button positionBtn;
    private Button infoBtn;

    private LocationManager locationManager;
    private Location lastKnownLocation;
    private Switch testSwitch;

    private String currentPhotoPath = "";
    private String userAgent = "Mozilla/5.0";


    private int position =0; // Används endast för testning när positionsswitchen är avcheckad

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        initComponents(v);
        addListeners();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE_REQUEST);
            }
        }

        return v;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void initComponents(View v) {
        editText = v.findViewById(R.id.editText);
        searchBtn = v.findViewById(R.id.searchBtn);
        openCameraBtn = v.findViewById(R.id.cameraBtn);
        positionBtn = v.findViewById(R.id.positionBtn);
        infoBtn = v.findViewById(R.id.infoBtn);
        testSwitch = v.findViewById(R.id.testSwitch);
        testSwitch.setVisibility(View.INVISIBLE); // behövs ingen testswitch
        updateLastKnownLocation();
    }

    private void enableButtons(boolean b){
        editText.setEnabled(b);
        searchBtn.setEnabled(b);
        openCameraBtn.setEnabled(b);
        positionBtn.setEnabled(b);
        infoBtn.setEnabled(b);
        testSwitch.setEnabled(b);
        ((MainActivity)getActivity()).setBottomNavigationViewEnabled(b);
    }

    private void addListeners() {
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().isEmpty()){
                    dispatchOpenStreetActivityIntent(editText.getText().toString());
                } else{
                    Toast.makeText(getContext(), "Sökfältet är tomt", Toast.LENGTH_SHORT).show();
                }
            }
        });
        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(getContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                        dispatchTakePictureIntent();
                    }
                    else{
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                    }
                }
            }
        });
        positionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        setEditTextFromLocation();
                    }
                    else{
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_FINE_LOCATION_REQUEST);
                    }
                }
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent informationIntent = new Intent(getContext(), InformationActivity.class);
                startActivity(informationIntent);
            }
        });
    }

    private void dispatchTakePictureIntent(){
        Intent cameraIntent = new Intent(getContext(), CameraActivity.class);
        startActivityForResult(cameraIntent,CAMERA_REQUEST);
    }

    private void dispatchOpenStreetActivityIntent(final String str) {
        enableButtons(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WikipediaSearcher searcher = new WikipediaSearcher(str, userAgent);
                    String title = searcher.getTitle();
                    String streetInfo = searcher.getExtract();

                    Intent intent = new Intent(getContext(), StreetActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("streetInfo", streetInfo);

                    Bitmap bmp = searcher.getImage();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (bmp != null && getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                            intent.putExtra("image",getImageUri(bmp));
                    }
                    startActivity(intent);
                    ((MainActivity)getActivity()).recentSearches.add(title);
                }
                catch (Exception e){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Inga resultat funna", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }
                finally {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            enableButtons(true);
                        }
                    });
                }
            }
        });
        thread.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            switch (requestCode){
                case CAMERA_REQUEST:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                        dispatchTakePictureIntent();
                    }
                    break;

                case ACCESS_FINE_LOCATION_REQUEST:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                        setEditTextFromLocation();
                    }
                    break;

                case WRITE_EXTERNAL_STORAGE_REQUEST:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){

                    }
                    break;

            }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    String searchText = data.getStringExtra("searchText").split("\\R")[0];
                    if (searchText != null && !searchText.isEmpty() && !searchText.equals("Ingen text funnen")){
                        editText.setText(searchText);
                        dispatchOpenStreetActivityIntent(searchText);
                        Toast.makeText(getContext(), "Text upptäckt!", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(getContext(), "Kunde inte hitta någon text", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void setEditTextFromLocation(){
        if (lastKnownLocation == null){
            updateLastKnownLocation();
        }

        try {
            if (!testSwitch.isChecked()){
                if (locationManager.isLocationEnabled() && lastKnownLocation!= null){
                    editText.setText(getStreetNameFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                }
                else{
                    Toast.makeText(getContext(), "Platstjänster avstängt. Slå på platstjänster för denna funktion.", Toast.LENGTH_SHORT).show();
                }

                if (locationManager.isLocationEnabled() && lastKnownLocation == null){
                    Toast.makeText(getContext(), "Kan inte hitta din plats, vänta några sekunder och försök igen eller starta om applikationen.", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                String[] streets = {"Bellmansgatan", "Drottninggatan", "Mäster mikaels gata"};
                editText.setText(streets[position++%streets.length]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStreetNameFromLocation(double lat, double lng) throws IOException {
        String streetName = "Ingen gata funnen";
        Geocoder geoCoder = new Geocoder(getContext());
        List<Address> addresses = geoCoder.getFromLocation(lat, lng, 5);

        for (Address address: addresses){
            if(address.getThoroughfare() != null){
                return address.getThoroughfare();
            }
        }

        return streetName;
    }


    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void updateLastKnownLocation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null && locationManager.isLocationEnabled()){
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                }
            }
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastKnownLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
