package dbxprts.camionajetrak;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class CalidadOnProgressFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String TAG = VigilanciaFragment.class.getSimpleName();
    private SearchView mSearchView;
    private ListView mListView;
    Multimap<String, String> listEntries = ArrayListMultimap.create();
    private static String url = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/calidad/onprogress";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CalidadOnProgressFragment() {
        // Required empty public constructor
    }

    public static CalidadOnProgressFragment newInstance(String param1, String param2) {
        CalidadOnProgressFragment fragment = new CalidadOnProgressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_plus).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_calidad, container, false);
        ((MainActivity) getActivity()).setActionBarTitle("Calidad");
        mSearchView = (SearchView) view.findViewById(R.id.search_view);
        mListView = (ListView) view.findViewById(R.id.list_view);
        TextView bienvenido = (TextView) getActivity().findViewById(R.id.Bienvenido);
        bienvenido.setVisibility(View.GONE);
        ImageView img = (ImageView) getActivity().findViewById(R.id.ImagenInicio);
        img.setVisibility(View.GONE);
        new CalidadOnProgressFragment.populateMap().execute();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Inflate the layout for this fragment
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class populateMap extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress = new ProgressDialog(getActivity(), R.style.LoadingDialogStyle);
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
                        String idEvento = BL.getString("id_entrada_camion");
                        String placas = BL.getString("placas");
                        String producto = BL.getString("producto");
                        String cliente = BL.getString("nombre_cliente");
                        String operador = BL.getString("operador");
                        String inicio_calidad = BL.getString("inicio_calidad");
                        listEntries.put(idEvento,placas);
                        listEntries.put(idEvento,producto);
                        listEntries.put(idEvento,cliente);
                        listEntries.put(idEvento,operador);
                        listEntries.put(idEvento,inicio_calidad);
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
        if(mListView.getChildCount() > 0) {
            if (TextUtils.isEmpty(newText)) {
                ((CustomAdapter) mListView.getAdapter()).getFilter().filter(null);
            } else {
                ((CustomAdapter) mListView.getAdapter()).getFilter().filter(newText);
            }
        } else {
            ((CustomAdapter) mListView.getAdapter()).getFilter().filter(null);
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
                String idEvento = resultsMap.get("Third Line");
                String placa = resultsMap.get("First Line");
                String producto = resultsMap.get("Second Line");
                String cliente = resultsMap.get("Fourth Line");
                String operador = resultsMap.get("Fifth Line");
                String inicio_calidad = resultsMap.get("Sixth Line");
                Intent i = new Intent(getContext(), CalidadPostActivity.class);
                i.putExtra("idEvento",idEvento);
                i.putExtra("placa",placa);
                i.putExtra("producto",producto);
                i.putExtra("cliente",cliente);
                i.putExtra("operador",operador);
                i.putExtra("inicio_calidad",inicio_calidad);
                startActivityForResult(i,10001);
            }
        });
    }

    public void populateListView() {
        List<HashMap<String, String>> listItems = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(getActivity(), listItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});
        for (String key : listEntries.keySet()) {
            Collection<String> keys = listEntries.get(key);
            HashMap<String, String> resultsMap = new HashMap<>();
            List<String> userList = new ArrayList<>(keys);
            resultsMap.put("First Line", userList.get(0));
            resultsMap.put("Second Line", userList.get(1));
            resultsMap.put("Third Line", key);
            resultsMap.put("Fourth Line", userList.get(2));
            resultsMap.put("Fifth Line", userList.get(3));
            resultsMap.put("Sixth Line", userList.get(4));
            listItems.add(resultsMap);
            mListView.setTextFilterEnabled(true);
            mListView.setAdapter(adapter);
            setListItemBehavior(listItems);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001) && (resultCode == Activity.RESULT_OK)) {
            new CalidadOnProgressFragment.populateMap().execute();
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();

        }
    }
}
