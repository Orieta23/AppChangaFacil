package com.example.chambafacil;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditarTrabajoFragment extends Fragment {

    private EditText edtTitulo, edtDescripcion, edtUbicacion, edtSueldo;
    private Button btnGuardar, btnEliminar;
    private FirebaseFirestore db;
    private String trabajoId;

    public EditarTrabajoFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editar_trabajo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtTitulo = view.findViewById(R.id.edtTitulo);
        edtDescripcion = view.findViewById(R.id.edtDescripcion);
        edtUbicacion = view.findViewById(R.id.edtUbicacion);
        edtSueldo = view.findViewById(R.id.edtSueldo);
        btnGuardar = view.findViewById(R.id.btnGuardarCambios);
        btnEliminar = view.findViewById(R.id.btnEliminarTrabajo);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            trabajoId = getArguments().getString("idTrabajo");
            if (!TextUtils.isEmpty(trabajoId)) {
                cargarDatosTrabajo();
            } else {
                mostrarToast("Error: ID del trabajo no recibido");
            }
        } else {
            mostrarToast("Error: No se recibieron argumentos");
        }

        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
    }

    private void cargarDatosTrabajo() {
        db.collection("trabajos").document(trabajoId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Trabajo trabajo = snapshot.toObject(Trabajo.class);
                        if (trabajo != null) {
                            edtTitulo.setText(trabajo.getTitulo());
                            edtDescripcion.setText(trabajo.getDescripcion());
                            edtUbicacion.setText(trabajo.getUbicacion());
                            edtSueldo.setText(trabajo.getSueldo());
                        }
                    } else {
                        mostrarToast("No se encontró la publicación");
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    mostrarToast("Error al cargar datos: " + e.getMessage());
                });
    }

    private void guardarCambios() {
        String titulo = edtTitulo.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();
        String ubicacion = edtUbicacion.getText().toString().trim();
        String sueldo = edtSueldo.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion) ||
                TextUtils.isEmpty(ubicacion) || TextUtils.isEmpty(sueldo)) {
            mostrarToast("Completa todos los campos");
            return;
        }

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("titulo", titulo);
        cambios.put("descripcion", descripcion);
        cambios.put("ubicacion", ubicacion);
        cambios.put("sueldo", sueldo);

        db.collection("trabajos").document(trabajoId)
                .update(cambios)
                .addOnSuccessListener(aVoid -> {
                    mostrarToast("Cambios guardados");
                    // Ya no navegamos a otro fragment
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    mostrarToast("Error al guardar: " + e.getMessage());
                });
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que querés eliminar esta publicación?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarTrabajo())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTrabajo() {
        db.collection("trabajos").document(trabajoId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    mostrarToast("Publicación eliminada");
                    volverAMisOfertas();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    mostrarToast("Error al eliminar: " + e.getMessage());
                });
    }

    private void volverAMisOfertas() {
        if (isAdded()) {
            requireActivity().onBackPressed(); // alternativa segura a popBackStack si no sabés desde dónde llegaste
        }
    }

    private void mostrarToast(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }
}