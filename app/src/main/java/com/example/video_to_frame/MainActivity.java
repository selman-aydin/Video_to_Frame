package com.example.video_to_frame;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;



public class MainActivity extends AppCompatActivity {
    Button camerabtn;
    private VideoView videoView;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camerabtn = findViewById(R.id.cameraButton);



        camerabtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                cameraStart(videoView);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void cameraStart(View view) {
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, "New Picture");
//        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
//        mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        //        Camera Intent
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
//        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
//
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,5);
        startActivityForResult(intent,1);
        onActivityResult(1,1,intent);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            VideoView videoView = findViewById(R.id.video_view);
            videoView.setVideoURI(data.getData());
            videoView.start();

        }
    }
}

