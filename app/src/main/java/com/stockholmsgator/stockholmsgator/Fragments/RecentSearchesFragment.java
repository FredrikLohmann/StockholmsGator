package com.stockholmsgator.stockholmsgator.Fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stockholmsgator.stockholmsgator.Activities.MainActivity;
import com.stockholmsgator.stockholmsgator.Adapters.RecentSearchRWAdapter;
import com.stockholmsgator.stockholmsgator.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecentSearchesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecentSearchesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecentSearchesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;

    public RecentSearchesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecentSearchesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecentSearchesFragment newInstance(String param1, String param2) {
        RecentSearchesFragment fragment = new RecentSearchesFragment();
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
        View view = inflater.inflate(R.layout.fragment_recent_searches, container, false);
        initComponents(view);
        return view;
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

    private void initComponents(View view) {
        recyclerView = view.findViewById(R.id.recentsList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecentSearchRWAdapter adapter = new RecentSearchRWAdapter(getActivity(), getRecentSearchesList());
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> getRecentSearchesList(){
        return ((MainActivity)getActivity()).recentSearches.getRecentSearches();
    }
}
