package com.example.printfrompdf;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    Button btnSennToPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSennToPrinter = (Button) findViewById(R.id.btn_sendto);

        btnSennToPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String cURL = "https://printerplus.rcti.es/tests/1/test_pdf_01.pdf";
                    PDFTool.printPDFUrl(MainActivity.this, cURL);

                }catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    public void printPDF(Context ctx, Uri repFile) {

        Intent mIntent = new Intent(Intent.ACTION_SEND);
        mIntent.setClassName("es.rcti.printerplus","es.rcti.printerplus.SharedContent");
        mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mIntent.putExtra(Intent.EXTRA_STREAM, repFile);
        mIntent.setType("application/pdf");

        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        ctx.startActivity( mIntent );
    }

}