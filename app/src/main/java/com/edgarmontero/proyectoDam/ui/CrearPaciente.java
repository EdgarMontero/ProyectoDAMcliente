package com.edgarmontero.proyectoDam.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentCrearConsultaBinding;
import com.edgarmontero.proyectoDam.databinding.FragmentCrearPacienteBinding;


public class CrearPaciente extends Fragment {

    private FragmentCrearPacienteBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCrearPacienteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}