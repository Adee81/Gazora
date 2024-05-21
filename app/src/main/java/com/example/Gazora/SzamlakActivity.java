package com.example.Gazora;

import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SzamlakActivity extends AppCompatActivity {

    private static final String TAG = "SzamlakActivity";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ExpandableListView expandableListView;
    private SzamlakListAdapter adapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_szamlak);

        expandableListView = findViewById(R.id.listViewSzamlak);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            fetchSzamlak(userEmail);
        }
    }

    private void fetchSzamlak(String userEmail) {
        CollectionReference szamlakRef = db.collection("users").document(userEmail).collection("szamlak");
        szamlakRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    listDataHeader = new ArrayList<>();
                    listDataChild = new HashMap<>();

                    for (DocumentSnapshot document : task.getResult()) {
                        String honap = document.getId();
                        listDataHeader.add(honap);

                        List<String> details = new ArrayList<>();
                        details.add("Elszámolási időszak: " + formatTimestamp(document.getTimestamp("elszidotol")) + " - " + formatTimestamp(document.getTimestamp("elszidoig")));
                        details.add("Fizetve: " + (document.getBoolean("fizetve") ? "Igen" : "Rendezendő"));
                        details.add("Fogyasztás: " + getNumberAsString(document, "fogyasztas") + " m³");
                        details.add("Határidő: " + formatTimestamp(document.getTimestamp("hatarido")));
                        details.add("Hely: " + document.getString("hely"));
                        details.add("Összeg: " + getNumberAsString(document, "osszeg"));

                        listDataChild.put(honap, details);
                    }

                    adapter = new SzamlakListAdapter(SzamlakActivity.this, listDataHeader, listDataChild);
                    expandableListView.setAdapter(adapter);
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
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

    private String getNumberAsString(DocumentSnapshot document, String field) {
        Number number = document.getDouble(field);
        if (number != null) {
            return String.valueOf(number.longValue());
        }
        return "N/A";
    }
}
