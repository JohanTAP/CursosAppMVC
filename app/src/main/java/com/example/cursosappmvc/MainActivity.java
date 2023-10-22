package com.example.cursosappmvc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cursosappmvc.view.fragment.CursoFragment;
import com.example.cursosappmvc.view.fragment.PerfilFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_curso) {
                    selectedFragment = new CursoFragment();
                } else if (item.getItemId() == R.id.nav_perfil) {
                    selectedFragment = new PerfilFragment();
                }

                // Comenzamos la transacción del fragmento
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();

                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // Cargamos el fragmento de "Cursos" por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CursoFragment()).commit();
        }
    }
}
