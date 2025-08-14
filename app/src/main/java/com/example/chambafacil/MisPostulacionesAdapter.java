package com.example.chambafacil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chambafacil.ui.slideshow.Postulacion;

import java.util.Date;
import java.util.List;

public class MisPostulacionesAdapter extends RecyclerView.Adapter<MisPostulacionesAdapter.ViewHolder> {

    private final Fragment fragment;
    private final List<Postulacion> listaPostulaciones;

    public MisPostulacionesAdapter(Fragment fragment, List<Postulacion> listaPostulaciones) {
        this.fragment = fragment;
        this.listaPostulaciones = listaPostulaciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_postulacion, parent, false); // 👈 cambiado el layout
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Postulacion postulacion = listaPostulaciones.get(position);

        holder.txtTitulo.setText(postulacion.getTitulo());
        holder.txtUbicacion.setText("📍 " + postulacion.getUbicacion());
        holder.txtSueldo.setText("💰 " + postulacion.getSueldo());

        if (postulacion.getFecha_postulacion() != null) {
            Date fecha = postulacion.getFecha_postulacion().toDate();
            String tiempo = calcularTiempoTranscurrido(fecha);
            holder.txtFecha.setText("Te postulaste " + tiempo);
        } else {
            holder.txtFecha.setText("Te postulaste (sin fecha disponible)");
        }

        holder.btnVerPostulacion.setOnClickListener(v -> {
            String idTrabajo = postulacion.getIdTrabajo();
            if (idTrabajo == null || idTrabajo.isEmpty()) {
                Toast.makeText(fragment.getContext(), "ID de trabajo inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("idTrabajo", idTrabajo);

            NavController navController = Navigation.findNavController(
                    fragment.requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_nav_postulaciones_to_detallePostulacionFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return listaPostulaciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtFecha, txtUbicacion, txtSueldo;
        Button btnVerPostulacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloPostulacion);
            txtFecha = itemView.findViewById(R.id.txtFechaPostulacion);
            txtUbicacion = itemView.findViewById(R.id.txtUbicacionPostulacion);
            txtSueldo = itemView.findViewById(R.id.txtSueldoPostulacion);
            btnVerPostulacion = itemView.findViewById(R.id.btnVerPostulacion);
        }
    }

    private String calcularTiempoTranscurrido(Date fechaPostulacion) {
        long ahora = System.currentTimeMillis();
        long diferencia = ahora - fechaPostulacion.getTime();

        long minutos = diferencia / (1000 * 60);
        long horas = minutos / 60;
        long dias = horas / 24;

        if (dias > 0) return "hace " + dias + (dias == 1 ? " día" : " días");
        if (horas > 0) return "hace " + horas + (horas == 1 ? " hora" : " horas");
        if (minutos > 0) return "hace " + minutos + (minutos == 1 ? " minuto" : " minutos");
        return "recién ahora";
    }
}