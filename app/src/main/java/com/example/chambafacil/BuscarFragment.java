package com.example.chambafacil;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BuscarFragment extends Fragment {

    private EditText edtBuscar;
    private RecyclerView recyclerView;
    private BuscarAdapter adapter;
    private List<Trabajo> listaTrabajos = new ArrayList<>();
    private List<Trabajo> listaFiltrada = new ArrayList<>();
    private FirebaseFirestore db;

    public BuscarFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar, container, false);

        edtBuscar = view.findViewById(R.id.edtBuscar);
        recyclerView = view.findViewById(R.id.recyclerTrabajos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        // 🔹 Aquí está la corrección en el listener del adaptador
        adapter = new BuscarAdapter(getContext(), listaFiltrada, true, trabajo -> {
            // Creamos una instancia del BottomSheetDialogFragment
            DetalleTrabajoFragment detalleFragment = DetalleTrabajoFragment.newInstance(trabajo.getId());

            // Lo mostramos usando el FragmentManager
            detalleFragment.show(getParentFragmentManager(), "detalleTrabajo");
        });

        recyclerView.setAdapter(adapter);
        cargarTrabajos();

        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarTrabajos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void cargarTrabajos() {
        db.collection("trabajos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaTrabajos.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Trabajo trabajo = document.toObject(Trabajo.class);
                        if (trabajo != null) {
                            trabajo.setId(document.getId());
                            listaTrabajos.add(trabajo);
                        }
                    }
                    filtrarTrabajos(edtBuscar.getText().toString());
                });
    }

    private void filtrarTrabajos(String texto) {
        listaFiltrada.clear();
        for (Trabajo trabajo : listaTrabajos) {
            if (trabajo.getTitulo().toLowerCase().contains(texto.toLowerCase())) {
                listaFiltrada.add(trabajo);
            }
        }
        adapter.notifyDataSetChanged();
    }
}