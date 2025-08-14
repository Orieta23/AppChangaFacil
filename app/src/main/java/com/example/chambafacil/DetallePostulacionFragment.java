package com.example.chambafacil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DetallePostulacionFragment extends Fragment {

    private static final String TAG = "DetallePostulacion";

    private TextView txtTitulo, txtDescripcion, txtUbicacion, txtSueldo, txtFecha, txtEmpresa;
    private Button btnVerPerfilEmpleador, btnCalificarEmpleador;
    private FirebaseFirestore db;
    private FirebaseUser user;

    private String idTrabajo;
    private String empleadorUid;

    public DetallePostulacionFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_postulacion, container, false);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        txtTitulo = view.findViewById(R.id.txtDetalleTitulo);
        txtDescripcion = view.findViewById(R.id.txtDetalleDescripcion);
        txtUbicacion = view.findViewById(R.id.txtDetalleUbicacion);
        txtSueldo = view.findViewById(R.id.txtDetalleSueldo);
        txtFecha = view.findViewById(R.id.txtDetalleFecha);
        txtEmpresa = view.findViewById(R.id.txtDetalleEmpresa);
        btnVerPerfilEmpleador = view.findViewById(R.id.btnVerPerfilEmpleador);
        btnCalificarEmpleador = view.findViewById(R.id.btnCalificarEmpleador);

        btnCalificarEmpleador.setVisibility(View.GONE);

        if (getArguments() != null) {
            idTrabajo = getArguments().getString("idTrabajo", null);
        }

        if (idTrabajo != null) {
            cargarDetallePostulacion(idTrabajo);
        } else {
            Toast.makeText(requireContext(), "⚠️ No se recibió el ID del trabajo", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void cargarDetallePostulacion(String idTrabajo) {
        db.collection("trabajos").document(idTrabajo)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        txtTitulo.setText(document.getString("titulo"));
                        txtDescripcion.setText(document.getString("descripcion"));
                        txtUbicacion.setText("📍 " + document.getString("ubicacion"));
                        txtSueldo.setText("💰 " + (document.getString("sueldo") != null ? document.getString("sueldo") : "No especificado"));
                        txtEmpresa.setText(document.getString("empresa"));

                        empleadorUid = document.getString("uidEmpleador");
                        if (empleadorUid == null) {
                            empleadorUid = document.getString("uid_empleador");
                        }

                        if (empleadorUid == null) {
                            Toast.makeText(getContext(), "⚠ UID del empleador no encontrado en esta oferta", Toast.LENGTH_LONG).show();
                        }

                        Date fechaPublicacion = document.getDate("fecha_publicacion");
                        if (fechaPublicacion != null) {
                            String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaPublicacion);
                            txtFecha.setText("📅 Publicado el " + fecha);
                        } else {
                            txtFecha.setText("📅 Fecha no disponible");
                        }

                        verificarEstadoPostulacion();
                        configurarBotones();
                    } else {
                        Toast.makeText(requireContext(), "Trabajo no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error al cargar detalles: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void verificarEstadoPostulacion() {
        if (user == null) return;

        db.collection("trabajos")
                .document(idTrabajo)
                .collection("postulantes")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(postulacionDoc -> {
                    if (postulacionDoc.exists()) {
                        String estado = postulacionDoc.getString("estado");
                        Boolean contratado = postulacionDoc.getBoolean("contratado");

                        if ("finalizado".equals(estado) && Boolean.TRUE.equals(contratado)) {
                            btnCalificarEmpleador.setVisibility(View.VISIBLE);
                            btnCalificarEmpleador.setOnClickListener(v -> mostrarDialogoCalificacion());
                        }
                    }
                });
    }

    private void configurarBotones() {
        btnVerPerfilEmpleador.setOnClickListener(v -> {
            if (empleadorUid != null) {
                PerfilEmpleadorFragment fragment = PerfilEmpleadorFragment.newInstance(empleadorUid, true);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "⚠ No se pudo cargar el perfil del empleador. Falta UID.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoCalificacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_calificaciones, null);
        builder.setView(dialogView);

        RatingBar ratingInput = dialogView.findViewById(R.id.ratingInput);
        EditText editComentario = dialogView.findViewById(R.id.editComentario);
        Button btnEnviar = dialogView.findViewById(R.id.btnEnviar);

        AlertDialog dialog = builder.create();

        btnEnviar.setOnClickListener(v -> {
            float estrellas = ratingInput.getRating();
            String comentario = editComentario.getText().toString().trim();

            if (estrellas == 0) {
                Toast.makeText(getContext(), "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user == null) {
                Toast.makeText(getContext(), "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("usuarios").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        String nombreEvaluador;

                        if (doc.contains("nombre") && doc.getString("nombre") != null && !doc.getString("nombre").trim().isEmpty()) {
                            nombreEvaluador = doc.getString("nombre");
                        } else if (doc.contains("empresa") && doc.getString("empresa") != null && !doc.getString("empresa").trim().isEmpty()) {
                            nombreEvaluador = doc.getString("empresa");
                        } else {
                            nombreEvaluador = "Usuario desconocido";
                        }

                        Map<String, Object> calificacion = new HashMap<>();
                        calificacion.put("puntuacion", estrellas);
                        calificacion.put("comentario", comentario.isEmpty() ? "Sin comentario" : comentario);
                        calificacion.put("fecha", FieldValue.serverTimestamp());
                        calificacion.put("idEvaluador", user.getUid());
                        calificacion.put("idEvaluado", empleadorUid);
                        calificacion.put("nombreUsuario", nombreEvaluador);

                        db.collection("calificaciones")
                                .add(calificacion)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "✅ Calificación enviada con éxito", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error al enviar calificación: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}