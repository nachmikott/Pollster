package edu.umd.cs.pollsternav.service;

import edu.umd.cs.pollsternav.CategoriesFragment;
import java.util.ArrayList;
/**
 * Created by nachmi on 4/25/17.
 */
public interface UserSpecificsServiceInterface {

    boolean loggedIn();
    void signOut();
    ArrayList<CategoriesFragment.Categories> getCategoryPreferences(String userName);
}
