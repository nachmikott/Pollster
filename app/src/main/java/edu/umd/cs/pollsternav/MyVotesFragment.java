package edu.umd.cs.pollsternav;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MyVotesFragment extends Fragment {

    private TextView vote_stats_a;
    private TextView vote_stats_b;
    private TextView vote_stats_c;
    private TextView vote_stats_d;

    public static CategoriesFragment newInstance() {
        CategoriesFragment fragment = new CategoriesFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        vote_stats_a = (TextView) view.findViewById(R.id.votes_stats_label_a);
        vote_stats_b = (TextView) view.findViewById(R.id.votes_stats_label_b);
        vote_stats_c = (TextView) view.findViewById(R.id.votes_stats_label_c);
        vote_stats_d = (TextView) view.findViewById(R.id.votes_stats_label_d);

        return view;
    }


    //takes in a,b,c, or d to know which label to change
    //percent is the stat to set in that textview
    public void setVotesLabel(char label, int percent){

        if (label == 'a'){
            vote_stats_a.setText("Option A\n"+percent+"%");
        }
        else if (label == 'b'){
            vote_stats_a.setText("Option B\n"+percent+"%");
        }
        else if (label == 'c'){
            vote_stats_a.setText("Option C\n"+percent+"%");
        }
        else if (label == 'd'){
            vote_stats_a.setText("Option D\n"+percent+"%");
        }
        else{
            vote_stats_a.setText("Incorrect label letter passed in");
        }

    }


}
