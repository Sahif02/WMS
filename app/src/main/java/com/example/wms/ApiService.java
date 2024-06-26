package com.example.wms;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @GET("api/users/{username}")
    Call<User> getUserByUsername(@Path("username") String username);

    @GET("api/lists")
    Call<List<Lists>> getList();

    @GET("api/lists/{listID}/items")
    Call<List<Item>> getItemByListID(@Path("listID") String listID);

    @PUT("api/lists/{listID}/items/{itemID}")
    Call<Void> updateStatusItem(@Path("listID") String listID, @Path("itemID") String itemID, @Body Item updatedStatusItem);
}

