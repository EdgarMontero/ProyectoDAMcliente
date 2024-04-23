package com.edgarmontero.proyectoDam;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

public class MenuPrincipalActivity extends AppCompatActivity {

    Button btnCrearConsulta, btnCrearPaciente, btnAgenda, btnHistorialPaciente, btnEditarPaciente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        btnCrearConsulta = findViewById(R.id.btnCrearConsulta);
        btnCrearPaciente = findViewById(R.id.btnCrearPaciente);
        btnAgenda = findViewById(R.id.btnAgenda);
        btnHistorialPaciente = findViewById(R.id.btnHistorialPaciente);
        btnEditarPaciente = findViewById(R.id.btnEditarPaciente);

        // Ejemplos de setOnClickListener para cada botón, iniciando actividades según el caso
        btnCrearConsulta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent para iniciar actividad de Crear Consulta
                // Intent intent = new Intent(MenuPrincipalActivity.this, CrearConsultaActivity.class);
                // startActivity(intent);
            }
        });
        // Agrega listeners similares para los otros botones
    }
}
