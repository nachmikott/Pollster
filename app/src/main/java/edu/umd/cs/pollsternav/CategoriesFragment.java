package edu.umd.cs.pollsternav;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.util.ArrayList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class CategoriesFragment extends Fragment {

    private ToggleButton all_categories_toggle;

    private CheckBox academics_checkbox;
    private CheckBox books_checkbox;
    private CheckBox electronics_checkbox;
    private CheckBox food_checkbox;
    private CheckBox misc_checkbox;
    private CheckBox movies_checkbox;
    private CheckBox nature_checkbox;
    private CheckBox shopping_checkbox;
    private CheckBox sports_checkbox;
    private Button saveButton;
    private Button cancelButton;

    public enum Categories {
        ACADEMICS,
        BOOKS,
        ELECTRONICS,
        FOOD,
        MISC,
        MOVIES,
        NATURE,
        SHOPPING,
        SPORTS
    }

    private final String CATEGORY_UPDATE = "CATEGORY_UPDATE";

    private CheckBox all_categories_checkbox;


    public static CategoriesFragment newInstance() {
        CategoriesFragment fragment = new CategoriesFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        academics_checkbox = (CheckBox) view.findViewById(R.id.academics_check);
        books_checkbox = (CheckBox) view.findViewById(R.id.books_check);
        electronics_checkbox = (CheckBox) view.findViewById(R.id.electronics_check);
        food_checkbox = (CheckBox) view.findViewById(R.id.food_check);
        misc_checkbox = (CheckBox) view.findViewById(R.id.misc_check);
        movies_checkbox = (CheckBox) view.findViewById(R.id.movies_check);
        nature_checkbox = (CheckBox) view.findViewById(R.id.nature_check);
        shopping_checkbox = (CheckBox) view.findViewById(R.id.shopping_check);
        sports_checkbox = (CheckBox) view.findViewById(R.id.sports_check);
        all_categories_checkbox = (CheckBox) view.findViewById(R.id.all_categories);




//        all_categories_toggle = (ToggleButton) view.findViewById(R.id.toggleButton);
//        all_categories_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        all_categories_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

               @Override
               public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                   if (isChecked) {
                       academics_checkbox.setChecked(true);
                       books_checkbox.setChecked(true);
                       electronics_checkbox.setChecked(true);
                       food_checkbox.setChecked(true);
                       misc_checkbox.setChecked(true);
                       movies_checkbox.setChecked(true);
                       nature_checkbox.setChecked(true);
                       shopping_checkbox.setChecked(true);
                       sports_checkbox.setChecked(true);
                   } else {
                       academics_checkbox.setChecked(false);
                       books_checkbox.setChecked(false);
                       electronics_checkbox.setChecked(false);
                       food_checkbox.setChecked(false);
                       misc_checkbox.setChecked(false);
                       movies_checkbox.setChecked(false);
                       nature_checkbox.setChecked(false);
                       shopping_checkbox.setChecked(false);
                       sports_checkbox.setChecked(false);
                   }
               }
            }
        );

        saveButton = (Button)view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            ArrayList<Categories> categories = new ArrayList<Categories>();

            if(academics_checkbox.isChecked()) categories.add(Categories.ACADEMICS);
            if(books_checkbox.isChecked()) categories.add(Categories.BOOKS);
            if(electronics_checkbox.isChecked()) categories.add(Categories.ELECTRONICS);
            if(food_checkbox.isChecked()) categories.add(Categories.FOOD);
            if(misc_checkbox.isChecked()) categories.add(Categories.MISC);
            if(movies_checkbox.isChecked()) categories.add(Categories.MOVIES);
            if(nature_checkbox.isChecked()) categories.add(Categories.NATURE);
            if(shopping_checkbox.isChecked()) categories.add(Categories.SHOPPING);
            if(sports_checkbox.isChecked()) categories.add(Categories.SPORTS);

            Intent data = new Intent();
            data.putExtra(CATEGORY_UPDATE, categories);
            getActivity().setResult(RESULT_OK, data);
            getActivity().finish();
            }
        });

        cancelButton = (Button)view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().setResult(RESULT_CANCELED);
                getActivity().finish();
            }
        });



        return view;
    }
}
