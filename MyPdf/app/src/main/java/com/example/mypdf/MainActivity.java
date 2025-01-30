package com.example.mypdf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnPdfSelectListener {
    private MainAdapter adapter;
    private List<File> pdfList;
    private RecyclerView recyclerView;
    private ActivityResultLauncher<Intent> storageActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        storageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (uri != null) {
                                Intent intent = new Intent(this, PdfActivity.class);
                                intent.setData(uri); // Pass the URI
                                startActivity(intent);
                            }
                        }
                    }
                });


        runtimePermission();
    }

    private void runtimePermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            Dexter.withContext(MainActivity.this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            displayPdf();

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();

                        }
                    }).check();
        } else {
            displayPdf();
        }
    }


    public ArrayList<File> findPdf(File file) {
        ArrayList<File> arrayList = new ArrayList<>();

        if (file == null || !file.exists() || !file.isDirectory()) {
            Log.e("findPdf", "Invalid directory: " + (file != null ? file.getAbsolutePath() : "null"));
            return arrayList; // Return empty list to avoid NullPointerException
        }

        File[] files = file.listFiles();

        if (files == null) {
            Log.e("findPdf", "No files found in: " + file.getAbsolutePath());
            return arrayList; // Return empty list instead of crashing
        }

        for (File singleFile : files) {
            if (singleFile.isDirectory() && !singleFile.isHidden()) {
                arrayList.addAll(findPdf(singleFile)); // Recursively search subdirectories
            } else {
                if (singleFile.getName().toLowerCase().endsWith(".pdf")) { // Handle case-sensitive extensions
                    arrayList.add(singleFile);
                }
            }
        }
        return arrayList;
    }


    public void displayPdf() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/pdf");
            storageActivityResultLauncher.launch(intent);
        } else {
            recyclerView = findViewById(R.id.rv);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
            pdfList = new ArrayList<>();
            pdfList.addAll(findPdf(Environment.getExternalStorageDirectory()));
            adapter = new MainAdapter(this, pdfList, this);
            recyclerView.setAdapter(adapter);
        }
    }


    @Override
    public void onPdfSelected(File file) {
        startActivity(new Intent(MainActivity.this, PdfActivity.class)
                .putExtra("path", file.getAbsolutePath()));

    }
}