package com.example.stationmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChargingRequests extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DatabaseReference databaseRef;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charging_requests);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Charging Requests");
        toolbar.inflateMenu(R.menu.menu);

        Menu menu= navigationView.getMenu();

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.Saved_Locations);

        databaseRef = FirebaseDatabase.getInstance().getReference().child("Charging Requests");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String oMail = currentUser.getEmail();

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LinearLayout container = findViewById(R.id.container);
                for (DataSnapshot requestSnapshot : snapshot.getChildren())
                {
                    String latitude = requestSnapshot.child("Latitude").getValue(Double.class).toString();
                    String longitude = requestSnapshot.child("Longitude").getValue(Double.class).toString();
                    String ownerMail = requestSnapshot.child("Owner Email").getValue(String.class);
                    String userMail = requestSnapshot.child("User Email").getValue(String.class);

                    if(ownerMail.equals(oMail))
                    {
                        View requestView = LayoutInflater.from(ChargingRequests.this).inflate(R.layout.layout_charging_request, container, false);

                        TextView tvLatitude = requestView.findViewById(R.id.tvLatitude);
                        TextView tvLongitude = requestView.findViewById(R.id.tvLongitude);
                        TextView tvOwnerMail = requestView.findViewById(R.id.tvOwnerMail);
                        TextView tvUserMail = requestView.findViewById(R.id.tvUserMail);

                        tvLatitude.setText("Latitude: " + latitude);
                        tvLongitude.setText("Longitude: " + longitude);
                        tvOwnerMail.setText("Owner's Email: " + ownerMail);
                        tvUserMail.setText("User's Email: " + userMail);

                        Button btnAccept = requestView.findViewById(R.id.btnAccept);
                        btnAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(ChargingRequests.this, "Request accepted for " + userMail, Toast.LENGTH_SHORT).show();
                                Toast.makeText(ChargingRequests.this, "Station: ("+latitude+", "+longitude+")", Toast.LENGTH_SHORT).show();
                                requestSnapshot.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Request removed successfully
                                                Toast.makeText(ChargingRequests.this, "Request removed for " + userMail, Toast.LENGTH_SHORT).show();

                                                // Remove the ownership request view from UI
                                                ViewGroup parentView = (ViewGroup) requestView.getParent();
                                                parentView.removeView(requestView);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Failed to remove request
                                                Toast.makeText(ChargingRequests.this, "Failed to remove request for " + userMail, Toast.LENGTH_SHORT).show();
                                                Log.e("Firebase", "Error removing request", e);
                                            }
                                        });
                            }
                        });

                        Button btnReject = requestView.findViewById(R.id.btnReject);
                        btnReject.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Handle reject button click
                                Toast.makeText(ChargingRequests.this, "Reject clicked for " + userMail, Toast.LENGTH_SHORT).show();

                                // Remove the ownership request from database
                                requestSnapshot.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Request removed successfully from database
                                                Toast.makeText(ChargingRequests.this, "Request removed for " + userMail, Toast.LENGTH_SHORT).show();

                                                // Remove the ownership request view from UI
                                                ViewGroup parentView = (ViewGroup) requestView.getParent();
                                                parentView.removeView(requestView);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Failed to remove request from database
                                                Toast.makeText(ChargingRequests.this, "Failed to remove request for " + userMail, Toast.LENGTH_SHORT).show();
                                                Log.e("Firebase", "Error removing request", e);
                                            }
                                        });
                            }
                        });

                        container.addView(requestView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChargingRequests.this, "Failed to read data.", Toast.LENGTH_SHORT).show();
                Log.e("Firebase", "Failed to read data.", error.toException());
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
            Intent intent = new Intent(ChargingRequests.this,CostTracker.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
//        else if(item.getItemId()==R.id.Saved_Locations){
//            Intent intent = new Intent(ChargingRequests.this,ChargingRequests.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//        }
        else if(item.getItemId()==R.id.MarkedLocation){
            Intent intent = new Intent(ChargingRequests.this,EVChargerActivity.class);
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
            Intent intent = new Intent(ChargingRequests.this,ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else if(item.getItemId()==R.id.logout_main){
            FirebaseAuth.getInstance().signOut(); // Sign out the user from Firebase

            Intent intent = new Intent(ChargingRequests.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);

    }
}