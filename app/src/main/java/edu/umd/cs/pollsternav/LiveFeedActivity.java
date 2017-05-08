package edu.umd.cs.pollsternav;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.opengl.Visibility;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Target;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
import java.util.Map;
import java.util.HashMap;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import android.net.Uri;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import java.util.ArrayList;
import android.graphics.Color;
import android.widget.ImageView;
import edu.umd.cs.pollsternav.model.Post;
import edu.umd.cs.pollsternav.service.impl.UserSpecificsService;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class LiveFeedActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnTouchListener {
    public int REQUEST_CODE_CHANGE_CATEGORIES = 1;
    private static final int REQUEST_CODE_ADD_NEW_POST = 2;
    private static final String EXTRA_POST_TITLE = "EXTRA_POST_TITLE";
    private static final String EXTRA_PIC1_PATH = "EXTRA_PIC1_PATH";
    private static final String EXTRA_PIC2_PATH = "EXTRA_PIC2_PATH";
    private static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    private static final String EXTRA_VOTES_1 = "EXTRA_VOTES_1";
    private static final String EXTRA_VOTES_2 = "EXTRA_VOTES_2";
    private DrawerLayout drawer;
    private FirebaseStorage storage = FirebaseStorage.getInstance();



    private List<Post> postsInViewFlipper;

    // make sure to set Target as strong reference
    private Target loadtarget;



    String username;
    public ViewFlipper liveFeedFlipper;
    public TextView postTitle;
    public UserSpecificsService userSpecs;
    public String DEBUG_TAG = "GESTURE DETECTION";
    public List<Post> posts;
    private float initialX;

    private HashMap<String, String> postTitleToFirebaseIdName;

    private ProgressDialog progress;
    private StorageReference fireBaseStorage;
    private DatabaseReference firesBaseDatabase;

    //GESTURE DETECTION
    int touchDownX;
    int touchDownY;
    long swipeDur;

    public TextView swipe_instr;
    public TextView add_instr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // While the initial Posts are loading, a progress bar for loading will pop up.
        progress = new ProgressDialog(this);
        progress.setMessage("Uploading Posts...");
        progress.show();

        // SQLite Service for maintaining specifications particularly for this user
        userSpecs = DependencyFactory.getUserSpecificsService(getApplicationContext());


        //Create the HashMap for obtaining the name of the post (in firebase)
        postTitleToFirebaseIdName = new HashMap<>();

        // Get the posts list. This is done asynchronously.. so the entire project may
        // start BEFORE the posts are put up. The intention of the progress dialog is to force the
        // App to wait until all the posts are loaded.
        getPostsFromFirebase();

        //Storage Reference and Databse Reference for uploading and downloading photos for images
        fireBaseStorage = FirebaseStorage.getInstance().getReference();
        firesBaseDatabase = FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.activity_live_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //The View Flipper and Animations for when transitioning from one post to another
        liveFeedFlipper = (ViewFlipper) findViewById(R.id.viewFlipperForPosts);
        liveFeedFlipper.setInAnimation(this, android.R.anim.fade_in);
        liveFeedFlipper.setOutAnimation(this, android.R.anim.fade_out);

        //Floating Action Button for making a new post.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_new_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 //Go to the new post screen
                Intent intent = new Intent(getBaseContext(), NewPostActivity.class);
                startActivity(intent);
            }
        });

        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //THIS IS JUST FOR THE TIME BEING, WE WANT TO MOVE ONTO A NEXT POST BY A FLING THROUGH A GESTURE
                if(liveFeedFlipper.getDisplayedChild() != liveFeedFlipper.getChildCount()) {
                    try {
                        //Getting the Votes of each image
                        TextView textViewVote1 = (TextView) liveFeedFlipper.getCurrentView().findViewById(R.id.upVote_for_post_1);
                        TextView textViewVote2 = (TextView) liveFeedFlipper.getCurrentView().findViewById(R.id.upVote_for_post_2);
                        int vote1 = Integer.parseInt(textViewVote1.getText().toString());
                        int vote2 = Integer.parseInt(textViewVote2.getText().toString());

                        //Determining which one was voted on last
                        ImageView votedFirst = (ImageView) liveFeedFlipper.getCurrentView().findViewById(R.id.voted_first);
                        ImageView votedSecond = (ImageView) liveFeedFlipper.getCurrentView().findViewById(R.id.voted_second);

                        //Getting actual Post object corresponding to current view
                        Post post = postsInViewFlipper.get(liveFeedFlipper.getDisplayedChild());

                        String keyValueFromFirebase = postTitleToFirebaseIdName.get(post.getTitle());

                        if(votedFirst.getVisibility() == View.VISIBLE) { //Means that the user voted for the first Image
                            System.out.println("USER VOTED FOR FIRST IMAGE. SO WE WILL UPDATE THAT VOTE");
                            firesBaseDatabase.child("posts").child(keyValueFromFirebase).child("votes1").setValue(vote1);
                        } else if (votedSecond.getVisibility() == View.VISIBLE) {
                            System.out.println("USER VOTED FOR SECOND IMAGE. SO WE WILL UPDATE THAT VOTE");
                            firesBaseDatabase.child("posts").child(keyValueFromFirebase).child("votes2").setValue(vote2);
                        } else {
                            //THE USER DIDN'T VOTE FOR ANYTHING SO WE WON"T DO ANYTHING
                        }
                    } catch (Exception e) {
                        System.out.println("Something went wrong!");
                    }

                    //TODO: HERE WE MUST CHECK IF THE USER HAS GONE THROUGH ALL THE POSTS HE CAN POSSIBLY GO THROUGH (YOU DON"T WANT TO GO BACK TO THE BEGINNING)
                    //WE WILL HAVE TO DISPLAY "SORRY, NO MORE POSTS FOR NOW, TRY CHANGING YOUR CATEGORY PREFERENCES, OR TRY AGAIN LATER, WHEN MORE FRIENDS POST!
                    liveFeedFlipper.showNext();
                } else {
                    TextView ending = (TextView) liveFeedFlipper.getCurrentView().findViewById(R.id.no_more_posts);
                    ending.setVisibility(View.VISIBLE);
                }
            }
        });

        //Drawer Layout for other functions (logout, choose categories etc).
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        liveFeedFlipper.setOnTouchListener(LiveFeedActivity.this);

        swipe_instr = (TextView) findViewById(R.id.swipe_instructions);
        add_instr = (TextView) findViewById(R.id.add_instructions);
        swipe_instr.setTextColor(Color.parseColor("#6469AA"));
        add_instr.setTextColor(Color.parseColor("#EFC270"));
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
            //TODO We may ha ve to do something slightly different here..
            getPostsFromFirebase();

        } else if (requestCode == REQUEST_CODE_ADD_NEW_POST) {
            //TODO WE MAY NOT HAVE TO DO ANYTHING HERE.. PART OF FIREBASE IS THAT THE EVENTLISTENER
            // WILL BE CALLED THE MOMENT THE DB IS UPDATED, WHICH WILL CAUSE THE LIST TO BE RESTORED
            // AND CALLING SETFLIPPERCONTENT AGAIN

            //getPostsFromFirebase();
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
        } else if (id == R.id.my_posts) {
            Intent createStoryIntent = new Intent(this, MyPostActivity.class);
            startActivity(createStoryIntent);
        } else if (id == R.id.my_votes) {
            //TODO: Make this work
            System.out.println("MY_VOTES");
        } else if (id == R.id.find_friends) {
            //TODO: FOR ANOTHER TIME
            System.out.println("FIND_FRIENDS");
        } else if (id == R.id.settings) {
            //TODO: FOR ANOTHER TIME
            System.out.println("Settings");
        } else if (id == R.id.log_out) {
            //Signing the user out.
            FirebaseAuth.getInstance().signOut();
            Intent loginActivityIntent = new Intent(this, LoginActivity.class);
            userSpecs.signOut();
            startActivity(loginActivityIntent);
        }

        // Drawer closes when done.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // This is called by the 'getPostsFromFirebase()' method after having collected all the posts
    private void setFlipperContent(ArrayList<Post> postList) {

        // Traverse through each post
        for (int i = 0; i < postList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.live_post_layout, null);

            //Storage Reference for the photos specifically.
            StorageReference storageRefForUser = storage.getReference();

            final TextView image1Votes;
            final TextView image2Votes;
            final ImageView image1;
            final ImageView image2;
            final ImageView voted1;
            final ImageView voted2;

            // Add the view to the liveFeedFlipper.
            liveFeedFlipper.addView(view);

            //Fill in the ImageViews with the actual thumbnail of this picture.
            image1 = (ImageView) view.findViewById(R.id.first_image);
            image2 = (ImageView) view.findViewById(R.id.second_image);

            //Thumbs Up for when voted will overlay the actual image
            voted1 = (ImageView) view.findViewById(R.id.voted_first);
            voted2 = (ImageView) view.findViewById(R.id.voted_second);

            //Setting the Title of the Post
            postTitle = (TextView) view.findViewById(R.id.title_of_post);
            postTitle.setText(postList.get(i).getTitle());

            //Setting the votes for each image.
            image1Votes = (TextView) view.findViewById(R.id.upVote_for_post_1);
            image2Votes = (TextView) view.findViewById(R.id.upVote_for_post_2);
            image1Votes.setText(String.valueOf(postList.get(i).getVotes1()));
            image2Votes.setText(String.valueOf(postList.get(i).getVotes2()));



            /****  Now we load the actual image from Firebase *****/

            //For Image One
            String fullUriForPic1 = postList.get(i).getUsername() + "/" +
                    postList.get(i).getCategory() + "/" +
                    postList.get(i).getPic1Uri();

            //DEBUGGING PURPOSES
            //String fullUriForPic1 = "nachmi@gmail.com/Academics/IMAG0403.jpg";

            // Load the bitmap version of the first picture into image1 on Success.
            storageRefForUser.child(fullUriForPic1).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // This will create a bitmap version of the uri's image, and put it into Image1
                    loadBitmapToImageView(uri.toString(), image1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception exception) {
                    // Handle any errors
                    System.out.println("FAILED TO GET URL FOR IMAGE");
                }
            });

            // For Image Two
             String fullUriForPic2 = postList.get(i).getUsername() + "/" +
                    postList.get(i).getCategory() + "/" +
                    postList.get(i).getPic2Uri();

            //DEBUGGING PURPOSES
            //fullUriForPic2 = "nachmi@gmail.com/Academics/IMAG0403.jpg";

            // Load the bitmap version of the first picture into image1 on Success.
            storageRefForUser.child(fullUriForPic2).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // This will create a bitmap version of the uri's image, and put it into Image1
                    loadBitmapToImageView(uri.toString(), image2);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception exception) {
                    // Handle any errors
                    System.out.println("FAILED TO GET URL FOR IMAGE");
                }
            });

            /****/

            //On Click Listeners For each image, When an image is clicked, its votes are incremented.
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

        // At this point we have loaded every post neccessary,
        // so we are ready to begin the user interactions.
        progress.dismiss();
    }

    // This loads a url bitmap into an imageView.
    public void loadBitmapToImageView(String url, ImageView imageView) {

        if (loadtarget == null) loadtarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                System.out.println("OnBitMaptLoaded");
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                System.out.println("OnBitMapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                System.out.println("OnPrepareLoad");
            }
        };

        // Picasso is a library we are using to seemlessly load the image
        // into the imageview in a good way
        Picasso.with(this).load(url).fit().centerCrop().into(imageView);
    }


    // Creates a list of posts from the Firebase Database (not the same as Storage!)
    // Once the list is completely created, setFlipperContent() is then called.
    private void getPostsFromFirebase() {
        final ArrayList<Post> postList = new ArrayList<Post>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference();

        // Before adding it to the list, a few conditions must be met.
        final String userName = userSpecs.getUserName();
        final List<CategoriesFragment.Categories> preferedCategories = userSpecs.getCategoryPreferences(userName);


        // This is Asynchronous!! It will pretty much run when it wants! (Which makes this a bit slow).
        databaseReference.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get all the children at this level
                Iterable<DataSnapshot> posts = dataSnapshot.getChildren();

                    // Each entry in the DB is then changed into a post object
                for (DataSnapshot child : posts) {
                    Post post = child.getValue(Post.class);

                    CategoriesFragment.Categories categoryOfPost
                            = CategoriesFragment.Categories.valueOf(post.getCategory().toUpperCase());

                    // Only take in OTHER PEOPLES posts of the RIGHT Categories
                    if(!userName.equals(post.getUsername()) && preferedCategories.contains(categoryOfPost))
                        postList.add(post);
                        //Add the title and the actual name
                        String value = child.getKey().toString();

                        postTitleToFirebaseIdName.put(post.getTitle(), value);
                }

                // When were done going through all the posts.
                setFlipperContent(postList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }



    //Check to see if we swipe left (x of finger up is less than finger down within a short enough time)
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int x = (int)motionEvent.getX();
        int y = (int)motionEvent.getY();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = x;
                touchDownY = y;
                swipeDur = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_UP:

                Log.d("dur",""+(System.currentTimeMillis() - swipeDur));
                if (System.currentTimeMillis() - swipeDur < 500 && x < touchDownX) {
                    Toast.makeText(getBaseContext(), "Showing Next", Toast.LENGTH_SHORT).show();
                    liveFeedFlipper.showNext();
                }
                break;
        }
        return true;
    }

}
