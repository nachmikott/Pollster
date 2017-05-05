package edu.umd.cs.pollsternav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import com.squareup.picasso.Target;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.database.Query;
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
    private FirebaseStorage storage = FirebaseStorage.getInstance();



    private int numberOfPost = 0;

    // make sure to set Target as strong reference
    private Target loadtarget;


    UserSpecificsService userSpecificsService;
    String username;
    public ViewFlipper liveFeedFlipper;
    public TextView postTitle;
    public UserSpecificsService userSpecs;
    public String DEBUG_TAG = "GESTURE DETECTION";
    public List<Post> posts;
    private float initialX;

    private ProgressDialog progress;
    private StorageReference fireBaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // While the initial Posts are loading, a progress bar for loading will pop up.
        progress = new ProgressDialog(this);
        progress.setMessage("Uploading Posts...");
        progress.show();

        // SQLite Service for maintaining specifications particularly for this user
        userSpecificsService = DependencyFactory.getUserSpecificsService(getApplicationContext());

        //Storage Reference for uploading and downloading photos for images
        fireBaseStorage = FirebaseStorage.getInstance().getReference();

        setContentView(R.layout.activity_live_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //The View Flipper and Animations for when transitioning from one post to another
        liveFeedFlipper = (ViewFlipper) findViewById(R.id.viewFlipperForPosts);
        liveFeedFlipper.setInAnimation(this, android.R.anim.fade_in);
        liveFeedFlipper.setOutAnimation(this, android.R.anim.fade_out);

        // Get user specifications service so we can access the category preferences later.
        userSpecs = DependencyFactory.getUserSpecificsService(this.getBaseContext());

        // Get the posts list. This is done asynchronously.. so the entire project may
        // start BEFORE the posts are put up. The intention of the progress dialog is to force the
        // App to wait until all the posts are loaded.
        getPostList();

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

        //Drawer Layout for other functions (logout, choose categories etc).
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
            //TODO We may have to do something slightly different here..
            getPostList();

        } else if (requestCode == REQUEST_CODE_ADD_NEW_POST) {
            //TODO WE MAY NOT HAVE TO DO ANYTHING HERE.. PART OF FIREBASE IS THAT THE EVENTLISTENER
            // WILL BE CALLED THE MOMENT THE DB IS UPDATED, WHICH WILL CAUSE THE LIST TO BE RESTORED
            // AND CALLING SETFLIPPERCONTENT AGAIN
            //getPostList();
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
            userSpecificsService.signOut();
            startActivity(loginActivityIntent);
        }

        // Drawer closes when done.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // This is called by the 'getPostList()' method after having collected all the posts
    private void setFlipperContent(ArrayList<Post> postList) {

        // Traverse through each post
        for (int i = 0; i < postList.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.live_post_layout, null);

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

            //Storage Reference for the photos specifically.
            StorageReference storageRefForUser = storage.getReference();

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

            //Setting the Title of the Post
            postTitle = (TextView) view.findViewById(R.id.title_of_post);
            postTitle.setText(postList.get(i).getTitle());

            //Setting the votes for each image.
            image1Votes = (TextView) view.findViewById(R.id.upVote_for_post_1);
            image2Votes = (TextView) view.findViewById(R.id.upVote_for_post_2);
            image1Votes.setText(String.valueOf(postList.get(i).getVotes1()));
            image2Votes.setText(String.valueOf(postList.get(i).getVotes2()));


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
    private void getPostList() {
        final ArrayList<Post> postList = new ArrayList<Post>();

        // Querying for all posts.
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .orderByChild("category");

        // This is Asynchronous!! It will pretty much run when it wants! (Which makes this a bit slow).
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Each entry in the DB is then changed into a post object
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Post post = snapshot.getValue(Post.class);

                        // Before adding it to the list, a few conditions must be met.
                        if(!userSpecs.getUserName().equals(post.getUsername())) // Only take in OTHER PEOPLES posts
                            if(userSpecs.getCategoryPreferences(userSpecs.getUserName()) // Only Posts with the category that fits the users preferences.
                                    .contains(CategoriesFragment.Categories.valueOf(post.getCategory())))
                                postList.add(post);
                    }

                    setFlipperContent(postList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }
}
