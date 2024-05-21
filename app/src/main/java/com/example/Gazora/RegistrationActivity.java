package com.example.Gazora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Gazora.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, nameEditText, motherNameEditText, birthPlaceEditText, lakcimEditText;
    private DatePicker birthDatePicker;
    private Button saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.Gazora.R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find all views by their IDs
        emailEditText = findViewById(R.id.registerEditTextEmail);
        passwordEditText = findViewById(R.id.registerEditTextPassword);
        nameEditText = findViewById(R.id.registerEditTextName);
        motherNameEditText = findViewById(R.id.registerEditTextMotherName);
        birthPlaceEditText = findViewById(R.id.registerEditTextBirthPlace);
        birthDatePicker = findViewById(R.id.registerDatePickerBirthDate);
        saveButton = findViewById(R.id.registerButtonSaveChanges);
        lakcimEditText = findViewById(R.id.registerEditTextAddress);

        // Set onClickListener for save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }
    private void registerNewUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Validáció az email és jelszó mezőkre
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Add meg az email címed!", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Adj meg egy érvényes jelszót!", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sikeres regisztráció
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getEmail();

                            // Felhasználó adatainak mentése Firestore-ba
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference userRef = db.collection("users").document(userId);

                            String name = nameEditText.getText().toString();
                            String motherName = motherNameEditText.getText().toString();
                            String birthPlace = birthPlaceEditText.getText().toString();
                            String lakcim = lakcimEditText.getText().toString();

                            int year = birthDatePicker.getYear();
                            int month = birthDatePicker.getMonth();
                            int day = birthDatePicker.getDayOfMonth();

                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year, month, day);
                            Date birthDate = calendar.getTime();

                            Timestamp birthTimestamp = new Timestamp(birthDate);
                            Timestamp registrationTimestamp = Timestamp.now();


                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("name", name);
                            userData.put("anyjaneve", motherName);
                            userData.put("szulhely", birthPlace);
                            userData.put("szuldatum", birthTimestamp);
                            userData.put("registertime", registrationTimestamp);
                            userData.put("lakcim", lakcim);


                            userRef.set(userData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Sikeres adatmentés Firestore-ba
                                            Log.d("RegistrationActivity", "User data saved successfully!");
                                            Toast.makeText(getApplicationContext(), "Sikeres regisztráció!", Toast.LENGTH_LONG).show();
                                            // Ide írd a további teendőket (pl. navigáció más Activity-re)
                                            startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("RegistrationActivity", "Error saving user data", e);
                                            // Hibakezelés: adatmentés sikertelen
                                        }
                                    });
                        } else {
                            // Sikertelen regisztráció
                            Log.w("RegistrationActivity", "User registration failed", task.getException());
                            Toast.makeText(getApplicationContext(), "Regisztráció sikertelen!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
