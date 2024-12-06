package com.expency;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expency.Model.Expense;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExpenseShowActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Expense> expenseList = new ArrayList<>();
    private FirebaseFirestore db;
    private RecyclerView rvExpenses;
    private ExpenseAdapter expenseAdapter;

    private ActivityResultLauncher<Intent> ajoutActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_show);

        initialize();

        ajoutActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        LoadExpenses();  // Reload expenses when modification is complete
                    }
                });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(ExpenseShowActivity.this, MainActivity.class);
                startActivity(intent);                return true;
            } else if (item.getItemId() == R.id.nav_statistics) {
                // Open Statistics Activity
                Intent intent = new Intent(ExpenseShowActivity.this, StatisticsActivity.class);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });
    }

    private void initialize() {
        rvExpenses = findViewById(R.id.rvExpenses);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));  // Set layout manager
        expenseAdapter = new ExpenseAdapter(expenseList, this);  // Pass Activity as listener
        rvExpenses.setAdapter(expenseAdapter);

        db = FirebaseFirestore.getInstance();
        LoadExpenses();
        setupSwipeToDelete();
        findViewById(R.id.fabAddExpense).setOnClickListener(v -> {
            Intent addExpenseIntent = new Intent(ExpenseShowActivity.this, AddExpenseActivity.class);
            ajoutActivityResultLauncher.launch(addExpenseIntent);
        });
    }

    private void LoadExpenses() {
        db.collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    expenseList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Expense expense = document.toObject(Expense.class);
                        expense.setId(document.getId());
                        expenseList.add(expense);
                    }
                    expenseAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de récupération des dépenses", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onClick(View v) {
        // Handle click event if needed
    }

    public void showOptionsDialog(View view, Expense selectedExpense, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose an Action")
                .setItems(new String[]{"Modify", "Delete"}, (dialog, which) -> {
                    if (which == 0) {

                        Intent modifyIntent = new Intent(ExpenseShowActivity.this, EditExpenseActivity.class);
                        modifyIntent.putExtra("expense_id", selectedExpense.getId());
                        ajoutActivityResultLauncher.launch(modifyIntent); // Use the launcher to handle the result
                    } else if (which == 1) {
                        deleteExpense(selectedExpense, position);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())
                .show();
    }

    private void deleteExpense(Expense selectedExpense, int position) {
        db.collection("expenses").document(selectedExpense.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    expenseList.remove(position);
                    expenseAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting expense", Toast.LENGTH_SHORT).show();
                });
    }


    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense swipedExpense = expenseList.get(position);

                new AlertDialog.Builder(ExpenseShowActivity.this)
                        .setTitle("Delete Expense")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteExpense(swipedExpense, position))
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            expenseAdapter.notifyItemChanged(position); // Restore item if canceled
                            dialog.dismiss();
                        })
                        .show();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    Paint paint = new Paint();
                    paint.setColor(getResources().getColor(android.R.color.holo_red_light));
                    paint.setStyle(Paint.Style.FILL);

                    View itemView = viewHolder.itemView;

                    // Draw red background
                    if (dX > 0) { // Swiping right
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(),
                                dX, (float) itemView.getBottom(), paint);
                    } else { // Swiping left
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                    }

                    // trash icon
                    Drawable icon = getResources().getDrawable(android.R.drawable.ic_menu_delete);
                    int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconBottom = iconTop + icon.getIntrinsicHeight();

                    if (dX > 0) { // Swiping right
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = iconLeft + icon.getIntrinsicWidth();
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    } else { // Swiping left
                        int iconRight = itemView.getRight() - iconMargin;
                        int iconLeft = iconRight - icon.getIntrinsicWidth();
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    }

                    icon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvExpenses);
    }

}
