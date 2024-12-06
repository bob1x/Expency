package com.expency;

import com.expency.Model.Expense;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 123;

    private PieChart pieChart;
    private FirebaseFirestore db;
    private FloatingActionButton fabExportCSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        pieChart = findViewById(R.id.pieChart);
        db = FirebaseFirestore.getInstance();
        LoadExpenses();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_statistics) {

                return true;
            } else {
                return false;
            }
        });
        initializeA();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        fabExportCSV = findViewById(R.id.fabExportCSV);
        fabExportCSV.setOnClickListener(view -> showExportDialog());
    }

    private void initializeA() {
        fabExportCSV = findViewById(R.id.fabExportCSV);

    }

    private void showExportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export CSV")
                .setMessage("Do you want to export the expenses data as CSV?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Call the method to fetch data and export it
                        exportCSV();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void exportCSV() {
        db.collection("expenses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        StringBuilder csvData = new StringBuilder();
                        csvData.append("Amount,Description,Date,Category\n");

                        for (DocumentSnapshot document : documents) {
                            // Get data from Firestore
                            double amount = document.getDouble("amount");
                            String description = document.getString("description");
                            String date = document.getString("date");
                            String category = document.getString("category");

                            // Append data to CSV
                            csvData.append(amount)
                                    .append(",")
                                    .append(description)
                                    .append(",")
                                    .append(date)
                                    .append(",")
                                    .append(category)
                                    .append("\n");
                        }

                        // Save CSV data to file
                        saveCSVToFile(csvData.toString());
                    } else {
                        Toast.makeText(this, "Error fetching data from Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveCSVToFile(String csvData) {
        // Check if external storage is available for writing
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                // For devices running Android 10 (API level 29) or earlier
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File file = new File(downloadsDirectory, "expenses.csv");

                    // Write the CSV data to the file
                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        outputStream.write(csvData.getBytes());
                    }

                    // Notify the user that the export was successful
                    Toast.makeText(this, "CSV exported to Downloads folder", Toast.LENGTH_SHORT).show();
                }
                // For Android 11 (API level 30) and above
                else {
                    // Get a Uri for the Downloads folder
                    ContentResolver resolver = getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "expenses.csv");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

                    // Write the CSV data to the file
                    try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                        outputStream.write(csvData.getBytes());
                    }

                    // Notify the user
                    Toast.makeText(this, "CSV exported to Downloads folder", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error exporting CSV", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "External storage not available", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportCSV();  // Permission granted, proceed with CSV export
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupPieChart(List<Expense> expenses) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // Calculate total expense categories
        float total = 0;
        float food = 0, entertainment = 0, transport = 0, shopping = 0, health = 0, other = 0;

        for (Expense expense : expenses) {
            total += expense.getAmount();  // Calculate the total amount
            switch (expense.getCategory()) {
                case "Food":
                    food += expense.getAmount();
                    break;
                case "Health":
                    health += expense.getAmount();
                    break;
                case "Transport":
                    transport += expense.getAmount();
                    break;
                case "Shopping":
                    shopping += expense.getAmount();
                    break;
                case "Entertainment":
                    entertainment += expense.getAmount();
                    break;
                default:
                    other += expense.getAmount();
                    break;
            }
        }

        // Add the data to the pie chart
        if (food > 0) entries.add(new PieEntry(food, "Food"));
        if (health > 0) entries.add(new PieEntry(health, "Health"));
        if (shopping > 0) entries.add(new PieEntry(shopping, "Shopping"));
        if (entertainment > 0) entries.add(new PieEntry(entertainment, "Entertainment"));
        if (transport > 0) entries.add(new PieEntry(transport, "Transport"));
        if (other > 0) entries.add(new PieEntry(other, "Other"));

        // Only show the chart if there are entries
        if (!entries.isEmpty()) {
            PieDataSet dataSet = new PieDataSet(entries, "Categories");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            // Set value text size and color
            dataSet.setValueTextSize(16f);
            dataSet.setValueTextColor(Color.BLACK);

            // Set slice spacing
            dataSet.setSliceSpace(6f);

            // Custom value formatter for percentage display
            float finalTotal = total;
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    // Calculate percentage
                    float percentage = (value / finalTotal) * 100;
                    return String.format("%.1f%%", percentage);
                }
            });

            // Set value text to be outside the slices
            dataSet.setValueLineColor(Color.BLACK);
            dataSet.setValueLinePart1Length(0.6f);
            dataSet.setValueLinePart2Length(0.4f);

            PieData pieData = new PieData(dataSet);
            pieChart.setData(pieData);

            // Increase the hole radius
            pieChart.setHoleRadius(50f);
            pieChart.setTransparentCircleRadius(61f);

            // Animate the chart
            pieChart.animateXY(1000, 1000);

            // Redraw the chart
            pieChart.invalidate();
        } else {
            Toast.makeText(this, "No chart data available", Toast.LENGTH_SHORT).show();
        }
    }


    private void LoadExpenses() {
        db.collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Expense> expenseList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Expense expense = document.toObject(Expense.class);
                        expense.setId(document.getId());
                        expenseList.add(expense);

                        // Log the fetched data
                        Log.d("StatisticsActivity", "Fetched expense: " + expense.getCategory() + " - " + expense.getAmount());
                    }

                    // Call setupPieChart to update the chart with the fetched expenses
                    setupPieChart(expenseList);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching expenses", e);
                    Toast.makeText(this, "Erreur de récupération des dépenses", Toast.LENGTH_SHORT).show();
                });
    }

}
