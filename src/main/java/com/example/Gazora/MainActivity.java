package com.example.Gazora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button logoutButton, szamlaimButton, diktalasButton, profilomButton;
    private TextView welcomeMainTextView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        welcomeMainTextView = findViewById(R.id.welcomeMainTextView);
        logoutButton = findViewById(R.id.logoutButton);
        szamlaimButton = findViewById(R.id.szamlaimButton);
        diktalasButton = findViewById(R.id.diktalasButton);
        profilomButton = findViewById(R.id.profilomButton);

        if (currentUser != null) {
            String userEmail = currentUser.getEmail(); // Felhasználó email címe

            // Felhasználó nevének lekérdezése Firestore-ból
            DocumentReference userRef = db.collection("users").document(userEmail);
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userName = document.getString("name");
                        welcomeMainTextView.setText("Üdvözöljük, kedves " + userName + "!");
                    } else {
                        welcomeMainTextView.setText("Üdvözöljük!");
                    }
                } else {
                    welcomeMainTextView.setText("Üdvözöljük!");
                }
            });
        }

        diktalasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Számlák lekérdezése Firestore-ból
                Intent intent = new Intent(MainActivity.this,
                        DiktalasActivity.class);
                startActivity(intent);
            }
        });

        szamlaimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Számlák lekérdezése Firestore-ból
                Intent intent = new Intent(MainActivity.this,
                        SzamlakActivity.class);
                startActivity(intent);
            }
        });

        profilomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Számlák lekérdezése Firestore-ból
                Intent intent = new Intent(MainActivity.this,
                        ProfileActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}
