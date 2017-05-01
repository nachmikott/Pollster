package edu.umd.cs.pollsternav;

/**
 * Created by Creed on 4/24/2017.
 */

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



/*  Currently the login system just stores in memory (not a real login system).

    We may want to make this either SQLite or a real database using a REST API and some cloud server
    like Heroku or AWS or something.
*/

public class NewPostFragment extends Fragment {

    Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);
        activity = getActivity();

        return view;
    }

    public static NewPostFragment newInstance() {
        return new NewPostFragment();
    }

}
