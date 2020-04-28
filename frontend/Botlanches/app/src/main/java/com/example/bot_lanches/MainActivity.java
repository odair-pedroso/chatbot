package com.example.bot_lanches;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class MainActivity extends AppCompatActivity implements AIListener {

    private static final String CLIENT_ACCESS_TOKEN = "f3cf87eb18e34b31adb1dc10576fa257";
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO =10000 ;

    private EditText consultaEditText;
    private TextView resultadoTextView;

    private AIService aiService;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        consultaEditText = findViewById(R.id.consulta_edittext);
        resultadoTextView = findViewById(R.id.resultado_textview);

        final AIConfiguration config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.PortugueseBrazil,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(new Locale("pt-BR"));
                }
            }
        });

    }

    public void consultar(View v) throws AIServiceException {
        //Toast.makeText(MainActivity.this, "Botlanches", Toast.LENGTH_SHORT).show();
        //resultadoTextView.setText(consultaEditText.getText().toString());

        if(consultaEditText.getText().toString().trim().equals("")){
            Toast.makeText(this, "Digite algo", Toast.LENGTH_SHORT).show();
            return;
        }
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(consultaEditText.getText().toString());

        new AsyncTask<AIRequest, Void, AIResponse>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                resultadoTextView.setText("Aguarde alguns instantes");
            }

            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiService.textRequest(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse == null) {
                    resultadoTextView.setText("Ocorreu um erro");
                }else{
                    resultadoTextView.setText(aiResponse.getResult().getFulfillment().getSpeech());
                }
            }
        }.execute(aiRequest);


    }

    @Override
    public void onResult(AIResponse result) {
        if(result !=null && !result.isError()){
            String resposta = result.getResult().getFulfillment().getSpeech();
            resultadoTextView.setText(resposta);
            if(tts != null)
                tts.speak(resposta,TextToSpeech.QUEUE_FLUSH,null,null);
        }else{
            resultadoTextView.setText("Retorno inválido");
        }
    }

    @Override
    public void onError(AIError error) {
       resultadoTextView.setText("Ocorreu um erro " + error.getMessage());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        resultadoTextView.setText("Escutando....");

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    public void consultarVoz(View v){

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                AlertDialog.Builder janela = new AlertDialog.Builder(this);
                janela.setTitle("Permissão necessária");
                janela.setMessage("Precisamos do microfone para que você possa se comunicar usando a voz no aplicativo");
                janela.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                    }
                });
                janela.show();

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            aiService.startListening();
        }




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode ==MY_PERMISSIONS_REQUEST_RECORD_AUDIO){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                aiService.startListening();
            }else{
                Toast.makeText(this,"Usuario não concedeu acesso ao microfone",Toast.LENGTH_SHORT).show();
            }

        }
    }
}

