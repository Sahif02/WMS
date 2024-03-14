package com.example.wms;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class list extends Fragment implements ListAdapter.OnItemClickListener{

    private RecyclerView recyclerView;
    private ApiService apiService;
    private ListAdapter listAdapter;
    private Button locatebtn; // Declare locatebtn as a member variable

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        locatebtn = view.findViewById(R.id.locate); // Initialize locatebtn
        locatebtn.setVisibility(View.GONE); // Initially hide locatebtn

        Button addButton = view.findViewById(R.id.addListButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });

        // Locate button
        locatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment locateFragment = new locate();

                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, locateFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        recyclerView = view.findViewById(R.id.listItemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        return view;
    }

    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        EditText inputListId = bottomSheetView.findViewById(R.id.inputListId);
        Button addButton = bottomSheetView.findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the list ID from the input field
                String listId = inputListId.getText().toString();

                // Check if user input is not empty
                if (!listId.isEmpty()) {
                    // Fetch the list items based on the entered list ID
                    checkListIdAndFetchItems(listId);
                }

                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }


    private void checkListIdAndFetchItems(String listId) {
        // Initialize Retrofit with logging
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://wms-api-u98x.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        // Create an instance of the ApiService interface
        apiService = retrofit.create(ApiService.class);

        Call<List<Item>> call = apiService.getItemByListID(listId);

        call.enqueue(new Callback<List<Item>>() {
            @Override
            public void onResponse(Call<List<Item>> call, Response<List<Item>> response) {
                if (response.isSuccessful()) {
                    List<Item> listItems = response.body();
                    // Populate the RecyclerView with history items
                    listAdapter = new ListAdapter(listItems, list.this);
                    recyclerView.setAdapter(listAdapter);

                    locatebtn.setVisibility(View.VISIBLE);
                } else {
                    // Handle error response
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    @Override
    public void onItemClick(Item listItem) {
        showBottomSheetDialog();
    }
}
