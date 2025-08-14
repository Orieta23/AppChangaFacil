package com.example.chambafacil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SoporteFragment extends Fragment {

    public SoporteFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_soporte, container, false);

        Button btnEmail = view.findViewById(R.id.btnContactoEmail);
        Button btnWhatsapp = view.findViewById(R.id.btnContactoWhatsapp);

        // ✅ Botón EMAIL con chooser y filtro de apps de correo
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"empretechapps@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Consulta desde Chamba Fácil");

            try {
                startActivity(Intent.createChooser(intent, "Enviar email con:"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), "No hay ninguna app de correo instalada", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Botón WHATSAPP
        btnWhatsapp.setOnClickListener(v -> {
            String numero = "+5491165388166"; // tu número real
            String mensaje = "Hola, necesito ayuda con la app Chamba Fácil.";
            String url = "https://wa.me/" + numero.replace("+", "") + "?text=" + Uri.encode(mensaje);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        return view;
    }
}