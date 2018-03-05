package dbxprts.camionajetrak;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VigilanciaNuevoPostActivity extends AppCompatActivity  {
    private String TAG = VigilanciaFragment.class.getSimpleName();

    SearchableSpinner spinnerOperador, spinnerCliente, spinnerProducto;

    TextView placa;

    private static String urlOperador = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/vigilancia/nuevo/operadores";
    private static String urlCliente = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/vigilancia/nuevo/clientes";
    private static String urlProducto = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/vigilancia/nuevo/productos";

    HashMap<String, String> operadoresMap = new HashMap<String, String>();
    HashMap<String, String> clientesMap = new HashMap<String, String>();
    HashMap<String, String> productosMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vigilancia_nuevo_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Nuevo Registro");
        setGUIElements();
        setSpinnerSpecs();
        new populateMap().execute();
    }

    public void setGUIElements(){
        spinnerOperador = (SearchableSpinner) findViewById(R.id.operador_spinner);
        spinnerCliente = (SearchableSpinner) findViewById(R.id.cliente_spinner);
        spinnerProducto = (SearchableSpinner) findViewById(R.id.producto_spinner);
        placa = (TextView) findViewById(R.id.Placas);
    }

    public void setSpinnerSpecs(){
        spinnerOperador.setTitle("Elige el operador.");
        spinnerOperador.setPositiveButton("Cerrar");
        spinnerCliente.setTitle("Elige el cliente.");
        spinnerCliente.setPositiveButton("Cerrar");
        spinnerProducto.setTitle("Elige el prodcuto.");
        spinnerProducto.setPositiveButton("Cerrar");
        placa.setText(getValuesFromIntent("placa"));
    }

    private class populateMap extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(VigilanciaNuevoPostActivity.this, R.style.LoadingDialogStyle);
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
            String jsonStrClientes = sh.makeServiceCall(urlCliente);
            String jsonStrProductos = sh.makeServiceCall(urlProducto);
            Log.e(TAG, "Response from url Operadores: " + jsonStrOperadores);
            Log.e(TAG, "Response from url Clientes: " + jsonStrClientes);
            Log.e(TAG, "Response from url Productos: " + jsonStrProductos);

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

            if (jsonStrClientes != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStrClientes);
                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for(int x = 0; x < items.length(); x++){
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        String id_cliente = BL.getString("id_cliente");
                        String nombre_cliente = BL.getString("nombre_cliente");
                        clientesMap.put(id_cliente,nombre_cliente);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }

            if (jsonStrProductos != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStrProductos);
                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for(int x = 0; x < items.length(); x++){
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        String id_producto = BL.getString("id_producto");
                        String producto = BL.getString("producto");
                        productosMap.put(id_producto,producto);
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

        List<String> clientesList = new ArrayList<>();
        for (Map.Entry<String, String> entry : clientesMap.entrySet())
        {
            clientesList.add(entry.getValue());
        }
        ArrayAdapter<String> clientesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, clientesList);
        clientesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clientesAdapter);

        List<String> productosList = new ArrayList<>();
        for (Map.Entry<String, String> entry : productosMap.entrySet())
        {
            productosList.add(entry.getValue());
        }
        ArrayAdapter<String> productosAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, productosList);
        productosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProducto.setAdapter(productosAdapter);

    }

    public String getValuesFromIntent(String requiredValue) {
        Bundle extras = getIntent().getExtras();
        String value = null;
        if (extras != null) {
            switch (requiredValue) {
                case "id_unidad":
                    value = extras.getString("id_unidad");
                    break;
                case "placa":
                    value = extras.getString("placa");
                    break;
            }
        }

        return value;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
