package dbxprts.camionajetrak;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    View view;
    LoginActivity loginActivity = new LoginActivity();
    TextView bienvenido;
    TextView navUsername;
    TextView navSubtitle;
    LocalDatabaseConnection localDB;
    Menu drawerMenu;
    NavigationView navigationView;
    int calidadOnProgress;
    int maniobrasOnProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Inicio");

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                updateMenuItems(navigationView);
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        view = this.getWindow().getDecorView();
        String nombre = getNombre().substring(0, 1).toUpperCase() + getNombre().substring(1).toLowerCase();
        String apellido = getApellido().substring(0, 1).toUpperCase() + getApellido().substring(1).toLowerCase();
        bienvenido = (TextView) findViewById(R.id.Bienvenido);
        bienvenido.setText("Bienvenido " + nombre);
        View headerView = navigationView.getHeaderView(0);
        navUsername = (TextView) headerView.findViewById(R.id.navUsername);
        navUsername.setText(nombre + " " + apellido);
        navSubtitle = (TextView) headerView.findViewById(R.id.navSubtitle);
        navSubtitle.setText(getActiveUser().toUpperCase());
        localDB = new LocalDatabaseConnection(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public String getActiveUser() {
        return ((GlobalVariables) this.getApplication()).getActiveUser();
    }

    public String getNombre() {
        return ((GlobalVariables) this.getApplication()).getNombre();
    }

    public String getApellido() {
        return ((GlobalVariables) this.getApplication()).getApellido();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_plus).setVisible(false);
        drawerMenu = menu;
        hideDrawerCategories(getIntent().getExtras().getString("id_area"), getIntent().getExtras().getString("permisos_mic"));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_plus) {
            Intent i = new Intent(getApplicationContext(), VigilanciaNuevoActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;


        if (id == R.id.vigilancia) {
            fragment = new VigilanciaFragment();

        } else if (id == R.id.maniobras) {
            fragment = new ManiobrasFragment();

        } else if (id == R.id.calidad) {
            fragment = new CalidadFragment();

        } else if (id == R.id.limpieza) {
            fragment = new LimpiezaFragment();

        } else if (id == R.id.calidadOnProcess) {
            fragment = new CalidadOnProgressFragment();
        } else if (id == R.id.maniobrasOnProcess) {
            fragment = new ManiobrasOnProgressFragment();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_main, fragment)
                .commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateMenuItems(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        if (menu != null) {
            MenuItem calidadItem = menu.findItem(R.id.calidadOnProcess);
            MenuItem maniobrasItem = menu.findItem(R.id.maniobrasOnProcess);
            MenuItem[] myTaskParams = {calidadItem, maniobrasItem};
            new getOnProgressItems(calidadItem, maniobrasItem).execute(myTaskParams);

            int calidadOnProcess = localDB.getCalidadCount();
            //calidadItem.setTitle("Calidad (" + this.calidadOnProgress + ")");
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private class getOnProgressItems extends AsyncTask<MenuItem, Void, Void> {
        private String TAG = "Main";
        private String calidadURL = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/calidad/onprogress";
        private String maniobrasURL = "http://dbxhosted.dbxprts.com:8080/apex/mi_rutina/prefix/camionaje/maniobras/onprogress";
        MenuItem calidadItem;
        MenuItem maniobrasItem;

        public getOnProgressItems(MenuItem calidadItem, MenuItem maniobrasItem) {
            calidadItem.setTitle("Calidad (...)");
            maniobrasItem.setTitle("Maniobras (...)");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(MenuItem... menuItem) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String calidadJsonStr = sh.makeServiceCall(calidadURL);
            String maniobrasJsonStr = sh.makeServiceCall(maniobrasURL);

            Log.e(TAG, "Response from calidad url: " + calidadJsonStr);
            Log.e(TAG, "Response from maniobras url: " + maniobrasJsonStr);

            if (calidadJsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(calidadJsonStr);
                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    calidadOnProgress = items.length();
                    calidadItem = menuItem[0];
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }

            if (maniobrasJsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(maniobrasJsonStr);
                    // Getting JSON Array node
                    JSONArray items = jsonObj.getJSONArray("items");
                    maniobrasOnProgress = items.length();
                    maniobrasItem = menuItem[1];
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            calidadItem.setTitle("Calidad (" + calidadOnProgress + ")");
            maniobrasItem.setTitle("Maniobras (" + maniobrasOnProgress + ")");
        }
    }

    public void hideDrawerCategories(String id_area, String permisos_mic) {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_Menu = navigationView.getMenu();
        int[] item_ids = {R.id.limpieza, R.id.calidad, R.id.calidadOnProcess, R.id.maniobras, R.id.maniobrasOnProcess};
        if (!permisos_mic.trim().equals("A")) {
            for (int x = 0; x < item_ids.length; x++) {
                MenuItem menuItem = nav_Menu.findItem(item_ids[x]);
                int id = menuItem.getItemId();
                //1002 = Logística, 1007 = Operación, 1008 = Producción
                if (id_area.equals("1002") || id_area.equals("1007") || id_area.equals("1008")) {
                    if (id == R.id.limpieza || id == R.id.calidad || id == R.id.calidadOnProcess) {
                        menuItem.setVisible(false);
                    }
                } else { //1004 = Calidad
                    if (id == R.id.maniobras || id == R.id.maniobrasOnProcess) {
                        menuItem.setVisible(false);
                    }
                }
            }
        }
    }

}
