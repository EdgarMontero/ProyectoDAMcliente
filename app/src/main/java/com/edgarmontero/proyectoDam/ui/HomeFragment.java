package com.edgarmontero.proyectoDam.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        setupBtn();

        return binding.getRoot();
    }

    private void setupBtn() {
        binding.btnHistorialPaciente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_nav_home_to_nav_historial);
            }
        });

        binding.btnCrearConsulta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_nav_home_to_nav_crear_consulta);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
