package com.edgarmontero.proyectoDam.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.edgarmontero.proyectoDam.R;
import com.edgarmontero.proyectoDam.utils.Validator;

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
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private Button registerButton;
    private Context context;
    private EditText dniEditText, nombreApellidoEditText, direccionEditText, fechaNacimientoEditText, telefonoEditText, password2EditText, passwordEditText, emailEditText, nameEditText;
    private int userId;
    private String nombreApellido, direccion, fecha, telefono, dni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupElements();
    }

    private void setupElements() {
        nameEditText = findViewById(R.id.etNombreUsuario);
        emailEditText = findViewById(R.id.etemail);
        telefonoEditText = findViewById(R.id.etTelefono);
        dniEditText = findViewById(R.id.etDni);
        direccionEditText = findViewById(R.id.etDireccion);
        fechaNacimientoEditText = findViewById(R.id.etFechaNacimiento);
        passwordEditText = findViewById(R.id.etpassword);
        password2EditText = findViewById(R.id.etpasswordrepeat);
        nombreApellidoEditText = findViewById(R.id.etNombreApellido);
        context = RegisterActivity.this;

        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String password2 = password2EditText.getText().toString();
                dni = dniEditText.getText().toString();
                nombreApellido = nombreApellidoEditText.getText().toString();
                direccion = direccionEditText.getText().toString();
                fecha = fechaNacimientoEditText.getText().toString();
                telefono = telefonoEditText.getText().toString();

                register(name, email, password, password2, dni, nombreApellido, direccion, fecha, telefono);
            }
        });

        fechaNacimientoEditText.setOnClickListener(view -> {
            Calendar calendario = Calendar.getInstance();
            int year = calendario.get(Calendar.YEAR);
            int month = calendario.get(Calendar.MONTH);
            int day = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this,
                    (datePicker, year1, monthOfYear, dayOfMonth) -> {
                        String fechaSeleccionada = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                        fechaNacimientoEditText.setText(fechaSeleccionada);
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void register(String name, String email, String password, String password2, String dni, String nombreApellido, String direccion, String fecha, String telefono) {
        if (areFieldsValid(name, email, password, password2, dni, telefono, nombreApellido, direccion, fecha)) {
            registerData(name, email, password);
        }
    }

    private boolean areFieldsValid(String name, String email, String password, String password2, String dni, String telefono, String nombreApellido, String direccion, String fecha) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty() || dni.isEmpty() || telefono.isEmpty() || nombreApellido.isEmpty() || direccion.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Validator.isEmailValid(email, context)) {
            return false;
        }
        if (!Validator.isPassValid(password, password2, context)) {
            return false;
        }
        if (!Validator.validarDNI(dni, context)) {
            return false;
        }
        if (!Validator.isPhoneValid(telefono, context)) {
            return false;
        }
        if (!Validator.isDateValid(fecha, context)) {
            return false;
        }
        return true;
    }

    private void registerData(String name, String email, String password) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(getString(R.string.ip) + "register.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
                    data += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
                    data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

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

                    final String response = result.toString().trim();

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        final String message = jsonObject.getString("message");
                        userId = jsonObject.getInt("user_id");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                savePatientData();
                                Toast.makeText(getApplicationContext(), message + " ID: " + userId, Toast.LENGTH_LONG).show();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void savePatientData() {
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
                data += "&" + URLEncoder.encode("nombre", "UTF-8") + "=" + URLEncoder.encode(nombreApellido, "UTF-8");
                data += "&" + URLEncoder.encode("fecha_nacimiento", "UTF-8") + "=" + URLEncoder.encode(fecha, "UTF-8");
                data += "&" + URLEncoder.encode("direccion", "UTF-8") + "=" + URLEncoder.encode(direccion, "UTF-8");
                data += "&" + URLEncoder.encode("telefono", "UTF-8") + "=" + URLEncoder.encode(telefono, "UTF-8");
                data += "&" + URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(userId), "UTF-8");

                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                processServerResponse(conn);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show());
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

        runOnUiThread(() -> {
            if (result.toString().contains("success")) {
                String finalMessage = "Paciente guardado con éxito";

                Toast.makeText(context, finalMessage, Toast.LENGTH_SHORT).show();
                finish();

            } else {
                String finalMessage2 = "Error al guardar el paciente";
                Toast.makeText(context, finalMessage2, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
