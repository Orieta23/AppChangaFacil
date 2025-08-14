package com.example.chambafacil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ResenasAdapter extends RecyclerView.Adapter<ResenasAdapter.ResenaViewHolder> {

    private final List<Resena> listaResenas;

    // ✅ Constructor
    public ResenasAdapter(List<Resena> listaResenas) {
        this.listaResenas = listaResenas;
    }

    @NonNull
    @Override
    public ResenaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resena, parent, false);
        return new ResenaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResenaViewHolder holder, int position) {
        Resena resena = listaResenas.get(position);

        // ✅ Mostrar nombre del usuario que hizo la reseña
        if (resena.getNombreUsuario() != null && !resena.getNombreUsuario().trim().isEmpty()) {
            holder.txtNombreUsuario.setText(resena.getNombreUsuario());
        } else {
            holder.txtNombreUsuario.setText("Usuario desconocido");
        }

        // ✅ Mostrar puntuación
        holder.ratingBar.setRating((float) resena.getPuntuacion());

        // ✅ Validar comentario nulo o vacío
        String comentario = resena.getComentario();
        if (comentario == null || comentario.trim().isEmpty()) {
            holder.txtComentario.setText("Sin comentario");
        } else {
            holder.txtComentario.setText(comentario.trim());
        }

        // ✅ Foto de perfil (si en un futuro se implementa)
        // holder.imgFotoUsuario.setImageResource(R.drawable.ic_person); // placeholder
    }

    @Override
    public int getItemCount() {
        return listaResenas != null ? listaResenas.size() : 0;
    }

    public static class ResenaViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFotoUsuario;
        TextView txtNombreUsuario, txtComentario;
        RatingBar ratingBar;

        public ResenaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFotoUsuario = itemView.findViewById(R.id.imgFotoUsuario); // Foto de perfil
            txtNombreUsuario = itemView.findViewById(R.id.txtNombreUsuarioResena); // Nombre dinámico
            ratingBar = itemView.findViewById(R.id.ratingResena);
            txtComentario = itemView.findViewById(R.id.txtComentarioResena);
        }
    }
}