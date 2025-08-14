package com.example.chambafacil;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log; // ✅ Import para logs
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostulantesFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostulanteAdapter adapter;
    private List<Postulante> listaPostulantes;

    private FirebaseFirestore db;
    private String idTrabajo; // ID de la oferta clickeada

    public PostulantesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // ✅ Inflar el layout
        View view = inflater.inflate(R.layout.fragment_postulantes, container, false);

        // ✅ Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerPostulantes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaPostulantes = new ArrayList<>();
        adapter = new PostulanteAdapter(requireContext(), listaPostulantes,
                getParentFragmentManager()); // ✅ Usar getParentFragmentManager()
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // ✅ Obtener argumentos usando getArguments
        if (getArguments() != null) {
            idTrabajo = getArguments().getString("idTrabajo");

            Log.d("DEBUG", "ID del trabajo recibido: " + idTrabajo);

            if (idTrabajo != null && !idTrabajo.isEmpty()) {
                cargarPostulantes();
            } else {
                Toast.makeText(getContext(), "Error: ID de trabajo nulo o vacío", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getContext(), "Error: No se recibió la oferta", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void cargarPostulantes() {
        db.collection("trabajos")
                .document(idTrabajo)
                .collection("postulaciones")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaPostulantes.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Postulante p = doc.toObject(Postulante.class);
                            listaPostulantes.add(p);
                        }
                        adapter.notifyDataSetChanged();

                        Log.d("DEBUG", "Postulantes cargados: " + listaPostulantes.size());
                    } else {
                        Toast.makeText(getContext(), "Error al cargar postulantes", Toast.LENGTH_SHORT).show();
                        Log.e("DEBUG", "Error: ", task.getException());
                    }
                });
    }

}