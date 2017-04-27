package edu.umd.cs.pollsternav;

/**
 * Created by nachmi on 4/25/17.
 */

import android.content.Context;
import edu.umd.cs.pollsternav.service.impl.UserSpecificsService;


public class DependencyFactory {
    private static UserSpecificsService userSpecificsService;

    public static UserSpecificsService getUserSpecificsService(Context context) {
        if (userSpecificsService == null) {
            //storyService = new InMemoryStoryService(context);
            userSpecificsService = new UserSpecificsService(context);
        }
        return userSpecificsService;
    }
}
