package com.edgarmontero.proyectoDam.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.edgarmontero.proyectoDam.MainActivity;
import com.edgarmontero.proyectoDam.Objetos.User;
import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.databinding.FragmentCrearPacienteBinding;


import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.List;
import java.util.Map;

public class CrearPaciente extends Fragment {

    private FragmentCrearPacienteBinding binding;
    List<User> usersList = new ArrayList<>();
    AutoCompleteTextView autoCompleteTextViewUser;
    Map<String, Integer> userNameToIdMap = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCrearPacienteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        // Asignar eventos
        final EditText etDni = binding.etDniPaciente;
        final EditText etNombre = binding.etNombrePaciente;
        final EditText etFechaNacimiento = binding.etFechaNacimiento;
        final EditText etDireccion = binding.etDireccion;
        final EditText etTelefono = binding.etTelefono;
        Button btnSave = binding.btnGuardarPaciente;

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí deberías agregar la lógica para guardar los datos del paciente
                savePatient(etDni.getText().toString(), etNombre.getText().toString(),
                        etFechaNacimiento.getText().toString(), etDireccion.getText().toString(),
                        etTelefono.getText().toString());
            }
        });

        etFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendario = Calendar.getInstance();
                int year = calendario.get(Calendar.YEAR);
                int month = calendario.get(Calendar.MONTH);
                int day = calendario.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Formato de la fecha
                                String fechaSeleccionada = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                etFechaNacimiento.setText(fechaSeleccionada);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        // Inflar y configurar vistas...
        autoCompleteTextViewUser = binding.autoCompleteTextViewUser;

        // Llenar la lista de usuarios desde la base de datos o API
        fetchUsers();

        // Configuración del adaptador
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, extractUserNames(usersList));
        autoCompleteTextViewUser.setAdapter(adapter);

        // Manejar selecciones
        autoCompleteTextViewUser.setOnItemClickListener((parent, view, position, id) -> {
            String userName = adapter.getItem(position);
            Integer userId = userNameToIdMap.get(userName);
            // Ahora tienes el ID del usuario seleccionado, que puedes usar para guardarlo en la base de datos
        });

        return root;
    }

    private void fetchUsers() {
        // Simulación de obtención de datos, reemplazar con llamada real a la base de datos o API
        usersList.add(new User(1, "Alice"));
        usersList.add(new User(2, "Bob"));
        // Asegúrate de llenar el mapa también
        for (User user : usersList) {
            userNameToIdMap.put(user.getName(), user.getIdUser());
        }
    }

    private List<String> extractUserNames(List<User> usersList) {
        List<String> names = new ArrayList<>();
        for (User user : usersList) {
            names.add(user.getName());
        }
        return names;
    }

    private void savePatient(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://10.0.2.2/api/guardarPaciente.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    String data = URLEncoder.encode("dni_paciente", "UTF-8") + "=" + URLEncoder.encode(dni, "UTF-8");
                    data += "&" + URLEncoder.encode("nombre", "UTF-8") + "=" + URLEncoder.encode(nombre, "UTF-8");
                    data += "&" + URLEncoder.encode("fecha_nacimiento", "UTF-8") + "=" + URLEncoder.encode(fechaNacimiento, "UTF-8");
                    data += "&" + URLEncoder.encode("direccion", "UTF-8") + "=" + URLEncoder.encode(direccion, "UTF-8");
                    data += "&" + URLEncoder.encode("telefono", "UTF-8") + "=" + URLEncoder.encode(telefono, "UTF-8");
                    data += "&" + URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8");  // Asegúrate de cambiar este ID según corresponda

                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    reader.close();
                    in.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        thread.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
