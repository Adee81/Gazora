package com.example.Gazora;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Gazora.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.Gazora.R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();

        // AuthStateListener létrehozása az autentikációs állapot figyelésére
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Ha van aktív felhasználói munkamenet, irányítsd át a MainActivity-re
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    finish(); // Bezárjuk a SplashScreenActivity-t
                } else {
                    // Ha nincs aktív felhasználói munkamenet, irányítsd át a LoginActivity-re
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                    finish(); // Bezárjuk a SplashScreenActivity-t
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Regisztráljuk az AuthStateListener-t az autentikációs állapot figyelésére
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Ha az Activity leáll, távolítjuk az AuthStateListener-t
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
