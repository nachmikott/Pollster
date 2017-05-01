package edu.umd.cs.pollsternav.model;

import edu.umd.cs.pollsternav.CategoriesFragment;

/**
 * Created by nachmi on 4/30/17.
 */

public class Post {

    private int pic1;
    private int pic2;
    private String user;
    private CategoriesFragment.Categories category;
    private int postID;
    private int pic1Votes;
    private int pic2Votes;
    private String titleOfPost;

    public Post(String user, int pic1, int pic2, int pic1Votes, int pic2Votes, String titleOfPost, CategoriesFragment.Categories category) {
        this.pic1 = pic1;
        this.pic2 = pic2;
        this.user = user;
        this.category = category;
        this.pic1Votes = pic1Votes;
        this.pic2Votes = pic2Votes;
        this.titleOfPost = titleOfPost;
    }

    public CategoriesFragment.Categories getCategory() {
        return category;
    }

    public int getPic1() {
        return pic1;
    }

    public int getPic2() {
        return pic2;
    }

    public String getUser() {
        return user;
    }

    public int getPostID() {
        return postID;
    }

    public int getPic1Votes() {
        return pic1Votes;
    }

    public int getPic2Votes() {
        return pic2Votes;
    }

    public void setPic2Votes(int newVoteNum) { pic2Votes = newVoteNum;}

    public void setPic1Votes(int newVoteNum) { pic1Votes = newVoteNum;}

    public String getTitleOfPost() {
        return titleOfPost;
    }
}

