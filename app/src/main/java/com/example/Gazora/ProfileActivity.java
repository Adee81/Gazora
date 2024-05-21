package com.example.Gazora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView addressTextView;
    private TextView motherNameTextView;
    private TextView birthPlaceTextView;
    private TextView birthDateTextView;

    private EditText editEmailEditText;
    private EditText editAddressEditText;
    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;

    private Button updateEmailButton;
    private Button updateAddressButton;
    private Button updatePasswordButton;
    private Button deleteProfileButton;
    private Button backToMainMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        addressTextView = findViewById(R.id.addressTextView);
        motherNameTextView = findViewById(R.id.motherNameTextView);
        birthPlaceTextView = findViewById(R.id.birthPlaceTextView);
        birthDateTextView = findViewById(R.id.birthDateTextView);

        editEmailEditText = findViewById(R.id.editEmailEditText);
        editAddressEditText = findViewById(R.id.editAddressEditText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        updateEmailButton = findViewById(R.id.updateEmailButton);
        updateAddressButton = findViewById(R.id.updateAddressButton);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        deleteProfileButton = findViewById(R.id.deleteProfileButton);
        backToMainMenuButton = findViewById(R.id.backToMainMenuButton);

        if (currentUser != null) {
            fetchUserProfile(currentUser.getEmail());
        }

        updateEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEmail();
            }
        });

        updateAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAddress();
            }
        });

        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });

        deleteProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProfile();
            }
        });

        backToMainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMainMenu();
            }
        });
    }

    private void fetchUserProfile(String userEmail) {
        DocumentReference userRef = db.collection("users").document(userEmail);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        nameTextView.setText("Név: " + document.getString("name"));
                        emailTextView.setText("E-mail cím: " + document.getString("email"));
                        addressTextView.setText("Lakcím: " + document.getString("lakcim"));
                        motherNameTextView.setText("Anyja neve: " + document.getString("anyjaneve"));
                        birthPlaceTextView.setText("Születési hely: " + document.getString("szulhely"));
                        birthDateTextView.setText("Születési dátum: " + formatTimestamp(document.getTimestamp("szuldatum")));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            return sdf.format(date);
        }
        return "";
    }

    private void updateEmail() {
        String newEmail = editEmailEditText.getText().toString();
        if (!newEmail.isEmpty()) {
            db.collection("users").whereEqualTo("email", newEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        currentUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    DocumentReference userRef = db.collection("users").document(currentUser.getEmail());
                                    userRef.update("email", newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                emailTextView.setText("E-mail cím: " + newEmail);
                                                Toast.makeText(ProfileActivity.this, "E-mail cím frissítve", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ProfileActivity.this, "Hiba történt az e-mail cím frissítése során", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Hiba történt az e-mail cím frissítése során", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Ez az e-mail cím már létezik", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Adjon meg egy új e-mail címet", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAddress() {
        String newAddress = editAddressEditText.getText().toString();
        if (!newAddress.isEmpty()) {
            DocumentReference userRef = db.collection("users").document(currentUser.getEmail());
            userRef.update("lakcim", newAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        addressTextView.setText("Lakcím: " + newAddress);
                        Toast.makeText(ProfileActivity.this, "Lakcím frissítve", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Hiba történt a lakcím frissítése során", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Adjon meg egy új lakcímet", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword() {
        String currentPassword = currentPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (!currentPassword.isEmpty() && !newPassword.isEmpty() && newPassword.equals(confirmPassword)) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
            currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        currentUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Jelszó frissítve", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Hiba történt a jelszó frissítése során", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Jelenlegi jelszó nem megfelelő", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Az új jelszavak nem egyeznek", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Minden mezőt ki kell tölteni", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProfile() {
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            // Először töröljük a szamlak kollekció összes dokumentumát
            CollectionReference szamlakRef = db.collection("users").document(userEmail).collection("szamlak");
            szamlakRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            szamlakRef.document(document.getId()).delete();
                        }

                        // Töröljük a felhasználói adatokat a Firestore-ból
                        DocumentReference userRef = db.collection("users").document(userEmail);
                        userRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Töröljük a felhasználót a Firebase Authentication-ből
                                    currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ProfileActivity.this, "Profil sikeresen törölve", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(ProfileActivity.this, "Hiba történt a profil törlése során", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Hiba történt a felhasználói adatok törlése során", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Hiba történt a számlák törlése során", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void backToMainMenu() {
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
    }
}
