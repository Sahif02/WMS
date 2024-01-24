package com.example.wms;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    @GET("api/users/{username}")
    Call<User> getUserByUsername(@Path("username") String username);
}

