package com.edgarmontero.proyectoDam.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentHistorialPacienteBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class HistorialPacienteFragment extends Fragment {

    private FragmentHistorialPacienteBinding binding;
    private String dniPaciente;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistorialPacienteBinding.inflate(inflater, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        dniPaciente = sharedPreferences.getString("dni_paciente", "");
        binding.buttonBuscarPaciente.setOnClickListener(view -> performSearch(dniPaciente));

        setupListView();
        setupDatePicker();
        performSearch(dniPaciente);


        return binding.getRoot();
    }

    private void setupDatePicker() {
        binding.editTextFechaFin.setOnClickListener(view -> {
            Calendar calendario = Calendar.getInstance();
            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (datePicker, year1, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        binding.editTextFechaFin.setText(fechaSeleccionada);
                    }, year, month, day);
            datePickerDialog.show();
        });

        binding.editTextFechaInicio.setOnClickListener(view -> {
            Calendar calendario = Calendar.getInstance();
            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (datePicker, year1, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        binding.editTextFechaInicio.setText(fechaSeleccionada);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void setupListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        binding.listViewConsultas.setAdapter(adapter);

    }

    private void performSearch(String dni) {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "buscarConsultas.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String fechaInicio = binding.editTextFechaInicio.getText().toString();
                String fechaFin = binding.editTextFechaFin.getText().toString();
                String estadoConsulta = binding.spinnerEstadoConsulta.getSelectedItem().toString();

                StringBuilder data = new StringBuilder();
                data.append(URLEncoder.encode("dni", "UTF-8")).append("=").append(URLEncoder.encode(dni.toUpperCase(), "UTF-8"));
                data.append("&").append(URLEncoder.encode("tipo_dni", "UTF-8")).append("=").append(URLEncoder.encode("paciente", "UTF-8"));

                if (!fechaInicio.isEmpty()) {
                    data.append("&").append(URLEncoder.encode("fecha_inicio", "UTF-8")).append("=").append(URLEncoder.encode(fechaInicio, "UTF-8"));
                }
                if (!fechaFin.isEmpty()) {
                    data.append("&").append(URLEncoder.encode("fecha_fin", "UTF-8")).append("=").append(URLEncoder.encode(fechaFin, "UTF-8"));
                }
                if (!estadoConsulta.equals("Todos")) {
                    data.append("&").append(URLEncoder.encode("estado_consulta", "UTF-8")).append("=").append(URLEncoder.encode(estadoConsulta, "UTF-8"));
                }

                writer.write(data.toString());
                writer.flush();
                writer.close();
                os.close();

                processSearchResponse(conn);
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                });
            }
        });
        thread.start();
    }

    private void processSearchResponse(HttpURLConnection conn) throws IOException {
        InputStream in = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        reader.close();
        in.close();
        conn.disconnect();

        final String response = result.toString();
        getActivity().runOnUiThread(() -> updateListView(response));
    }

    private void updateListView(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            ArrayList<String> consultasList = new ArrayList<>();
            final HashMap<Integer, String> consultaIds = new HashMap<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int idConsulta = obj.getInt("id_consulta");
                String consulta = "Consulta: " + obj.getString("tipo_consulta") +
                        "\nDescripción: " + obj.getString("descripcion_consulta") +
                        "\nFecha: " + obj.getString("fecha_consulta") +
                        "\nEstado: " + obj.getString("estado_consulta");
                consultasList.add(consulta);
                consultaIds.put(i, String.valueOf(idConsulta));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, consultasList);
            binding.listViewConsultas.setAdapter(adapter);

        } catch (JSONException e) {
            Toast.makeText(getContext(), "Error al procesar los datos", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
