package com.example.chambafacil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private NavController navController;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String tipoUsuario = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains("tipoUsuario")) {
                        Log.e("MainActivity", "Usuario sin tipo asignado");
                        startActivity(new Intent(this, SeleccionRolActivity.class));
                        finish();
                        return;
                    }

                    tipoUsuario = documentSnapshot.getString("tipoUsuario");

                    // Carga el layout correspondiente según el tipo de usuario
                    if ("postulante".equalsIgnoreCase(tipoUsuario)) {
                        setContentView(R.layout.activity_main_postulante);
                    } else {
                        setContentView(R.layout.activity_main); // empleador
                    }

                    iniciarUI();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error Firestore: " + e.getMessage());
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });
    }

    private void iniciarUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) {
            Log.e("MainActivity", "No se encontró el NavHostFragment");
            return;
        }

        navController = navHostFragment.getNavController();

        // Carga el navGraph adecuado según el tipo de usuario
        if ("postulante".equalsIgnoreCase(tipoUsuario)) {
            navController.setGraph(R.navigation.nav_graph);
        } else {
            navController.setGraph(R.navigation.nav_empleador);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        FloatingActionButton fabBuscar = findViewById(R.id.fabBuscar);
        if ("postulante".equalsIgnoreCase(tipoUsuario)) {
            if (fabBuscar != null) {
                fabBuscar.setOnClickListener(v -> {
                    if (bottomNavigationView.getMenu().findItem(R.id.nav_buscar) != null) {
                        bottomNavigationView.setSelectedItemId(R.id.nav_buscar);
                    }
                });
            }
        } else {
            if (fabBuscar != null) {
                fabBuscar.hide();
            }
        }

        // Fragmento inicial al abrir
        if ("postulante".equalsIgnoreCase(tipoUsuario)) {
            if (bottomNavigationView.getMenu().findItem(R.id.nav_buscar) != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_buscar);
            }
        } else {
            if (bottomNavigationView.getMenu().findItem(R.id.nav_ofrecer_trabajo) != null) {
                bottomNavigationView.setSelectedItemId(R.id.nav_ofrecer_trabajo);
            }
        }

        // Menú lateral (Drawer)
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);

        if (drawerLayout != null && navigationView != null && toolbar != null) {
            toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_configuracion) {
                    // ✅ Abre ConfiguracionFragment
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new ConfiguracionFragment())
                            .addToBackStack(null)
                            .commit();
                } else if (id == R.id.nav_acerca) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new AcercaDeFragment())
                            .addToBackStack(null)
                            .commit();
                } else if (id == R.id.nav_soporte) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new SoporteFragment())
                            .addToBackStack(null)
                            .commit();
                } else if (id == R.id.nav_cerrar_sesion) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else if (id == R.id.nav_cambiar_tipo_usuario) {
                    mostrarDialogoCambioTipo();
                }

                drawerLayout.closeDrawers();
                return true;
            });
        } else {
            Log.i("MainActivity", "No hay NavigationView disponible o Toolbar nula");
        }
    }

    private void mostrarDialogoCambioTipo() {
        String[] opciones = {"Postulante", "Empleador"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona tu tipo de usuario");
        builder.setSingleChoiceItems(opciones, -1, (dialog, which) -> {
            String tipoSeleccionado = opciones[which];
            actualizarTipoUsuarioEnFirestore(tipoSeleccionado);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void actualizarTipoUsuarioEnFirestore(String tipoSeleccionado) {
        if (user != null) {
            db.collection("usuarios").document(user.getUid())
                    .update("tipoUsuario", tipoSeleccionado.toLowerCase())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Tipo de usuario actualizado a " + tipoSeleccionado, Toast.LENGTH_SHORT).show();

                        // Reinicia MainActivity limpiamente
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return toggle != null && toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp() || super.onSupportNavigateUp();
    }
}