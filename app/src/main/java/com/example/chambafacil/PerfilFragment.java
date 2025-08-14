package com.example.chambafacil;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PerfilFragment extends Fragment {

    private EditText editNombre, editEmail, editTelefono, editUbicacion, editExperiencia;
    private ImageButton btnEditarNombre, btnEditarTelefono, btnEditarUbicacion, btnEditarExperiencia, btnEditarEmail;
    private Button btnGuardar, btnVerCalificaciones;
    private ImageView imgPerfil;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public PerfilFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Vistas
        editNombre = view.findViewById(R.id.editNombre);
        editEmail = view.findViewById(R.id.editEmail);
        editTelefono = view.findViewById(R.id.editTelefono);
        editUbicacion = view.findViewById(R.id.editUbicacion);
        editExperiencia = view.findViewById(R.id.editExperiencia);
        btnGuardar = view.findViewById(R.id.btnGuardarPerfil);
        btnVerCalificaciones = view.findViewById(R.id.btnVerCalificaciones);
        imgPerfil = view.findViewById(R.id.imgPerfil);

        btnEditarNombre = view.findViewById(R.id.btnEditarNombre);
        btnEditarTelefono = view.findViewById(R.id.btnEditarTelefono);
        btnEditarUbicacion = view.findViewById(R.id.btnEditarUbicacion);
        btnEditarExperiencia = view.findViewById(R.id.btnEditarExperiencia);
        btnEditarEmail = view.findViewById(R.id.btnEditarEmail);

        // Deshabilitar campos al inicio
        deshabilitarEdicion();

        // Activar edición con íconos
        btnEditarNombre.setOnClickListener(v -> editNombre.setEnabled(true));
        btnEditarTelefono.setOnClickListener(v -> editTelefono.setEnabled(true));
        btnEditarUbicacion.setOnClickListener(v -> editUbicacion.setEnabled(true));
        btnEditarExperiencia.setOnClickListener(v -> editExperiencia.setEnabled(true));
        btnEditarEmail.setOnClickListener(v -> editEmail.setEnabled(true));

        // Cargar datos desde Firestore
        if (user != null) {
            db.collection("usuarios").document(user.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            editNombre.setText(snapshot.getString("nombre"));
                            editEmail.setText(snapshot.getString("email"));
                            editTelefono.setText(snapshot.getString("telefono"));
                            editUbicacion.setText(snapshot.getString("ubicacion"));
                            editExperiencia.setText(snapshot.getString("experiencia"));
                        }
                    });
        }

        // Guardar cambios
        btnGuardar.setOnClickListener(v -> {
            String nombre = editNombre.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String telefono = editTelefono.getText().toString().trim();
            String ubicacion = editUbicacion.getText().toString().trim();
            String experiencia = editExperiencia.getText().toString().trim();

            if (nombre.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Completa al menos nombre y correo", Toast.LENGTH_SHORT).show();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            new Handler().postDelayed(() -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("nombre", nombre);
                userMap.put("email", email);
                userMap.put("telefono", telefono);
                userMap.put("ubicacion", ubicacion);
                userMap.put("experiencia", experiencia);

                db.collection("usuarios").document(user.getUid())
                        .update(userMap)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                            deshabilitarEdicion();
                            btnGuardar.setText("GUARDAR");
                            btnGuardar.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            btnGuardar.setText("GUARDAR");
                            btnGuardar.setEnabled(true);
                        });
            }, 1500);
        });

        // Botón para ver calificaciones y reseñas
        btnVerCalificaciones.setOnClickListener(v -> {
            if (user != null) {
                PerfilUsuarioFragment fragment = PerfilUsuarioFragment.newInstance(user.getUid(), "postulante");
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }

    private void deshabilitarEdicion() {
        editNombre.setEnabled(false);
        editEmail.setEnabled(false);
        editTelefono.setEnabled(false);
        editUbicacion.setEnabled(false);
        editExperiencia.setEnabled(false);
    }
}