package com.example.lantran;

import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.BN;
import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.EN;
import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.HI;
import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.MR;
import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.TE;
import static com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage.UR;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
     private Spinner fromspinner,tospinner;
     private TextInputEditText editsource;
     private ImageView mic;
     private MaterialButton button;
     private TextView text1;
     String[] fromlanguages={"From","English","Hindi","Marathi","Urdu","Bengali","Telugu"};
     String[] tolanguages={"To","English","Hindi","Marathi","Urdu","Bengali","Telugu"};
     private static final int REQUEST_PERMISSION_CODE = 1;
     int languageCode,fromlanguageCode,tolanguageCode = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromspinner= findViewById(R.id.idfromspinner);
        tospinner=findViewById(R.id.idtospinner);
        editsource=findViewById(R.id.ideditsource);
        mic=findViewById(R.id.idmic);
        button=findViewById(R.id.button);
        text1=findViewById(R.id.idtext1);
        fromspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromlanguageCode=getLanguageCode(fromlanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromlanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromspinner.setAdapter(fromAdapter);
        tospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tolanguageCode=getLanguageCode(tolanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,tolanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tospinner.setAdapter(toAdapter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text1.setText("");
                if(editsource.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "please enter text to translate", Toast.LENGTH_SHORT).show();
                }
                else if(fromlanguageCode==0){
                    Toast.makeText(MainActivity.this, "please select source language", Toast.LENGTH_SHORT).show();
                }
                else if(tolanguageCode==0){
                    Toast.makeText(MainActivity.this, "please select to translated language", Toast.LENGTH_SHORT).show();
                }
                else{
                      translateText(fromlanguageCode,tolanguageCode,editsource.getText().toString());
                }
            }
        });
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> result= data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                editsource.setText(result.get(0));
            }
        }
    }


    private void translateText(int fromlanguageCode, int tolanguageCode, String source){
              text1.setText("Downloading Model");
              FirebaseTranslatorOptions options=  new FirebaseTranslatorOptions.Builder()
                      .setSourceLanguage(fromlanguageCode)
                      .setTargetLanguage(tolanguageCode)
                      .build();
              FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
              FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
              translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void unused) {
                      text1.setText("Translating");
                      translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                          @Override
                          public void onSuccess(String s) {
                              text1.setText(s);
                          }
                      }).addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                              Toast.makeText(MainActivity.this,"Fail to translate..."+e.getMessage(),Toast.LENGTH_SHORT).show();
                          }
                      });
                  }
              }).addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                      Toast.makeText(MainActivity.this,"Fail download model"+e.getMessage(),Toast.LENGTH_SHORT).show();
                  }
              });

    }

     //{"From","English","Hindi","Marathi","Urdu","Bengali","Telugu"};
    public int getLanguageCode(String language) {
        int languageCode=0;
        switch(language){
            case "English":
                languageCode= EN;
                break;
            case "Hindi":
                languageCode= HI;
                break;
            case "Marathi":
                languageCode= MR;
                break;
            case "Urdu":
                languageCode= UR;
                break;
            case "Bengali":
                languageCode= BN;
                break;
            case "Telugu":
                languageCode= TE;
                break;
            default:
                languageCode=0;
        }
        return languageCode;
    }
}