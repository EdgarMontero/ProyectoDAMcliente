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
import com.edgarmontero.proyectoDam.utils.Validator;

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
    private EditText editTextBuscarUsuario, etNombre, etDni, etFechaNacimiento, etDireccion, etTelefono;
    private Button buttonBuscarUsuario, btnSave;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditarPacienteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Referencias a los componentes de la UI
        editTextBuscarUsuario = binding.editTextBuscarUsuario;
        buttonBuscarUsuario = binding.buttonBuscarUsuario;
        etNombre = binding.etNombrePaciente;
        etDni = binding.etDniPaciente;
        etFechaNacimiento = binding.etFechaNacimiento;
        etDireccion = binding.etDireccion;
        etTelefono = binding.etTelefono;
        btnSave = binding.btnGuardarPaciente;

        // Configuración de los botones
        buttonBuscarUsuario.setOnClickListener(v -> {
            String dniPaciente = editTextBuscarUsuario.getText().toString();
            buscarUsuario(dniPaciente);
        });

        btnSave.setOnClickListener(v -> {
            String dniPaciente = etDni.getText().toString();
            String nombre = etNombre.getText().toString();
            String fechaNacimiento = etFechaNacimiento.getText().toString();
            String direccion = etDireccion.getText().toString();
            String telefono = etTelefono.getText().toString();
            actualizarPaciente(dniPaciente, nombre, fechaNacimiento, direccion, telefono);
        });

        return root;
    }

    private void buscarUsuario(String dniPaciente) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "buscarPaciente.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = URLEncoder.encode("dni_paciente", "UTF-8") + "=" + URLEncoder.encode(dniPaciente, "UTF-8");

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
                    JSONObject jsonObject = new JSONObject(response.toString());

                    if(jsonObject.has("error")) {
                        String errorMsg = jsonObject.getString("error");
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show());
                    } else {
                        updateEditTexts(jsonObject);
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error en la conexión: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error técnico al procesar la búsqueda", Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
    }

    private void updateEditTexts(JSONObject jsonObject) throws JSONException {
        getActivity().runOnUiThread(() -> {
            try {

                // Hace visibles los EditText y el botón de guardar
                editTextBuscarUsuario.setVisibility(View.GONE);
                buttonBuscarUsuario.setVisibility(View.GONE);

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

    private void actualizarPaciente(String dniPaciente, String nombre, String fechaNacimiento, String direccion, String telefono) {
        if (nombre.isEmpty() || fechaNacimiento.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Validator.validarDNI(dniPaciente, getContext())) {
            return;
        }

        if (!Validator.isBirthDateValid(fechaNacimiento, getContext())) {
            return;
        }

        if (!Validator.isPhoneValid(telefono, getContext())) {
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "actualizarPaciente.php"); // Asegúrate de que la URL es correcta
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = URLEncoder.encode("accion", "UTF-8") + "=actualizar&" +
                        URLEncoder.encode("dni_paciente", "UTF-8") + "=" + URLEncoder.encode(dniPaciente, "UTF-8") + "&" +
                        URLEncoder.encode("nombre", "UTF-8") + "=" + URLEncoder.encode(nombre, "UTF-8") + "&" +
                        URLEncoder.encode("fecha_nacimiento", "UTF-8") + "=" + URLEncoder.encode(fechaNacimiento, "UTF-8") + "&" +
                        URLEncoder.encode("direccion", "UTF-8") + "=" + URLEncoder.encode(direccion, "UTF-8") + "&" +
                        URLEncoder.encode("telefono", "UTF-8") + "=" + URLEncoder.encode(telefono, "UTF-8");

                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String response = reader.readLine();
                    JSONObject jsonObject = new JSONObject(response);

                    getActivity().runOnUiThread(() -> {
                        try {
                            if (jsonObject.has("error")) {
                                Toast.makeText(getContext(), jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                                if (getActivity() != null) {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                    reader.close();
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error en la conexión: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error técnico al actualizar los datos", Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
    }
}