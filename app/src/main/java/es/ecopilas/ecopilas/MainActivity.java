package es.ecopilas.ecopilas;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MainActivity extends AppCompatActivity {

    //url a cargar
    String url = "https://ecopilas.es/";
    private WebView web;
    ProgressBar progressBar,progressBar2;
    Button boton1, boton2, boton3;
    ImageView image;
    TextView textoLoading;
    int truncate = 0;
    RelativeLayout layoutPortada;
    private String mGeolocationOrigin;
    private GeolocationPermissions.Callback mGeolocationCallback;
    private static final int RP_ACCESS_LOCATION = 1;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationManager locationManager;
    String provider;
    boolean webCargada = false;
    //Intent intent = getIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Definicion de variables
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        boton1 = (Button) findViewById(R.id.button1);
        boton2 = (Button) findViewById(R.id.button2);
        boton3 = (Button) findViewById(R.id.button3);
        textoLoading = (TextView) findViewById(R.id.textoLoading);
        web = (WebView)findViewById(R.id.ecopilasWeb);
        layoutPortada = (RelativeLayout) findViewById(R.id.rootRL);

        //Colores de los loadings
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#1a693b"), android.graphics.PorterDuff.Mode.MULTIPLY);
        progressBar2.getIndeterminateDrawable().setColorFilter(Color.parseColor("#1a693b"), android.graphics.PorterDuff.Mode.MULTIPLY);

        //Acciones al inicio
        ocultarAditivos();
        ocultarBarra2();

        //Necesitamos los permisos para acceder a la ubicación
        checkLocationPermission();

        //Cargamos la vista Web
        web.setWebChromeClient(new MyWebChromeClient());
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setGeolocationEnabled(true);
        web.setWebViewClient(new MyWebViewClient());
        if(getIntent() != null && getIntent().getExtras() != null) {
            //Recupera la información pasada en el Intent
            Bundle bundle = this.getIntent().getExtras();

            //Construimos el mensaje a mostrar
            // txtSaludo.setText("Hola " + bundle.getString("NOMBRE"));
            if (bundle != null) {
                if (bundle.containsKey("BOTON")) {
                    int botonClicado = bundle.getInt("BOTON");
                    if (botonClicado == 2) {
                        ocultarWeb();
                        mostrarPortada();
                    } else if (botonClicado == 3) {
                        ocultarPortada();
                        if (webCargada == false) {
                            web.setWebViewClient(new MyWebViewClient());
                            web.loadUrl("https://www.ecopilas.es/app/");
                            webCargada = true;
                        } else {
                            mostrarWeb();
                        }
                    }
                }
            }
        }
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            // Geolocation permissions coming from this app's Manifest will only be valid for devices with API_VERSION < 23.
            // On API 23 and above, we must check for permission, and possibly ask for it.
            final String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED) {
                // we're on SDK < 23 OR user has already granted permission
                callback.invoke(origin, true, false);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                    // user has denied this permission before and selected [/] DON'T ASK ME AGAIN
                    // TODO Best Practice: show an AlertDialog explaining why the user could allow this permission, then ask again
                } else {
                    // ask the user for permissions
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {permission}, RP_ACCESS_LOCATION);
                    mGeolocationOrigin = origin;
                    mGeolocationCallback = callback;
                }
            }
        }
    };

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_location_rationale)
                        .setMessage(R.string.permission_location_rationale)
                        .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    public void buttonClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
               /*ocultarWeb();
               ocultarPortada();*/
                Intent SaludoActivity = new Intent(getApplicationContext(), SaludoActivity.class);
                startActivity(SaludoActivity);
               break;
            case R.id.button2:
                ocultarWeb();
                mostrarPortada();
                break;
            case R.id.button3:
                ocultarPortada();
                if(webCargada == false){
                    web.setWebViewClient(new MyWebViewClient());
                    web.loadUrl("https://www.ecopilas.es/app/");
                    webCargada = true;
                }
                else{
                    mostrarWeb();
                }
                break;
        }
    }



    @Override
    public void onBackPressed() {
        if(web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    //Web
    public void ocultarWeb(){
        web.setVisibility(View.INVISIBLE);
    }

    public void mostrarWeb(){
        web.setVisibility(View.VISIBLE);
    }

    //Botones
    public void mostrarBotones(){
        boton1.setVisibility(View.VISIBLE);
        boton2.setVisibility(View.VISIBLE);
        boton3.setVisibility(View.VISIBLE);
    }

    public void ocultarBotones(){
        boton1.setVisibility(View.INVISIBLE);
        boton2.setVisibility(View.INVISIBLE);
        boton3.setVisibility(View.INVISIBLE);
    }


    //Solo Barra
    public void mostrarBarra(){
        progressBar.setVisibility(View.VISIBLE);
    }

    public void ocultarBarra(){
        progressBar.setVisibility(View.INVISIBLE);
    }

    //Solo Barra
    public void mostrarBarra2(){
        progressBar2.setVisibility(View.VISIBLE);
    }

    public void ocultarBarra2(){
        progressBar2.setVisibility(View.INVISIBLE);
    }
    //Aditivos a la barra
    public void ocultarAditivos(){
        //image.setVisibility(View.INVISIBLE);
        textoLoading.setVisibility(View.INVISIBLE);
    }
    public void mostrarAditivos(){
        //image.setVisibility(View.VISIBLE);
        textoLoading.setVisibility(View.VISIBLE);
    }

    //Aditivos a la barra
    public void mostrarPortada(){
        layoutPortada.setVisibility(View.VISIBLE);
    }
    public void ocultarPortada(){
        layoutPortada.setVisibility(View.INVISIBLE);
    }

    private class MyWebChromeClient extends WebChromeClient{
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }
    }

    private class MyWebViewClient extends WebViewClient{
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //ocultarBotones();
            //if(truncate != 0)
                //mostrarBarra2();
            //truncate++;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            ocultarBarra();
            //ocultarBarra2();
            //ocultarPortada();
            mostrarWeb();
            //mostrarBotones();
            //progressBar.setVisibility(View.GONE);
        }
    }
}
