package com.qbqb.todayszone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.log4j.chainsaw.Main;

public class RulesActivity extends Activity implements AdapterView.OnItemSelectedListener {

    Button btnBack;
    Spinner spnTopic;
    TextView txtContenuto;
    String coloreZona;
    DatabaseReference databaseReference;
    DataSnapshot dataSnapshot;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        Bundle extra = getIntent().getExtras();
        OpenDialogInfo();
        if(extra!=null){
            coloreZona = extra.getString("colore");
        }

        btnBack = (Button)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RulesActivity.this, MainActivity.class));
                Animatoo.animateFade(RulesActivity.this);
            }
        });

        txtContenuto = (TextView)findViewById(R.id.txtContenuto);
        txtContenuto.setMovementMethod(new ScrollingMovementMethod());

        spnTopic = (Spinner)findViewById(R.id.spnTopic);
        ArrayAdapter<CharSequence> adapterTopic = ArrayAdapter.createFromResource(this,R.array.topic,android.R.layout.simple_spinner_item);
        adapterTopic.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTopic.setAdapter(adapterTopic);
        spnTopic.setOnItemSelectedListener(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataSnapshot = snapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String auxIndex;

        switch(coloreZona){
            case "rosso":auxIndex = "ZROSSO_";break;
            case "arancione":auxIndex = "ZARANCIONE_";break;
            case "giallo":auxIndex = "ZGIALLO_";break;
            default:auxIndex = "";
        }

        switch(adapterView.getItemAtPosition(i).toString()){
            case "Attivita commerciali":txtContenuto.setText( dataSnapshot.child(auxIndex + "attivitacommerciali").getValue().toString());break;
            case "Attivita culturali":txtContenuto.setText( dataSnapshot.child(auxIndex + "attivitaculturali").getValue().toString());break;
            case "Attivita personali":txtContenuto.setText( dataSnapshot.child(auxIndex + "attivitapersonali").getValue().toString());break;
            case "Attivita sportiva":txtContenuto.setText( dataSnapshot.child(auxIndex + "attivitasportiva").getValue().toString());break;
            case "Lavoro":txtContenuto.setText( dataSnapshot.child(auxIndex + "lavoro").getValue().toString());break;
            case "Mascherina":txtContenuto.setText( dataSnapshot.child(auxIndex + "mascherina").getValue().toString());break;
            case "Spostamenti":txtContenuto.setText( dataSnapshot.child(auxIndex + "spostamenti").getValue().toString());break;
            case "Uffici Pubblici":txtContenuto.setText( dataSnapshot.child(auxIndex + "ufficipubblici").getValue().toString());break;
            case "Universita":txtContenuto.setText( dataSnapshot.child(auxIndex + "universita").getValue().toString());break;
            case "Violazioni e sanzioni":txtContenuto.setText( dataSnapshot.child(auxIndex + "violazionisanzioni").getValue().toString());break;
            default:txtContenuto.setText("");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void OpenDialogInfo(){
        AlertDialog alertDialog = new AlertDialog.Builder(RulesActivity.this).create();
        alertDialog.setTitle("Info");
        alertDialog.setMessage("Queste informazioni non tengono conto delle direttive regionali");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
