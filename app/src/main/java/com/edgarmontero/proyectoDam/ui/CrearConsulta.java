package com.edgarmontero.proyectoDam.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentCrearConsultaBinding;
import com.edgarmontero.proyectoDam.databinding.FragmentHistorialPacienteBinding;

public class CrearConsulta extends Fragment {

    private FragmentCrearConsultaBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCrearConsultaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configuración del botón
        binding.btnCrearConsulta.setOnClickListener(v -> crearConsulta());

        return root;
    }

    private void crearConsulta() {
        String medicoId = binding.etMedicoId.getText().toString();
        String pacienteId = binding.etPacienteId.getText().toString();
        String tipoConsulta = binding.etTipoConsulta.getText().toString();
        String descripcion = binding.etDescripcionConsulta.getText().toString();
        String fecha = binding.etFechaConsulta.getText().toString();

        // Aquí deberías agregar el código para insertar los datos en la base de datos
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
