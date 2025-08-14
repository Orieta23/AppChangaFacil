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

public class PerfilUsuarioFragment extends Fragment {

    private TextView txtNombre, txtEmail, txtTelefono, txtUbicacion, txtExperiencia, txtPromedio;
    private RatingBar ratingBarPromedio;
    private Button btnCalificar;
    private RecyclerView recyclerResenas;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String uidUsuario;    // UID del usuario a mostrar
    private String tipoUsuario;   // "postulante" o "empleador"

    private List<Resena> listaResenas;
    private ResenasAdapter resenasAdapter;

    public PerfilUsuarioFragment() {}

    public static PerfilUsuarioFragment newInstance(String uid, String tipo) {
        PerfilUsuarioFragment fragment = new PerfilUsuarioFragment();
        Bundle args = new Bundle();
        args.putString("uidUsuario", uid);
        args.putString("tipoUsuario", tipo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_usuario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
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

        // Si es el propio perfil, ocultar botón Calificar
        if (currentUser != null && uidUsuario.equals(currentUser.getUid())) {
            btnCalificar.setVisibility(View.GONE);
        }

        // Configurar RecyclerView de reseñas
        recyclerResenas.setLayoutManager(new LinearLayoutManager(getContext()));
        listaResenas = new ArrayList<>();
        resenasAdapter = new ResenasAdapter(listaResenas);
        recyclerResenas.setAdapter(resenasAdapter);

        // Cargar datos del perfil y calificaciones
        cargarDatosUsuario();
        cargarCalificaciones();

        btnCalificar.setOnClickListener(v -> mostrarDialogoCalificacion());
    }

    private void cargarDatosUsuario() {
        db.collection("usuarios").document(uidUsuario).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        txtNombre.setText(doc.getString("nombre"));
                        txtEmail.setText(doc.getString("email"));
                        txtTelefono.setText(doc.getString("telefono"));
                        txtUbicacion.setText(doc.getString("ubicacion"));
                        txtExperiencia.setText(doc.getString("experiencia"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void cargarCalificaciones() {
        db.collection("calificaciones")
                .whereEqualTo("idEvaluado", uidUsuario)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    double suma = 0;
                    listaResenas.clear();

                    if (query.isEmpty()) {
                        Toast.makeText(getContext(), "Este usuario aún no tiene calificaciones", Toast.LENGTH_SHORT).show();
                    }

                    for (DocumentSnapshot doc : query) {
                        Double puntuacion = doc.getDouble("puntuacion");
                        String comentario = doc.getString("comentario");

                        if (puntuacion != null) suma += puntuacion;
                        listaResenas.add(new Resena(
                                puntuacion != null ? puntuacion : 0,
                                comentario != null ? comentario : "Sin comentario"
                        ));
                    }

                    double promedio = query.size() > 0 ? suma / query.size() : 0;
                    ratingBarPromedio.setRating((float) promedio);
                    txtPromedio.setText(String.format("%.1f/5", promedio));

                    resenasAdapter.notifyDataSetChanged();
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
            String comentario = editComentario.getText().toString().trim();

            if (puntuacion == 0) {
                Toast.makeText(requireContext(), "Debes seleccionar una puntuación", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(comentario)) comentario = "Sin comentario";

            Map<String, Object> calificacion = new HashMap<>();
            calificacion.put("idEvaluador", currentUser.getUid());
            calificacion.put("idEvaluado", uidUsuario);
            calificacion.put("tipoEvaluado", tipoUsuario);
            calificacion.put("puntuacion", puntuacion);
            calificacion.put("comentario", comentario);
            calificacion.put("fecha", FieldValue.serverTimestamp());

            db.collection("calificaciones").add(calificacion)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(requireContext(), "Calificación enviada", Toast.LENGTH_SHORT).show();
                        cargarCalificaciones();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Error al enviar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}