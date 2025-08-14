package com.example.chambafacil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class VerPostulantesFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostulanteAdapter adapter;
    private List<Postulante> listaPostulantes;
    private FirebaseFirestore db;
    private String trabajoId;

    public VerPostulantesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ver_postulantes, container, false);

        recyclerView = view.findViewById(R.id.recyclerPostulantes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        listaPostulantes = new ArrayList<>();
        adapter = new PostulanteAdapter(requireContext(), listaPostulantes, getParentFragmentManager()); // ✅ Constructor corregido
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        trabajoId = getArguments() != null ? getArguments().getString("idTrabajo") : null;

        if (trabajoId != null && !trabajoId.isEmpty()) {
            Log.d("VerPostulantes", "ID del trabajo recibido: " + trabajoId);
            cargarPostulantes();
        } else {
            Toast.makeText(getContext(), "Error: ID del trabajo no recibido", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void cargarPostulantes() {
        db.collection("trabajos")
                .document(trabajoId)
                .collection("postulantes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaPostulantes.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String idUsuario = doc.getId(); // UID del postulante

                        db.collection("usuarios")
                                .document(idUsuario)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        Postulante postulante = new Postulante();
                                        postulante.setIdUsuario(idUsuario); // ✅ UID que luego usará getUid()
                                        postulante.setIdTrabajo(trabajoId);
                                        postulante.setNombre(userDoc.getString("nombre"));
                                        postulante.setEmail(userDoc.getString("email"));

                                        String telefono = userDoc.getString("telefono");
                                        postulante.setTelefono((telefono != null && !telefono.isEmpty()) ? telefono : "sin número");

                                        postulante.setUbicacion(userDoc.getString("ubicacion"));
                                        postulante.setExperiencia(userDoc.getString("descripcion")); // ✅ descripción como experiencia

                                        listaPostulantes.add(postulante);
                                        adapter.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("VerPostulantes", "Error al obtener usuario", e));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar postulantes", Toast.LENGTH_SHORT).show();
                    Log.e("VerPostulantes", "Error al obtener postulaciones", e);
                });
    }
}