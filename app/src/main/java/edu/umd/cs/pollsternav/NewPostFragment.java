package edu.umd.cs.pollsternav;

/**
 * Created by Creed on 4/24/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class NewPostFragment extends Fragment {

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

        imageViewVote1 = (ImageView) view.findViewById(R.id.ivVote1);
        imageViewVote2 = (ImageView) view.findViewById(R.id.ivVote2);
        textTitle = (EditText) view.findViewById(R.id.editText);
        spinnerCategory = (Spinner) view.findViewById(R.id.spinnerCategory);
        //AJAY - onClicked action when clickingon Button Save
        view.findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = textTitle.getText().toString();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(activity, "Title must not empty!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (spinnerCategory.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                    Toast.makeText(activity, "You must select category!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mImageForVote1Uri == null) {
                    Toast.makeText(activity, "You must select photo for vote 1!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mImageForVote2Uri == null) {
                    Toast.makeText(activity, "You must select photo for vote 2!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getFileNameFromMediaUri(mImageForVote1Uri) == null) {
                    Toast.makeText(activity, "Wrong format for image vote 1. Please select another!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (getFileNameFromMediaUri(mImageForVote2Uri) == null) {
                    Toast.makeText(activity, "Wrong format for image vote 2. Please select another!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //AJAY - call this method to create and store a new post with selecting data when all conditions have passed
                newPost(mImageForVote1Uri, mImageForVote2Uri, 0, 0, categories[spinnerCategory.getSelectedItemPosition()], title);
            }
        });

        view.findViewById(R.id.picturesVote1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICTURE_1);
            }
        });
        view.findViewById(R.id.picturesVote2).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICTURE_2);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //AJAY- After selecting image files
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_PICTURE_1) {
                mImageForVote1Uri = uri;
                imageViewVote1.setImageURI(uri);
                imageViewVote1.setVisibility(View.VISIBLE);
            } else if (requestCode == REQUEST_PICTURE_2) {
                mImageForVote2Uri = uri;
                imageViewVote2.setImageURI(uri);
                imageViewVote2.setVisibility(View.VISIBLE);
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
}


