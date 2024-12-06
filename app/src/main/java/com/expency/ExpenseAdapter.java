package com.expency;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.expency.Model.Expense;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private ExpenseShowActivity activity;
    private List<Expense> expenseList;
    TextView  expenseDate;


    // Constructor - If the list is null, initialize it to an empty list
    public ExpenseAdapter(List<Expense> expenseList, ExpenseShowActivity activity) {
        this.expenseList = expenseList;
        this.activity = activity;
    }

//    public ExpenseAdapter(List<Expense> expenseListe, MainActivity mainActivity) {
//        this.expenseList = expenseList;
//        this.activity = activity;
//    }

    @Override
    public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.expenseName.setText(expense.getDescription());
        holder.amount.setText("TND " + expense.getAmount());
        holder.expenseDate.setText(expense.getDate());  // Set the date

        // Set the position as a tag
        holder.itemView.setTag(position);

        // Set the click listener
        holder.itemView.setOnClickListener(v -> {
            // Pass selected expense and position to the dialog
            activity.showOptionsDialog(v, expense, position); // Calling the method from Activity
        });
    }

    @Override
    public int getItemCount() {
        // Return 0 if the list is null or empty
        return expenseList != null ? expenseList.size() : 0;
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView expenseName, amount,expenseDate ;

        public ExpenseViewHolder(View itemView) {

            super(itemView);
            expenseName = itemView.findViewById(R.id.tvExpenseName);
            amount = itemView.findViewById(R.id.tvAmount);
            expenseDate = itemView.findViewById(R.id.expenseDate);
        }
    }
}
