package com.demarkelabs.crud_guide_java;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private ProgressDialog progressDialog;
    private View popupInputDialogView = null;
    private EditText titleInput = null;
    private EditText descriptionInput = null;
    private Button saveTodoButton = null;
    private Button cancelUserDataButton = null;

    private FloatingActionButton openInputPopupDialogButton = null;
    private RecyclerView recyclerView;
    private TextView empty_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(MainActivity.this);

        initMainActivityControls();
        getTodoList();


        openInputPopupDialogButton.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Create a TODO");
            alertDialogBuilder.setCancelable(true);
            initPopupViewControls();
            //We are setting our custom popup view by AlertDialog.Builder
            alertDialogBuilder.setView(popupInputDialogView);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            saveTodoButton.setOnClickListener(view2 -> {
                ParseObject todo = new ParseObject("Todo");
                if (titleInput.getText().toString().length() != 0 && descriptionInput.getText().toString().length() != 0) {
                    alertDialog.cancel();
                    progressDialog.show();
                    todo.put("title", titleInput.getText().toString());
                    todo.put("description", descriptionInput.getText().toString());
                    todo.saveInBackground(e -> {
                        progressDialog.dismiss();
                        if (e == null) {
                            getTodoList();
                        } else {
                            showAlert("Error", e.getMessage());
                        }
                    });
                } else {
                    showAlert("Error", "Please enter a title and description");
                }
            });
            cancelUserDataButton.setOnClickListener(view1 -> alertDialog.cancel());
        });
    }

    private void initMainActivityControls() {
        recyclerView = findViewById(R.id.recyclerView);
        empty_text = findViewById(R.id.empty_text);
        if (openInputPopupDialogButton == null) {
            openInputPopupDialogButton = findViewById(R.id.fab);
        }
    }

    private void getTodoList() {
        progressDialog.show();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Todo");
        query.orderByDescending("createdAt");
        query.findInBackground((objects, e) -> {
            progressDialog.dismiss();
            if (e == null) {
                //We are initializing Todo object list to our adapter
                initTodoList(objects);
            } else {
                showAlert("Error", e.getMessage());
            }
        });
    }

    private void initTodoList(List<ParseObject> list) {
        if (list!=null && list.size()==0)
            empty_text.setVisibility(View.VISIBLE);
        else if (list != null)
            empty_text.setVisibility(View.GONE);
        else{
            empty_text.setVisibility(View.GONE);
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        TodoAdapter adapter = new TodoAdapter(list, this);

        adapter.clickListenerToDelete.observe(this, parseObject -> {
            progressDialog.show();
            parseObject.deleteInBackground(e -> {
                progressDialog.dismiss();
                if (e == null) {
                    getTodoList();
                } else {
                    showAlert("Error",e.getMessage());
                }
            });
        });

        adapter.clickListenerToEdit.observe(this, parseObject -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setTitle("Update a TODO");
            alertDialogBuilder.setCancelable(true);
            initPopupViewControls(parseObject.getString("title"), parseObject.getString("description"));
            alertDialogBuilder.setView(popupInputDialogView);
            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            saveTodoButton.setOnClickListener(view2 -> {
                if (titleInput.getText().toString().length() != 0 && descriptionInput.getText().toString().length() != 0) {
                    alertDialog.cancel();
                    progressDialog.show();
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Todo");
                    query.getInBackground(parseObject.getObjectId(), (todo, e) -> {
                        if (e == null) {
                            todo.put("title", titleInput.getText().toString());
                            todo.put("description", descriptionInput.getText().toString());
                            todo.saveInBackground(e1 -> {
                                progressDialog.dismiss();
                                if (e1 == null) {
                                    getTodoList();
                                } else {
                                    showAlert("Error", e1.getMessage());
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            showAlert("Error", e.getMessage());
                        }
                    });
                } else {
                    showAlert("Error", "Please enter a title and description");
                }


            });
            cancelUserDataButton.setOnClickListener(view1 -> alertDialog.cancel());
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
    }

    private void initPopupViewControls() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        popupInputDialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null);
        titleInput = popupInputDialogView.findViewById(R.id.titleInput);
        descriptionInput = popupInputDialogView.findViewById(R.id.descriptionInput);
        saveTodoButton = popupInputDialogView.findViewById(R.id.button_save_todo);
        cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user_data);
    }

    private void initPopupViewControls(String title, String description) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        popupInputDialogView = layoutInflater.inflate(R.layout.custom_alert_dialog, null);
        titleInput = popupInputDialogView.findViewById(R.id.titleInput);
        descriptionInput = popupInputDialogView.findViewById(R.id.descriptionInput);
        saveTodoButton = popupInputDialogView.findViewById(R.id.button_save_todo);
        cancelUserDataButton = popupInputDialogView.findViewById(R.id.button_cancel_user_data);

        titleInput.setText(title);
        descriptionInput.setText(description);
    }

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
        AlertDialog ok = builder.create();
        ok.show();
    }
}