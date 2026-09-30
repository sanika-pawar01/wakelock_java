package com.example.wakelockjava.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wakelockjava.R;
import com.example.wakelockjava.camera.PushUpAnalyzer;
import com.example.wakelockjava.camera.SkeletonOverlayView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PushUpChallengeActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION = 201;

    private PreviewView previewView;
    private SkeletonOverlayView skeletonOverlay;
    private TextView tvCounter;
    private TextView tvStatus;

    private int target;
    private PushUpAnalyzer analyzer;

    private PoseDetector poseDetector;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ThemeHelper.applyTheme(this);

        setContentView(R.layout.activity_pushup_challenge);

        previewView = findViewById(R.id.previewView);
        skeletonOverlay = findViewById(R.id.skeletonOverlay);
        tvCounter = findViewById(R.id.tvCounter);
        tvStatus = findViewById(R.id.tvStatus);

        target = getIntent().getIntExtra("target", 10);
        String difficulty = getIntent().getStringExtra("difficulty");

        tvCounter.setText("0 / " + target);

        analyzer = new PushUpAnalyzer(difficulty);

        cameraExecutor = Executors.newSingleThreadExecutor();

        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();

        poseDetector = PoseDetection.getClient(options);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    @androidx.annotation.Nullable
                    android.media.Image mediaImage = imageProxy.getImage();
                    if (mediaImage != null) {
                        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                        int iw = imageProxy.getWidth();
                        int ih = imageProxy.getHeight();
                        boolean isRotated = imageProxy.getImageInfo().getRotationDegrees() % 180 != 0;
                        int displayWidth = isRotated ? ih : iw;
                        int displayHeight = isRotated ? iw : ih;

                        poseDetector.process(image)
                                .addOnSuccessListener(pose -> {
                                    skeletonOverlay.setPose(pose, displayWidth, displayHeight);
                                    analyzer.processPose(pose);
                                    updateUI();
                                })
                                .addOnCompleteListener(task -> imageProxy.close());
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector selector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, selector, preview, analysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void updateUI() {
        runOnUiThread(() -> {
            int current = analyzer.getRepCount();
            tvCounter.setText(String.valueOf(current));
            ((TextView)findViewById(R.id.tvTarget)).setText("of " + target);
            tvStatus.setText(analyzer.getFeedback());

            if (analyzer.getFeedback().contains("Perfect") || analyzer.getFeedback().contains("Good push-up")) {
                skeletonOverlay.triggerHighlight();
                View counterCard = findViewById(R.id.counterCard);
                counterCard.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction(() -> 
                    counterCard.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                ).start();
            }

            if (current >= target) {
                completeChallenge();
            }
        });
    }

    private void completeChallenge() {
        if (poseDetector != null) poseDetector.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();

        Intent intent = new Intent(this, CompletionActivity.class);
        intent.putExtra("alarm_id", getIntent().getLongExtra("alarm_id", -1));
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (false) super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (poseDetector != null) poseDetector.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }
}