package edu.umd.cs.pollsternav;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import edu.umd.cs.pollsternav.service.impl.UserSpecificsService;

public class LiveFeedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public int REQUEST_CODE_CHANGE_CATEGORIES = 1;
    private DrawerLayout drawer;

    UserSpecificsService userSpecificsService;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userSpecificsService = DependencyFactory.getUserSpecificsService(getApplicationContext());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = (String)getIntent().getExtras().get("USER");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_post);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Go to the new post screen
                Intent intent = new Intent(getBaseContext(), NewPostActivity.class);
                startActivity(intent);

            }
        });



        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader =  navigationView.getHeaderView(0);
        TextView nav_user = (TextView) navHeader.findViewById(R.id.username_text);
        String userName = userSpecificsService.getUserName();

        //nav_user.setText(userSpecificsService.getUserName());
        nav_user.setText(userName);
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

            Toast.makeText(this, "Categories Are " + userSpecificsService.getCategoryPreferences(userSpecificsService.getUserName()).toString() ,
                    Toast.LENGTH_LONG).show();
            //Debugging Purposes
//            Toast.makeText(this, "Categories Are " + data.getExtras().get("CATEGORY_UPDATE").toString() ,
//                    Toast.LENGTH_LONG).show();

            //HERE IS WHERE WE MUST UPDATE THE LIVEFEED BASED ON THE PREFERENCE OF CATEGORIES OF THE USER.

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.categories) {
            Intent categoriesIntent = new Intent(this, CategoriesActivity.class);
            startActivityForResult(categoriesIntent, REQUEST_CODE_CHANGE_CATEGORIES);
            drawer.closeDrawer(Gravity.LEFT);
            return true;
        } else if (id == R.id.my_posts) {
            Intent myPostsIntent = new Intent(this, MyPostActivity.class);
            startActivity(myPostsIntent);
        } else if (id == R.id.my_votes) {

        } else if (id == R.id.find_friends) {

        } else if (id == R.id.settings) {

        } else if (id == R.id.log_out) {
            userSpecificsService.signOut();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
