package com.example.chambafacil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ConfiguracionFragment extends Fragment {

    private Switch switchNotificaciones, switchDarkMode;
    private TextView txtCambiarPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracion, container, false);

        switchNotificaciones = view.findViewById(R.id.switchNotificaciones);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        txtCambiarPassword = view.findViewById(R.id.txtCambiarPassword);

        // Notificaciones
        switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(getContext(),
                        isChecked ? "Notificaciones activadas" : "Notificaciones desactivadas",
                        Toast.LENGTH_SHORT).show()
        );

        // Modo oscuro
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(getContext(),
                        isChecked ? "Modo oscuro activado" : "Modo oscuro desactivado",
                        Toast.LENGTH_SHORT).show()
        );

        // Cambiar contraseña
        txtCambiarPassword.setOnClickListener(v ->
                Toast.makeText(getContext(), "Abrir pantalla de cambio de contraseña", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}