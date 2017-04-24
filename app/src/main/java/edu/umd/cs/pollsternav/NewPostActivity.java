package edu.umd.cs.pollsternav;

import android.os.Bundle;

/**
 * Created by Creed on 4/24/2017.
 */

public class NewPostActivity extends SingleFragmentActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public NewPostFragment createFragment() {
        return NewPostFragment.newInstance();
    }

}
