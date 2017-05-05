package edu.umd.cs.pollsternav.model;

import edu.umd.cs.pollsternav.CategoriesFragment;

/**
 * Created by nachmi on 4/30/17.
 */

public class Post {

    private String category;
    private String pic1Uri;
    private String pic2Uri;
    private String title;
    private String userId;
    private String username;
    private int votes1;
    private int votes2;

    public Post(String category, String pic1Uri, String pic2Uri, String title, String userId, String username, int votes1, int votes2) {
        this.category  = category;
        this.pic1Uri = pic1Uri;
        this.pic2Uri = pic2Uri;
        this.title = title;
        this.userId = userId;
        this.username = username;
        this.votes1 = votes1;
        this.votes2 = votes2;
    }

    public Post() {}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPic1Uri() {
        return pic1Uri;
    }

    public void setPic1Uri(String pic1Uri) {
        this.pic1Uri = pic1Uri;
    }

    public String getPic2Uri() {
        return pic2Uri;
    }

    public void setPic2Uri(String pic2Uri) {
        this.pic2Uri = pic2Uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getVotes1() {
        return votes1;
    }

    public void setVotes1(int votes1) {
        this.votes1 = votes1;
    }

    public int getVotes2() {
        return votes2;
    }

    public void setVotes2(int votes2) {
        this.votes2 = votes2;
    }
}

