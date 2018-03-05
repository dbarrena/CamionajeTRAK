package dbxprts.camionajetrak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class LimpiezaPostActivity extends AppCompatActivity {

    RadioGroup RG1, RG2, RG3, RG4, RG5, RG6, RG7, RG8, RG9, RG10, RG11, RG12, RG13;
    Button registroLimpiezaBtn;
    TextView HoraLimpiezaTag, HoraLimpieza;
    ArrayList<RadioGroup> radioButtonsList;
    ArrayList<String> radioButtonsValues = new ArrayList<>();
    String baseURL = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina.update_limpieza?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limpieza_post);
        setGUIElements();
        setGUIBehavior();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void setGUIElements() {
        RG1 = (RadioGroup) findViewById(R.id.FirstQuestionRadioGroup);
        RG2 = (RadioGroup) findViewById(R.id.SecondQuestionRadioGroup);
        RG3 = (RadioGroup) findViewById(R.id.ThirdQuestionRadioGroup);
        RG4 = (RadioGroup) findViewById(R.id.FourthQuestionRadioGroup);
        RG5 = (RadioGroup) findViewById(R.id.FifthQuestionRadioGroup);
        RG6 = (RadioGroup) findViewById(R.id.SixthQuestionRadioGroup);
        RG7 = (RadioGroup) findViewById(R.id.SeventhQuestionRadioGroup);
        RG8 = (RadioGroup) findViewById(R.id.EighthQuestionRadioGroup);
        RG9 = (RadioGroup) findViewById(R.id.NinthQuestionRadioGroup);
        RG10 = (RadioGroup) findViewById(R.id.TenthQuestionRadioGroup);
        RG11 = (RadioGroup) findViewById(R.id.EleventhQuestionRadioGroup);
        RG12 = (RadioGroup) findViewById(R.id.TwelfthQuestionRadioGroup);
        RG13 = (RadioGroup) findViewById(R.id.ThirteenthQuestionRadioGroup);
        radioButtonsList = new ArrayList<>(Arrays.asList(RG1, RG2, RG3, RG4, RG5, RG6, RG7, RG8, RG9, RG10, RG11, RG12, RG13));
        registroLimpiezaBtn = (Button) findViewById(R.id.registroLimpiezaBtn);
        HoraLimpiezaTag = (TextView) findViewById(R.id.HoraLimpiezaTag);
        HoraLimpieza = (TextView) findViewById(R.id.HoraLimpieza);
    }

    public void setGUIBehavior() {
        registroLimpiezaBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new CallAPI().execute();
            }
        });
        RG13.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.Q13Y) {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat fecha = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat hora = new SimpleDateFormat("HH:mm");
                    String fecha_formatted = fecha.format(cal.getTime());
                    String hora_formatted = hora.format(cal.getTime());
                    HoraLimpiezaTag.setVisibility(View.VISIBLE);
                    HoraLimpieza.setVisibility(View.VISIBLE);
                    HoraLimpieza.setText(fecha_formatted + " " + hora_formatted);
                } else if (checkedId == R.id.Q13N) {
                    HoraLimpiezaTag.setVisibility(View.GONE);
                    HoraLimpieza.setVisibility(View.GONE);
                }
            }
        });
    }

    public void getRadioButtonsValues() {
        radioButtonsValues.clear();
        for (int x = 0; x < radioButtonsList.size(); x++) {
            if (getPositionOfSelectedButton(radioButtonsList.get(x)) == 0) {
                radioButtonsValues.add("1");
            } else {
                radioButtonsValues.add("0");
            }
        }
    }

    public int getPositionOfSelectedButton(RadioGroup RG) {
        int selectedButtonID = RG.getCheckedRadioButtonId();
        View radioButton = RG.findViewById(selectedButtonID);
        int position = RG.indexOfChild(radioButton);
        return position;
    }

    public String buildURL() {
        String final_url = "";
        getRadioButtonsValues();
        String fecha = (String) HoraLimpieza.getText();
        String[] fecha_splited = fecha.split("\\s+");
        final_url = baseURL + "p_id_entrada=" + getValuesFromIntent("idEvento") + "&q1=" + radioButtonsValues.get(0) + "&q2=" + radioButtonsValues.get(1)
                + "&q3=" + radioButtonsValues.get(2) + "&q4=" + radioButtonsValues.get(3) + "&q5=" + radioButtonsValues.get(4) + "&q6=" + radioButtonsValues.get(4)
                + "&q7=" + radioButtonsValues.get(6) + "&q8=" + radioButtonsValues.get(7) + "&q9=" + radioButtonsValues.get(8) + "&q10=" + radioButtonsValues.get(9)
                + "&q11=" + radioButtonsValues.get(10) + "&q12=" + radioButtonsValues.get(11) + "&q13=" + radioButtonsValues.get(12)+"&p_libera_fecha="+fecha_splited[0]
                +"&p_libera_hora="+fecha_splited[1]
                + "&p_usuario_android=" + ((GlobalVariables) this.getApplication()).getActiveUser();;
        return final_url;
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
            }
        }

        return value;
    }

    public class CallAPI extends AsyncTask<String, String, String> {
        ProgressDialog progress = new ProgressDialog(LimpiezaPostActivity.this, R.style.LoadingDialogStyle);

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

                Log.e("URL LIMPIEZA POST", url.toString());

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
