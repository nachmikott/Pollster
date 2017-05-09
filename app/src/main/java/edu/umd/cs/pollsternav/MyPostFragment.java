package edu.umd.cs.pollsternav;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Task;

import edu.umd.cs.pollsternav.model.Post;
import edu.umd.cs.pollsternav.service.impl.UserSpecificsService;

public class MyPostFragment extends Fragment {

    private ArrayList<Post> my_posts;
    private StorageReference fireBaseStorage;
    private RecyclerView recyclerPost;
    public UserSpecificsService userSpecs;
    private ListView listView;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public ArrayList<String> arr;
    public ArrayAdapter adapter;

    String[] countries = new String[] {
            "India",
            "Pakistan",
            "Sri Lanka",
            "China",
            "Bangladesh",
            "Nepal",
            "Afghanistan",
            "North Korea",
            "South Korea",
            "Japan"
    };

    // Array of integers points to images stored in /res/drawable-ldpi/
    int[] flags = new int[]{
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera,
            R.drawable.ic_menu_camera
    };

    int[] flags2 = new int[]{
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery,
            R.drawable.ic_menu_gallery
    };

    public static MyPostFragment newInstance() {
        MyPostFragment fragment = new MyPostFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_my_post, container, false);
        my_posts = new ArrayList<Post>();
        userSpecs = DependencyFactory.getUserSpecificsService(getContext());
        fireBaseStorage = FirebaseStorage.getInstance().getReference();
        getPostsFromFirebase(view.getContext(), view);
        //populates listview with posts of user


        // Each row in the list stores country name, currency and flag
        /*List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();

        for(int i=0;i<10;i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put("flag", Integer.toString(flags[i]) );
            hm.put("flag2", Integer.toString(flags2[i]) );
            hm.put("txt", "Country : " + countries[i]);
            aList.add(hm);
        }

        // Keys used in Hashmap
        String[] from = {"flag","flag2","txt"};

        // Ids of views in listview_layout
        int[] to = { R.id.icon1,R.id.icon2,R.id.Itemname};

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item
        SimpleAdapter adapter = new SimpleAdapter(view.getContext(), aList, R.layout.my_post_list, from, to);

        // Getting a reference to listview of main.xml layout file
        listView = (ListView) view.findViewById(R.id.post_list);

        // Setting the adapter to the listView
        listView.setAdapter(adapter);
        */

        return view;
    }



    public void populate(Context context, View v){

        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();

        for(int i=0;i<my_posts.size();i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put("flag", Integer.toString(flags[0]) );
            hm.put("txt", "Title: "+my_posts.get(i).getTitle()+"\nVotes for 1st Picture: "+my_posts.get(i).getVotes1()+"\nVotes for 2nd Picture: "+my_posts.get(i).getVotes2());
            aList.add(hm);
        }

        // Keys used in Hashmap
        String[] from = {"flag","txt"};

        // Ids of views in listview_layout
        int[] to = { R.id.icon1,R.id.Itemname};

        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item
        SimpleAdapter adapter = new SimpleAdapter(v.getContext(), aList, R.layout.my_post_list, from, to);

        /*
        float values[]={600,400};
        LinearLayout linear=(LinearLayout) v.findViewById(R.id.pie_chart);
        values=calculateData(values);
        linear.addView(new MyGraphview(context,values));
        */

        // Getting a reference to listview of main.xml layout file
        listView = (ListView) v.findViewById(R.id.post_list);

        // Setting the adapter to the listView
        listView.setAdapter(adapter);
/*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                //Log.v("TAG", "CLICKED row number: " + arg2);

                String fullUriForPic1 = my_posts.get(arg2).getUsername() + "/" +
                        my_posts.get(arg2).getCategory() + "/" +
                        my_posts.get(arg2).getPic1Uri();

                String fullUriForPic2 = my_posts.get(arg2).getUsername() + "/" +
                        my_posts.get(arg2).getCategory() + "/" +
                        my_posts.get(arg2).getPic2Uri();


                Intent intent = new Intent(getActivity(), Photos_My_Post_Activity.class);
                intent.putExtra("pic1uri", fullUriForPic1);
                intent.putExtra("pic2uri", fullUriForPic2);
                startActivity(intent);
            }

        });
*/



    }






    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerPost = (RecyclerView) view.findViewById(R.id.recycler_post);
        recyclerPost.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
    }


    private void setMy_posts(ArrayList<Post> post_list){
        this.my_posts = post_list;
    }

    private void getPostsFromFirebase(Context context, View view) {
        final ArrayList<Post> postList = new ArrayList<Post>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = database.getReference();

        // Before adding it to the list, a few conditions must be met.
        final String userName = userSpecs.getUserName();
        final List<CategoriesFragment.Categories> preferedCategories = userSpecs.getCategoryPreferences(userName);
        final Context temp_context = context;
        final View temp_view = view;

        // This is Asynchronous!! It will pretty much run when it wants! (Which makes this a bit slow).
        databaseReference.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get all the children at this level
                Iterable<DataSnapshot> posts = dataSnapshot.getChildren();

                // Each entry in the DB is then changed into a post object
                for (DataSnapshot child : posts) {
                    Post post = child.getValue(Post.class);

                    // Only take in my posts
                    if(userName.equals(post.getUsername())){
                        postList.add(post);
                    }

                }
                setMy_posts(postList);
                populate(temp_context, temp_view);
            }

            //listView = (ListView) temp_view.findViewById(R.id.post_list);
            //ArrayAdapter arrayAdapter = new ArrayAdapter(temp_context, R.layout.my_post_list, friends);
            //listView.setAdapter(arrayAdapter);

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


    }

    private float[] calculateData(float[] data) {
        // TODO Auto-generated method stub
        float total=0;
        for(int i=0;i<data.length;i++)
        {
            total+=data[i];
        }
        for(int i=0;i<data.length;i++)
        {
            data[i]=360*(data[i]/total);
        }
        return data;

    }
    public class MyGraphview extends View
    {
        private Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private float[] value_degree;
        private int[] COLORS={Color.BLUE,Color.GREEN,Color.GRAY,Color.CYAN,Color.RED};
        RectF rectf = new RectF (10, 10, 200, 200);
        int temp=0;
        public MyGraphview(Context context, float[] values) {

            super(context);
            value_degree=new float[values.length];
            for(int i=0;i<values.length;i++)
            {
                value_degree[i]=values[i];
            }
        }
        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            for (int i = 0; i < value_degree.length; i++) {//values2.length; i++) {
                if (i == 0) {
                    paint.setColor(COLORS[i]);
                    canvas.drawArc(rectf, 0, value_degree[i], true, paint);
                }
                else
                {
                    temp += (int) value_degree[i - 1];
                    paint.setColor(COLORS[i]);
                    canvas.drawArc(rectf, temp, value_degree[i], true, paint);
                }
            }
        }

    }

}
