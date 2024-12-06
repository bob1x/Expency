    package com.expency;

    import static com.google.common.reflect.Reflection.initialize;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.widget.ArrayAdapter;
    import android.widget.EditText;
    import android.widget.ListView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.appcompat.app.ActionBarDrawerToggle;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.view.GravityCompat;
    import androidx.drawerlayout.widget.DrawerLayout;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import androidx.appcompat.widget.Toolbar;
    import com.expency.Model.Expense;
    import com.expency.R;
    import com.google.android.material.bottomnavigation.BottomNavigationView;
    import com.google.android.material.navigation.NavigationView;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;
    import com.google.firebase.firestore.QueryDocumentSnapshot;

    import java.util.ArrayList;
    import java.util.List;

    public class MainActivity extends AppCompatActivity {
        private TextView tvWelcome;
        private FirebaseAuth mAuth;
        private DrawerLayout drawerLayout;
        private NavigationView navigationView;

        private FirebaseFirestore db;
        private ActivityResultLauncher<Intent> ajoutActivityResultLauncher;
        private RecyclerView rvExpenses;
        private ExpenseAdapterA expenseAdapter;
        private List<Expense> expenseList = new ArrayList<>();
        private TextView tvTotalSpending;
        private double totalSpending = 0.0;





        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                // Redirect to SignupActivity if no user is signed in
                startActivity(new Intent(this, SignupActivity.class));
                finish();
                return;
            }


            initialize();
            drawerLayout = findViewById(R.id.drawer_layout);
            Toolbar toolbar = findViewById(R.id.toolbar);
            navigationView = findViewById(R.id.nav_view);

            // Set up NavigationView's header with user info
            TextView navHeaderUserName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_user_name);
            navHeaderUserName.setText(mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "Guest");
            setSupportActionBar(toolbar);


            LoadExpenses();
            Calci();
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            toolbar.setNavigationOnClickListener(view -> {
                drawerLayout.openDrawer(GravityCompat.START);
                Log.d("Debug", "Navigation icon clicked!");
            });
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_view_expenses) {
                    Intent intent = new Intent(MainActivity.this, ExpenseShowActivity.class);
                    startActivity(intent);
                } else if (itemId == R.id.nav_logout) {
                    logout();
                    Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();

                }
                return true;
            });

            if (savedInstanceState == null) {
                navigationView.setCheckedItem(R.id.nav_view_expenses);
            }
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_home) {
                    return true;
                } else if (item.getItemId() == R.id.nav_statistics) {
                    Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            });



            ajoutActivityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            LoadExpenses();
                        }
                    });

        }
        @Override
        public void onBackPressed() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
        private void initialize() {

            rvExpenses = findViewById(R.id.rvExpenses);
            rvExpenses.setLayoutManager(new LinearLayoutManager(this));  // Set layout manager
            expenseAdapter = new ExpenseAdapterA(expenseList, this);  // Pass Activity as listener
            rvExpenses.setAdapter(expenseAdapter);
            // UI Components
            tvWelcome = findViewById(R.id.tvWelcome);
            // Firebase Authentication
            mAuth = FirebaseAuth.getInstance();

            db = FirebaseFirestore.getInstance();
            ecouteurs();
            tvTotalSpending = findViewById(R.id.tvTotalSpending);
            updateTotalSpending();


        }

        private void ecouteurs() {
            findViewById(R.id.fabAddExpense).setOnClickListener(v -> Add() );

            TextView seeMoreLink = findViewById(R.id.seeMoreLink);
            seeMoreLink.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ExpenseShowActivity.class);
                startActivity(intent);  // Start the activity
            });


        }

        private void logout() {
            mAuth.signOut(); // Sign out from Firebase
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close the current activity (MainActivity)
            Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        }

        private void LoadExpenses() {
            db.collection("expenses")
                    .limit(3)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        expenseList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Expense expense = document.toObject(Expense.class);
                            expenseList.add(expense);
                        }
                        expenseAdapter.notifyDataSetChanged();

                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching expenses", e);
                        Toast.makeText(this, "Error fetching recent expenses.", Toast.LENGTH_SHORT).show();
                    });
        }
        private void Calci() {
            db.collection("expenses")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        totalSpending = 0.0;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Expense expense = document.toObject(Expense.class);
                            totalSpending += expense.getAmount();
                        }

                        updateTotalSpending();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching expenses", e);
                        Toast.makeText(this, "Error fetching all expenses.", Toast.LENGTH_SHORT).show();
                    });
        }

        private void Add() {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            ajoutActivityResultLauncher.launch(intent);
        }

        private void updateTotalSpending() {
            tvTotalSpending.setText("Total Spending: " + String.format("%.2f", totalSpending) + " TND");
        }

    }
