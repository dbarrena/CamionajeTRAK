package dbxprts.camionajetrak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class VigilanciaNuevoActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private String TAG = VigilanciaFragment.class.getSimpleName();
    private SearchView mSearchView;
    private ListView mListView;
    Multimap<String, String> listEntries = ArrayListMultimap.create();
    // URL to get contacts JSON
    private static String url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/vigilancia/placas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vigilancia_nuevo);
        setGUIElements();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Unidades");
        new populateMap().execute();
    }

    public void setGUIElements(){
        mSearchView = (SearchView) findViewById(R.id.search_view);
        mListView = (ListView) findViewById(R.id.list_view);
    }

    private class populateMap extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(VigilanciaNuevoActivity.this, R.style.LoadingDialogStyle);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listEntries.clear();
            progress.setTitle("Cargando");
            progress.setMessage("Conectando con base de datos...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    for(int x = 0; x < items.length(); x++){
                        // Getting JSON Object Node
                        JSONObject BL = items.getJSONObject(x);
                        //Getting items in Object Node
                        String id_unidad = BL.getString("id_unidad");
                        String placas = BL.getString("placas");
                        Log.e(TAG, "placas "+placas+" id "+id_unidad);
                        listEntries.put(id_unidad,placas);
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            populateListView();
            setupSearchView();
            progress.dismiss();
        }
    }

    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setQueryHint("Busqueda");
        mSearchView.setFocusable(false);
        mSearchView.setIconified(false);
        mSearchView.clearFocus();
    }

    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            ((CustomAdapter)mListView.getAdapter()).getFilter().filter(null);
        } else {
            ((CustomAdapter)mListView.getAdapter()).getFilter().filter(newText);
        }
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public void setListItemBehavior(final List<HashMap<String, String>> listItems){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                HashMap<String, String> resultsMap = listItems.get(position);
                String placa = resultsMap.get("First Line");
                String id_unidad = resultsMap.get("Third Line");
                Intent i = new Intent(getApplicationContext(), VigilanciaNuevoPostActivity.class);
                i.putExtra("placa",placa);
                i.putExtra("id_unidad",id_unidad);
                startActivity(i);
            }
        });
    }

    public void populateListView() {
        List<HashMap<String, String>> listItems = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(VigilanciaNuevoActivity.this, listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});
        for (String key : listEntries.keySet()) {
            Collection<String> keys = listEntries.get(key);
            HashMap<String, String> resultsMap = new HashMap<>();
            List<String> userList = new ArrayList<>(keys);
            resultsMap.put("First Line", userList.get(0));
            resultsMap.put("Third Line", key);
            listItems.add(resultsMap);
            mListView.setTextFilterEnabled(true);
            mListView.setAdapter(adapter);
            setListItemBehavior(listItems);
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
