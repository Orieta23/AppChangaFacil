package com.example.chambafacil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PerfilEmpleadorFragment extends Fragment {

    private static final String TAG = "PerfilEmpleador";

    private EditText editEmpresa, editEmail, editTelefono, editUbicacion, editDescripcion;
    private Button btnGuardar, btnVerCalificaciones;
    private ImageView imgEmpresa;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private String empleadorUid;
    private boolean verCalificaciones;

    public PerfilEmpleadorFragment() {}

    public static PerfilEmpleadorFragment newInstance(String uidEmpleador, boolean verCalificaciones) {
        PerfilEmpleadorFragment fragment = new PerfilEmpleadorFragment();
        Bundle args = new Bundle();
        args.putString("empleadorUid", uidEmpleador);
        args.putBoolean("verCalificaciones", verCalificaciones);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_empleador, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        editEmpresa = view.findViewById(R.id.editEmpresa);
        editEmail = view.findViewById(R.id.editEmailEmpresa);
        editTelefono = view.findViewById(R.id.editTelefonoEmpresa);
        editUbicacion = view.findViewById(R.id.editUbicacionEmpresa);
        editDescripcion = view.findViewById(R.id.editDescripcionEmpresa);
        btnGuardar = view.findViewById(R.id.btnGuardarEmpresa);
        btnVerCalificaciones = view.findViewById(R.id.btnVerCalificaciones);
        imgEmpresa = view.findViewById(R.id.imgEmpresa);

        if (getArguments() != null) {
            empleadorUid = getArguments().getString("empleadorUid", null);
            verCalificaciones = getArguments().getBoolean("verCalificaciones", false);
        }

        if (verCalificaciones && empleadorUid != null) {
            btnGuardar.setVisibility(View.GONE);
            cargarDatosEmpleador(empleadorUid);
            btnVerCalificaciones.setOnClickListener(v -> abrirCalificaciones(empleadorUid));
        } else {
            cargarDatos();
            btnGuardar.setOnClickListener(v -> guardarDatos());
            btnVerCalificaciones.setOnClickListener(v -> {
                if (user != null) abrirCalificaciones(user.getUid());
            });
        }

        return view;
    }

    private void cargarDatosEmpleador(String uid) {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d(TAG, "Datos empleador (vista): " + doc.getData());
                        editEmpresa.setText(doc.getString("empresa") != null ? doc.getString("empresa") : "");
                        editEmail.setText(doc.getString("email") != null ? doc.getString("email") : "");
                        editTelefono.setText(doc.getString("telefono") != null ? doc.getString("telefono") : "");
                        editUbicacion.setText(doc.getString("ubicacion") != null ? doc.getString("ubicacion") : "");
                        editDescripcion.setText(doc.getString("descripcionEmpresa") != null ? doc.getString("descripcionEmpresa") : "");

                        editEmpresa.setEnabled(false);
                        editEmail.setEnabled(false);
                        editTelefono.setEnabled(false);
                        editUbicacion.setEnabled(false);
                        editDescripcion.setEnabled(false);
                    } else {
                        Log.w(TAG, "No se encontraron datos para el empleador UID: " + uid);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando datos del empleador: ", e);
                    Toast.makeText(getContext(), "Error al cargar datos del empleador", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarDatos() {
        if (user != null) {
            db.collection("usuarios").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Log.d(TAG, "Datos empleador (edición): " + doc.getData());
                            editEmpresa.setText(doc.getString("empresa") != null ? doc.getString("empresa") : "");
                            editEmail.setText(doc.getString("email") != null ? doc.getString("email") : "");
                            editTelefono.setText(doc.getString("telefono") != null ? doc.getString("telefono") : "");
                            editUbicacion.setText(doc.getString("ubicacion") != null ? doc.getString("ubicacion") : "");
                            editDescripcion.setText(doc.getString("descripcionEmpresa") != null ? doc.getString("descripcionEmpresa") : "");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error cargando datos del empleador autenticado: ", e);
                        Toast.makeText(getContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void guardarDatos() {
        if (user == null) return;

        String empresa = editEmpresa.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String ubicacion = editUbicacion.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();

        if (empresa.isEmpty()) {
            Toast.makeText(getContext(), "El nombre de la empresa es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("empresa", empresa);  // Campo principal para empleadores
        datos.put("nombre", empresa);   // Compatibilidad con reseñas
        datos.put("email", email);
        datos.put("telefono", telefono);
        datos.put("ubicacion", ubicacion);
        datos.put("descripcionEmpresa", descripcion);

        db.collection("usuarios").document(user.getUid())
                .set(datos, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Perfil actualizado correctamente: " + datos);
                    Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar perfil: ", e);
                    Toast.makeText(getContext(), "Error al guardar perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private void abrirCalificaciones(String uid) {
        Log.d(TAG, "Abriendo calificaciones para UID: " + uid);
        PerfilCalificacionesFragment fragment = PerfilCalificacionesFragment.newInstance(uid, "empleador");
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
}