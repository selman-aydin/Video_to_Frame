package com.example.video_to_frame;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.LongBuffer;

import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.PyTorchAndroid;
import org.pytorch.Tensor;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    Button camerabtn,galleryButton;
    private VideoView videoView;
    TextView txtview;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;
    private Module mModuleGenerator;
    private Tensor mInputTensor;
    private LongBuffer mInputTensorBuffer;
    private Bitmap inputImage0,inputImage1,inputImage2,inputImage3,inputImage4,inputImage5,inputImage6,inputImage7;
    private Bitmap inputImage0h,inputImage1h,inputImage2h,inputImage3h,inputImage4h,inputImage5h,inputImage6h,inputImage7h;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camerabtn = findViewById(R.id.cameraButton);
        txtview   = findViewById(R.id.commandview);
        galleryButton   = findViewById(R.id.galleryButton);



        camerabtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                cameraStart(videoView);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryStart(videoView);
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
            System.out.println("lololo");
            System.out.println(data.getData().getPath());
            System.out.println(data.getData());
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = this.getContentResolver().query(data.getData(), proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String real_path = cursor.getString(column_index);
            System.out.println(real_path);
            videoView.setVideoURI(data.getData());
            String fileName = "video6.mp4";

            videoView.start();

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            //retriever.setDataSource("/storage/emulated/0/Download/video3.mp4");

            try {
                System.out.println(String.valueOf(getAssets().openFd(fileName).createInputStream()));
                retriever.setDataSource(getAssets().openFd(fileName).getFileDescriptor(),getAssets().openFd(fileName).getStartOffset(),getAssets().openFd(fileName).getDeclaredLength());
                System.out.println("ahha");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //  /storage/emulated/0/Download/1.mp4


            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            float duration_millisec = Integer.parseInt(duration); //duration in millisec
            float duration_second = duration_millisec / 1000;  //millisec to sec
            float per_second = duration_second/8;

            for (int i=0; i<8;i++){

                int saniye = Math.round(per_second*i*1000000);
                if (i==0) {
                    System.out.println(Integer.toString(saniye));
                     inputImage0 = retriever.getFrameAtTime((saniye),MediaMetadataRetriever.OPTION_CLOSEST);
                     inputImage0h = Bitmap.createScaledBitmap(inputImage0, 299, 299, true);
                    String filename0 = Integer.toString(i) + ".png";
                    File sd0 = Environment.getExternalStorageDirectory();
                    File dest0 = new File(sd0, filename0);
                    try {
                        FileOutputStream out0 = new FileOutputStream(dest0);
                        inputImage0.compress(Bitmap.CompressFormat.PNG, 90, out0);
                        out0.flush();
                        out0.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i==1) {
                    System.out.println(Integer.toString(saniye));
                     inputImage1 = retriever.getFrameAtTime((saniye),MediaMetadataRetriever.OPTION_CLOSEST);
                     inputImage1h = Bitmap.createScaledBitmap(inputImage1, 299, 299, true);
                    String filename1 = Integer.toString(i) + ".png";
                    File sd1 = Environment.getExternalStorageDirectory();
                    File dest1 = new File(sd1, filename1);
                    try {
                        FileOutputStream out1 = new FileOutputStream(dest1);
                        inputImage1.compress(Bitmap.CompressFormat.PNG, 90, out1);
                        out1.flush();
                        out1.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i==2) {
                    System.out.println(Integer.toString(saniye));
                     inputImage2 = retriever.getFrameAtTime((saniye),MediaMetadataRetriever.OPTION_CLOSEST);
                     inputImage2h = Bitmap.createScaledBitmap(inputImage2, 299, 299, true);
                    String filename2 = Integer.toString(i) + ".png";
                    File sd2 = Environment.getExternalStorageDirectory();
                    File dest2 = new File(sd2, filename2);
                    try {
                        FileOutputStream out2 = new FileOutputStream(dest2);
                        inputImage2.compress(Bitmap.CompressFormat.PNG, 90, out2);
                        out2.flush();
                        out2.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i==3) {
                    System.out.println(Integer.toString(saniye));
                     inputImage3 = retriever.getFrameAtTime((saniye),MediaMetadataRetriever.OPTION_CLOSEST);
                     inputImage3h = Bitmap.createScaledBitmap(inputImage3, 299, 299, true);
                    String filename3 = Integer.toString(i) + ".png";
                    File sd3 = Environment.getExternalStorageDirectory();
                    File dest3 = new File(sd3, filename3);
                    try {
                        FileOutputStream out3 = new FileOutputStream(dest3);
                        inputImage3.compress(Bitmap.CompressFormat.PNG, 90, out3);
                        out3.flush();
                        out3.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i==4) {
                    System.out.println(Integer.toString(saniye));
                     inputImage4 = retriever.getFrameAtTime((saniye),MediaMetadataRetriever.OPTION_CLOSEST);
                     inputImage4h = Bitmap.createScaledBitmap(inputImage4, 299, 299, true);
                    String filename4 = Integer.toString(i) + ".png";
                    File sd4 = Environment.getExternalStorageDirectory();
                    File dest4 = new File(sd4, filename4);
                    try {
                        FileOutputStream out4 = new FileOutputStream(dest4);
                        inputImage4.compress(Bitmap.CompressFormat.PNG, 90, out4);
                        out4.flush();
                        out4.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i==5) {
                    System.out.println(Integer.toString(saniye));
                     inputImage5 = retriever.getFrameAtTime((saniye),MediaMetadataRetriever.OPTION_CLOSEST);
                     inputImage5h = Bitmap.createScaledBitmap(inputImage5, 299, 299, true);
                    String filename5 = Integer.toString(i) + ".png";
                    File sd5 = Environment.getExternalStorageDirectory();
                    File dest5 = new File(sd5, filename5);
                    try {
                        FileOutputStream out5 = new FileOutputStream(dest5);
                        inputImage5.compress(Bitmap.CompressFormat.PNG, 90, out5);
                        out5.flush();
                        out5.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i==6) {
                    System.out.println(Integer.toString(saniye));
                     inputImage6 = retriever.getFrameAtTime((saniye),MediaMetadataRetriever.OPTION_CLOSEST);
                     inputImage6h = Bitmap.createScaledBitmap(inputImage6, 299, 299, true);
                    String filename6 = Integer.toString(i) + ".png";
                    File sd6 = Environment.getExternalStorageDirectory();
                    File dest6 = new File(sd6, filename6);
                    try {
                        FileOutputStream out6 = new FileOutputStream(dest6);
                        inputImage6.compress(Bitmap.CompressFormat.PNG, 90, out6);
                        out6.flush();
                        out6.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (i==7) {
                    System.out.println(Integer.toString(saniye));
                     inputImage7 = retriever.getFrameAtTime((saniye),MediaMetadataRetriever.OPTION_CLOSEST);
                     inputImage7h = Bitmap.createScaledBitmap(inputImage7, 299, 299, true);
                    String filename7 = Integer.toString(i) + ".png";
                    File sd7 = Environment.getExternalStorageDirectory();
                    File dest7 = new File(sd7, filename7);
                    try {
                        FileOutputStream out7 = new FileOutputStream(dest7);
                        inputImage7.compress(Bitmap.CompressFormat.PNG, 90, out7);
                        out7.flush();
                        out7.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }



            }

            String created_caption = generateCaption();
            txtview.setText(created_caption);
            System.out.println(created_caption);




        }



    }

    private String generateCaption() {
//        mModuleEncoder = PyTorchAndroid.loadModuleFromAsset(getAssets(), "encoder.pth");
//        mModuleDecoder = PyTorchAndroid.loadModuleFromAsset(getAssets(), "decoder.pth");
        mModuleGenerator = PyTorchAndroid.loadModuleFromAsset(getAssets(), "generator3.pth");

        String json;
        JSONObject wrd2idx;
        JSONObject idx2wrd;
        try {
            InputStream is = getAssets().open("index2word.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
            idx2wrd = new JSONObject(json);


        } catch (JSONException | IOException e) {
            android.util.Log.e("TAG", "JSONException | IOException ", e);
            return null;
        }

        // preparing input tensor
        final Tensor inputTensor0 = TensorImageUtils.bitmapToFloat32Tensor(inputImage0h,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor inputTensor1 = TensorImageUtils.bitmapToFloat32Tensor(inputImage1h,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor inputTensor2 = TensorImageUtils.bitmapToFloat32Tensor(inputImage2h,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor inputTensor3 = TensorImageUtils.bitmapToFloat32Tensor(inputImage3h,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor inputTensor4 = TensorImageUtils.bitmapToFloat32Tensor(inputImage4h,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor inputTensor5 = TensorImageUtils.bitmapToFloat32Tensor(inputImage5h,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor inputTensor6 = TensorImageUtils.bitmapToFloat32Tensor(inputImage6h,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final Tensor inputTensor7 = TensorImageUtils.bitmapToFloat32Tensor(inputImage7h,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

        // running the model
//        final Tensor featureTensor = mModuleGenerator.forward(IValue.from(inputTensor)).toTensor();
        final Tensor featureTensor = mModuleGenerator.forward(IValue.from(inputTensor0),IValue.from(inputTensor1),IValue.from(inputTensor2),IValue.from(inputTensor3),
                IValue.from(inputTensor4),IValue.from(inputTensor5),IValue.from(inputTensor6),IValue.from(inputTensor7)).toTensor();

        float[] result = featureTensor.getDataAsFloatArray();

        StringBuilder caption = new StringBuilder();
        try {
            for (float i : result) {
                if (i == 2.0) {
                    break;
                }
                caption.append(" ").append(idx2wrd.getString("" + Math.round(i)));
            }
        }
        catch (JSONException e) {
            android.util.Log.e("TAG", "JSONException ", e);
        }
        return caption.toString();


    }


    public void galleryStart(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);

    }



}

