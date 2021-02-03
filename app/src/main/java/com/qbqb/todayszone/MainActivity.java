package com.qbqb.todayszone;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.log4j.chainsaw.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends AppCompatActivity {

    //-------------------------------------------------------------------------------------------------
    //                                           VARIABLES
    //-------------------------------------------------------------------------------------------------
    TextView txtTitleLocality;
    TextView txtLocality;
    TextView txtRegione;
    Button btnRefresh;
    Button btnChange;
    Button btnZone;
    Button btnColor;
    LottieAnimationView noInternetAnimation;
    Localita[] vetLocalita = new Localita[8000]; // vettore con le località

    static String comune;
    String colore;
    Boolean hasGPSfounded = true;

    //gps
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    Geocoder geocoder;
    List<Address> addresses;

    //data
    DatabaseReference databaseReference;
    ArrayList<Regione> lstRegioni = new ArrayList<>();

    //---------------------------------------------------------------------------------------------------------
    //                                              ON CREATE
    //---------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noInternetAnimation = (LottieAnimationView) findViewById(R.id.animationConnection);
        txtTitleLocality = (TextView) findViewById(R.id.txtTitleLocality);
        txtLocality = (TextView) findViewById(R.id.txtLocality);
        txtRegione = (TextView) findViewById(R.id.txtRegione);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateGPS();
                //if location not found
                Toast.makeText(getBaseContext(), "Refresh...", Toast.LENGTH_SHORT).show();
                if (controlConnection()) {// se va
                    noInternetAnimation.setVisibility(View.INVISIBLE);
                    if (lstRegioni.isEmpty()) {
                        databaseReference = FirebaseDatabase.getInstance().getReference();
                        updateLocationColor();
                    }
                    colore = getColorZone();
                    updateColor();
                }
                if (txtLocality.getText().equals("")) {
                    OpenDialogInsert();
                }
            }
        });
        btnChange = (Button) findViewById(R.id.btnChange);
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialogInsert = new AlertDialog.Builder(MainActivity.this);
                dialogInsert.setTitle("Enter your location");
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                dialogInsert.setView(input);
                dialogInsert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        comune = input.getText().toString();
                        txtLocality.setText(comune);
                        Toast.makeText(getBaseContext(), " " + comune + " is the new location", Toast.LENGTH_SHORT).show();
                        txtRegione.setText(regionePosizione(comune));
                        colore = getColorZone();
                        updateColor();
                    }
                });
                dialogInsert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                dialogInsert.show();
            }
        });
        btnZone = (Button) findViewById(R.id.btnAux);
        btnZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (txtLocality.getText().equals("") || txtLocality.getText().equals(":("))
                    Toast.makeText(getBaseContext(), "Location not found", Toast.LENGTH_SHORT).show();
                else {
                    if (controlConnection()) { // se va
                        noInternetAnimation.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(MainActivity.this, RulesActivity.class);
                        intent.putExtra("colore", colore);
                        startActivity(intent);
                        Animatoo.animateFade(MainActivity.this);
                    }
                }
            }
        });
        btnColor = (Button) findViewById(R.id.btnColor);

        //EXCEL
        AggiornaListaRegioni();

        //Data
        if (controlConnection()) { // se va
            databaseReference = FirebaseDatabase.getInstance().getReference();
            updateLocationColor();
            noInternetAnimation.setVisibility(View.INVISIBLE);
        }

        updateGPS();

        if (hasGPSfounded) {
            colore = getColorZone();
            updateColor();
        } else { // location not found
            OpenDialogInsert();
            btnColor.setBackgroundResource(R.drawable.secbtn);
        }

    }


    //----------------------------------------------------------------------------------------------------------------------------
    //                                                  OTHER FUNCTIONS
    //----------------------------------------------------------------------------------------------------------------------------
    // ottengo i colori dal database
    private void updateLocationColor() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Regione r1 = new Regione("Abruzzo", snapshot.child("Abruzzo").getValue().toString());
                Regione r2 = new Regione("Basilicata", snapshot.child("Basilicata").getValue().toString());
                Regione r3 = new Regione("Calabria", snapshot.child("Calabria").getValue().toString());
                Regione r4 = new Regione("Campania", snapshot.child("Campania").getValue().toString());
                Regione r5 = new Regione("Emilia-Romagna", snapshot.child("Emilia-Romagna").getValue().toString());
                Regione r6 = new Regione("Friuli-Venezia Giulia", snapshot.child("Friuli-Venezia Giulia").getValue().toString());
                Regione r7 = new Regione("Lazio", snapshot.child("Lazio").getValue().toString());
                Regione r8 = new Regione("Liguria", snapshot.child("Liguria").getValue().toString());
                Regione r9 = new Regione("Lombardia", snapshot.child("Lombardia").getValue().toString());
                Regione r10 = new Regione("Marche", snapshot.child("Marche").getValue().toString());
                Regione r11 = new Regione("Molise", snapshot.child("Molise").getValue().toString());
                Regione r12 = new Regione("Piemonte", snapshot.child("Piemonte").getValue().toString());
                Regione r13 = new Regione("Puglia", snapshot.child("Puglia").getValue().toString());
                Regione r14 = new Regione("Sardegna", snapshot.child("Sardegna").getValue().toString());
                Regione r15 = new Regione("Sicilia", snapshot.child("Sicilia").getValue().toString());
                Regione r16 = new Regione("Toscana", snapshot.child("Toscana").getValue().toString());
                Regione r17 = new Regione("Trentino-Alto Adige", snapshot.child("Trentino-Alto Adige").getValue().toString());
                Regione r18 = new Regione("Umbria", snapshot.child("Umbria").getValue().toString());
                Regione r19 = new Regione("Valle d'Aosta", snapshot.child("Valle d'Aosta").getValue().toString());
                Regione r20 = new Regione("Veneto", snapshot.child("Veneto").getValue().toString());
                lstRegioni.add(r1);
                lstRegioni.add(r2);
                lstRegioni.add(r3);
                lstRegioni.add(r4);
                lstRegioni.add(r5);
                lstRegioni.add(r6);
                lstRegioni.add(r7);
                lstRegioni.add(r8);
                lstRegioni.add(r9);
                lstRegioni.add(r10);
                lstRegioni.add(r11);
                lstRegioni.add(r12);
                lstRegioni.add(r13);
                lstRegioni.add(r14);
                lstRegioni.add(r15);
                lstRegioni.add(r16);
                lstRegioni.add(r17);
                lstRegioni.add(r18);
                lstRegioni.add(r19);
                lstRegioni.add(r20);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Creiamo la lista da file excel
    private void AggiornaListaRegioni() {
        try {
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("elencoComuni.xls");
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0);
            int index = 0;
            String comune = "", regione = "";

            for (int i = 0; i < sheet.getRows(); i++) {
                for (int j = 0; j < sheet.getColumns(); j++) {
                    Cell z = sheet.getCell(j, i);
                    if (j % 2 != 0)
                        regione = z.getContents();
                    else
                        comune = z.getContents();
                }
                vetLocalita[index] = new Localita(comune, regione);
                index++;
            }
        } catch (Exception e) {
        }
    }

    //Funzione che ritorna la regione dalla quale appartiene il comune
    private String regionePosizione(String comune) {
        String regione = "";
        for (int i = 0; i < 7905; i++) {
            if (vetLocalita[i].COMUNE.toLowerCase().equals(comune.toLowerCase())) {
                regione = vetLocalita[i].REGIONE;
                break;
            }
        }
        if (regione.equals("")) {
            Toast.makeText(this, "location not found", Toast.LENGTH_SHORT).show();
            return "Error, retry";
        }
        return regione;
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this, new String[]
                   {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    try {
                        Toast.makeText(getBaseContext(), "Obtained location ", Toast.LENGTH_SHORT).show();
                        hasGPSfounded = true;
                        updateUI(location);
                    } catch (IOException e) {
                       e.printStackTrace();
                   }
                }
                else {
                    hasGPSfounded = false;
                }
            }
        });
    }

    // aggiorno le localita attuali in base alla posizione data dal GPS
    private void updateUI(Location location) throws IOException {
        geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        txtLocality.setText(addresses.get(0).getLocality());
        txtRegione.setText(regionePosizione((String)txtLocality.getText()));
        colore = getColorZone();
        updateColor();
    }

    //searching manually the location
    private void OpenDialogInsert() {
        final AlertDialog.Builder dialogInsert = new AlertDialog.Builder(MainActivity.this);
        dialogInsert.setTitle("Location not found. Enter it manually");
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialogInsert.setView(input);
        dialogInsert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                comune = input.getText().toString();
                txtLocality.setText(comune);
                txtRegione.setText(regionePosizione(comune));
                colore = getColorZone();
                updateColor();
            }
        });
        dialogInsert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(txtLocality.getText().equals(""))
                    txtLocality.setText(":(");
                dialogInterface.cancel();
            }
        });
        dialogInsert.show();
    }

    // imposto il colore del btnColor in base al colore della regione
    private void updateColor() {
        switch(colore){
            case "giallo":btnColor.setBackgroundResource(R.drawable.yellow_button);break;
            case "arancione":btnColor.setBackgroundResource(R.drawable.orange_button);break;
            case "rosso":btnColor.setBackgroundResource(R.drawable.red_button);break;
            default:
        }
    }

    // ottengo il colore della regione in base alla regione attuale
    private String getColorZone() {
        String reg = txtRegione.getText().toString();
        for(Regione r : lstRegioni) {
            if(r.regione.contains(reg))
                return r.colore;
        }
        return "";
    }

    // controlliamo se ha la connessione attiva
    private Boolean controlConnection() {
        ConnectivityManager cm = (ConnectivityManager)MainActivity.this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting()){
            noInternetAnimation.setVisibility(View.INVISIBLE);
            return true;
        } else {
            Toast.makeText(getBaseContext(), "You don't have any active connection", Toast.LENGTH_SHORT).show();
            noInternetAnimation.setVisibility(View.VISIBLE);
            btnColor.setBackgroundResource(R.drawable.secbtn);
            return false;
        }
    }
}
