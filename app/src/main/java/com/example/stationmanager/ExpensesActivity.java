package com.example.stationmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExpensesActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private ScrollView scrollView;
    private LinearLayout expensesLayout;
    private Button addButton;
    private Button totalButton;
    private Double totalExpense = 0.0;
    GraphView graphView;
    private String ownerk ;
    int i = 0;
    Double latt ;
    Double longg;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        // Initialize Firebase components
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        databaseReference = mDatabase.getReference();

        scrollView = findViewById(R.id.scrollView);
        expensesLayout = findViewById(R.id.expensesLayout);
        addButton = findViewById(R.id.addButton);
        totalButton = findViewById(R.id.totalButton);

        // Get latitude and longitude values passed from CostTracker
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String latitude = extras.getString("latitude");
            final String longitude = extras.getString("longitude");

            latt = Double.parseDouble(latitude);
            longg = Double.parseDouble(longitude);

            // Fetch expenses data corresponding to the selected charging station
            fetchExpenses(latitude, longitude);

            // Set click listener for Add button
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAddExpenseDialog(latitude, longitude);
                }
            });

            // Set click listener for Total button
            totalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    calculateTotalExpense();
                }
            });
        }
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String ownerMail = currentUser.getEmail();
        DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through each owner's data
                for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve owner's email
                    String ownerEmail = ownerSnapshot.child("email").getValue(String.class);

                    // Check if the owner's email matches the current user's email
                    if (ownerEmail.equals(ownerMail)) {
                        // Get the key of the owner
                        String ownerKey = ownerSnapshot.getKey();

                        ownerk = ownerKey;


                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(ExpensesActivity.this, "Failed to read owners data.", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Failed to read owners data.", databaseError.toException());
            }
        });

    }

    private void fetchExpenses(final String latitude, final String longitude) {
        Double lati = Double.parseDouble(latitude);
        Double longi = Double.parseDouble(longitude);
        // Query Firebase database to get expenses data for the selected charging station

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String ownerMail = currentUser.getEmail();

            // Add expense to Firebase database

        DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through each owner's data
                for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve owner's email
                    String ownerEmail = ownerSnapshot.child("email").getValue(String.class);

                    // Check if the owner's email matches the current user's email
                    if (ownerEmail.equals(ownerMail)) {
                        // Get the key of the owner
                        String ownerKey = ownerSnapshot.getKey();

                        ownerk = ownerKey;

                        // Retrieve existing locations if any
                        DatabaseReference ownerLocationRef = ownersRef.child(ownerKey).child("Locations");
                        ownerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Check if the location exists for the selected charging station
                                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                    Double latitu = locationSnapshot.child("latitude").getValue(Double.class);
                                    Double longitu = locationSnapshot.child("longitude").getValue(Double.class);

                                    // Assuming lati and longi are already defined
                                    if (lati.equals(latitu) && longi.equals(longitu)) {
                                        DatabaseReference expensesRef = locationSnapshot.child("Expenses").getRef();
                                        expensesRef.addChildEventListener(new ChildEventListener(){
                                                    @Override
                                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                        // When a new expense is added, update the UI
                                                        String expenseKey = dataSnapshot.getKey();
                                                        String description = dataSnapshot.child("description").getValue(String.class);
                                                        String amount = dataSnapshot.child("amount").getValue(String.class);
                                                        // Concatenate description and amount into a single string for UI display
                                                        String expenseValue = description + ": $" + amount;
                                                        addExpenseToLayout(expenseKey, expenseValue);
                                                    }

                                                    @Override
                                                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                        // Not needed for this implementation
                                                    }

                                                    @Override
                                                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                                        // When an expense is removed, update the UI
                                                        String expenseKey = dataSnapshot.getKey();
                                                        removeExpenseFromLayout(expenseKey);
                                                    }

                                                    @Override
                                                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                                        // Not needed for this implementation
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        // Handle error
                                                    }
                                                });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle error
                                Toast.makeText(ExpensesActivity.this, "Failed to read locations data.", Toast.LENGTH_SHORT).show();
                                Log.e("Firebase", "Failed to read locations data.", databaseError.toException());
                            }
                        });

                        // Exit loop since the owner is found
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(ExpensesActivity.this, "Failed to read owners data.", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Failed to read owners data.", databaseError.toException());
            }
        });


    }

    private void addExpenseToLayout(final String expenseKey, String expenseValue) {
        // Create TextView for displaying expense
        final TextView textView = new TextView(this);
        textView.setText(expenseValue);
        textView.setPadding(16, 8, 16, 8);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmRemoveDialog(expenseKey);
            }
        });
        textView.setTag(expenseKey); // Set tag to identify expense
        expensesLayout.addView(textView);
    }

    private void removeExpenseFromLayout(String expenseKey) {
        // Find TextView with the corresponding expenseKey and remove it from the layout
        for (int i = 0; i < expensesLayout.getChildCount(); i++) {
            View view = expensesLayout.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (textView.getTag() != null && textView.getTag().equals(expenseKey)) {
                    expensesLayout.removeView(textView);
                    break; // Exit loop after removing the TextView
                }
            }
        }
    }

    private void showAddExpenseDialog(final String latitude, final String longitude) {
        Double lati = Double.parseDouble(latitude);
        Double longi = Double.parseDouble(longitude);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Expense");

        // Set up the layout for the dialog
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);
        final EditText inputDescription = viewInflated.findViewById(R.id.inputDescription);
        final EditText inputAmount = viewInflated.findViewById(R.id.inputAmount);
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = inputDescription.getText().toString();
                String amount = inputAmount.getText().toString();

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                String ownerMail = currentUser.getEmail();

                if (!description.isEmpty() && !amount.isEmpty()) {
                    // Add expense to Firebase database

                    DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference().child("Owners");
                    ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Iterate through each owner's data
                            for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                                // Retrieve owner's email
                                String ownerEmail = ownerSnapshot.child("email").getValue(String.class);

                                // Check if the owner's email matches the current user's email
                                if (ownerEmail.equals(ownerMail)) {
                                    // Get the key of the owner
                                    String ownerKey = ownerSnapshot.getKey();

                                    ownerk = ownerKey;

                                    // Retrieve existing locations if any
                                    DatabaseReference ownerLocationRef = ownersRef.child(ownerKey).child("Locations");
                                    ownerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            // Check if the location exists for the selected charging station
                                            for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                Double latitu = locationSnapshot.child("latitude").getValue(Double.class);
                                                Double longitu = locationSnapshot.child("longitude").getValue(Double.class);

                                                // Assuming lati and longi are already defined
                                                if (lati.equals(latitu) && longi.equals(longitu)) {
                                                    DatabaseReference expensesRef = locationSnapshot.child("Expenses").getRef();
                                                    String expenseKey = expensesRef.push().getKey();
                                                    Map<String, Object> expenseMap = new HashMap<>();
                                                    expenseMap.put("description", description);
                                                    expenseMap.put("amount", amount);
                                                    expensesRef.child(expenseKey).setValue(expenseMap);
                                                    break; // Exit loop since the location is found
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            // Handle error
                                            Toast.makeText(ExpensesActivity.this, "Failed to read locations data.", Toast.LENGTH_SHORT).show();
                                            Log.e("Firebase", "Failed to read locations data.", databaseError.toException());
                                        }
                                    });

                                    // Exit loop since the owner is found
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                            Toast.makeText(ExpensesActivity.this, "Failed to read owners data.", Toast.LENGTH_SHORT).show();
                            Log.e("Firebase", "Failed to read owners data.", databaseError.toException());
                        }
                    });
                } else {
                    Toast.makeText(ExpensesActivity.this, "Please enter both description and amount.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showConfirmRemoveDialog(final String expenseKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Expense");
        builder.setMessage("Are you sure you want to remove this expense?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                String ownerMail = currentUser.getEmail();

                // Add expense to Firebase database

                DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference().child("Owners");
                ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Iterate through each owner's data
                        for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                            // Retrieve owner's email
                            String ownerEmail = ownerSnapshot.child("email").getValue(String.class);

                            // Check if the owner's email matches the current user's email
                            if (ownerEmail.equals(ownerMail)) {
                                // Get the key of the owner
                                String ownerKey = ownerSnapshot.getKey();

                                ownerk = ownerKey;
//                                Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();
                                // Retrieve existing locations if any
                                DatabaseReference ownerLocationRef = ownersRef.child(ownerKey).child("Locations");
                                ownerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        // Check if the location exists for the selected charging station
//                                        Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();
                                        for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                            Double latitu = locationSnapshot.child("latitude").getValue(Double.class);
                                            Double longitu = locationSnapshot.child("longitude").getValue(Double.class);
//                                            Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();

                                            // Assuming lati and longi are already defined
                                            if (latt.equals(latitu) && longg.equals(longitu)) {
                                                DatabaseReference expensesRef = locationSnapshot.child("Expenses").child(expenseKey).getRef();
//                                                Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();
                                                expensesRef.removeValue();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle error
                                        Toast.makeText(ExpensesActivity.this, "Failed to read locations data.", Toast.LENGTH_SHORT).show();
                                        Log.e("Firebase", "Failed to read locations data.", databaseError.toException());
                                    }
                                });

                                // Exit loop since the owner is found
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        Toast.makeText(ExpensesActivity.this, "Failed to read owners data.", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "Failed to read owners data.", databaseError.toException());
                    }
                });


                // Optionally, update the UI to reflect the removed expense
                removeExpenseFromLayout(expenseKey);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void calculateTotalExpense() {
//        totalExpense = 0.0;
//
//        // Loop through all TextViews in expensesLayout and calculate the total expense
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//        String ownerMail = currentUser.getEmail();
//
//        // Add expense to Firebase database
//
//        DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference().child("Owners");
//        ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                // Iterate through each owner's data
//                for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
//                    // Retrieve owner's email
//                    String ownerEmail = ownerSnapshot.child("email").getValue(String.class);
//
//                    // Check if the owner's email matches the current user's email
//                    if (ownerEmail.equals(ownerMail)) {
//                        // Get the key of the owner
//                        String ownerKey = ownerSnapshot.getKey();
//
//                        ownerk = ownerKey;
////                                Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();
//                        // Retrieve existing locations if any
//                        DatabaseReference ownerLocationRef = ownersRef.child(ownerKey).child("Locations");
//                        ownerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                // Check if the location exists for the selected charging station
////                                        Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();
//                                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
//                                    Double latitu = locationSnapshot.child("latitude").getValue(Double.class);
//                                    Double longitu = locationSnapshot.child("longitude").getValue(Double.class);
////                                            Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();
//
//                                    // Assuming lati and longi are already defined
//                                    if (latt.equals(latitu) && longg.equals(longitu)) {
//                                        DatabaseReference expensesRef = locationSnapshot.child("Expenses").getRef();
//
//// Check if there are any children under the "Expenses" node
////                                        Toast.makeText(ExpensesActivity.this, "You got here", Toast.LENGTH_SHORT).show();
//                                        if (expensesRef != null) {
//                                            expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                                @Override
//                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                                                    Toast.makeText(ExpensesActivity.this, "You got here", Toast.LENGTH_SHORT).show();
//                                                    if (dataSnapshot.exists()) {
//                                                        // Iterate over each child of the "Expenses" node
////                                                        Toast.makeText(ExpensesActivity.this, "You got here", Toast.LENGTH_SHORT).show();
//                                                        for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
//                                                            // Get the amount of the expense and add it to the totalExpense
////                                                            Toast.makeText(ExpensesActivity.this, "You got here", Toast.LENGTH_SHORT).show();
////                                                            Toast.makeText(ExpensesActivity.this, "oo" + Objects.requireNonNull(expenseSnapshot.child("amount").getValue(String.class)), Toast.LENGTH_SHORT).show();
//                                                            Double amount = Double.parseDouble(Objects.requireNonNull(expenseSnapshot.child("amount").getValue(String.class)));
//                                                            totalExpense += amount;
////                                                            Toast.makeText(ExpensesActivity.this, "" + totalExpense, Toast.LENGTH_SHORT).show();
//                                                        }
////                                                        Toast.makeText(ExpensesActivity.this, "total "+totalExpense, Toast.LENGTH_SHORT).show();
//                                                        String uu = totalExpense.toString();
//
////                                                        Toast.makeText(this, ""+uu, Toast.LENGTH_SHORT).show();
//                                                        String totalExpenseText = String.format("Total Expense: %.2f", totalExpense);
//
//                                                        // For example, showing it in a Toast
//                                                        Toast.makeText(ExpensesActivity.this, totalExpenseText, Toast.LENGTH_SHORT).show();
//
//                                                        // Now totalExpense contains the total expense for the selected location
//                                                    } else {
//                                                        // Handle the case where there are no expenses for the current location
//                                                        Log.d("ExpensesActivity", "No expenses found for the current location");
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void onCancelled(@NonNull DatabaseError databaseError) {
//                                                    // Handle error
//                                                    Log.e("ExpensesActivity", "Failed to read expenses data.", databaseError.toException());
//                                                }
//                                            });
//                                        } else {
//                                            // Handle the case where there are no expenses node for the current location
//                                            Log.d("ExpensesActivity", "No expenses node found for the current location");
//                                        }
//                                        break;
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                // Handle error
//                                Toast.makeText(ExpensesActivity.this, "Failed to read locations data.", Toast.LENGTH_SHORT).show();
//                                Log.e("Firebase", "Failed to read locations data.", databaseError.toException());
//                            }
//                        });
//
//                        // Exit loop since the owner is found
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // Handle error
//                Toast.makeText(ExpensesActivity.this, "Failed to read owners data.", Toast.LENGTH_SHORT).show();
//                Log.e("Firebase", "Failed to read owners data.", databaseError.toException());
//            }
//        });
//
//        // Show the total expense to the user
//        // You can display it in a TextView, Toast, or any other UI component


        graphView = findViewById(R.id.graph);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                // on below line we are adding
                // each point on our x and y axis.
        });

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String ownerMail = currentUser.getEmail();

        // Add expense to Firebase database

        DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference().child("Owners");
        ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through each owner's data
                for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve owner's email
                    String ownerEmail = ownerSnapshot.child("email").getValue(String.class);

                    // Check if the owner's email matches the current user's email
                    if (ownerEmail.equals(ownerMail)) {
                        // Get the key of the owner
                        String ownerKey = ownerSnapshot.getKey();

                        ownerk = ownerKey;
//                                Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();
                        // Retrieve existing locations if any
                        DatabaseReference ownerLocationRef = ownersRef.child(ownerKey).child("Locations");
                        ownerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Check if the location exists for the selected charging station
//                                        Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();
                                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                    Double latitu = locationSnapshot.child("latitude").getValue(Double.class);
                                    Double longitu = locationSnapshot.child("longitude").getValue(Double.class);
//                                            Toast.makeText(ExpensesActivity.this, "something " + expenseKey, Toast.LENGTH_SHORT).show();

                                    // Assuming lati and longi are already defined
                                    if (latt.equals(latitu) && longg.equals(longitu)) {
                                        DatabaseReference expensesRef = locationSnapshot.child("Expenses").getRef();

// Check if there are any children under the "Expenses" node
//                                        Toast.makeText(ExpensesActivity.this, "You got here", Toast.LENGTH_SHORT).show();
                                        if (expensesRef != null) {
                                            expensesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                                    Toast.makeText(ExpensesActivity.this, "You got here", Toast.LENGTH_SHORT).show();
                                                    if (dataSnapshot.exists()) {
                                                        // Iterate over each child of the "Expenses" node
//                                                        Toast.makeText(ExpensesActivity.this, "You got here", Toast.LENGTH_SHORT).show();\

                                                        for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                                                            // Get the amount of the expense and add it to the totalExpense
//                                                            Toast.makeText(ExpensesActivity.this, "You got here", Toast.LENGTH_SHORT).show();
//                                                            Toast.makeText(ExpensesActivity.this, "oo" + Objects.requireNonNull(expenseSnapshot.child("amount").getValue(String.class)), Toast.LENGTH_SHORT).show();
                                                            Double amount = Double.parseDouble(Objects.requireNonNull(expenseSnapshot.child("amount").getValue(String.class)));
                                                            totalExpense += amount;
                                                            i++;

                                                            DataPoint[] newDataPoints = new DataPoint[]{
                                                                    new DataPoint(i, totalExpense),
                                                                    // Add more data points as needed
                                                            };

// Create a new array to hold the combined data points.
                                                            // Create an ArrayList to store the data points.
                                                            ArrayList<DataPoint> combinedDataPointsList = new ArrayList<>();

// Add existing data points to the combinedDataPointsList.
                                                            // Access the data points from the series using getDataPoints() method.
                                                            DataPoint[] dataPoints = getDataPointsFromSeries(series);

// Iterate over the data points array and add each data point to combinedDataPointsList.
                                                            for (int i = 0; i < dataPoints.length; i++) {
                                                                combinedDataPointsList.add(dataPoints[i]);
                                                            }


// Add new data points to the combinedDataPointsList.
                                                            for (DataPoint newDataPoint : newDataPoints) {
                                                                combinedDataPointsList.add(newDataPoint);
                                                            }

// Convert the ArrayList to an array.
                                                            DataPoint[] combinedDataPoints = combinedDataPointsList.toArray(new DataPoint[0]);

// Create a new series with the combined data points.
                                                            LineGraphSeries<DataPoint> combinedSeries = new LineGraphSeries<>(combinedDataPoints);

// Update the existing series with the combined series.
                                                            series.resetData(combinedDataPoints);// Adjust this value based on your data

                                                            // Set manual Y bounds to ensure proper range and values
//                                                            Viewport viewport = graphView.getViewport();
//
//// Set the minimum and maximum bounds for the x-axis
//                                                            viewport.setMinX(series.getLowestValueX() - 1);
//                                                            viewport.setMaxX(series.getHighestValueX() + 1);

//                                                            Toast.makeText(ExpensesActivity.this, "" + totalExpense, Toast.LENGTH_SHORT).show();
                                                        }
                                                        totalExpense = 0.0;
//                                                        Toast.makeText(ExpensesActivity.this, "total "+totalExpense, Toast.LENGTH_SHORT).show();
                                                        String uu = totalExpense.toString();

//                                                        Toast.makeText(this, ""+uu, Toast.LENGTH_SHORT).show();
                                                        String totalExpenseText = String.format("Total Expense: %.2f", totalExpense);

                                                        // For example, showing it in a Toast
                                                        Toast.makeText(ExpensesActivity.this, totalExpenseText, Toast.LENGTH_SHORT).show();

                                                        // Now totalExpense contains the total expense for the selected location
                                                    } else {
                                                        // Handle the case where there are no expenses for the current location
                                                        Log.d("ExpensesActivity", "No expenses found for the current location");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    // Handle error
                                                    Log.e("ExpensesActivity", "Failed to read expenses data.", databaseError.toException());
                                                }
                                            });
                                        } else {
                                            // Handle the case where there are no expenses node for the current location
                                            Log.d("ExpensesActivity", "No expenses node found for the current location");
                                        }
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle error
                                Toast.makeText(ExpensesActivity.this, "Failed to read locations data.", Toast.LENGTH_SHORT).show();
                                Log.e("Firebase", "Failed to read locations data.", databaseError.toException());
                            }
                        });

                        // Exit loop since the owner is found
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(ExpensesActivity.this, "Failed to read owners data.", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Failed to read owners data.", databaseError.toException());
            }
        });

        // on below line we are adding data to our graph view.


        // after adding data to our line graph series.
        // on below line we are setting
        // title for our graph view.
        graphView.setTitle("My Graph View");

        // on below line we are setting
        // text color to our graph view.
        graphView.setTitleColor(R.color.purple_200);

        // on below line we are setting
        // our title text size.
        graphView.setTitleTextSize(18);

        // on below line we are adding
        // data series to our graph view.
        graphView.addSeries(series);


    }

    private DataPoint[] getDataPointsFromSeries(LineGraphSeries<DataPoint> series) {
        List<DataPoint> dataPointsList = new ArrayList<>();
        Iterator<DataPoint> iterator = series.getValues(series.getLowestValueX(), series.getHighestValueX());

        while (iterator.hasNext()) {
            DataPointInterface dataPointInterface = iterator.next();
            dataPointsList.add(new DataPoint(dataPointInterface.getX(), dataPointInterface.getY()));
        }

        // Convert the list to an array
        DataPoint[] dataPointsArray = new DataPoint[dataPointsList.size()];
        dataPointsArray = dataPointsList.toArray(dataPointsArray);

        return dataPointsArray;
    }
}