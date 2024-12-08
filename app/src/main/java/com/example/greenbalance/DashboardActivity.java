package com.example.greenbalance;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.greenbalance.adapters.NotebookAdapter;
import com.example.greenbalance.properties.Configuration;
import com.example.greenbalance.model.NotebookItemModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import android.content.Context;

public class DashboardActivity extends AppCompatActivity {

    TextView welcomeTextView;
    private RecyclerView recyclerView;
    private NotebookAdapter adapter;
    private List<NotebookItemModel> notebookList = new ArrayList<>();

    LinearLayoutManager layoutManager;
    private static final int NOTEBOOK_REQUEST_CODE = 1;

    private TextView todayExpensesTextView;
    private TextView todayEarningsTextView;
    private TextView monthExpensesTextView;
    private TextView monthEarningsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "User");

        welcomeTextView = findViewById(R.id.welcome_text);
        // Set the welcome message
        welcomeTextView.setText("Welcome, " + username + "!");

        recyclerView = findViewById(R.id.notebook_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            adapter = new NotebookAdapter(notebookList, new NotebookAdapter.OnNotebookClickListener() {
            @Override
            public void onNotebookClick(NotebookItemModel notebook) {
                if (notebook.getId().equals("add_new")) {
                    showCreateNotebookDialog();
                } else {
                    Intent intent = new Intent(DashboardActivity.this, NotebookActivity.class);
                    intent.putExtra("notebook_id", notebook.getId());
                    intent.putExtra("notebook_title", notebook.getTitle());
                    intent.putExtra("notebook_description", notebook.getDescription());
                    startActivityForResult(intent, NOTEBOOK_REQUEST_CODE);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        todayExpensesTextView = findViewById(R.id.today_expenses);
        todayEarningsTextView = findViewById(R.id.today_earnings);
        monthExpensesTextView = findViewById(R.id.month_expenses);
        monthEarningsTextView = findViewById(R.id.month_earnings);

        // Set up user profile button click listener
        ImageButton userProfileButton = findViewById(R.id.user_profile_button);
        userProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Check internet connection before fetching data
        if (!isInternetAvailable()) {
            showNoInternetDialog("No Internet Connection", 
                "Unable to start. Please check your internet connection and try again.");
        } else {
            checkApiConnection(() -> {
                fetchNotebooks();
                fetchReports();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Refresh username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        
        // Update welcome message
        welcomeTextView.setText("Welcome, " + username + "!");
    }

    private void fetchNotebooks() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = preferences.getString("id", null);


        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(this);

        String API_URL = Configuration.API_URL + "/v1/notebooks/"+userId;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                response -> {
                    try {
                        // Check if the response is successful
                        if (response.getBoolean("success")) {
                            JSONArray notebooks = response.getJSONArray("data");
                            notebookList.clear();
                            
                            // Add the "plus" card at the beginning
                            notebookList.add(new NotebookItemModel("add_new", "Add New", "Create a new notebook", 0.0f));
                            
                            for (int i = 0; i < notebooks.length(); i++) {
                                JSONObject obj = notebooks.getJSONObject(i);
                                String id = obj.getString("id");
                                String title = obj.getString("title");
                                String description = obj.getString("description");
                                Float value = Float.valueOf(obj.getString("value"));

                                notebookList.add(new NotebookItemModel(id, title, description, value));
                            }

                            Log.d(
                                    "[Dashboard Activity] notebookList",
                                    "List: " + notebookList.toString()
                            );
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(DashboardActivity.this, "Failed to load notebooks", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DashboardActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.d("API LINK", API_URL);
                    Toast.makeText(DashboardActivity.this, "Error fetching notebooks", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", error.toString());
                }
        );

        queue.add(request);
    }

    private void showCreateNotebookDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_notebook, null);
        
        EditText titleInput = dialogView.findViewById(R.id.edit_text_title);
        EditText descriptionInput = dialogView.findViewById(R.id.edit_text_description);
        
        builder.setView(dialogView)
                .setTitle("Create New Notebook")
                .setPositiveButton("Create", (dialog, id) -> {
                    String title = titleInput.getText().toString();
                    String description = descriptionInput.getText().toString();
                    String url = Configuration.API_URL + "/v1/notebooks/";
                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("userId", getSharedPreferences("user_prefs", MODE_PRIVATE).getString("id", null));
                        jsonBody.put("title", title);
                        jsonBody.put("description", description);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.POST, url, jsonBody,
                            response -> {
                                try {
                                    if (response.getBoolean("success")) {
                                        Toast.makeText(this, "Notebook created successfully", Toast.LENGTH_SHORT).show();
                                        fetchNotebooks();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> Toast.makeText(this, "Error updating notebook", Toast.LENGTH_SHORT).show()
                    ) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("x-api-key", Configuration.API_KEY);
                            return headers;
                        }
                    };

                    Volley.newRequestQueue(this).add(request);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Refresh the username by fetching from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String username = prefs.getString("username", "User");
            welcomeTextView.setText("Welcome, " + username + "!");
        }
    }

    private void fetchReports() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = preferences.getString("id", null);

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Configuration.API_URL + "/v1/dashboard/reports/" + userId;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONObject data = response.getJSONObject("data");
                            
                            // Format the numbers to 2 decimal places
                            String expenses = String.format("%.2f", Math.abs(data.getDouble("expenses")));
                            String gained = String.format("%.2f", data.getDouble("gained"));
                            String monthExpenses = String.format("%.2f", Math.abs(data.getDouble("monthExpenses")));
                            String monthGained = String.format("%.2f", data.getDouble("monthGained"));

                            // Update UI elements
                            todayExpensesTextView.setText(expenses);
                            todayEarningsTextView.setText(gained);
                            monthExpensesTextView.setText(monthExpenses);
                            monthEarningsTextView.setText(monthGained);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DashboardActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(DashboardActivity.this, "Error fetching reports", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("x-api-key", Configuration.API_KEY);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    private void checkApiConnection(Runnable onSuccess) {
        String rootUrl = Configuration.API_URL.split("/api")[0];
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                rootUrl,
                null,
                response -> {
                    // API is accessible, proceed with normal flow
                    onSuccess.run();
                },
                error -> {
                    // API is not accessible
                    runOnUiThread(() -> {
                        showNoInternetDialog("API Server Unreachable", 
                            "Unable to connect to the server. Please try again later.");
                    });
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("x-api-key", Configuration.API_KEY);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void showNoInternetDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_no_internet, null);
        
        // Update the dialog text views with the provided title and message
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        titleView.setText(title);
        messageView.setText(message);
        
        builder.setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("Exit", (dialog, id) -> {
                    finish();
                })
                .setNeutralButton("Retry", (dialog, id) -> {
                    if (!isInternetAvailable()) {
                        showNoInternetDialog("No Internet Connection", 
                            "Unable to start. Please check your internet connection and try again.");
                    } else {
                        checkApiConnection(() -> {
                            fetchNotebooks();
                            fetchReports();
                        });
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}