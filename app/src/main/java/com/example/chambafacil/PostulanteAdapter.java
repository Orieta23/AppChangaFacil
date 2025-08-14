package com.example.chambafacil;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostulanteAdapter extends RecyclerView.Adapter<PostulanteAdapter.PostulanteViewHolder> {

    private Context context;
    private List<Postulante> listaPostulantes;
    private FragmentManager fragmentManager;

    public PostulanteAdapter(Context context, List<Postulante> listaPostulantes, FragmentManager fragmentManager) {
        this.context = context;
        this.listaPostulantes = listaPostulantes;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public PostulanteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_postulacion_card, parent, false);
        return new PostulanteViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PostulanteViewHolder holder, int position) {
        Postulante p = listaPostulantes.get(position);

        holder.txtNombre.setText("👤 " + p.getNombre());
        holder.txtEmail.setText("✉️ " + p.getEmail());

        if (!TextUtils.isEmpty(p.getTelefono())) {
            holder.txtTelefono.setText("📞 WhatsApp: " + p.getTelefono());
            holder.txtTelefono.setTextColor(Color.parseColor("#2E7D32"));
        } else {
            holder.txtTelefono.setText("📞 Sin número");
            holder.txtTelefono.setTextColor(Color.GRAY);
        }

        // Al hacer clic, abrir el perfil
        holder.itemView.setOnClickListener(v -> {
            PerfilUsuarioFragment fragment = PerfilUsuarioFragment.newInstance(
                    p.getUid(),
                    "postulante"
            );

            fragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return listaPostulantes.size();
    }

    public static class PostulanteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtEmail, txtTelefono;

        public PostulanteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            txtTelefono = itemView.findViewById(R.id.txtTelefono);
        }
    }
}