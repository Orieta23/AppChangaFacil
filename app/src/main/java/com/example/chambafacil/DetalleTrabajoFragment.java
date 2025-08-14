package com.example.chambafacil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DetalleTrabajoFragment extends BottomSheetDialogFragment {

    private TextView tvTitulo, tvDescripcion, tvUbicacion, tvSueldo, tvUsuario;
    private Button btnPostularme;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance(); // Inicializamos FirebaseAuth

    private static final String ARG_ID_TRABAJO = "idTrabajo";
    private String idTrabajo;

    public static DetalleTrabajoFragment newInstance(String idTrabajo) {
        DetalleTrabajoFragment fragment = new DetalleTrabajoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID_TRABAJO, idTrabajo);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle, container, false);

        tvTitulo = view.findViewById(R.id.tvTitulo);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);
        tvUbicacion = view.findViewById(R.id.tvUbicacion);
        tvSueldo = view.findViewById(R.id.tvSueldo);
        tvUsuario = view.findViewById(R.id.tvUsuario);
        btnPostularme = view.findViewById(R.id.btnPostularmeDetalle);

        if (getArguments() != null) {
            idTrabajo = getArguments().getString(ARG_ID_TRABAJO);
            cargarDatosTrabajo(idTrabajo);
        } else {
            Toast.makeText(getContext(), "No se encontró el ID del trabajo", Toast.LENGTH_SHORT).show();
            dismiss();
        }

        return view;
    }

    private void cargarDatosTrabajo(String id) {
        db.collection("trabajos").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String titulo = documentSnapshot.getString("titulo");
                        String descripcion = documentSnapshot.getString("descripcion");
                        String ubicacion = documentSnapshot.getString("ubicacion");
                        String sueldo = documentSnapshot.getString("sueldo");
                        String idUsuarioPublicador = documentSnapshot.getString("uidEmpleador");

                        Log.d("DetalleTrabajo", "Título del trabajo: " + titulo);
                        Log.d("DetalleTrabajo", "Sueldo: " + sueldo);
                        Log.d("DetalleTrabajo", "ID del usuario que publica: " + idUsuarioPublicador);

                        tvTitulo.setText(titulo != null ? titulo : "Sin título");
                        tvDescripcion.setText(descripcion != null ? descripcion : "Sin descripción");
                        tvUbicacion.setText(ubicacion != null ? ubicacion : "Sin ubicación");
                        tvSueldo.setText(sueldo != null ? sueldo : "Sin sueldo");

                        if (idUsuarioPublicador != null) {
                            cargarDatosUsuario(idUsuarioPublicador);
                        } else {
                            tvUsuario.setText("Publicado por: Usuario desconocido");
                        }

                        // 🔹 Nueva lógica: verificamos la postulación antes de configurar el botón
                        verificarEstadoPostulacion(id);

                    } else {
                        Toast.makeText(getContext(), "Trabajo no encontrado", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar trabajo", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

    private void cargarDatosUsuario(String idUsuario) {
        db.collection("usuarios").document(idUsuario).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombreUsuario = documentSnapshot.getString("nombre");
                        Log.d("DetalleTrabajo", "Nombre del usuario: " + nombreUsuario);
                        tvUsuario.setText("Publicado por: " + (nombreUsuario != null ? nombreUsuario : "Usuario desconocido"));
                    } else {
                        tvUsuario.setText("Publicado por: Usuario desconocido");
                    }
                });
    }

    // 🔹 Nuevo método para verificar el estado de la postulación
    private void verificarEstadoPostulacion(String idTrabajo) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            btnPostularme.setText("Postularme");
            btnPostularme.setEnabled(true);
            configurarBotonPostularme(idTrabajo); // Se configura el botón si no está postulado
            return;
        }

        String uid = user.getUid();
        db.collection("trabajos")
                .document(idTrabajo)
                .collection("postulantes")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // El usuario ya se postuló
                        btnPostularme.setText("Ya postulado");
                        btnPostularme.setEnabled(false);
                        // 🔹 Usando el nuevo color verde claro
                        btnPostularme.setBackgroundTintList(
                                getResources().getColorStateList(R.color.light_green, getContext().getTheme()));
                    } else {
                        // El usuario no se ha postulado
                        btnPostularme.setText("Postularme");
                        btnPostularme.setEnabled(true);
                        configurarBotonPostularme(idTrabajo); // Se configura el botón si no está postulado
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DetalleTrabajo", "Error al verificar postulación: " + e.getMessage());
                    btnPostularme.setText("Error");
                    btnPostularme.setEnabled(false);
                });
    }

    // 🔹 Método modificado para solo manejar la postulación
    private void configurarBotonPostularme(String idTrabajo) {
        btnPostularme.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();

            if (user == null) {
                Toast.makeText(getContext(), "Debes iniciar sesión para postularte", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            Map<String, Object> datosPostulante = new HashMap<>();
            datosPostulante.put("idUsuario", uid);

            db.collection("trabajos")
                    .document(idTrabajo)
                    .collection("postulantes")
                    .document(uid)
                    .set(datosPostulante)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Te postulaste correctamente", Toast.LENGTH_SHORT).show();

                        // 🔹 Lógica para cambiar el botón después de la postulación exitosa
                        btnPostularme.setText("Ya postulado");
                        btnPostularme.setEnabled(false);
                        // 🔹 Usando el nuevo color verde claro después de la postulación
                        btnPostularme.setBackgroundTintList(
                                getResources().getColorStateList(R.color.light_green, getContext().getTheme()));

                        Map<String, Object> misPostulaciones = new HashMap<>();
                        misPostulaciones.put("idTrabajo", idTrabajo);
                        misPostulaciones.put("fecha", new Date());

                        db.collection("usuarios")
                                .document(uid)
                                .collection("mis_postulaciones")
                                .document(idTrabajo)
                                .set(misPostulaciones);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}