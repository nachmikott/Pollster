package edu.umd.cs.pollsternav;

/**
 * Created by Creed on 4/24/2017.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.pollsternav.service.impl.UserSpecificsService;

import static android.content.ContentValues.TAG;


/*  Currently the login system just stores in memory (not a real login system).

    We may want to make this either SQLite or a real database using a REST API and some cloud server
    like Heroku or AWS or something.
*/

public class NewPostFragment extends Fragment implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {


    public String DEBUG_TAG = "GESTURE";

    private static final int REQUEST_PICTURE_1 = 1;
    private static final int REQUEST_PICTURE_2 = 2;

    Activity activity;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    public UserSpecificsService userSpecs;
    private ImageView imageViewVote1;
    private ImageView imageViewVote2;
    private Uri mImageForVote1Uri;
    private Uri mImageForVote2Uri;
    private EditText textTitle;
    private Spinner spinnerCategory;
    private String[] categories;

    private ImageButton choose1;
    private ImageButton choose2;

    private TextView select1_text;
    private TextView select2_text;

    private Button saveBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Firebase Database Reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //Firebase Storage Reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_post, container, false);
        activity = getActivity();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // SQLite Service for maintaining specifications particularly for this user
        userSpecs = DependencyFactory.getUserSpecificsService(getActivity().getApplicationContext());
        categories = getResources().getStringArray(R.array.categories);

        choose1 = (ImageButton) view.findViewById(R.id.choose1);
        choose2 = (ImageButton) view.findViewById(R.id.choose2);


        textTitle = (EditText) view.findViewById(R.id.editText);
        spinnerCategory = (Spinner) view.findViewById(R.id.spinnerCategory);
        //AJAY - onClicked action when clickingon Button Save
        view.findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = textTitle.getText().toString();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(activity, "Title must not be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (spinnerCategory.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                    Toast.makeText(activity, "You must select a category!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mImageForVote1Uri == null) {
                    Toast.makeText(activity, "You must select a photo for vote 1!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mImageForVote2Uri == null) {
                    Toast.makeText(activity, "You must select a photo for vote 2!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getFileNameFromMediaUri(mImageForVote1Uri) == null) {
                    Toast.makeText(activity, "Wrong format for image vote 1. Please select another!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getFileNameFromMediaUri(mImageForVote2Uri) == null) {
                    Toast.makeText(activity, "Wrong format for image vote 2. Please select another!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialog.show(activity, "Please wait", "Uploading your post...");

                //AJAY - call this method to create and store a new post with selecting data when all conditions have passed
                newPost(mImageForVote1Uri, mImageForVote2Uri, 0, 0, categories[spinnerCategory.getSelectedItemPosition()], title);
            }
        });

        choose1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICTURE_1);
            }
        });
       choose2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICTURE_2);
            }
        });

        select1_text = (TextView) view.findViewById(R.id.select1_text);
        select2_text = (TextView) view.findViewById(R.id.select2_text);
        select1_text.setTextColor(Color.parseColor("#6469AA"));
        select2_text.setTextColor(Color.parseColor("#6469AA"));

        saveBtn = (Button) view.findViewById(R.id.button_save);
        saveBtn.getBackground().setColorFilter(0xFFEFC270, PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //AJAY- After selecting image files
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_PICTURE_1) {
                mImageForVote1Uri = uri;
                choose1.setImageURI(uri);
                choose1.setAdjustViewBounds(true);
                choose1.setScaleType(ImageView.ScaleType.CENTER_CROP);
                select1_text.setVisibility(View.INVISIBLE);
                choose1.getBackground().setColorFilter(0x00979AC6, PorterDuff.Mode.MULTIPLY);
            } else if (requestCode == REQUEST_PICTURE_2) {
                mImageForVote2Uri = uri;
                choose2.setImageURI(uri);
                choose2.setAdjustViewBounds(true);
                choose2.setScaleType(ImageView.ScaleType.CENTER_CROP);
                choose2.getBackground().setColorFilter(0x00979AC6, PorterDuff.Mode.MULTIPLY);
                select2_text.setVisibility(View.INVISIBLE);
            }
        }
