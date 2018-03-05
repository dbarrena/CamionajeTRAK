package dbxprts.camionajetrak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalidadPostActivity extends AppCompatActivity {

    TextView placa, operador, producto, cliente, hora_inicio;
    Button btnRegistro;
    Chronometer cronometro;
    boolean isStart = true;
    boolean isEntregaCertificado = false;
    String baseurlInicio = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_inicio_calidad?";
    String baseurlFin = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_fin_calidad?";
    String baseurlEntregaCert = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_entrega_certificado?";
    LocalDatabaseConnection localDB;
    Spinner aprobadoCalidadSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calidad_post);
        setGUIElements();
        setGUIValues();
        setButtonBehavior();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        localDB = new LocalDatabaseConnection(this);
        if(getValuesFromIntent("inicio_calidad") != null){
            btnRegistro.setBackgroundColor(Color.RED);
            btnRegistro.setText("Fin Muestreo");
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
        cronometro = (Chronometer) findViewById(R.id.Cronometro);
        aprobadoCalidadSpinner = (Spinner) findViewById(R.id.AprobadoCalidad);
        aprobadoCalidadSpinner.setVisibility(View.GONE);
    }

    public void setButtonBehavior() {
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.RegistroBtn:
                        if (isStart && !isEntregaCertificado) {
                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            String formatted = format.format(cal.getTime());
                            hora_inicio.setText(formatted);
                            cronometro.setBase(SystemClock.elapsedRealtime());
                            AlertDialog.Builder builder = new AlertDialog.Builder(CalidadPostActivity.this, R.style.MyAlertDialogStyle);
                            builder.setTitle("Confirmar");
                            builder.setMessage("Deseas registrar este evento?");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    btnRegistro.setBackgroundColor(Color.RED);
                                    btnRegistro.setText("Fin Muestreo");
                                    cronometro.start();
                                    new CallAPI().execute();
                                }
                            });
                            builder.setNegativeButton("Cancelar", null);
                            builder.show();
                        } else if (isEntregaCertificado) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CalidadPostActivity.this, R.style.MyAlertDialogStyle);
                            builder.setTitle("Confirmar");
                            builder.setMessage("Deseas registrar este evento?");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new CallAPI().execute();
                                }
                            });
                            builder.setNegativeButton("Cancelar", null);
                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CalidadPostActivity.this, R.style.MyAlertDialogStyle);
                            builder.setTitle("Confirmar");
                            builder.setMessage("Deseas registrar este evento?");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    btnRegistro.setBackgroundColor(Color.parseColor("#FF470F78"));
                                    btnRegistro.setText("Entrega Certificado");
                                    aprobadoCalidadSpinner.setVisibility(View.VISIBLE);
                                    cronometro.setVisibility(View.GONE);
                                    new CallAPI().execute();
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
        if(getValuesFromIntent("inicio_calidad") != null){
            hora_inicio.setText(stripDateString(getValuesFromIntent("inicio_calidad")));
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
                case "inicio_calidad":
                    value = extras.getString("inicio_calidad");
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

        int aprobadoCalidadValue = 0;

        int selector;


        if (isStart && !isEntregaCertificado) { //Es inicio calidad
            selector = 0;
        } else if (!isEntregaCertificado && !isStart) { //Es fin calidad
            selector = 1;
        } else { //Es entrega certificado
            selector = 2;
        }

        switch (selector) {
            case 0:
                final_url = baseurlInicio + "p_id_entrada=" + getValuesFromIntent("idEvento") + "&p_fecha=" + fecha_formatted
                        + "&p_hora=" + hora_formatted + "&p_usuario_android=" + ((GlobalVariables) this.getApplication()).getActiveUser();
                break;
            case 1:
                final_url = baseurlFin + "p_id_entrada=" + getValuesFromIntent("idEvento") + "&p_fecha=" + fecha_formatted
                        + "&p_hora=" + hora_formatted + "&p_usuario_android=" + ((GlobalVariables) this.getApplication()).getActiveUser();
                break;
            case 2:
                if(aprobadoCalidadSpinner.getSelectedItem().toString().equals("Si")){
                    aprobadoCalidadValue = 1;
                }
                final_url = baseurlEntregaCert + "p_id_entrada=" + getValuesFromIntent("idEvento") + "&p_fecha=" + fecha_formatted
                        + "&p_hora=" + hora_formatted + "&p_aprobado_calidad=" + aprobadoCalidadValue + "&p_usuario_android=" + ((GlobalVariables) this.getApplication()).getActiveUser();
                break;
        }

        return final_url;
    }

    public class CallAPI extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(CalidadPostActivity.this, R.style.LoadingDialogStyle);

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
            if (isStart && !isEntregaCertificado) { //Inicio Maniobra
                isStart = false;
                /*localDB.insertIntoDatabase(Integer.parseInt(getValuesFromIntent("idEvento")), getValuesFromIntent("placa"),
                        getValuesFromIntent("producto"), getValuesFromIntent("cliente"),
                        "Fin Maniobra", hora_inicio.getText().toString());*/
            } else if (!isEntregaCertificado && !isStart) { //Fin Maniobra
                isEntregaCertificado = true;
                //localDB.updateEtapaCalidad(Integer.parseInt(getValuesFromIntent("idEvento")), "Entrega Certificado");
            } else { //Entrega Calidad
                //localDB.deleteCalidad(Integer.parseInt(getValuesFromIntent("idEvento")));
                Toast.makeText(getApplicationContext(), "Se registró con éxito el evento", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    }

    public String stripDateString(String date){
        String strippedDate = null;
        String day = date.split("T")[0];
        String hour_first = date.split("T")[1];
        String hour_final = hour_first.split("Z")[0];
        strippedDate = day+" "+hour_final;
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
