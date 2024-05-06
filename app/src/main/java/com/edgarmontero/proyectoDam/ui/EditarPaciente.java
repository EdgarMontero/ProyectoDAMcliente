package com.edgarmontero.proyectoDam.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentEditarPacienteBinding;
import com.edgarmontero.proyectoDam.databinding.FragmentHistorialPacienteBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class EditarPaciente extends Fragment {

    private FragmentEditarPacienteBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditarPacienteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText editTextBuscarUsuario = binding.editTextBuscarUsuario;
        Button buttonBuscarUsuario = binding.buttonBuscarUsuario;

        buttonBuscarUsuario.setOnClickListener(v -> {
            String usuarioBuscado = editTextBuscarUsuario.getText().toString();
            buscarUsuario(usuarioBuscado);
            editTextBuscarUsuario.setVisibility(View.GONE);
            buttonBuscarUsuario.setVisibility(View.GONE);

        });



        return root;
    }

    private void buscarUsuario(String nombreUsuario) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.buscarPacienteURL)); // Asegúrate de cambiar esta URL.
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = URLEncoder.encode("nombre_usuario", "UTF-8") + "=" + URLEncoder.encode(nombreUsuario, "UTF-8");

                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    reader.close();
                    updateEditTexts(new JSONObject(response.toString()));
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void updateEditTexts(JSONObject jsonObject) throws JSONException {
        getActivity().runOnUiThread(() -> {
            try {
                // Hace visibles los EditText y el botón de guardar
                EditText etNombre = binding.etNombrePaciente;
                etNombre.setVisibility(View.VISIBLE);

                EditText etDni = binding.etDniPaciente;
                etDni.setVisibility(View.VISIBLE);

                EditText etFechaNacimiento = binding.etFechaNacimiento;
                etFechaNacimiento.setVisibility(View.VISIBLE);

                EditText etDireccion = binding.etDireccion;
                etDireccion.setVisibility(View.VISIBLE);

                EditText etTelefono = binding.etTelefono;
                etTelefono.setVisibility(View.VISIBLE);

                Button btnSave = binding.btnGuardarPaciente;
                btnSave.setVisibility(View.VISIBLE);

                // Actualiza los campos con los datos recibidos
                etNombre.setText(jsonObject.getString("nombre"));
                etDni.setText(jsonObject.getString("dni_paciente"));
                etFechaNacimiento.setText(jsonObject.getString("fecha_nacimiento"));
                etDireccion.setText(jsonObject.getString("direccion"));
                etTelefono.setText(jsonObject.getString("telefono"));
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error en el formato de los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}