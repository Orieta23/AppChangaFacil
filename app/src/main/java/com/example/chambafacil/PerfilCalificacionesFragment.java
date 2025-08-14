package com.example.chambafacil;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilCalificacionesFragment extends Fragment {

    private TextView txtNombre, txtEmail, txtTelefono, txtUbicacion, txtExperiencia, txtPromedio;
    private RatingBar ratingBarPromedio;
    private Button btnCalificar;
    private RecyclerView recyclerResenas;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String uidUsuario;
    private String tipoUsuario;

    private List<Resena> listaResenas;
    private ResenasAdapter resenasAdapter;

    public static PerfilCalificacionesFragment newInstance(String uid, String tipo) {
        PerfilCalificacionesFragment fragment = new PerfilCalificacionesFragment();
        Bundle args = new Bundle();
        args.putString("uidUsuario", uid);
        args.putString("tipoUsuario", tipo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_calificaciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtNombre = view.findViewById(R.id.txtNombre);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtTelefono = view.findViewById(R.id.txtTelefono);
        txtUbicacion = view.findViewById(R.id.txtUbicacion);
        txtExperiencia = view.findViewById(R.id.txtExperiencia);
        txtPromedio = view.findViewById(R.id.txtPromedio);
        ratingBarPromedio = view.findViewById(R.id.ratingBarPromedio);
        btnCalificar = view.findViewById(R.id.btnCalificar);
        recyclerResenas = view.findViewById(R.id.recyclerResenas);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (getArguments() != null) {
            uidUsuario = getArguments().getString("uidUsuario");
            tipoUsuario = getArguments().getString("tipoUsuario");
        }

        if (currentUser != null && uidUsuario.equals(currentUser.getUid())) {
            btnCalificar.setVisibility(View.GONE); // Ocultar botón para calificar el propio perfil
        }

        recyclerResenas.setLayoutManager(new LinearLayoutManager(getContext()));
        listaResenas = new ArrayList<>();
        resenasAdapter = new ResenasAdapter(listaResenas);
        recyclerResenas.setAdapter(resenasAdapter);

        cargarDatosUsuario();
        cargarCalificaciones();

        btnCalificar.setOnClickListener(v -> mostrarDialogoCalificacion());
    }

    private void cargarDatosUsuario() {
        db.collection("usuarios").document(uidUsuario).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        txtNombre.setText(doc.getString("nombre") != null ? doc.getString("nombre") : "Nombre no disponible");
                        txtEmail.setText(doc.getString("email") != null ? doc.getString("email") : "Correo no disponible");
                        txtTelefono.setText(doc.getString("telefono") != null ? doc.getString("telefono") : "Teléfono no disponible");
                        txtUbicacion.setText(doc.getString("ubicacion") != null ? doc.getString("ubicacion") : "Ubicación no disponible");
                        txtExperiencia.setText(doc.getString("experiencia") != null ? doc.getString("experiencia") : "Sin experiencia");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cargarCalificaciones() {
        db.collection("calificaciones")
                .whereEqualTo("idEvaluado", uidUsuario)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    double suma = 0;
                    listaResenas.clear();

                    for (DocumentSnapshot doc : query) {
                        Double puntuacion = doc.getDouble("puntuacion");
                        String comentario = doc.getString("comentario");
                        String nombreGuardado = doc.getString("nombreUsuario"); // 🔑 Priorizar este campo
                        String idEvaluador = doc.getString("idEvaluador");

                        if (puntuacion != null) suma += puntuacion;

                        if (nombreGuardado != null && !nombreGuardado.trim().isEmpty()) {
                            // ✅ Usar directamente el nombre guardado en la calificación
                            listaResenas.add(new Resena(
                                    puntuacion != null ? puntuacion : 0,
                                    comentario != null ? comentario : "Sin comentario",
                                    nombreGuardado
                            ));
                            resenasAdapter.notifyDataSetChanged();
                        } else if (idEvaluador != null) {
                            // Si no hay nombreUsuario, consultar datos del evaluador en Firestore
                            db.collection("usuarios").document(idEvaluador).get()
                                    .addOnSuccessListener(userDoc -> {
                                        String nombreEvaluador;
                                        if (userDoc.exists()) {
                                            if (userDoc.contains("nombre") && userDoc.getString("nombre") != null && !userDoc.getString("nombre").trim().isEmpty()) {
                                                nombreEvaluador = userDoc.getString("nombre");
                                            } else if (userDoc.contains("empresa") && userDoc.getString("empresa") != null && !userDoc.getString("empresa").trim().isEmpty()) {
                                                nombreEvaluador = userDoc.getString("empresa");
                                            } else {
                                                nombreEvaluador = "Usuario desconocido";
                                            }
                                        } else {
                                            nombreEvaluador = "Usuario desconocido";
                                        }

                                        listaResenas.add(new Resena(
                                                puntuacion != null ? puntuacion : 0,
                                                comentario != null ? comentario : "Sin comentario",
                                                nombreEvaluador
                                        ));
                                        resenasAdapter.notifyDataSetChanged();
                                    });
                        }
                    }

                    double promedio = query.size() > 0 ? suma / query.size() : 0;
                    ratingBarPromedio.setRating((float) promedio);
                    txtPromedio.setText(String.format("%.1f/5", promedio));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar calificaciones: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void mostrarDialogoCalificacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_calificaciones, null);
        builder.setView(dialogView);

        RatingBar ratingInput = dialogView.findViewById(R.id.ratingInput);
        EditText editComentario = dialogView.findViewById(R.id.editComentario);
        Button btnEnviar = dialogView.findViewById(R.id.btnEnviar);

        AlertDialog dialog = builder.create();

        btnEnviar.setOnClickListener(v -> {
            float puntuacion = ratingInput.getRating();
            String comentarioTexto = editComentario.getText().toString().trim();
            final String comentarioFinal = TextUtils.isEmpty(comentarioTexto) ? "Sin comentario" : comentarioTexto;

            if (puntuacion == 0) {
                Toast.makeText(requireContext(), "Debes seleccionar una puntuación", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser == null) {
                Toast.makeText(requireContext(), "Error: usuario no autenticado", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            db.collection("usuarios").document(currentUser.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        String nombreEvaluador;
                        if (doc.contains("nombre") && doc.getString("nombre") != null && !doc.getString("nombre").trim().isEmpty()) {
                            nombreEvaluador = doc.getString("nombre");
                        } else if (doc.contains("empresa") && doc.getString("empresa") != null && !doc.getString("empresa").trim().isEmpty()) {
                            nombreEvaluador = doc.getString("empresa");
                        } else {
                            nombreEvaluador = "Usuario desconocido";
                        }

                        // ✅ Guardar calificación con nombreUsuario incluido
                        Map<String, Object> calificacion = new HashMap<>();
                        calificacion.put("idEvaluador", currentUser.getUid());
                        calificacion.put("idEvaluado", uidUsuario);
                        calificacion.put("tipoEvaluado", tipoUsuario);
                        calificacion.put("puntuacion", puntuacion);
                        calificacion.put("comentario", comentarioFinal);
                        calificacion.put("nombreUsuario", nombreEvaluador);
                        calificacion.put("fecha", FieldValue.serverTimestamp());

                        db.collection("calificaciones").add(calificacion)
                                .addOnSuccessListener(docRef -> {
                                    Toast.makeText(requireContext(), "Calificación enviada", Toast.LENGTH_SHORT).show();
                                    cargarCalificaciones();
                                    dialog.dismiss();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Error al enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Error al obtener nombre del evaluador", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}