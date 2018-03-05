package dbxprts.camionajetrak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VigilanciaPostActivity extends AppCompatActivity {
    private String TAG = VigilanciaFragment.class.getSimpleName();

    TextView producto, cliente;
    SearchableSpinner spinnerOperador, spinnerPlaca;
    Button btnRegistro;
    String baseurl = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_vigilancia?";
    private static String urlOperador = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/vigilancia/nuevo/operadores";
    private static String urlPlaca = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/vigilancia/placas";
    HashMap<String, String> operadoresMap = new HashMap<String, String>();
    HashMap<String, String> placasMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vigilancia_post);
        setGUIElements();
        setGUIValues();
        setButtonBehavior();
        setSpinnerSpecs();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        new populateMap().execute();
    }

    public void setGUIElements() {
        spinnerPlaca = (SearchableSpinner) findViewById(R.id.Placa);
        spinnerOperador = (SearchableSpinner) findViewById(R.id.Operador);
        producto = (TextView) findViewById(R.id.Producto);
        cliente = (TextView) findViewById(R.id.Cliente);
        btnRegistro = (Button) findViewById(R.id.RegistroBtn);
    }

    public void setGUIValues() {
        cliente.setText(getValuesFromIntent("cliente"));
        producto.setText(getValuesFromIntent("producto"));
    }

    public String getValuesFromIntent(String requiredValue) {
        Bundle extras = getIntent().getExtras();
        String value = null;
        if (extras != null) {
            switch (requiredValue) {
                case "idEvento":
                    value = extras.getString("idEvento");
                    break;
                case "cliente":
                    value = extras.getString("cliente");
                    break;
                case "producto":
                    value = extras.getString("producto");
                    break;
            }
        }

        return value;
    }

    public void setButtonBehavior() {
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VigilanciaPostActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle("Confirmar");
                builder.setMessage("Deseas registrar esta maniobra?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new CallAPI().execute();
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.show();
            }
        });
    }

    public String buildURL(){
        String final_url;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat hora = new SimpleDateFormat("HH:mm");
        String fecha_formatted = fecha.format(cal.getTime());
        String hora_formatted = hora.format(cal.getTime());
        final_url = baseurl+"id_entrada="+getValuesFromIntent("idEvento")+"&registro_vig_fecha="+fecha_formatted
                +"&registro_vig_hora="+hora_formatted + "&modificado_por=" + ((GlobalVariables) this.getApplication()).getActiveUser();
        return final_url;
    }

    public class CallAPI extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(VigilanciaPostActivity.this, R.style.LoadingDialogStyle);

        public CallAPI() {
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
            Toast.makeText(getApplicationContext(), "Se registró con éxito el evento", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        }
    }

    private class populateMap extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(VigilanciaPostActivity.this, R.style.LoadingDialogStyle);
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
            String jsonStrOperadores = sh.makeServiceCall(urlOperador);
            String jsonStrPlacas = sh.makeServiceCall(urlPlaca);
            Log.e(TAG, "Response from url Operadores: " + jsonStrOperadores);
            Log.e(TAG, "Response from url Placas: " + jsonStrPlacas);

            if (jsonStrOperadores != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStrOperadores);
                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for(int x = 0; x < items.length(); x++){
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        String id_operador = BL.getString("id_operador");
                        String operador = BL.getString("operador");
                        operadoresMap.put(id_operador,operador);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }

            if (jsonStrPlacas != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStrPlacas);
                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for(int x = 0; x < items.length(); x++){
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        String id_unidad = BL.getString("id_unidad");
                        String placas = BL.getString("placas");
                        Log.e(TAG, "placas "+placas+" id "+id_unidad);
                        placasMap.put(id_unidad,placas);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            populateSpinners();
            progress.dismiss();
        }
    }

    public void populateSpinners(){
        List<String> operadoresList = new ArrayList<>();
        for (Map.Entry<String, String> entry : operadoresMap.entrySet())
        {
            operadoresList.add(entry.getValue());
        }
        ArrayAdapter<String> operadoresAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, operadoresList);
        operadoresAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOperador.setAdapter(operadoresAdapter);

        List<String> placasList = new ArrayList<>();
        for (Map.Entry<String, String> entry : placasMap.entrySet())
        {
            placasList.add(entry.getValue());
        }
        ArrayAdapter<String> placasAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, placasList);
        placasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlaca.setAdapter(placasAdapter);
    }

    public void setSpinnerSpecs(){
        spinnerOperador.setTitle("Elige el operador.");
        spinnerOperador.setPositiveButton("Cerrar");
        spinnerPlaca.setTitle("Elige las placas.");
        spinnerPlaca.setPositiveButton("Cerrar");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
