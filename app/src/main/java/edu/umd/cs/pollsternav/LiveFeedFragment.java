package edu.umd.cs.pollsternav;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LiveFeedFragment extends Fragment {

    private LiveFeedRecyclerViewAdapter adapter;

    public LiveFeedFragment() {
    }


    public static edu.umd.cs.pollsternav.LiveFeedFragment newInstance() {
        return new edu.umd.cs.pollsternav.LiveFeedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
            adapter = new LiveFeedRecyclerViewAdapter();
            recyclerView.setAdapter(adapter);
        }
    }

    public LiveFeedRecyclerViewAdapter getAdapter() {
        return adapter;
    }

    final class LiveFeedRecyclerViewAdapter extends RecyclerView.Adapter<LiveFeedRecyclerViewAdapter.ViewHolder> {
        List<Post> posts = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_live_feed, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bindPostBy(posts.get(position));
        }

        @Override
        public int getItemCount() {
            return posts.size();
        }

        public void addNewPost(Post post) {
            posts.add(post);
            notifyItemInserted(posts.size() - 1);
        }

        final class ViewHolder extends RecyclerView.ViewHolder {

            private final ImageView imagePost1;
            private final ImageView imagePost2;
            private final TextView textPostCategory;
            private final TextView textPostUser;
            private final TextView upvotePost1;
            private final TextView upvotePost2;

            public ViewHolder(View itemView) {
                super(itemView);

                imagePost1 = (ImageView) itemView.findViewById(R.id.image_post_1);
                imagePost2 = (ImageView) itemView.findViewById(R.id.image_post_2);
                textPostUser = (TextView) itemView.findViewById(R.id.text_post_user);
                textPostCategory = (TextView) itemView.findViewById(R.id.text_post_category);
                upvotePost1 = (TextView) itemView.findViewById(R.id.upvote_for_post_1);
                upvotePost2 = (TextView) itemView.findViewById(R.id.upvote_for_post_2);

                imagePost1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        upvote(1, getAdapterPosition());
                    }
                });
                upvotePost1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        upvote(1, getAdapterPosition());
                    }
                });
                imagePost2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        upvote(2, getAdapterPosition());
                    }
                });
                upvotePost2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        upvote(2, getAdapterPosition());
                    }
                });
            }

            public void bindPostBy(Post post) {
                textPostUser.setText(String.format(textPostUser.getText().toString(), post.postedByUser));
                textPostCategory.setText(post.category.toString());
                imagePost1.setImageResource(Integer.valueOf(post.pic1Path));
                imagePost2.setImageResource(Integer.valueOf(post.pic2Path));
                upvotePost1.setText(post.vote1 + "");
                upvotePost2.setText(post.vote2 + "");
            }

            private void upvote(int index, int position) {
                if (index == 1) {
                    posts.get(position).vote1++;
                } else {
                    posts.get(position).vote2++;
                }
                notifyItemChanged(position);
            }
        }
    }
}
