package com.edgarmontero.proyectoDam.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentEditarPerfilBinding;
import com.edgarmontero.proyectoDam.utils.Validator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

public class EditarPerfilFragment extends Fragment {

    private FragmentEditarPerfilBinding binding;
    private EditText etNombre, etDni, etFechaNacimiento, etDireccion, etTelefono;
    private Button btnSave;
    private String dniPaciente;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditarPerfilBinding.inflate(inflater, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        dniPaciente = sharedPreferences.getString("dni_paciente", "");

        setupViewBindings();
        setupDatePicker();
        buscarUsuario(dniPaciente);

        return binding.getRoot();
    }

    private void setupDatePicker() {
        etFechaNacimiento.setOnClickListener(view -> {
            Calendar calendario = Calendar.getInstance();
            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (datePicker, year1, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        etFechaNacimiento.setText(fechaSeleccionada);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupViewBindings() {
        etNombre = binding.etNombrePaciente;
        etDni = binding.etDniPaciente;
        etFechaNacimiento = binding.etFechaNacimiento;
        etDireccion = binding.etDireccion;
        etTelefono = binding.etTelefono;
        btnSave = binding.btnGuardarPaciente;

        btnSave.setOnClickListener(v -> {
            String dniPaciente = etDni.getText().toString();
            String nombre = etNombre.getText().toString();
            String fechaNacimiento = etFechaNacimiento.getText().toString();
            String direccion = etDireccion.getText().toString();
            String telefono = etTelefono.getText().toString();
            savePatient(dniPaciente, nombre, fechaNacimiento, direccion, telefono);
        });
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

    private void savePatient(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        if (areFieldsValid(dni, nombre, fechaNacimiento, direccion, telefono)) {
            savePatientData(dni, nombre, fechaNacimiento, direccion, telefono);
        }
    }

    private boolean areFieldsValid(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        Context context = getContext();
        if (dni.isEmpty() || nombre.isEmpty() || fechaNacimiento.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Validator.validarDNI(dni, context)) {
            return false;
        }

        if (!Validator.isFechaNacimintoValida(fechaNacimiento, context)) {
            return false;
        }

        if (!Validator.isPhoneValid(telefono, context)) {
            return false;
        }

        return true;
    }

    private void savePatientData(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "actualizarPaciente.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("dni_paciente", "UTF-8") + "=" + URLEncoder.encode(dniPaciente, "UTF-8");
                data += "&" + URLEncoder.encode("nombre", "UTF-8") + "=" + URLEncoder.encode(nombre, "UTF-8");
                data += "&" + URLEncoder.encode("fecha_nacimiento", "UTF-8") + "=" + URLEncoder.encode(fechaNacimiento, "UTF-8");
                data += "&" + URLEncoder.encode("direccion", "UTF-8") + "=" + URLEncoder.encode(direccion, "UTF-8");
                data += "&" + URLEncoder.encode("telefono", "UTF-8") + "=" + URLEncoder.encode(telefono, "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                processServerResponse(conn);
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }
        });
        thread.start();
    }

    private void processServerResponse(HttpURLConnection conn) {
        try {
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
                            NavHostFragment.findNavController(this)
                                    .navigate(R.id.action_nav_editar_perfil_to_nav_home);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                });
                reader.close();
            } else {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error en la conexión: " + responseCode, Toast.LENGTH_SHORT).show());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error técnico al actualizar los datos", Toast.LENGTH_SHORT).show());
        }
    }

}