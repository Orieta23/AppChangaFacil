package com.example.chambafacil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilDetalleFragment extends Fragment {

    private ImageView imgFotoPerfilDetalle;
    private TextView txtNombreDetalle, txtEmailDetalle, txtTelefonoDetalle, txtUbicacionDetalle, txtFechaNacimientoDetalle, txtExperienciasDetalle;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public PerfilDetalleFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_detalle, container, false);

        // Inicializar vistas
        imgFotoPerfilDetalle = view.findViewById(R.id.imgFotoPerfilDetalle);
        txtNombreDetalle = view.findViewById(R.id.txtNombreDetalle);
        txtEmailDetalle = view.findViewById(R.id.txtEmailDetalle);
        txtTelefonoDetalle = view.findViewById(R.id.txtTelefonoDetalle);
        txtUbicacionDetalle = view.findViewById(R.id.txtUbicacionDetalle);
        txtFechaNacimientoDetalle = view.findViewById(R.id.txtFechaNacimientoDetalle);
        txtExperienciasDetalle = view.findViewById(R.id.txtExperienciasDetalle);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        cargarDatosUsuario();

        return view;
    }

    private void cargarDatosUsuario() {
        db.collection("usuarios").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        txtNombreDetalle.setText(documentSnapshot.getString("nombre"));
                        txtEmailDetalle.setText(documentSnapshot.getString("email"));
                        txtTelefonoDetalle.setText(documentSnapshot.getString("telefono"));
                        txtUbicacionDetalle.setText(documentSnapshot.getString("ubicacion"));
                        txtFechaNacimientoDetalle.setText(documentSnapshot.getString("fechaNacimiento"));
                        txtExperienciasDetalle.setText(documentSnapshot.getString("experiencias"));

                        String fotoUrl = documentSnapshot.getString("fotoPerfil");
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(fotoUrl)
                                    .into(imgFotoPerfilDetalle);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}