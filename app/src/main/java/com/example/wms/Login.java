package com.example.wms;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform login when the "Sign In" button is clicked
                performLogin();
            }
        });

        // Handle "Forgot Password?" action
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle forgot password action
                Toast.makeText(Login.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogin() {
        String username = usernameEditText.getText().toString();

        // Call the API using Retrofit
        Call<User> call = apiService.getUserByUsername(username);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String storedPassword = user.getPassword();
                    String storedRole = user.getRole();

                    // Check entered password against stored password
                    String enteredPassword = passwordEditText.getText().toString();

                    if (enteredPassword.equals(storedPassword)) {

                        if(storedRole.equals("staff")){

                            // Passwords match, login successful
                            Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();


                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.putExtra("userDetails", user);
                            startActivity(intent);

                            finish(); // Close the LoginActivity

                        }else{
                            // Passwords match, login successful
                            Toast.makeText(Login.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // Passwords do not match, login failed
                        Toast.makeText(Login.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle unsuccessful API response
                    Toast.makeText(Login.this, "API Request Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // Handle API request failure
                Toast.makeText(Login.this, "API Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
