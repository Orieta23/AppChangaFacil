package com.example.chambafacil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.List;

public class MisOfertasPostulantesAdapter extends RecyclerView.Adapter<MisOfertasPostulantesAdapter.MisOfertasViewHolder> {

    private final Context context;
    private final List<Trabajo> listaTrabajos;

    public MisOfertasPostulantesAdapter(Context context, List<Trabajo> listaTrabajos) {
        this.context = context;
        this.listaTrabajos = listaTrabajos;
    }

    @NonNull
    @Override
    public MisOfertasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_trabajo_sin_boton, parent, false);
        return new MisOfertasViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull MisOfertasViewHolder holder, int position) {
        Trabajo trabajo = listaTrabajos.get(position);

        // Título
        holder.txtTituloTrabajo.setText(trabajo.getTitulo() != null ? trabajo.getTitulo() : "Sin título");

        // Ubicación
        holder.txtUbicacionTrabajo.setText("📍 " + (trabajo.getUbicacion() != null ? trabajo.getUbicacion() : "Sin ubicación"));

        // Sueldo
        if (trabajo.getSueldo() != null && !trabajo.getSueldo().isEmpty()) {
            holder.txtSueldoTrabajo.setText("💰 $" + trabajo.getSueldo());
        } else {
            holder.txtSueldoTrabajo.setText("💰 Sin sueldo");
        }

        // Descripción
        holder.txtDescripcionTrabajo.setText(trabajo.getDescripcion() != null ? trabajo.getDescripcion() : "Sin descripción");

        // Fecha de publicación (maneja Date y Timestamp)
        Date fecha = trabajo.getFechaPublicacion();
        if (fecha == null && trabajo instanceof Trabajo) {
            Object rawFecha = null;
            try {
                // Intentar leer desde Firestore manualmente si fue cargado como Timestamp
                rawFecha = (Object) trabajo.getFechaPublicacion();
            } catch (Exception ignored) {}

            if (rawFecha instanceof Timestamp) {
                fecha = ((Timestamp) rawFecha).toDate();
            }
        }

        if (fecha != null) {
            String tiempo = tiempoTranscurrido(fecha);
            holder.txtFechaTrabajo.setText("🕒 Publicado " + tiempo);
        } else {
            holder.txtFechaTrabajo.setText("🕒 Publicado recientemente");
        }
    }

    @Override
    public int getItemCount() {
        return listaTrabajos != null ? listaTrabajos.size() : 0;
    }

    // Método para calcular el tiempo transcurrido desde la publicación
    private String tiempoTranscurrido(Date fechaPublicacion) {
        long ahora = System.currentTimeMillis();
        long diferencia = ahora - fechaPublicacion.getTime();

        long segundos = diferencia / 1000;
        long minutos = segundos / 60;
        long horas = minutos / 60;
        long dias = horas / 24;

        if (dias > 0) {
            return "hace " + dias + (dias == 1 ? " día" : " días");
        } else if (horas > 0) {
            return "hace " + horas + (horas == 1 ? " hora" : " horas");
        } else if (minutos > 0) {
            return "hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        } else {
            return "recién publicado";
        }
    }

    // ViewHolder para los elementos
    public static class MisOfertasViewHolder extends RecyclerView.ViewHolder {
        TextView txtTituloTrabajo, txtUbicacionTrabajo, txtSueldoTrabajo, txtDescripcionTrabajo, txtFechaTrabajo;

        public MisOfertasViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTituloTrabajo = itemView.findViewById(R.id.txtTituloTrabajo);
            txtUbicacionTrabajo = itemView.findViewById(R.id.txtUbicacionTrabajo);
            txtSueldoTrabajo = itemView.findViewById(R.id.txtSueldoTrabajo);
            txtDescripcionTrabajo = itemView.findViewById(R.id.txtDescripcionTrabajo);
            txtFechaTrabajo = itemView.findViewById(R.id.txtFechaTrabajo);
        }
    }
}