//
//            //TODO: AJAY, THIS IS WHERE WE ARE TRYING TO DO THE UPLOAD. THE REASON WHY, IS THAT
//            // THE USER WILL PRESS ON ONE OF THE BUTTONS IN THIS FRAGMENT< AND IT WILL SEND THEM TO THE PHOTO UPLOAD THING IN THEIR PHONE.
//            // AFTER THEY HAVE SELECTED THE PHOTO, IT WILL COME HERE FOR AN ANSWER. TRY RUNNING THIS IN DEBUG MODE, WITH A BREAKPOINT AROUND HERE TO GET AN UNDERSTANDING.
    }

    public static NewPostFragment newInstance() {
        return new NewPostFragment();
    }

    /**
     * Ajay - Creating new post with picture files (from selectingUri of vote 1 and vote 2)
     * and storing those infomation to Firebase
     * @param pic1Uri
     * @param pic2Uri
     * @param votes1
     * @param votes2
     * @param category
     * @param title
     */
    public void newPost(Uri pic1Uri, Uri pic2Uri, int votes1, int votes2, String category, String title) {
        // Create new post at /posts/$postid
        HashMap<String, Object> postAttributes = new HashMap<>();

        //put attributes to column in order to publish new post to Firebase
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {//if login with Firebase
            postAttributes.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
        postAttributes.put("username", userSpecs.getUserName());
        postAttributes.put("category", category);
        postAttributes.put("title", title);
        postAttributes.put("votes1", votes1);
        postAttributes.put("votes2", votes2);
        if (pic1Uri != null) {
            postAttributes.put("pic1Uri", getFileNameFromMediaUri(pic1Uri));
        }
        if (pic2Uri != null) {
            postAttributes.put("pic2Uri", getFileNameFromMediaUri(pic2Uri));
        }

        Map<String, Object> postUpdates = new HashMap<>();
        String newPostId = mDatabase.child("posts").push().getKey();
        postUpdates.put("/posts/" + newPostId, postAttributes);

        // Write a post to the database
        mDatabase.updateChildren(postUpdates);

        //if pic1Uri is not null, start upload pic1Uri to cloud Firebase
        if (pic1Uri != null) {
            uploadFileToCloud(pic1Uri, newPostId, category);
        }

        //if pic2Uri is not null, start upload pic1Uri to cloud Firebase
        if (pic2Uri != null) {
            uploadFileToCloud(pic2Uri, newPostId, category);
        }
    }

    /**
     * Ajay - Upload file (in Uri) to Firebase with category info as well
     * @param uri
     * @param postId
     * @param category
     */
    private void uploadFileToCloud(Uri uri, String postId, String category) {
        // Create a reference to upload file to Firebase Cloud
        StorageReference fileUploadRef = mStorageRef.child(FirebaseAuth.getInstance().getCurrentUser().getEmail() + "/" + category + "/" + getFileNameFromMediaUri(uri));

        //start uploading image to Firebase Storage
        UploadTask uploadTask = fileUploadRef.putFile(uri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if (downloadUrl != null) {
                    Log.d(TAG, "Download url - " + downloadUrl.toString());
                    finishActivity();
                }
            }
        });
    }

    /**
     * Ajay - finish the activity when completing upload files
     */
    private void finishActivity() {
        activity.finish();
    }

    /**
     * Ajay- Get file name from media uri
     * @param uri
     * @return
     */
    private String getFileNameFromMediaUri(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("file")) {
            fileName = uri.getLastPathSegment();
        } else {
            Cursor cursor = null;
            try {
                cursor = activity.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DISPLAY_NAME}, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    Log.d(TAG, "name is " + fileName);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return fileName;
    }



    //EVERYTHING BELOW IS FOR GESTURES


    @Override
    public boolean onDown(MotionEvent event) {
        Toast.makeText(activity,"Down",
                Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Toast.makeText(activity,"Fling",
                Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }


    public boolean onTouchEvent(MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(DEBUG_TAG,"Action was DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE) :
                Log.d(DEBUG_TAG,"Action was MOVE");
                return true;
            case (MotionEvent.ACTION_UP) :
                Log.d(DEBUG_TAG,"Action was UP");
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                Log.d(DEBUG_TAG,"Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(DEBUG_TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default :
                return activity.onTouchEvent(event);
        }
    }

}


