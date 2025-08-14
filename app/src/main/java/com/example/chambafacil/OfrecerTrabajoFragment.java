package com.example.chambafacil;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OfrecerTrabajoFragment extends Fragment {

    private EditText editTitulo, editUbicacion, editSalario, editDescripcion;
    private Button btnPublicar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public OfrecerTrabajoFragment() {
        // Constructor obligatorio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ofrecer_trabajo, container, false);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Referencias UI
        editTitulo = view.findViewById(R.id.editTitulo);
        editUbicacion = view.findViewById(R.id.editUbicacion);
        editSalario = view.findViewById(R.id.editSalario);
        editDescripcion = view.findViewById(R.id.editDescripcion);
        btnPublicar = view.findViewById(R.id.btnPublicar);

        btnPublicar.setOnClickListener(v -> publicarTrabajo());

        return view;
    }

    private void publicarTrabajo() {
        String titulo = editTitulo.getText().toString().trim();
        String ubicacion = editUbicacion.getText().toString().trim();
        String salario = editSalario.getText().toString().trim();
        String descripcion = editDescripcion.getText().toString().trim();

        // Validaciones
        if (titulo.isEmpty() || ubicacion.isEmpty() || salario.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Crear datos del trabajo
        Map<String, Object> nuevoTrabajo = new HashMap<>();
        nuevoTrabajo.put("titulo", titulo);
        nuevoTrabajo.put("ubicacion", ubicacion);
        nuevoTrabajo.put("sueldo", salario);
        nuevoTrabajo.put("descripcion", descripcion);

        // 🔹️ Aquí está el cambio. Se cambió 'uid_empleador' a 'uidEmpleador'
        nuevoTrabajo.put("uidEmpleador", user.getUid());

        nuevoTrabajo.put("fechaPublicacion", FieldValue.serverTimestamp());

        db.collection("trabajos")
                .add(nuevoTrabajo)
                .addOnSuccessListener(documentReference -> {
                    Log.d("DEBUG_PUBLICACION", "Trabajo publicado: " + titulo);
                    Toast.makeText(getContext(), "Trabajo publicado correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    Log.e("DEBUG_PUBLICACION", "Error al publicar el trabajo: ", e);
                    Toast.makeText(getContext(), "Error al publicar el trabajo", Toast.LENGTH_SHORT).show();
                });
    }

    private void limpiarCampos() {
        editTitulo.setText("");
        editUbicacion.setText("");
        editSalario.setText("");
        editDescripcion.setText("");
    }
}