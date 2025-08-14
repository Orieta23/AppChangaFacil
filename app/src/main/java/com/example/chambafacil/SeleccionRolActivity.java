package com.example.chambafacil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SeleccionRolActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_rol);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Button btnPostulante = findViewById(R.id.btnPostulante);
        Button btnEmpleador = findViewById(R.id.btnEmpleador);

        btnPostulante.setOnClickListener(v -> guardarRolYContinuar("postulante"));
        btnEmpleador.setOnClickListener(v -> guardarRolYContinuar("empleador"));
    }

    private void guardarRolYContinuar(String rol) {
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> datos = new HashMap<>();
        datos.put("tipoUsuario", rol);

        db.collection("usuarios").document(uid)
                .set(datos, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Rol seleccionado: " + rol, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "No se pudo guardar el rol", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}