package com.example.chambafacil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class DatosRegistroFragment extends Fragment {

    private EditText editNombreApellido, editFechaNacimiento, editEmail, editTelefono;
    private Button btnGuardar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public DatosRegistroFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_datos_registro, container, false);

        editNombreApellido = view.findViewById(R.id.editNombreApellido);
        editFechaNacimiento = view.findViewById(R.id.editFechaNacimiento);
        editEmail = view.findViewById(R.id.editEmail);
        editTelefono = view.findViewById(R.id.editTelefono);
        btnGuardar = view.findViewById(R.id.btnGuardarDatos);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        btnGuardar.setOnClickListener(v -> guardarDatos());

        return view;
    }

    private void guardarDatos() {
        if (user == null) return;

        String nombreApellido = editNombreApellido.getText().toString().trim();
        String fecha = editFechaNacimiento.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombreApellido", nombreApellido);
        datos.put("fechaNacimiento", fecha);
        datos.put("email", email);
        datos.put("telefono", telefono);

        db.collection("usuarios").document(user.getUid())
                .set(datos, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Datos guardados", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}