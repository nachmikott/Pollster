package edu.umd.cs.pollsternav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LiveFeedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public int REQUEST_CODE_CHANGE_CATEGORIES = 1;
    private static final int REQUEST_CODE_ADD_NEW_POST = 2;
    private static final String EXTRA_POST_TITLE = "EXTRA_POST_TITLE";
    private static final String EXTRA_PIC1_PATH = "EXTRA_PIC1_PATH";
    private static final String EXTRA_PIC2_PATH = "EXTRA_PIC2_PATH";
    private static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    private static final String EXTRA_VOTES_1 = "EXTRA_VOTES_1";
    private static final String EXTRA_VOTES_2 = "EXTRA_VOTES_2";

    private DrawerLayout drawer;

    String username;
    private int numberOfPost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = (String)getIntent().getExtras().get("USER");

        if(getIntent().getExtras().get("CATEGORY_UPDATE") != null) {
            Log.d("Categories", getIntent().getExtras().get("CATEGORY_UPDATE").toString());
            Toast.makeText(this, "Categories Are " + getIntent().getExtras().get("CATEGORY_UPDATE").toString() ,
                    Toast.LENGTH_LONG).show();
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_post);
        //fab.getBackground().setColorFilter(0xFF979AC6, PorterDuff.Mode.MULTIPLY);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Go to the new post screen
                Intent intent = new Intent(getBaseContext(), NewPostActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_NEW_POST);

            }
        });



        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        View navHeader =  navigationView.getHeaderView(0);
//        TextView nav_user = (TextView) navHeader.findViewById(R.id.username_text);
//        nav_user.setText(username);
//
//        navHeader.getBackground().setColorFilter(0xFF979AC6, PorterDuff.Mode.MULTIPLY);

        Fragment liveFeedFragment = getSupportFragmentManager().findFragmentById(R.id.content_main);
        if (liveFeedFragment == null) {
            liveFeedFragment = LiveFeedFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, liveFeedFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHANGE_CATEGORIES) {
            if (data == null) {
                return;
            }
            //Debugging Purposes
//            Toast.makeText(this, "Categories Are " + data.getExtras().get("CATEGORY_UPDATE").toString() ,
//                    Toast.LENGTH_LONG).show();

            //HERE IS WHERE WE MUST UPDATE THE LIVEFEED BASED ON THE PREFERENCE OF CATEGORIES OF THE USER.

        } else if (requestCode == REQUEST_CODE_ADD_NEW_POST) {
            //TODO: Here is where we must update to sqlite and update to livefeed
            String title = data.getStringExtra(EXTRA_POST_TITLE);
            String pic1Path = data.getStringExtra(EXTRA_PIC1_PATH);
            String pic2Path = data.getStringExtra(EXTRA_PIC2_PATH);
            int category = data.getIntExtra(EXTRA_CATEGORY, -1);
            int votes1 = data.getIntExtra(EXTRA_VOTES_1, 0);
            int votes2 = data.getIntExtra(EXTRA_VOTES_2, 0);
            insertNewPost(username, title, category, pic1Path, pic2Path, votes1, votes2);
        }
    }

    private void insertNewPost(String username, String title, int category, String pic1Path, String pic2Path, int votes1, int votes2) {
        if (title == null) {
            numberOfPost++;
            title = "Sample post " + numberOfPost;
        }

        if (category == -1) {
            category = numberOfPost % Post.Categories.values().length;
        }
        if (pic1Path == null) {
            pic1Path = String.valueOf(R.drawable.ic_post_1);
        }
        if (pic2Path == null) {
            pic2Path = String.valueOf(R.drawable.ic_post_2);
        }

        Post post = new Post(username, title, Post.Categories.values()[category], pic1Path, pic2Path, votes1, votes2);

        final Fragment liveFeedFragment = getSupportFragmentManager().findFragmentById(R.id.content_main);
        if (liveFeedFragment instanceof LiveFeedFragment) {
            ((LiveFeedFragment) liveFeedFragment).getAdapter().addNewPost(post);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.categories) {
            Intent createStoryIntent = new Intent(this, CategoriesActivity.class);
            startActivityForResult(createStoryIntent, REQUEST_CODE_CHANGE_CATEGORIES);
            drawer.closeDrawer(Gravity.LEFT);
            return true;
        } else if (id == R.id.my_posts) {
            Intent createStoryIntent = new Intent(this, MyPostActivity.class);
            startActivity(createStoryIntent);
        } else if (id == R.id.my_votes) {

        } else if (id == R.id.find_friends) {

        } else if (id == R.id.settings) {

        } else if (id == R.id.log_out) {
            FirebaseAuth.getInstance().signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
