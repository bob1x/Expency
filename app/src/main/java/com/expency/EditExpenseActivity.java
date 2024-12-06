package com.expency;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditExpenseActivity extends AppCompatActivity {

    private EditText edDescription, edAmount;
    private Spinner spCategory;
    private Button btnUpdate, btnCancel;
    private FirebaseFirestore db;
    private String expenseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_expense);

        // Initialize fields
        edDescription = findViewById(R.id.edDescription);
        edAmount = findViewById(R.id.edAmount);
        spCategory = findViewById(R.id.spCategory);  // Spinner for Category
        btnUpdate = findViewById(R.id.btnUpdate);
        btnCancel = findViewById(R.id.btnCancel);
        db = FirebaseFirestore.getInstance();

        // Load expense details
        expenseId = getIntent().getStringExtra("expense_id");
        loadExpenseDetails();

        // Add listeners
        btnUpdate.setOnClickListener(v -> updateExpense());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadExpenseDetails() {
        if (expenseId != null) {
            db.collection("expenses").document(expenseId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            edDescription.setText(documentSnapshot.getString("description"));
                            edAmount.setText(String.valueOf(documentSnapshot.getDouble("amount")));
                            String category = documentSnapshot.getString("category");

                            // Set selected category in Spinner (assuming categories are predefined in Spinner)
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spCategory.getAdapter();
                            int spinnerPosition = adapter.getPosition(category);
                            spCategory.setSelection(spinnerPosition);
                        } else {
                            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error loading expense", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateExpense() {
        String description = edDescription.getText().toString().trim();
        String amountText = edAmount.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString(); // Get selected category

        if (description.isEmpty() || amountText.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedExpense = new HashMap<>();
        updatedExpense.put("description", description);
        updatedExpense.put("amount", amount);
        updatedExpense.put("category", category);

        db.collection("expenses").document(expenseId).update(updatedExpense)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error updating expense", Toast.LENGTH_SHORT).show());
    }
}
