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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DiktalasActivity extends AppCompatActivity {

    private static final String TAG = "DiktalasActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView idoszakTextView;
    private EditText fogyasztasEditText;
    private Button rogzitesButton;
    private String userEmail;
    private String userAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diktalas);

        idoszakTextView = findViewById(R.id.idoszakTextView);
        fogyasztasEditText = findViewById(R.id.fogyasztasEditText);
        rogzitesButton = findViewById(R.id.rogzitesButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            fetchUserAddress(userEmail);
            setIdoszakTextView();
        }

        rogzitesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rogzites();
            }
        });
    }

    private void fetchUserAddress(String userEmail) {
        DocumentReference userRef = db.collection("users").document(userEmail);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userAddress = document.getString("lakcim");
                    } else {
                        Log.w(TAG, "No such document");
                    }
                } else {
                    Log.w(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void setIdoszakTextView() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        String currentDateString = sdf.format(currentDate);

        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        Date previousMonthDate = calendar.getTime();
        String previousMonthDateString = sdf.format(previousMonthDate);

        String idoszak = "Időszak: " + previousMonthDateString + " - " + currentDateString;
        idoszakTextView.setText(idoszak);
    }

    private void rogzites() {
        String fogyasztasStr = fogyasztasEditText.getText().toString().trim();
        if (fogyasztasStr.isEmpty()) {
            Toast.makeText(DiktalasActivity.this, "Kérjük, adja meg a fogyasztást", Toast.LENGTH_SHORT).show();
            return;
        }

        int fogyasztas = Integer.parseInt(fogyasztasStr);
        int osszeg = fogyasztas * 130;

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        Date elszidotolDate = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 14);
        calendar.add(Calendar.MONTH, 1);
        Date hataridoDate = calendar.getTime();

        calendar.add(Calendar.MONTH, -2);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date honapDate = calendar.getTime();

        Timestamp elszidotolTimestamp = new Timestamp(elszidotolDate);
        Timestamp elszidoigTimestamp = new Timestamp(currentDate);
        Timestamp hataridoTimestamp = new Timestamp(hataridoDate);
        Timestamp honapTimestamp = new Timestamp(honapDate);

        Map<String, Object> szamlaData = new HashMap<>();
        szamlaData.put("elszidotol", elszidotolTimestamp);
        szamlaData.put("elszidoig", elszidoigTimestamp);
        szamlaData.put("fogyasztas", fogyasztas);
        szamlaData.put("fizetve", false);
        szamlaData.put("hatarido", hataridoTimestamp);
        szamlaData.put("hely", userAddress);
        szamlaData.put("honap", honapTimestamp);
        szamlaData.put("osszeg", osszeg);

        db.collection("users").document(userEmail).collection("szamlak").document(new SimpleDateFormat("yyyy.MM", Locale.getDefault()).format(honapDate))
                .set(szamlaData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(DiktalasActivity.this, "Fogyasztás rögzítve", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DiktalasActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.w(TAG, "Error adding document", task.getException());
                            Toast.makeText(DiktalasActivity.this, "Hiba történt az adatok rögzítésekor", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}