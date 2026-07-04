package com.example.facedetectionydetecciondemallafacial;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import java.io.InputStream;

public class FatigueLensActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private ImageView imagePreview;
    private TextView previewText;
    private Uri selectedImageUri;
    private Uri cameraImageUri;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fatigue_lens);

        imagePreview = findViewById(R.id.imagePreview);
        previewText = findViewById(R.id.previewText);

        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnGallery = findViewById(R.id.btnGallery);
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navAnalysis = findViewById(R.id.navAnalysis);

        configureLaunchers();

        btnCamera.setOnClickListener(v -> openCameraWithPermission());
        btnGallery.setOnClickListener(v -> openGallery());

        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Configuración pendiente", Toast.LENGTH_SHORT).show()
        );

        navHome.setOnClickListener(v ->
                Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
        );

        navAnalysis.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Primero captura o selecciona una imagen", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí después puedes abrir tu pantalla de análisis.
                // Ejemplo:
                // Intent intent = new Intent(this, AnalysisActivity.class);
                // intent.putExtra("image_uri", selectedImageUri.toString());
                // startActivity(intent);
                Toast.makeText(this, "Imagen lista para análisis", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configureLaunchers() {
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraImageUri != null) {
                        selectedImageUri = cameraImageUri;
                        showSelectedImage(selectedImageUri);
                    } else {
                        Toast.makeText(this, "No se capturó ninguna imagen", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        showSelectedImage(selectedImageUri);
                    } else {
                        Toast.makeText(this, "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FATIGUELENS_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private void showSelectedImage(Uri uri) {
        try {
            imagePreview.setImageTintList(null);
            imagePreview.clearColorFilter();

            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            if (bitmap != null) {
                imagePreview.setImageBitmap(bitmap);
                imagePreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                previewText.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "No se pudo mostrar la imagen", Toast.LENGTH_SHORT).show();
            }

            if (inputStream != null) {
                inputStream.close();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Debes permitir el uso de la cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }
}