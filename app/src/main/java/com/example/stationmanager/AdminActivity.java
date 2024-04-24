package com.example.stationmanager;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Get a reference to the Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Ownership Requests");

        // Retrieve ownership requests
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout container = findViewById(R.id.container);

                // Iterate through each ownership request
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    // Access latitude, longitude, and ownership email
                    Double lat1 = requestSnapshot.child("Latitude").getValue(Double.class);
                    Double long1 = requestSnapshot.child("Longitude").getValue(Double.class);
                    String latitude = requestSnapshot.child("Latitude").getValue(Double.class).toString();
                    String longitude = requestSnapshot.child("Longitude").getValue(Double.class).toString();
                    String email = requestSnapshot.child("Owner Email").getValue(String.class);

                    // Inflate layout for each ownership request
                    View requestView = LayoutInflater.from(AdminActivity.this).inflate(R.layout.layout_ownership_request, container, false);

                    // Set text for latitude, longitude, and email TextViews
                    TextView tvLatitude = requestView.findViewById(R.id.tvLatitude);
                    TextView tvLongitude = requestView.findViewById(R.id.tvLongitude);
                    TextView tvEmail = requestView.findViewById(R.id.tvEmail);
                    tvLatitude.setText("Latitude: " + latitude);
                    tvLongitude.setText("Longitude: " + longitude);
                    tvEmail.setText("Owner's Email: " + email);

                    // Handle accept button click
                    // Handle accept button click
                    // Handle accept button click
                    // Handle accept button click
                    Button btnAccept = requestView.findViewById(R.id.btnAccept);
                    btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Handle accept button click
                            Toast.makeText(AdminActivity.this, "Accept clicked for " + email, Toast.LENGTH_SHORT).show();

                            // Query the "Owners" node to retrieve all owners
                            DatabaseReference ownersRef = FirebaseDatabase.getInstance().getReference().child("Owners");
                            ownersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // Iterate through each owner's data
                                    for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                                        // Retrieve owner's email
                                        String ownerEmail = ownerSnapshot.child("email").getValue(String.class);

                                        // Check if the owner's email matches the email from the ownership request
                                        if (ownerEmail.equals(email)) {
                                            // Get the key of the owner
                                            String ownerKey = ownerSnapshot.getKey();

                                            // Retrieve existing locations if any
                                            DatabaseReference ownerLocationRef = ownersRef.child(ownerKey).child("Locations");
                                            ownerLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    List<Location> locations = new ArrayList<>();

                                                    // Iterate through each location if any
                                                    for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                                        double latitude = locationSnapshot.child("latitude").getValue(Double.class);
                                                        double longitude = locationSnapshot.child("longitude").getValue(Double.class);

                                                        Location location = new Location(latitude, longitude);
                                                        locations.add(location);
                                                    }

                                                    // Add the new location
                                                    locations.add(new Location(lat1, long1));

                                                    // Update the locations node
                                                    ownerLocationRef.setValue(locations)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    // Location data added successfully
                                                                    Toast.makeText(AdminActivity.this, "Location data added for " + email, Toast.LENGTH_SHORT).show();

                                                                    // Remove the ownership request from database
                                                                    requestSnapshot.getRef().removeValue()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    // Request removed successfully
                                                                                    Toast.makeText(AdminActivity.this, "Request removed for " + email, Toast.LENGTH_SHORT).show();

                                                                                    // Remove the ownership request view from UI
                                                                                    ViewGroup parentView = (ViewGroup) requestView.getParent();
                                                                                    parentView.removeView(requestView);
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    // Failed to remove request
                                                                                    Toast.makeText(AdminActivity.this, "Failed to remove request for " + email, Toast.LENGTH_SHORT).show();
                                                                                    Log.e("Firebase", "Error removing request", e);
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Failed to add location data
                                                                    Toast.makeText(AdminActivity.this, "Failed to add location data for " + email, Toast.LENGTH_SHORT).show();
                                                                    Log.e("Firebase", "Error adding location data", e);
                                                                }
                                                            });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    // Handle error
                                                    Toast.makeText(AdminActivity.this, "Failed to read locations data.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(AdminActivity.this, "Failed to read owners data.", Toast.LENGTH_SHORT).show();
                                    Log.e("Firebase", "Failed to read owners data.", databaseError.toException());
                                }
                            });
                        }
                    });


                    // Handle reject button click
                    // Handle reject button click
                    Button btnReject = requestView.findViewById(R.id.btnReject);
                    btnReject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Handle reject button click
                            Toast.makeText(AdminActivity.this, "Reject clicked for " + email, Toast.LENGTH_SHORT).show();

                            // Remove the ownership request from database
                            requestSnapshot.getRef().removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Request removed successfully from database
                                            Toast.makeText(AdminActivity.this, "Request removed for " + email, Toast.LENGTH_SHORT).show();

                                            // Remove the ownership request view from UI
                                            ViewGroup parentView = (ViewGroup) requestView.getParent();
                                            parentView.removeView(requestView);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Failed to remove request from database
                                            Toast.makeText(AdminActivity.this, "Failed to remove request for " + email, Toast.LENGTH_SHORT).show();
                                            Log.e("Firebase", "Error removing request", e);
                                        }
                                    });
                        }
                    });


                    // Add inflated layout to the container
                    container.addView(requestView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(AdminActivity.this, "Failed to read value.", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Failed to read value.", databaseError.toException());
            }
        });
    }
}
