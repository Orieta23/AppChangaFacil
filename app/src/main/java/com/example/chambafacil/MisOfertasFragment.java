package com.example.chambafacil;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MisOfertasFragment extends Fragment {

    private RecyclerView recyclerView;
    private MisOfertasAdapter2 adapter;
    private List<Trabajo> listaOfertas;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public MisOfertasFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_ofertas, container, false);

        recyclerView = view.findViewById(R.id.recyclerMisOfertas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaOfertas = new ArrayList<>();
        adapter = new MisOfertasAdapter2(getContext(), listaOfertas);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cargarMisOfertas();

        // 🔁 Escucha actualizaciones después de editar una oferta
        getParentFragmentManager().setFragmentResultListener("trabajo_editado", this, (key, bundle) -> {
            Log.d("DEBUG", "Se recibió señal de trabajo_editado -> recargando ofertas...");
            cargarMisOfertas();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMisOfertas(); // Refrescar siempre que el fragmento vuelva a primer plano
    }

    private void cargarMisOfertas() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión para ver tus ofertas", Toast.LENGTH_SHORT).show();
            return;
        }

        String uidActual = user.getUid();
        Log.d("DEBUG", "UID actual del usuario autenticado: " + uidActual);

        // ✅ Query real: traer solo las ofertas que coincidan con este empleador
        db.collection("trabajos")
                .whereEqualTo("uid_empleador", uidActual)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    listaOfertas.clear();

                    if (querySnapshot.isEmpty()) {
                        Log.d("DEBUG", "No se encontraron ofertas para este UID.");
                        Toast.makeText(getContext(), "No tienes ofertas publicadas", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot doc : querySnapshot) {
                            Trabajo trabajo = doc.toObject(Trabajo.class);
                            if (trabajo != null) {
                                trabajo.setId(doc.getId());
                                listaOfertas.add(trabajo);
                                Log.d("DEBUG", "Oferta cargada: " + trabajo.getTitulo() +
                                        " | ID: " + doc.getId() +
                                        " | UID_empleador: " + doc.getString("uid_empleador"));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("DEBUG", "Total ofertas cargadas: " + listaOfertas.size());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al cargar ofertas: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error al cargar ofertas", Toast.LENGTH_SHORT).show();
                });
    }
}