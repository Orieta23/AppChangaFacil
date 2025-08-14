package com.example.chambafacil;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MisOfertasAdapter2 extends RecyclerView.Adapter<MisOfertasAdapter2.ViewHolder> {

    private final Context context;
    private final List<Trabajo> listaOfertas;

    public MisOfertasAdapter2(Context context, List<Trabajo> listaOfertas) {
        this.context = context;
        this.listaOfertas = listaOfertas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_mis_ofertas, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trabajo trabajo = listaOfertas.get(position);

        // Título y descripción
        holder.txtTitulo.setText(trabajo.getTitulo() != null ? trabajo.getTitulo() : "Sin título");
        holder.txtDescripcion.setText(trabajo.getDescripcion() != null ? trabajo.getDescripcion() : "Sin descripción");

        // Ubicación
        holder.txtUbicacion.setText("📍 " + (trabajo.getUbicacion() != null ? trabajo.getUbicacion() : "Sin ubicación"));

        // Sueldo
        if (trabajo.getSueldo() != null && !trabajo.getSueldo().isEmpty()) {
            holder.txtSueldo.setText("💰 $" + trabajo.getSueldo());
        } else {
            holder.txtSueldo.setText("💰 Sin sueldo");
        }

        // Fecha con soporte para Timestamp y logs
        Date fecha = null;
        Object rawFecha = trabajo.getFechaPublicacion();
        if (rawFecha != null) {
            Log.d("DEBUG_FECHA", "Oferta: " + trabajo.getTitulo() + " - Tipo de fecha: " + rawFecha.getClass().getName());
            if (rawFecha instanceof Timestamp) {
                fecha = ((Timestamp) rawFecha).toDate();
                Log.d("DEBUG_FECHA", "Convertido de Timestamp a Date: " + fecha);
            } else if (rawFecha instanceof Date) {
                fecha = (Date) rawFecha;
                Log.d("DEBUG_FECHA", "Fecha recibida directamente como Date: " + fecha);
            } else {
                Log.w("DEBUG_FECHA", "Tipo inesperado para fecha_publicacion en: " + trabajo.getTitulo());
            }
        } else {
            Log.d("DEBUG_FECHA", "fecha_publicacion es NULL para: " + trabajo.getTitulo());
        }

        if (fecha != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.txtFecha.setText("📅 " + sdf.format(fecha));
        } else {
            holder.txtFecha.setText("📅 Fecha desconocida");
        }

        // Botón editar
        holder.btnEditar.setOnClickListener(v -> {
            if (trabajo.getId() == null || trabajo.getId().isEmpty()) {
                Toast.makeText(context, "ID de trabajo inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString("idTrabajo", trabajo.getId());
            Fragment fragment = new EditarTrabajoFragment();
            fragment.setArguments(bundle);

            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Botón ver postulantes
        holder.btnVerPostulantes.setOnClickListener(v -> {
            if (trabajo.getId() == null || trabajo.getId().isEmpty()) {
                Toast.makeText(context, "ID de trabajo inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString("idTrabajo", trabajo.getId());
            Fragment fragment = new VerPostulantesFragment();
            fragment.setArguments(bundle);

            FragmentTransaction transaction = ((FragmentActivity) context).getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.nav_host_fragment, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return listaOfertas != null ? listaOfertas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtDescripcion, txtUbicacion, txtFecha, txtSueldo;
        Button btnEditar, btnVerPostulantes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloTrabajo);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcionTrabajo);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacionTrabajo);
            txtFecha = itemView.findViewById(R.id.txtFechaTrabajo);
            txtSueldo = itemView.findViewById(R.id.txtSueldoTrabajo);
            btnEditar = itemView.findViewById(R.id.btnEditarPublicacion);
            btnVerPostulantes = itemView.findViewById(R.id.btnVerPostulantes);
        }
    }
}