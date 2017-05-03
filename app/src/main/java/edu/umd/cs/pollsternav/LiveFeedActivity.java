package edu.umd.cs.pollsternav;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.widget.ViewFlipper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import java.util.ArrayList;
import android.graphics.Color;
import android.widget.ImageView;
import edu.umd.cs.pollsternav.CategoriesFragment;
import edu.umd.cs.pollsternav.model.Post;
import edu.umd.cs.pollsternav.service.impl.UserSpecificsService;
import java.util.List;
import android.content.Context;
import android.view.GestureDetector;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class LiveFeedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GestureDetector.OnGestureListener{
    public int REQUEST_CODE_CHANGE_CATEGORIES = 1;
    private static final int REQUEST_CODE_ADD_NEW_POST = 2;
    private static final String EXTRA_POST_TITLE = "EXTRA_POST_TITLE";
    private static final String EXTRA_PIC1_PATH = "EXTRA_PIC1_PATH";
    private static final String EXTRA_PIC2_PATH = "EXTRA_PIC2_PATH";
    private static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    private static final String EXTRA_VOTES_1 = "EXTRA_VOTES_1";
    private static final String EXTRA_VOTES_2 = "EXTRA_VOTES_2";
    private DrawerLayout drawer;


    private int numberOfPost = 0;

    UserSpecificsService userSpecificsService;
    String username;
    public ViewFlipper liveFeedFlipper;
    public TextView postTitle;
    public UserSpecificsService userSpecs;
    public GestureDetectorCompat gestureDetector;
    public String DEBUG_TAG = "GESTURE DETECTION";
    public List<Post> posts;
    private float initialX;

    private StorageReference fireBaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SQLite Service for maintaining specifications particularly for this user
        userSpecificsService = DependencyFactory.getUserSpecificsService(getApplicationContext());

        //Storage Reference for uploading and downloading photos for images
        fireBaseStorage = FirebaseStorage.getInstance().getReference();

        setContentView(R.layout.activity_live_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = (String)getIntent().getExtras().get("USER");

        liveFeedFlipper = (ViewFlipper) findViewById(R.id.viewFlipperForPosts);
        liveFeedFlipper.setInAnimation(this, android.R.anim.fade_in);
        liveFeedFlipper.setOutAnimation(this, android.R.anim.fade_out);

        //Gesture Detector
        gestureDetector = new GestureDetectorCompat(this, this);

        // Get user specifications service so we can access the category preferences later.
        userSpecs = DependencyFactory.getUserSpecificsService(this.getBaseContext());

        // Set the flipperview with all the user prefered category posts
        setFlipperContent();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_post);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                liveFeedFlipper.showNext();
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
            //super.onBackPressed();
        }
    }

    public boolean onTouchEvent(MotionEvent touchevent) {
        switch (touchevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = touchevent.getX();
                break;
            case MotionEvent.ACTION_UP:
                float finalX = touchevent.getX();
                if (initialX > finalX) {
                    if (liveFeedFlipper.getDisplayedChild() == 1)
                        break;

                    liveFeedFlipper.showNext();
                } else {
                    if (liveFeedFlipper.getDisplayedChild() == 0)
                        break;
                    liveFeedFlipper.showPrevious();
                }
                break;
        }
        return false;
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
            //HERE IS WHERE WE MUST UPDATE THE LIVEFEED BASED ON THE PREFERENCE OF CATEGORIES OF THE USER.
            setFlipperContent();


        } else if (requestCode == REQUEST_CODE_ADD_NEW_POST) {


            //TODO: Here is where we must update to FireBase and update to livefeed
            String title = data.getStringExtra(EXTRA_POST_TITLE);
            String pic1Path = data.getStringExtra(EXTRA_PIC1_PATH);
            String pic2Path = data.getStringExtra(EXTRA_PIC2_PATH);
            //CategoriesFragment.Categories category = data.getIntExtra(EXTRA_CATEGORY, -1);
            int votes1 = data.getIntExtra(EXTRA_VOTES_1, 0);
            int votes2 = data.getIntExtra(EXTRA_VOTES_2, 0);
            //TODO: This is hardcoded! We should be recieving the Category type from the intent as well.
            insertNewPost(username, title, /*HARDCODED! MUST BE CHANGED!!!*/CategoriesFragment.Categories.BOOKS, pic1Path, pic2Path, votes1, votes2);


        }
    }

    private void insertNewPost(String username, String title, CategoriesFragment.Categories category, String pic1Path, String pic2Path, int votes1, int votes2) {



//        if (title == null) {
//            numberOfPost++;
//            title = "Sample post " + numberOfPost;
//        }
//
//        if (category == -1) {
//            category = numberOfPost % Post.Categories.values().length;
//        }
//        if (pic1Path == null) {
//            pic1Path = String.valueOf(R.drawable.ic_post_1);
//        }
//        if (pic2Path == null) {
//            pic2Path = String.valueOf(R.drawable.ic_post_2);
//        }
//
//        Post post = new Post(username, title, Post.Categories.values()[category], pic1Path, pic2Path, votes1, votes2);

//        final Fragment liveFeedFragment = getSupportFragmentManager().findFragmentById(R.id.content_main);
//        if (liveFeedFragment instanceof LiveFeedFragment) {
//            ((LiveFeedFragment) liveFeedFragment).getAdapter().addNewPost(post);
//        }
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
            Intent loginActivityIntent = new Intent(this, LoginActivity.class);
            userSpecificsService.signOut();
            startActivity(loginActivityIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setFlipperContent() {
        posts = getPostList(); // THIS IS AN EXAMPLE
        int end = posts.size();

        for (int i = 0; i < end; i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.live_post_layout, null);

            final TextView image1Votes;
            final TextView image2Votes;
            final ImageView image1;
            final ImageView image2;
            final ImageView voted1;
            final ImageView voted2;

            liveFeedFlipper.addView(view);


            //Fill in the ImageViews with the actual thumbnail of this picture.
            image1 = (ImageView) view.findViewById(R.id.first_image);
            image2 = (ImageView) view.findViewById(R.id.second_image);

            image1.setBackgroundResource(posts.get(i).getPic1());
            image2.setBackgroundResource(posts.get(i).getPic2());

            //Thumbs Up for when voted will overlay the actual image
            voted1 = (ImageView) view.findViewById(R.id.voted_first);
            voted2 = (ImageView) view.findViewById(R.id.voted_second);

            if(posts.get(i).getCategory().equals(CategoriesFragment.Categories.NATURE)) {
                // WE WILL WANT TO USE A BITMAP
                //image1.setImageBitmap();
                image1.setBackgroundColor(Color.GREEN);
                image2.setBackgroundColor(Color.GREEN);
            }

            postTitle = (TextView) view.findViewById(R.id.title_of_post);
            postTitle.setText(posts.get(i).getTitleOfPost());

            image1Votes = (TextView) view.findViewById(R.id.upVote_for_post_1);
            image2Votes = (TextView) view.findViewById(R.id.upVote_for_post_2);

            image1Votes.setText(String.valueOf(posts.get(i).getPic1Votes()));
            image2Votes.setText(String.valueOf(posts.get(i).getPic2Votes()));



            //On Click Listeners For Eeach Image, When an image is clicked, its votes are incremented.
            image1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(voted1.getVisibility() != View.VISIBLE) { // User votes once.
                        voted1.setVisibility(View.VISIBLE);

                        Integer newVote = Integer.parseInt(image1Votes.getText().toString()) + 1;
                        image1Votes.setText(newVote.toString());

                        image1.setColorFilter(Color.argb(150,200,200,200));

                        if(voted2.getVisibility() == View.VISIBLE) { // Switch from vote2 to vote1
                            voted2.setVisibility(View.INVISIBLE);

                            image2.setColorFilter(null);

                            Integer newVote2 = Integer.parseInt(image2Votes.getText().toString()) - 1;
                            image2Votes.setText(newVote2.toString());
                        }

                    } else { // User takes back their vote.
                        voted1.setVisibility(View.INVISIBLE);

                        image1.setColorFilter(null);

                        Integer newVote = Integer.parseInt(image1Votes.getText().toString()) - 1;
                        image1Votes.setText(newVote.toString());
                    }
                }
            });
            image2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(voted2.getVisibility() != View.VISIBLE) { // User votes once.
                        voted2.setVisibility(View.VISIBLE);

                        Integer newVote = Integer.parseInt(image2Votes.getText().toString()) + 1;
                        image2Votes.setText(newVote.toString());

                        image2.setColorFilter(Color.argb(150,200,200,200));

                        if(voted1.getVisibility() == View.VISIBLE) { // Switch from vote2 to vote1
                            voted1.setVisibility(View.INVISIBLE);

                            image1.setColorFilter(null);

                            Integer newVote1 = Integer.parseInt(image1Votes.getText().toString()) - 1;
                            image1Votes.setText(newVote1.toString());
                        }



                    } else { // User takes back their vote.
                        voted2.setVisibility(View.INVISIBLE);

                        image2.setColorFilter(null);

                        Integer newVote = Integer.parseInt(image2Votes.getText().toString()) - 1;
                        image2Votes.setText(newVote.toString());
                    }

                }
            });
        }
        // This will be in the form of a Gesture.
        setFlipperAnimation();
    }

    private void setFlipperAnimation() {

    }

    private List<Post> getPostList() {

        // THIS IS WHERE WE CALL TO THE DATABASE!! WE WILL USE THE CATEGORY PREFERENCES ACCESSED BY THE SQL LITE SERVER ON THE USERS PHON:
        // 1) User the UserName, to MAKE SURE YOU DO NOT TAKE IN THE CURRENT USERS'S POSTS
        //     USING THIS-- >userSpecs.getUserName()
        // 2) MAKE THE CALL TO FIREBASE TO BRING IN A HUGE LIST OF POSTS, BUT ONLY THE ONES THAT MATCH THE TYPE OF CATEGORIES CHOSEN.
        //     USING THIS --> userSpecs.getCategoryPreferences(userSpecs.getUserName()); // THIS RETURNS A LIST OF CATEGORIES

        /// ***
        // 3) POPULATE THE ACTUAL POST OBJECT LIST..
        //FOR TESTING PURPOSES
        int id = getResources().getIdentifier("ic_launcher_pollster_icon_24dp", "mipmap", getPackageName());

        Post onePost = new Post("USER", id, id, 0, 0, "MOVIE POST",  CategoriesFragment.Categories.MOVIES);

        Post secondPost = new Post("USER", id, id, 0, 0, "NATURE POST",  CategoriesFragment.Categories.NATURE);

        ArrayList<Post> postList = new ArrayList<Post>();
        postList.add(onePost);
        postList.add(secondPost);

        return postList;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }
}
