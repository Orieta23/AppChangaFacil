package com.example.chambafacil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        new Handler().postDelayed(() -> {
            FirebaseUser user = auth.getCurrentUser();

            if (user == null) {
                Log.d(TAG, "Usuario NO logueado → LoginActivity");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            String uid = user.getUid();
            if (uid == null || uid.isEmpty()) {
                Log.e(TAG, "UID nulo o vacío → LoginActivity");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            Log.d(TAG, "Usuario logueado con UID: " + uid);

            db.collection("usuarios").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("tipoUsuario")) {
                                Log.d(TAG, "tipoUsuario encontrado → MainActivity");
                                startActivity(new Intent(this, MainActivity.class));
                            } else {
                                Log.d(TAG, "tipoUsuario NO encontrado → SeleccionRolActivity");
                                startActivity(new Intent(this, SeleccionRolActivity.class));
                            }
                        } else {
                            Log.d(TAG, "Documento de usuario no existe → SeleccionRolActivity");
                            startActivity(new Intent(this, SeleccionRolActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error accediendo a Firestore: " + e.getMessage());
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    });

        }, 2000);
    }
}