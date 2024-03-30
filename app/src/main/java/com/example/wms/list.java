package com.example.wms;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

                    if(!listItems.isEmpty()) {
                        // Populate the RecyclerView with history items
                        listAdapter = new ListAdapter(listItems, list.this);
                        recyclerView.setAdapter(listAdapter);

                        locatebtn.setVisibility(View.VISIBLE);

                        locatebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Fragment locateFragment = new locate();

                                FragmentManager fragmentManager = getFragmentManager();
                                Bundle bundle = new Bundle();

                                bundle.putString("listID", listId);
                                locateFragment.setArguments(bundle);

                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, locateFragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });
                    }
                    else {
                        Toast.makeText(requireContext(), "List Not Found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    Toast.makeText(requireContext(), "List Not Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Item>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    private void showBottomSheet(Item listItem) {
        // Create a BottomSheetDialog and set the layout
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_item_details_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Get TextViews and Buttons from the layout
        TextView itemname = bottomSheetView.findViewById(R.id.itemName);
        TextView quantity = bottomSheetView.findViewById(R.id.itemQuantity);
        TextView location = bottomSheetView.findViewById(R.id.itemLocation);
        EditText itemId = bottomSheetView.findViewById(R.id.itemID);
        Button updateButton = bottomSheetView.findViewById(R.id.updateButton);
        Button cancelButton = bottomSheetView.findViewById(R.id.cancelButton);

        // Set text for TextViews
        itemname.setText("Item Name: " + listItem.getItemName());
        quantity.setText("Quantity: " + listItem.getQuantity());
        location.setText("Location: " + listItem.getLocation());

        // Set click listener for updateButton
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the entered item ID from the EditText
                String enteredItemId = itemId.getText().toString();
                String status = "completed";

                // Check if the entered item ID matches the item's actual ID
                if (enteredItemId.equals(listItem.getItemID())) {

                    Item updatedItem = new Item();
                    updatedItem.setItemID(listItem.getItemID());
                    updatedItem.setStatus(status);

                    Call<Void> call = apiService.updateStatusItem(listItem.getListID(), listItem.getItemID(), updatedItem);

                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                // Handle successful update, for example, show a success message
                                Toast.makeText(requireContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();

                                bottomSheetDialog.dismiss(); // Close the update activity

                                checkListIdAndFetchItems(listItem.getListID());
                            } else {
                                // Log the error details
                                Log.e("UpdateItem", "Failed to update item. Response code: " + response.code() + ", Message: " + response.message());

                                // Handle error response
                                Toast.makeText(requireContext(), "Failed to update item", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Log the failure details
                            Log.e("UpdateReservation", "Network error", t);

                            // Handle failure
                            Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    // If IDs don't match, display a toast indicating incorrect ID
                    Toast.makeText(requireContext(), "Incorrect Item ID", Toast.LENGTH_SHORT).show();
                }

                bottomSheetDialog.dismiss(); // Dismiss the bottom sheet
            }
        });


        // Set click listener for cancelButton
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the bottom sheet
                bottomSheetDialog.dismiss();
            }
        });

        // Show the bottom sheet
        bottomSheetDialog.show();
    }

    @Override
    public void onItemClick(Item listItem) {
        if ("completed".equals(listItem.getStatus())) {
            Toast.makeText(requireContext(), "Completed", Toast.LENGTH_SHORT).show();
        } else {
            // Item status is not "completed", show a message indicating it cannot be updated
            showBottomSheet(listItem);
        }
    }

}
