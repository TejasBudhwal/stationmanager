package com.example.stationmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CostTracker extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private LinearLayout containerLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_tracker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cost Tracker");

        toolbar.inflateMenu(R.menu.menu);
//        mapView.findViewById(R.id.mapView);

        Menu menu= navigationView.getMenu();

//        menu.findItem(R.id.logout_main).setVisible(false);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
        navigationView.setCheckedItem(R.id.SimpleMap);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        databaseReference = mDatabase.getReference();

        containerLayout = findViewById(R.id.container);

        // Fetch charging stations data for the current user
        fetchChargingStations();
    }

    private void fetchChargingStations() {
        if (currentUser != null) {
            // Get the user's email to search for their data in Firebase
            String userEmail = currentUser.getEmail();

            // Query Firebase database to get charging station data under the user's email
            databaseReference.child("Owners").orderByChild("email").equalTo(userEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Loop through each owner found with the user's email
                            for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                                // Get the key of the owner which will be used to access the charging stations
                                String ownerKey = ownerSnapshot.getKey();

                                // Fetch charging stations data under the owner
                                fetchChargingStationsForOwner(ownerKey);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
        }
    }

    private void fetchChargingStationsForOwner(String ownerKey) {
        // Query Firebase database to get charging station data for the owner
        databaseReference.child("Owners").child(ownerKey).child("Locations")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Loop through each charging station location
                        for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                            // Get latitude and longitude values for the charging station
                            String latitude = locationSnapshot.child("latitude").getValue(Double.class).toString();
                            String longitude = locationSnapshot.child("longitude").getValue(Double.class).toString();

                            // Create a TextView to display charging station information
                            TextView textView = (TextView) LayoutInflater.from(CostTracker.this)
                                    .inflate(R.layout.item_charging_station, containerLayout, false);
                            textView.setText("Latitude: " + latitude + ", Longitude: " + longitude);

                            // Set click listener to open expenses activity
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open expenses activity and pass latitude and longitude
                                    Intent intent = new Intent(CostTracker.this, ExpensesActivity.class);
                                    intent.putExtra("latitude", latitude);
                                    intent.putExtra("longitude", longitude);
                                    startActivity(intent);
                                }
                            });

                            // Add TextView to the LinearLayout
                            containerLayout.addView(textView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.SimpleMap){
//            Intent intent = new Intent(CostTracker.this,CostTracker.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
        }
        else if(item.getItemId()==R.id.Saved_Locations){
            Intent intent = new Intent(CostTracker.this,ChargingRequests.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.MarkedLocation){
            Intent intent = new Intent(CostTracker.this,EVChargerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
//        else if(item.getItemId()==R.id.PathFinder){
//            Intent intent = new Intent(EVChargerActivity.this,PathFinder.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//        }
        else if(item.getItemId()==R.id.share){
            Toast.makeText(this, "Please Share", Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId()==R.id.rate_us){
            Toast.makeText(this, "Please Rate US", Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId()==R.id.profile_main){
            Intent intent = new Intent(CostTracker.this,ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.logout_main){
            FirebaseAuth.getInstance().signOut(); // Sign out the user from Firebase

            Intent intent = new Intent(CostTracker.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);

    }
}