package com.example.android.myfishy.ui.create_meal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.View;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android.myfishy.MainActivity;
import com.example.android.myfishy.R;
import com.example.android.myfishy.db.entities.Meal;
import com.example.android.myfishy.db.entities.NutritionFactTable;
import com.example.android.myfishy.repo.HealthyRepository;
import com.example.android.myfishy.ui.add_nourishment.AddNourishmentFragment;
import com.example.android.myfishy.utilities.OnCloseFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateMealActivity extends AppCompatActivity implements OnCloseFragment, IngredientListAdapter.OnNutritionListener {

    public static final String CREATE_MEAL_ACTIVITY_TAG =
            HealthyRepository.buildTag(HealthyRepository.CREATE_MEAL_TAG, HealthyRepository.ACTIVITY_TAG);

    private FragmentManager fragmentManager;
    private Fragment fragment;
    private EditText editTextMealName;
    private EditText editTextMealInstructions;

    private CreateMealViewModel createMealViewModel;
    private IngredientListAdapter ingredientListAdapter;
    private List<NutritionFactTable> nutritionFactTableList;
    private Set<String> ingredientSet;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meal);
        createMealViewModel =
                new ViewModelProvider(this).get(CreateMealViewModel.class);
        nutritionFactTableList = new ArrayList<>();
        ingredientSet = new HashSet<>();
        editTextMealName = findViewById(R.id.editText_mealName);

        final LifecycleOwner cool = this;

        int mealType = 0;
        if (savedInstanceState != null) {
            mealType = savedInstanceState.getInt(HealthyRepository.MEAL_TYPE_EXTRA);
        }

        FloatingActionButton fab = findViewById(R.id.activity_create_meal_fab);
        final int finalMealType = mealType;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Meal meal = new Meal(
                        MainActivity.getCurrUser(),
                        editTextMealName.getText().toString(),
                        finalMealType,
                        System.currentTimeMillis()
                );
                createMealViewModel.insertMeal(meal);

                createMealViewModel.getLastSavedMealId().observe(cool, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        for (NutritionFactTable item : nutritionFactTableList) {
                            createMealViewModel.insertNourishment(integer, item);
                        }
                    }
                });

                Intent replyIntent = new Intent();
                setResult(RESULT_OK, replyIntent);
                finish();
            }
        });
        fragmentManager = getSupportFragmentManager();

        RecyclerView recyclerView = findViewById(R.id.list_ingredients_recyclerView);
        ingredientListAdapter = new IngredientListAdapter(this, this);
        recyclerView.setAdapter(ingredientListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void searchForNourishment(View view) {
        fragment = AddNourishmentFragment.newInstance(this,
                new ArrayList<>(ingredientSet));
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction
                .add(R.id.create_meal_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void closeFragment(List<?> bundle) {
        MainActivity.hideKeyboard(this);
        fragmentManager.beginTransaction().remove(fragment).commit();

        for (Object item : bundle) {
            NutritionFactTable nut = (NutritionFactTable) item;
            nutritionFactTableList.add(nut);
            ingredientSet.add(nut.getNourishment_name());
        }
        if (!ingredientSet.isEmpty()) {
            ingredientListAdapter.setNutritionNames(new ArrayList<>(ingredientSet));
            ingredientListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onNutritionListener(int position) {

    }
}