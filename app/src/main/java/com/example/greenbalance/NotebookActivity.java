package com.example.greenbalance;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.greenbalance.adapters.EntryAdapter;
import com.example.greenbalance.properties.Configuration;
import com.example.greenbalance.model.EntryModel;
import com.example.greenbalance.utility.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.TimeZone;

public class NotebookActivity extends AppCompatActivity {
    private String notebookId;
    private TextView notebookTitleView;
    private RecyclerView entriesRecyclerView;
    private EntryAdapter adapter;
    private List<EntryModel> entriesList = new ArrayList<>();
    private final String userId = Prefs.getKeyId();
    private String notebookTitle;
    private String notebookDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook);

        notebookId = getIntent().getStringExtra("notebook_id");
        notebookTitle = getIntent().getStringExtra("notebook_title");
        notebookDescription = getIntent().getStringExtra("notebook_description");

        notebookTitleView = findViewById(R.id.notebook_title);
        notebookTitleView.setText(notebookTitle);

        Button addEntryButton = findViewById(R.id.add_entry_button);
        addEntryButton.setOnClickListener(v -> showEntryDialog(null));

        ImageButton notebookMenuButton = findViewById(R.id.notebook_menu_button);
        notebookMenuButton.setOnClickListener(v -> showNotebookMenu());

        ImageButton ntbkBack = findViewById(R.id.ntbkback);

        ntbkBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        fetchEntries();
    }

    private void fetchNotebookById() {
        String url = Configuration.API_URL + "/v1/notebooks/notebook/" + notebookId;
        
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject notebook = response.getJSONObject("data");
                        notebookTitle = notebook.getString("title");
                        notebookDescription = notebook.getString("description");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error fetching notebook", Toast.LENGTH_SHORT).show()
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

    private void showNotebookMenu() {
        // First fetch latest notebook data
        fetchNotebookById();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_notebook_menu, null);

        EditText titleInput = dialogView.findViewById(R.id.notebook_title_input);
        EditText descriptionInput = dialogView.findViewById(R.id.notebook_description_input);
        
        // Pre-fill with latest data
        titleInput.setText(notebookTitle);
        descriptionInput.setText(notebookDescription);
        
        builder.setView(dialogView)
                .setTitle("Notebook Options")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = titleInput.getText().toString();
                    String newDescription = descriptionInput.getText().toString();
                    updateNotebook(newTitle, newDescription);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .setNeutralButton("Delete Notebook", (dialog, which) -> confirmDelete());

        builder.create().show();
    }

    private void updateNotebook(String title, String description) {
        String url = Configuration.API_URL + "/v1/notebooks/";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", notebookId);
            jsonBody.put("userId", userId);
            jsonBody.put("title", title);
            jsonBody.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            notebookTitle = title;
                            notebookDescription = description;
                            notebookTitleView.setText(title);
                            Toast.makeText(this, "Notebook updated successfully", Toast.LENGTH_SHORT).show();
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
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Notebook")
                .setMessage("Are you sure you want to delete this notebook? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteNotebook())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNotebook() {
        String url = Configuration.API_URL + "/v1/notebooks/delete";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", notebookId);
            jsonBody.put("userId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Notebook deleted successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish(); // Return to previous activity
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error deleting notebook", Toast.LENGTH_SHORT).show()
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

    private void setupRecyclerView() {
        entriesRecyclerView = findViewById(R.id.entries_recycler_view);
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EntryAdapter(entriesList, entry -> showEntryDialog(entry));
        entriesRecyclerView.setAdapter(adapter);
    }

    private void showEntryDialog(EntryModel entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_entry, null);

        EditText titleInput = dialogView.findViewById(R.id.entry_title_input);
        EditText descriptionInput = dialogView.findViewById(R.id.entry_description_input);
        EditText valueInput = dialogView.findViewById(R.id.entry_value_input);

        if (entry != null) {
            // Edit mode
            titleInput.setText(entry.getTitle());
            descriptionInput.setText(entry.getDescription());
            valueInput.setText(String.valueOf(entry.getValue()));
        }

        builder.setView(dialogView)
                .setTitle(entry == null ? "Add Entry" : "Edit Entry")
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleInput.getText().toString();
                    String description = descriptionInput.getText().toString();
                    float value;
                    try {
                        value = Float.parseFloat(valueInput.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (entry == null) {
                        // Add new entry
                        addEntry(title, description, value);
                    } else {
                        // Update existing entry
                        updateEntry(entry.getId(), title, description, value);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        if (entry != null) {
            builder.setNeutralButton("Delete", (dialog, which) -> deleteEntry(entry.getId()));
        }

        builder.create().show();
    }

    private void addEntry(String title, String description, float value) {
        String url = Configuration.API_URL + "/v1/entries/";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", userId);
            jsonBody.put("notebookId", notebookId);
            jsonBody.put("title", title);
            jsonBody.put("description", description);
            jsonBody.put("value", value);
            
            // Add current timestamp in the desired format
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy (HH:mm)", Locale.US);
            String currentTime = dateFormat.format(new Date());
            jsonBody.put("createdAt", currentTime);
            
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Entry added successfully", Toast.LENGTH_SHORT).show();
                            fetchEntries();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error adding entry", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-api-key", Configuration.API_KEY);  // Add API key to headers
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void updateEntry(String entryId, String title, String description, float value) {
        String url = Configuration.API_URL + "/v1/entries/";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", entryId);
            jsonBody.put("userId", userId);
            jsonBody.put("notebookId", notebookId);
            jsonBody.put("title", title);
            jsonBody.put("description", description);
            jsonBody.put("value", value);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Entry updated successfully", Toast.LENGTH_SHORT).show();
                            fetchEntries();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error updating entry", Toast.LENGTH_SHORT).show()
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-api-key", Configuration.API_KEY);  // Add API key to headers
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void deleteEntry(String entryId) {
        String url = Configuration.API_URL + "/v1/entries/delete";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id", entryId);
            jsonBody.put("userId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
                            fetchEntries();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error deleting entry", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("x-api-key", Configuration.API_KEY);  // Add API key to headers
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void fetchEntries() {
        String url = Configuration.API_URL + "/v1/notebooks/" + notebookId + "/entries";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray entries = response.getJSONArray("data");
                            entriesList.clear();
                            for (int i = 0; i < entries.length(); i++) {
                                JSONObject entry = entries.getJSONObject(i);
                                
                                // Format the date
                                String createdAt = "";
                                if (entry.has("createdAt")) {
                                    String rawDate = entry.getString("createdAt");
                                    try {
                                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                        SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd-yyyy (HH:mm)", Locale.US);
                                        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        Date date = inputFormat.parse(rawDate);
                                        createdAt = outputFormat.format(date);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        createdAt = "Date unknown";
                                    }
                                }

                                entriesList.add(new EntryModel(
                                        entry.getString("id"),
                                        entry.getString("title"),
                                        entry.getString("description"),
                                        Float.parseFloat(entry.getString("value")),
                                        createdAt
                                ));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing entries", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching entries", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
}