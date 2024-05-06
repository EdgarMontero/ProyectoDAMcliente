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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrearPaciente extends Fragment {

    private FragmentCrearPacienteBinding binding;

    private static final char[] LETRAS_DNI = {
            'T', 'R', 'W', 'A', 'G', 'M', 'Y', 'F', 'P', 'D', 'X', 'B', 'N', 'J', 'Z', 'S', 'Q', 'V', 'H', 'L', 'C', 'K', 'E'
    };
    List<User> usersList = new ArrayList<>();
    AutoCompleteTextView autoCompleteTextViewUser;
    Map<String, Integer> userNameToIdMap = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCrearPacienteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        autoCompleteTextViewUser = binding.autoCompleteTextViewUser;

        fetchUsers();

        setupViewBindings();

        return root;
    }

    private void setupViewBindings() {
        final EditText etDni = binding.etDniPaciente;
        final EditText etNombre = binding.etNombrePaciente;
        final EditText etFechaNacimiento = binding.etFechaNacimiento;
        final EditText etDireccion = binding.etDireccion;
        final EditText etTelefono = binding.etTelefono;
        Button btnSave = binding.btnGuardarPaciente;

        btnSave.setOnClickListener(v -> savePatient(etDni.getText().toString(), etNombre.getText().toString(),
                etFechaNacimiento.getText().toString(), etDireccion.getText().toString(),
                etTelefono.getText().toString()));

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

    private void fetchUsers() {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip)+"fetchUsers.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();
                in.close();

                parseJson(result.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void parseJson(String json) {
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int id = obj.getInt("id_user");
                String name = obj.getString("name");
                usersList.add(new User(id, name));
            }

            getActivity().runOnUiThread(() -> updateUI());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        for (User user : usersList) {
            userNameToIdMap.put(user.getName(), user.getIdUser());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, extractUserNames(usersList));
        autoCompleteTextViewUser.setAdapter(adapter);
    }

    private List<String> extractUserNames(List<User> usersList) {
        List<String> names = new ArrayList<>();
        for (User user : usersList) {
            names.add(user.getName());
        }
        return names;
    }

    private void savePatient(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        // Comprobación de campos vacíos
        if (!areFieldsValid(dni, nombre, fechaNacimiento, direccion, telefono)) {
            return;
        }

        // Validación de la fecha de nacimiento
        if (!isBirthDateValid(fechaNacimiento)) {
            return;
        }

        // Validación del teléfono
        if (!isPhoneValid(telefono)) {
            return;
        }

        // Realizar conexión y guardar datos del paciente
        savePatientData(dni, nombre, fechaNacimiento, direccion, telefono);
    }

    public static boolean validarDNI(String dni) {
        // Verifica que la longitud sea correcta y que el último carácter sea una letra
        if (dni == null || dni.length() != 9 || !Character.isLetter(dni.charAt(8))) {
            return false;
        }

        // Extrae la parte numérica y la letra del DNI
        String parteNumerica = dni.substring(0, 8);
        char letra = Character.toUpperCase(dni.charAt(8));  // Convierte la letra a mayúscula

        try {
            // Convierte la parte numérica a entero
            int numerosDNI = Integer.parseInt(parteNumerica);
            // Calcula el índice de la letra de control
            int indice = numerosDNI % 23;
            // Obtiene la letra correspondiente del array
            char letraCalculada = LETRAS_DNI[indice];

            // Comprueba si la letra calculada coincide con la letra del DNI
            return letra == letraCalculada;
        } catch (NumberFormatException e) {
            // En caso de que la parte numérica no sea un número válido
            return false;
        }
    }


    private boolean areFieldsValid(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        if (dni.isEmpty() || nombre.isEmpty() || fechaNacimiento.isEmpty() || direccion.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(getContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!validarDNI(dni)) {
            Toast.makeText(getContext(), "DNI no válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isBirthDateValid(String fechaNacimiento) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaNac = sdf.parse(fechaNacimiento);
            Date fechaActual = new Date();
            if (!fechaNac.before(fechaActual)) {
                Toast.makeText(getContext(), "La fecha de nacimiento debe ser anterior al día de hoy", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Formato de fecha no válido", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isPhoneValid(String telefono) {
        if (!telefono.matches("\\d{9}")) {
            Toast.makeText(getContext(), "El teléfono debe tener 9 dígitos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void savePatientData(String dni, String nombre, String fechaNacimiento, String direccion, String telefono) {
        // Convertir DNI a mayúsculas y hacerlo final para usar en lambda
        final String finalDni = dni.toUpperCase();

        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.ip) + "guardarPaciente.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("dni_paciente", "UTF-8") + "=" + URLEncoder.encode(finalDni, "UTF-8");
                data += "&" + URLEncoder.encode("nombre", "UTF-8") + "=" + URLEncoder.encode(nombre, "UTF-8");
                data += "&" + URLEncoder.encode("fecha_nacimiento", "UTF-8") + "=" + URLEncoder.encode(fechaNacimiento, "UTF-8");
                data += "&" + URLEncoder.encode("direccion", "UTF-8") + "=" + URLEncoder.encode(direccion, "UTF-8");
                data += "&" + URLEncoder.encode("telefono", "UTF-8") + "=" + URLEncoder.encode(telefono, "UTF-8");

                // Obtener el usuario desde el contexto actual
                String selectedUserName = autoCompleteTextViewUser.getText().toString();
                Integer userId = userNameToIdMap.get(selectedUserName);
                if (userId == null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                data += "&" + URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(userId), "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                // Procesar respuesta
                processServerResponse(conn);
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT). show();
                });
            }
        });
        thread.start();
    }

    private void processServerResponse(HttpURLConnection conn) throws IOException {
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

        if (result.toString().contains("success")) {
            String finalMessage = "Paciente guardado con éxito";
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), finalMessage, Toast.LENGTH_SHORT).show();
                // Retroceder al fragmento anterior
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        } else {
            String finalMessage2 = "Error al guardar el paciente";
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), finalMessage2, Toast.LENGTH_SHORT).show();
            });
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
