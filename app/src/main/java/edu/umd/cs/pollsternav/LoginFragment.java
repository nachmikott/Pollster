package edu.umd.cs.pollsternav;

/**
 * Created by Creed on 4/24/2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;



/*  Currently the login system just stores in memory (not a real login system).

    We may want to make this either SQLite or a real database using a REST API and some cloud server
    like Heroku or AWS or something.
*/

public class LoginFragment extends Fragment {

    Activity activity;

    EditText user_et;
    EditText pass_et;

    Button login_button;
    Button signup_button;

    InputFilter inputFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        activity = getActivity();

        //Will use this to restrict Edit Texts to letters and numbers
        inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence cs, int start,
                                       int end, Spanned spanned, int dStart, int dEnd) {
                if (cs.equals("")) { // for backspace
                    return cs;
                }
                if (cs.toString().matches("[a-zA-Z0-9]+")) {
                    return cs;
                }
                return "";
            }
        };

        //Initialize the textfields and restrict their input to letters and numbers
        user_et = (EditText)view.findViewById(R.id.username_et);
        user_et.setFilters((new InputFilter[] { inputFilter }));
        pass_et = (EditText)view.findViewById(R.id.password_et);
        pass_et.setFilters((new InputFilter[] { inputFilter }));

        user_et.getBackground().setColorFilter(0xFFEFC270, PorterDuff.Mode.SRC_IN);
        pass_et.getBackground().setColorFilter(0xFFEFC270, PorterDuff.Mode.SRC_IN);

        //Click login, check if user and pass exist
        login_button = (Button)view.findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                click_login();
            }
        });
        login_button.getBackground().setColorFilter(0xFFEFC270, PorterDuff.Mode.MULTIPLY);


        //Click sign up, create a popup to make a new user/pass
        signup_button = (Button)view.findViewById(R.id.signup_button);
        signup_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                click_signup(0, "");
            }
        });
        signup_button.getBackground().setColorFilter(0xFF979AC6, PorterDuff.Mode.MULTIPLY);

        return view;
    }

    //Create an alert dialog where the user will enter their new username and password
    public void click_signup(int prompt_id, String prefillUser) {

        //Modify the message at the top of the dialog
        final String prompt_title;
        final String prompt_message;

        //TODO: Make these xml strings
        switch (prompt_id) {
            default: prompt_title = "Welcome to Pollster!";
                prompt_message = "Enter the following to create your account"; break;
            case 2: prompt_title = "Invalid entries";
                prompt_message = "Username and password must have at least 3 characters each"; break;
            case 3: prompt_title = "Passwords did not match";
                prompt_message = "Please re-enter your password"; break;
            case 4: prompt_title = "Username already exists";
                prompt_message = "Try another username"; break;

        }

        //Build the two text fields into the alert dialog
        LinearLayout fields_layout = new LinearLayout(activity);
        fields_layout.setOrientation(LinearLayout.VERTICAL);
        final EditText signup_user_et = new EditText(activity);
        signup_user_et.setFilters((new InputFilter[] { inputFilter }));
        signup_user_et.setHint("Username");
        fields_layout.addView(signup_user_et);

        //If the password was entered wrong, keep the username filled for convenience
        signup_user_et.setText(prefillUser);

        final EditText signup_password_et = new EditText(activity);
        signup_password_et.setHint("Password");
        signup_password_et.setFilters((new InputFilter[] { inputFilter }));
        fields_layout.addView(signup_password_et);
        signup_password_et.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        //If the password was entered wrong, put the focus back on the password field
        if (prompt_id == 3) {
            signup_password_et.requestFocus();
        }

        final EditText signup_password_confirm_et = new EditText(activity);
        signup_password_confirm_et.setHint("Confirm password");
        signup_password_confirm_et.setFilters((new InputFilter[] { inputFilter }));
        fields_layout.addView(signup_password_confirm_et);
        signup_password_confirm_et.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);

        //Always show the soft keyboard
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        //Show the alert dialog to the user
        new AlertDialog.Builder(activity)
                .setTitle(prompt_title)
                .setMessage(prompt_message)
                .setView(fields_layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String newUser = signup_user_et.getText().toString();
                        String newPass = signup_password_et.getText().toString();
                        String confirmPass = signup_password_confirm_et.getText().toString();

                        //Validate the username and password

                        if (newUser.length() < 3 || newPass.length() < 3) {
                            click_signup(2, "");
                            return;
                        }

                        if (!newPass.equals(confirmPass)) {
                            click_signup(3, newUser);
                            return;
                        }

                        //TODO: Check if the user is in the database
                        Boolean userAlreadyPresent = false;
                        if (userAlreadyPresent) {
                            click_signup(4, "");
                            return;
                        }

                        //TODO: Add the new account to the SQLite database
                        //...

                        Toast.makeText(getActivity(), "Successfully created account!",
                                Toast.LENGTH_SHORT).show();

                        login_complete(newUser);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Nothing, just exits
                    }
                })
                .setCancelable(false)
                .show();

    }

    public void click_login() {

        String enteredUser = user_et.getText().toString();
        String enteredPass = pass_et.getText().toString();

        //TODO: Check if it is in the database

        Boolean verified = (enteredUser.length() > 0 && enteredPass.length() > 0);

        if (verified == false) {
            Toast.makeText(getActivity(), "Incorrect username or password (for testing, just enter any user/pass)",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        login_complete(enteredUser);

    }

    //Start the feed activity, passing the username to it
    public void login_complete(String user) {
        Toast.makeText(getActivity(), "Starting the feed activity for user: "+user,
                Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(activity, LiveFeedActivity.class);
        startActivity(intent);
    }



    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

}
