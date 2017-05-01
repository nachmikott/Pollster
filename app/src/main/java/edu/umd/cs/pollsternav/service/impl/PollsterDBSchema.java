package edu.umd.cs.pollsternav.service.impl;

/**
 * Created by nachmi on 4/25/17.
 */
public class PollsterDBSchema {
    public static final class UserTable {
        static final String NAME = "USER";

        public static final class Columns {
            static final String USER_NAME = "USER_NAME";
            static final String PWD = "PWD";
        }
    }

    public static final class  CategoryPreferences {
        static final String NAME = "CATEGORIES";

        public static final class Columns {
            static final String USER = "USER";
            static final String ACADEMICS = "ACADEMICS";
            static final String BOOKS = "BOOKS";
            static final String ELECTRONICS = "ELECTRONICS";
            static final String FOOD = "FOOD";
            static final String MISC = "MISC";
            static final String MOVIES = "MOVIES";
            static final String NATURE = "NATURE";
            static final String SHOPPING = "SHOPPING";
            static final String SPORTS = "SPORTS";
        }
    }
}