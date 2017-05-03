package edu.umd.cs.pollsternav;

/**
 * Created by Creed on 4/24/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import edu.umd.cs.pollsternav.service.impl.UserSpecificsService;

import static com.google.android.gms.internal.zzt.TAG;


/*  Currently the login system just stores in memory (not a real login system).

    We may want to make this either SQLite or a real database using a REST API and some cloud server
    like Heroku or AWS or something.
*/

public class NewPostFragment extends Fragment {

    Activity activity;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    private static final int GALLERY_INTENT = 2;

    public UserSpecificsService userSpecs;



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



        view.findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = getActivity().getIntent();
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();


            }
        });

        view.findViewById(R.id.picturesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);

                intent.setType("image/*");

                startActivityForResult(intent, GALLERY_INTENT);

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == -1) {
            Uri uri = data.getData();

            StorageReference filePath = mStorageRef.child("Photos").child(uri.getLastPathSegment());


            //TODO: AJAY, THIS IS WHERE WE ARE TRYING TO DO THE UPLOAD. THE REASON WHY, IS THAT
            // THE USER WILL PRESS ON ONE OF THE BUTTONS IN THIS FRAGMENT< AND IT WILL SEND THEM TO THE PHOTO UPLOAD THING IN THEIR PHONE.
            // AFTER THEY HAVE SELECTED THE PHOTO, IT WILL COME HERE FOR AN ANSWER. TRY RUNNING THIS IN DEBUG MODE, WITH A BREAKPOINT AROUND HERE TO GET AN UNDERSTANDING.
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getActivity(), "Upload Done", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "FAILED", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static NewPostFragment newInstance() {
        return new NewPostFragment();
    }

//    public void newPost(File pic1, File pic2, int votes1, int votes2, String category, String title) {
//        // Create new post at /posts/$postid
//        HashMap<String, Object> postAttributes = new HashMap<>();
//
//        //put attributes to column in order to publish new post to Firebase
//        postAttributes.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
//        postAttributes.put("category", category);
//        postAttributes.put("title", title);
//        postAttributes.put("votes1", votes1);
//        postAttributes.put("votes2", votes2);
//        if (pic1 != null) {
//            postAttributes.put("pic1", pic1.getName());
//        }
//        if (pic2 != null) {
//            postAttributes.put("pic2", pic2.getName());
//        }
//
//        Map<String, Object> postUpdates = new HashMap<>();
//        String newPostId = mDatabase.child("posts").push().getKey();
//        postUpdates.put("/posts/" + newPostId, postAttributes);
//
//        // Write a post to the database
//        mDatabase.updateChildren(postUpdates);
//
//        //if pic1 is not null, start upload pic1 to cloud Firebase
//        if (pic1 != null) {
//            uploadFileToCloud(pic1, newPostId);
//        }
//
//        //if pic2 is not null, start upload pic1 to cloud Firebase
//        if (pic2 != null) {
//            uploadFileToCloud(pic2, newPostId);
//        }
//    }
//
//    private void uploadFileToCloud(File pic, String newPostId) {
//        InputStream stream;
//        try {
//            stream = new FileInputStream(pic);
//            // Create a reference to upload file to Firebase Cloud
//            StorageReference fileUploadRef = mStorageRef.child(newPostId + "/" + pic.getName());
//
//            //start uploading image to Firebase Storage
//            UploadTask uploadTask = fileUploadRef.putStream(stream);
//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Handle unsuccessful uploads
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                    if (downloadUrl != null) {
//                        Log.d(TAG, "Download url - " + downloadUrl.toString());
//                    }
//                }
//            });
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
}


