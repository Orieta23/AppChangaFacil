package com.example.chambafacil;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuscarAdapter extends RecyclerView.Adapter<BuscarAdapter.ViewHolder> {

    // Interface para manejar el click en los items del RecyclerView
    public interface OnItemClickListener {
        void onItemClick(Trabajo trabajo);
    }

    private Context context;
    private List<Trabajo> listaTrabajos;
    private boolean mostrarBoton;
    private OnItemClickListener listener; // <--- Agregamos el listener

    // Constructor corregido para aceptar el listener
    public BuscarAdapter(Context context, List<Trabajo> listaTrabajos, boolean mostrarBoton, OnItemClickListener listener) {
        this.context = context;
        this.listaTrabajos = listaTrabajos;
        this.mostrarBoton = mostrarBoton;
        this.listener = listener; // <--- Inicializamos el listener
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_trabajo, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trabajo trabajo = listaTrabajos.get(position);

        holder.txtTitulo.setText(trabajo.getTitulo());
        holder.txtUbicacion.setText("📌 " + trabajo.getUbicacion());
        holder.txtSueldo.setText("💰 " + trabajo.getSueldo());

        if (trabajo.getFechaPublicacion() != null) {
            holder.txtFecha.setText("Publicado " + tiempoTranscurrido(trabajo.getFechaPublicacion()));
        } else {
            holder.txtFecha.setText("Publicado recientemente");
        }

        holder.btnPostularme.setVisibility(mostrarBoton ? View.VISIBLE : View.GONE);

        // 🔹 Click para postularse
        holder.btnPostularme.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                Toast.makeText(context, "Debes iniciar sesión para postularte", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();

            Map<String, Object> datosPostulante = new HashMap<>();
            datosPostulante.put("idUsuario", uid);

            db.collection("trabajos")
                    .document(trabajo.getId())
                    .collection("postulantes")
                    .document(uid)
                    .set(datosPostulante)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Te postulaste correctamente", Toast.LENGTH_SHORT).show();

                        Map<String, Object> misPostulaciones = new HashMap<>();
                        misPostulaciones.put("idTrabajo", trabajo.getId());
                        misPostulaciones.put("fecha", new Date());

                        db.collection("usuarios")
                                .document(uid)
                                .collection("mis_postulaciones")
                                .document(trabajo.getId())
                                .set(misPostulaciones);
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // 🔹 Click en toda la tarjeta → Se llama al listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(trabajo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTrabajos.size();
    }

    private String tiempoTranscurrido(Date fecha) {
        long diferencia = System.currentTimeMillis() - fecha.getTime();
        long minutos = diferencia / 60000;
        long horas = minutos / 60;
        long dias = horas / 24;

        if (dias > 0) return "hace " + dias + (dias == 1 ? " día" : " días");
        if (horas > 0) return "hace " + horas + (horas == 1 ? " hora" : " horas");
        if (minutos > 0) return "hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        return "recién publicado";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtUbicacion, txtSueldo, txtFecha;
        Button btnPostularme;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloTrabajo);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacionTrabajo);
            txtSueldo = itemView.findViewById(R.id.txtSueldoTrabajo);
            txtFecha = itemView.findViewById(R.id.txtFechaPublicacion);
            btnPostularme = itemView.findViewById(R.id.btnPostularme);
        }
    }
}