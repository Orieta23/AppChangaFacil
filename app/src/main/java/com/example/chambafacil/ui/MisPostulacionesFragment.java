package com.example.chambafacil.ui.slideshow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chambafacil.MisPostulacionesAdapter;
import com.example.chambafacil.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MisPostulacionesFragment extends Fragment {

    private RecyclerView recyclerView;
    private MisPostulacionesAdapter adapter;
    private List<Postulacion> listaPostulaciones;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public MisPostulacionesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_postulaciones, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMisPostulaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        listaPostulaciones = new ArrayList<>();
        adapter = new MisPostulacionesAdapter(this, listaPostulaciones);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cargarPostulaciones();

        return view;
    }

    private void cargarPostulaciones() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("usuarios")
                .document(user.getUid())
                .collection("mis_postulaciones")
                .get()
                .addOnSuccessListener(snapshot -> {
                    listaPostulaciones.clear();

                    Log.d("DEBUG", "Cantidad de postulaciones encontradas: " + snapshot.size());

                    if (snapshot.isEmpty()) {
                        Toast.makeText(getContext(), "No tenés postulaciones aún", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    final int total = snapshot.size();
                    final int[] cargados = {0};

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String idTrabajo = doc.getString("idTrabajo");
                        Timestamp fecha = doc.getTimestamp("fecha");

                        Log.d("DEBUG", "Leyendo idTrabajo: " + idTrabajo);

                        if (idTrabajo != null && !idTrabajo.isEmpty()) {
                            db.collection("trabajos")
                                    .document(idTrabajo)
                                    .get()
                                    .addOnSuccessListener(trabajoSnap -> {
                                        if (trabajoSnap.exists()) {
                                            Postulacion postulacion = new Postulacion();
                                            postulacion.setId(doc.getId());
                                            postulacion.setIdTrabajo(idTrabajo);
                                            postulacion.setTitulo(trabajoSnap.getString("titulo"));
                                            postulacion.setDescripcion(trabajoSnap.getString("descripcion"));
                                            postulacion.setUbicacion(trabajoSnap.getString("ubicacion"));
                                            postulacion.setSueldo(trabajoSnap.getString("sueldo"));
                                            postulacion.setFecha_postulacion(fecha);

                                            listaPostulaciones.add(postulacion);
                                        }

                                        cargados[0]++;
                                        if (cargados[0] == total) {
                                            Log.d("DEBUG", "Postulaciones cargadas: " + listaPostulaciones.size());
                                            adapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("DEBUG", "Error al obtener trabajo: " + e.getMessage());
                                        cargados[0]++;
                                        if (cargados[0] == total) {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        } else {
                            Log.w("DEBUG", "idTrabajo nulo o vacío en documento: " + doc.getId());
                            cargados[0]++;
                            if (cargados[0] == total) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar postulaciones", Toast.LENGTH_SHORT).show();
                    Log.e("DEBUG", "Error Firebase: " + e.getMessage());
                });
    }
}