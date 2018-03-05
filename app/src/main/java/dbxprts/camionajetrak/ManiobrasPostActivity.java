package dbxprts.camionajetrak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ManiobrasPostActivity extends AppCompatActivity {

    TextView placa, operador, producto, cliente, hora_inicio;
    Spinner zona_industria;
    Button btnRegistro;
    Chronometer cronometro;
    boolean isStart = true;
    String baseurlInicio = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_inicio_maniobras?";
    String baseurlFin = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_fin_maniobras?";
    String urlZonasIndustria = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/spinner/zonaindustria/";
    HashMap<String, String> zonaIndustriaMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maniobras_post);
        setGUIElements();
        new GetZonasIndustria().execute();
        setButtonBehavior();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (getValuesFromIntent("inicio_maniobra") != null) {
            btnRegistro.setBackgroundColor(Color.RED);
            btnRegistro.setText("Fin Maniobra");
            cronometro.setVisibility(View.GONE);
            isStart = false;
        }
    }

    public void setGUIElements() {
        placa = (TextView) findViewById(R.id.Placa);
        operador = (TextView) findViewById(R.id.Operador);
        producto = (TextView) findViewById(R.id.Producto);
        cliente = (TextView) findViewById(R.id.Cliente);
        operador = (TextView) findViewById(R.id.Operador);
        hora_inicio = (TextView) findViewById(R.id.HoraInicio);
        btnRegistro = (Button) findViewById(R.id.RegistroBtn);
        zona_industria = (Spinner) findViewById(R.id.ZonaIndustria);
        cronometro = (Chronometer) findViewById(R.id.Cronometro);
    }

    public void setButtonBehavior() {
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.RegistroBtn:
                        if (isStart) {
                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            String formatted = format.format(cal.getTime());
                            hora_inicio.setText(formatted);
                            cronometro.setBase(SystemClock.elapsedRealtime());
                            AlertDialog.Builder builder = new AlertDialog.Builder(ManiobrasPostActivity.this, R.style.MyAlertDialogStyle);
                            builder.setTitle("Confirmar");
                            builder.setMessage("Deseas registrar esta maniobra?");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    btnRegistro.setBackgroundColor(Color.RED);
                                    btnRegistro.setText("Fin Maniobra");
                                    cronometro.start();
                                    new InsertIntoDatabase().execute();
                                }
                            });
                            builder.setNegativeButton("Cancelar", null);
                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ManiobrasPostActivity.this, R.style.MyAlertDialogStyle);
                            builder.setTitle("Confirmar");
                            builder.setMessage("Deseas registrar esta maniobra?");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new InsertIntoDatabase().execute();
                                }
                            });
                            builder.setNegativeButton("Cancelar", null);
                            builder.show();
                        }
                        break;
                }
            }
        });
    }

    public void setGUIValues() {
        placa.setText(getValuesFromIntent("placa"));
        cliente.setText(getValuesFromIntent("cliente"));
        producto.setText(getValuesFromIntent("producto"));
        operador.setText(getValuesFromIntent("operador"));

        String[] spinnerArray = new String[zonaIndustriaMap.size()];
        for (int i = 0; i < zonaIndustriaMap.size(); i++) {
            spinnerArray[i] = zonaIndustriaMap.entrySet().toArray()[i].toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, zonaIndustriaMap.keySet().toArray(new String[0]));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zona_industria.setAdapter(adapter);
        int spinnerPosition = adapter.getPosition(getValuesFromIntent("zona_industria"));
        zona_industria.setSelection(spinnerPosition);

        if (getValuesFromIntent("inicio_maniobra") != null) {
            hora_inicio.setText(stripDateString(getValuesFromIntent("inicio_maniobra")));
        }
    }

    public String getValuesFromIntent(String requiredValue) {
        Bundle extras = getIntent().getExtras();
        String value = null;
        if (extras != null) {
            switch (requiredValue) {
                case "idEvento":
                    value = extras.getString("idEvento");
                    break;
                case "placa":
                    value = extras.getString("placa");
                    break;
                case "cliente":
                    value = extras.getString("cliente");
                    break;
                case "producto":
                    value = extras.getString("producto");
                    break;
                case "operador":
                    value = extras.getString("operador");
                    break;
                case "zona_industria":
                    value = extras.getString("zona_industria");
                    break;
                case "inicio_maniobra":
                    value = extras.getString("inicio_maniobra");
                    break;
                case "id_producto":
                    value = extras.getString("id_producto");
                    break;
                case "id_tipo_maniobra":
                    value = extras.getString("id_tipo_maniobra");
                    break;
            }
        }

        return value;
    }

    public String buildURL() {
        String final_url = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat hora = new SimpleDateFormat("HH:mm");
        String fecha_formatted = fecha.format(cal.getTime());
        String hora_formatted = hora.format(cal.getTime());
        String id_zona_industria = zonaIndustriaMap.get(zona_industria.getSelectedItem());
        int selector;
        if (isStart) {
            selector = 0;
        } else {
            selector = 1;
        }
        switch (selector) {
            case 0:
                final_url = baseurlInicio + "p_id_entrada=" + getValuesFromIntent("idEvento") + "&p_fecha=" + fecha_formatted
                        + "&p_hora=" + hora_formatted + "&p_usuario_android=" + ((GlobalVariables) this.getApplication()).getActiveUser()
                        + "&p_id_zona_industria=" + id_zona_industria;
                break;
            case 1:
                final_url = baseurlFin + "p_id_entrada=" + getValuesFromIntent("idEvento") + "&p_fecha=" + fecha_formatted
                        + "&p_hora=" + hora_formatted + "&p_usuario_android=" + ((GlobalVariables) this.getApplication()).getActiveUser();
                break;
        }
        return final_url;
    }

    private class GetZonasIndustria extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(ManiobrasPostActivity.this, R.style.LoadingDialogStyle);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setTitle("Cargando");
            progress.setMessage("Conectando con base de datos...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(urlZonasIndustria + getValuesFromIntent("id_producto") + "/" + getValuesFromIntent("id_tipo_maniobra"));
            Log.e("Maniobras Post", "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for (int x = 0; x < items.length(); x++) {
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        String id_zona_industria = BL.getString("id_zona_industria");
                        String zona_industria = BL.getString("zona_industria");
                        zonaIndustriaMap.put(zona_industria, id_zona_industria);
                    }

                } catch (final JSONException e) {
                    Log.e("Maniobras Post", "Json parsing error: " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setGUIValues();
            progress.dismiss();
        }
    }

    public class InsertIntoDatabase extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(ManiobrasPostActivity.this, R.style.LoadingDialogStyle);

        public InsertIntoDatabase() {
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setMessage("Registrando Evento...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }


        @Override
        protected String doInBackground(String... params) {

            String urlString = buildURL(); // URL to call

            String resultToDisplay = "";

            InputStream in = null;
            try {

                URL url = new URL(urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                in = new BufferedInputStream(urlConnection.getInputStream());


            } catch (Exception e) {

                System.out.println(e.getMessage());

                return e.getMessage();

            }

            try {
                resultToDisplay = IOUtils.toString(in, "UTF-8");
                //to [convert][1] byte stream to a string
                Log.d("HTTP result ", resultToDisplay);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultToDisplay;
        }


        @Override
        protected void onPostExecute(String result) {
            //Update the UI
            progress.dismiss();
            Log.d("HTTP POST ", "SUCCESS");
            if (isStart) {
                isStart = false;
            } else {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    }

    public String stripDateString(String date) {
        String strippedDate = null;
        String day = date.split("T")[0];
        String hour_first = date.split("T")[1];
        String hour_final = hour_first.split("Z")[0];
        strippedDate = day + " " + hour_final;
        return strippedDate;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
