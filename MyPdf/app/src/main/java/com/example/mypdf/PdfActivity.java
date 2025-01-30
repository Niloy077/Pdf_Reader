package com.example.mypdf;
//doing this for no purpose
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class PdfActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        PDFView pdfView = findViewById(R.id.pdfView);

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Uri uri = getIntent().getData(); // Get URI from Intent (SAF)
                if (uri != null) {
                    pdfView.fromUri(uri).load();
                } else {
                    Toast.makeText(this, "Error: No URI provided.", Toast.LENGTH_SHORT).show();
                }
            } else {
                String filePath = getIntent().getStringExtra("path");
                if (filePath != null) {
                    File file = new File(filePath);
                    Uri uri = Uri.fromFile(file);
                    pdfView.fromUri(uri).load();
                } else {
                    Toast.makeText(this, "Error: No file path provided.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace(); // Print the error for debugging
        }
    }
}