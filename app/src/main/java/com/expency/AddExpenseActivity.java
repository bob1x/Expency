package com.expency;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.expency.Model.Expense;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddExpenseActivity extends AppCompatActivity {
    private EditText etAmount;
    private EditText etDescription;
    private EditText etDate;
    private Spinner spCategory;
    private Button btnSaveExpense;
    private Button btnCancel;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        initActivity();
    }

    private void initActivity() {
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        spCategory = findViewById(R.id.spCategory);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnCancel = findViewById(R.id.btnCancel);

        // Populate the Spinner with predefined categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Set up date picker for etDate
        etDate.setOnClickListener(view -> showDatePickerDialog());

        addListeners();
    }

    private void addListeners() {
        btnSaveExpense.setOnClickListener(view -> saveExpense());
        btnCancel.setOnClickListener(view -> cancel());
    }

    private void showDatePickerDialog() {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddExpenseActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Format the date as YYYY-MM-DD
                    String date = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    etDate.setText(date);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void saveExpense() {
        String amountString = etAmount.getText().toString();
        String description = etDescription.getText().toString();
        String date = etDate.getText().toString();
        String category = spCategory.getSelectedItem().toString();

        // Validate input fields
        if (amountString.isEmpty() || description.isEmpty() || date.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountString);

        Map<String, Object> expense = new HashMap<>();
        expense.put("amount", amount);
        expense.put("description", description);
        expense.put("date", date);
        expense.put("category", category);

        db.collection("expenses")
                .add(expense)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Clear fields after successful save
                        etAmount.setText("");
                        etDescription.setText("");
                        etDate.setText("");
                        spCategory.setSelection(0);
                        etAmount.requestFocus();

                        // Redirect to ExpensesShowActivity
                        Intent intent = new Intent(AddExpenseActivity.this, ExpenseShowActivity.class);
                        startActivity(intent);
                        finish(); // Optionally, finish the current activity if you don't want to return to it

                        Toast.makeText(AddExpenseActivity.this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("AddExpenseActivity", "Error adding expense", e);
                        Toast.makeText(AddExpenseActivity.this, "Error adding expense", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }
}

