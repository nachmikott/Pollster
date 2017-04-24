package edu.umd.cs.pollsternav;

import android.os.Bundle;

/**
 * Created by Creed on 4/24/2017.
 */

/*This is the entry point for the application. Make sure the android manifest*/

public class LoginActivity extends SingleFragmentActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public LoginFragment createFragment() {
        return LoginFragment.newInstance();
    }
}
