package edu.umd.cs.pollsternav;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MyVotesActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return MyVotesFragment.newInstance();
    }
}
