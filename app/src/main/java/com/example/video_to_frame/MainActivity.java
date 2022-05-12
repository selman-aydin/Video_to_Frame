package com.example.video_to_frame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.LongBuffer;
import java.text.DecimalFormat;
import java.util.Locale;

import android.speech.tts.TextToSpeech;


public class MainActivity extends AppCompatActivity {
    static final int REQUEST_VIDEO_CAPTURE = 1;
    static final int IMAGE_CAMERA_CODE = 2;
    static final int SELECT_IMAGE = 12;
    static int s2 = 1, s3 = 1, s4=1;
    private static int selectedLanguage = 1, selectedMode = 1;
    private static String targetLang = null;
    public VideoView videoView;
    Button camerabtn, galleryButton;
    TextView txtview, timingtxt;
    MaterialButtonToggleGroup toggleButon;
    ImageView imgView;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;
    float x1, x2, y1, y2;
    String s1 = null;
    private Module mModuleGenerator;
    private Tensor mInputTensor;
    private LongBuffer mInputTensorBuffer;
    private Uri videoData, imageUri;
    private Bitmap inputImage0, inputImage1, inputImage2, inputImage3, inputImage4, inputImage5, inputImage6, inputImage7, bitmapImage;
    private Bitmap inputImage0h, inputImage1h, inputImage2h, inputImage3h, inputImage4h, inputImage5h, inputImage6h, inputImage7h, bitmapImageh;

    private TextToSpeech tts;
    private TextToSpeech tts_custom;

    public static int getSelectedLanguage(int selectedLanguage) {
        Log.d("DEBUG", "LANGUAGE IS SELECTED");
        Log.d("DEBUG", String.valueOf(selectedLanguage));
        return selectedLanguage;
    }

    public static void setSelectedLanguage(int state) {
        selectedLanguage = state;
        s2 = state;

    }






    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = (VideoView) findViewById(R.id.video_view);
        camerabtn = findViewById(R.id.cameraButton);
        imgView = findViewById(R.id.image_view);
        txtview = findViewById(R.id.commandview);
        galleryButton = findViewById(R.id.galleryButton);
        timingtxt = findViewById(R.id.timing);
        toggleButon = findViewById(R.id.toggleGroup);
        videoView.setClipToOutline(true);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("WeCap");

        camerabtn.setOnClickListener(v -> cameraStart(videoView));
        galleryButton.setOnClickListener(v -> galleryStart(videoView));
        txtview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenCaption();
            }
        });

        videoView.setVisibility(View.INVISIBLE);
        imgView.setVisibility(View.INVISIBLE);

        Locale pLang = Locale.getDefault();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
        tts_custom = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts_custom.setLanguage(pLang);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        toggleButon.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if(group.getCheckedButtonId()==R.id.button_imageCaptioning)
                {
                    selectedMode = 2;

                }else if(group.getCheckedButtonId()==R.id.button_videoCaptioning) {
                    selectedMode = 1;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (s1 != null) {
            videoView.setVideoURI(Uri.parse(s1));
            videoView.start();
            videoView.setOnCompletionListener(mp -> videoView.start());
        }
        System.out.println(s2);
        System.out.println(s3);
        if (s2 != s3){
            s4 = s3;
            s3 = s2;
            Log.d("girdimi", "girdi");
            getSelectedLanguage(selectedLanguage);
            targetLang = getLangCode(selectedLanguage);
            translateText(targetLang);

        }
    }

    public boolean onTouchEvent(MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1 < x2) {
                    Intent i = new Intent(MainActivity.this, SwipeLeft.class);
                    galleryStart(videoView);

                } else if (x1 > x2) {
                    Intent i = new Intent(MainActivity.this, SwipeRight.class);

                    if (selectedMode == 1) {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
                        startActivityForResult(intent, 1);
                        onActivityResult(1, 1, intent);
                    }

                    if (selectedMode == 2) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 12);

                    }
                }
                break;

        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void cameraStart(View view) {
        /*
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //        Camera Intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtracamera.png(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        */


        if (selectedMode == 1) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
            if (selectedLanguage == 41) {
                tts_custom.speak("Kamera açıldı", TextToSpeech.QUEUE_FLUSH, null);
            } else {
                tts.speak("Camera is opened", TextToSpeech.QUEUE_FLUSH, null);
            }
            startActivityForResult(intent, 1);
        }

        if (selectedMode == 2) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (selectedLanguage == 41) {
                tts_custom.speak("Kamera açıldı", TextToSpeech.QUEUE_FLUSH, null);
            } else {
                tts.speak("Camera is opened", TextToSpeech.QUEUE_FLUSH, null);
            }
            startActivityForResult(intent, 2);

        }


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && requestCode == REQUEST_VIDEO_CAPTURE) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            videoData = data.getData();

            SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            s1 = sh.getString("video_uri", String.valueOf(videoData));

            videoView.setVideoURI(videoData);
            videoView.start();
            videoView.setOnCompletionListener(mp -> videoView.start());

            Context context = getApplicationContext();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, data.getData());


            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            float duration_millisec = Integer.parseInt(duration); //duration in millisec
            float duration_second = duration_millisec / 1000;  //millisec to sec
            float per_second = duration_second / 8;

            for (int i = 0; i < 8; i++) {

                int saniye = Math.round(per_second * i * 1000000);
                if (i == 0) {
                    System.out.println(Integer.toString(saniye));
                    inputImage0 = retriever.getFrameAtTime((saniye), MediaMetadataRetriever.OPTION_CLOSEST);
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
                if (i == 1) {
                    System.out.println(Integer.toString(saniye));
                    inputImage1 = retriever.getFrameAtTime((saniye), MediaMetadataRetriever.OPTION_CLOSEST);
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
                if (i == 2) {
                    System.out.println(Integer.toString(saniye));
                    inputImage2 = retriever.getFrameAtTime((saniye), MediaMetadataRetriever.OPTION_CLOSEST);
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
                if (i == 3) {
                    System.out.println(Integer.toString(saniye));
                    inputImage3 = retriever.getFrameAtTime((saniye), MediaMetadataRetriever.OPTION_CLOSEST);
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
                if (i == 4) {
                    System.out.println(Integer.toString(saniye));
                    inputImage4 = retriever.getFrameAtTime((saniye), MediaMetadataRetriever.OPTION_CLOSEST);
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
                if (i == 5) {
                    System.out.println(Integer.toString(saniye));
                    inputImage5 = retriever.getFrameAtTime((saniye), MediaMetadataRetriever.OPTION_CLOSEST);
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
                if (i == 6) {
                    System.out.println(Integer.toString(saniye));
                    inputImage6 = retriever.getFrameAtTime((saniye), MediaMetadataRetriever.OPTION_CLOSEST);
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
                if (i == 7) {
                    System.out.println(Float.toString(duration_millisec));
                    System.out.println(Integer.toString(saniye));
                    inputImage7 = retriever.getFrameAtTime((saniye), MediaMetadataRetriever.OPTION_CLOSEST);
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
            translateTextToLanguage();
            double startTime = System.nanoTime();
            String created_caption = null;
            try {
                created_caption = generateCaption();
            } catch (IOException e) {
                e.printStackTrace();
            }
            videoView.setVisibility(View.VISIBLE);
            imgView.setVisibility(View.INVISIBLE);
            timingtxt.setVisibility(View.VISIBLE);
            txtview.setVisibility(View.VISIBLE);
            txtview.setText(created_caption);
            System.out.println(created_caption);
            double endTime = System.nanoTime();
            timingtxt.setText((new DecimalFormat("##.##").format((endTime - startTime) / 1000000000)) + " second");

        }
        if (resultCode == 0 && requestCode == REQUEST_VIDEO_CAPTURE) {
            videoView.setVideoURI(videoData);
            videoView.start();
            videoView.setOnCompletionListener(mp -> videoView.start());
        }

        if (resultCode == RESULT_OK && requestCode == IMAGE_CAMERA_CODE) {

            bitmapImage = (Bitmap) data.getExtras().get("data");
            imgView.setImageBitmap(bitmapImage);
            timingtxt.setVisibility(View.VISIBLE);
            txtview.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);
            imgView.setVisibility(View.VISIBLE);
            String generatedCaption = null;
            try {
                double startTime = System.nanoTime();
                generatedCaption = runImageCaption(Preprocess(bitmapImage));
                double endTime = System.nanoTime();
                timingtxt.setText((new DecimalFormat("##.##").format((endTime - startTime) / 1000000000)) + " second");
            } catch (IOException e) {
                e.printStackTrace();
            }
            translateTextToLanguage();
            txtview.setText(generatedCaption);
        }

        if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try {
                bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgView.setImageBitmap(bitmapImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            videoView.setVisibility(View.INVISIBLE);
            imgView.setVisibility(View.VISIBLE);
            timingtxt.setVisibility(View.VISIBLE);
            txtview.setVisibility(View.VISIBLE);
            String generatedCaption = null;
            try {
                double startTime = System.nanoTime();
                generatedCaption = runImageCaption(Preprocess(bitmapImage));
                double endTime = System.nanoTime();
                timingtxt.setText((new DecimalFormat("##.##").format((endTime - startTime) / 1000000000)) + " second");
            } catch (IOException e) {
                e.printStackTrace();
            }
            translateTextToLanguage();
            txtview.setText(generatedCaption);
        }


    }

    private String generateCaption() throws IOException {
//        mModuleEncoder = PyTorchAndroid.loadModuleFromAsset(getAssets(), "encoder.pth");
//        mModuleDecoder = PyTorchAndroid.loadModuleFromAsset(getAssets(), "decoder.pth");
        mModuleGenerator = LiteModuleLoader.load(assetFilePath(MainActivity.this, "model.ptl"));

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
        final Tensor featureTensor = mModuleGenerator.forward(
                IValue.from(inputTensor0), IValue.from(inputTensor1), IValue.from(inputTensor2), IValue.from(inputTensor3),
                IValue.from(inputTensor4), IValue.from(inputTensor5), IValue.from(inputTensor6), IValue.from(inputTensor7))
                .toTensor();

        float[] result = featureTensor.getDataAsFloatArray();

        StringBuilder caption = new StringBuilder();
        try {
            for (float i : result) {
                if (i == 2.0) {
                    break;
                }
                caption.append(" ").append(idx2wrd.getString("" + Math.round(i)));
            }
        } catch (JSONException e) {
            android.util.Log.e("TAG", "JSONException ", e);
        }
        if (selectedLanguage == 41) {
            tts_custom.speak("Altyazı üretildi", TextToSpeech.QUEUE_FLUSH, null);
        } else {
            tts.speak("Caption is generated", TextToSpeech.QUEUE_FLUSH, null);
        }
        return caption.toString();




    }

    public void galleryStart(View view) {


        if (selectedMode == 1) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            if (selectedLanguage == 41) {
                tts_custom.speak("Galeri açıldı", TextToSpeech.QUEUE_FLUSH, null);
            } else {
                tts.speak("Gallery is opened", TextToSpeech.QUEUE_FLUSH, null);
            }
            startActivityForResult(intent, 1);

        }
        if (selectedMode == 2) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            if (selectedLanguage == 41) {
                tts_custom.speak("Galeri açıldı", TextToSpeech.QUEUE_FLUSH, null);
            } else {
                tts.speak("Gallery is opened", TextToSpeech.QUEUE_FLUSH, null);
            }
            startActivityForResult(intent, SELECT_IMAGE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_preference:
                goToPreference();
                return true;
//            case R.id.logOut:
//                startActivity(new Intent(MainActivity.this, MainActivity.class));
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToPreference() {
        Intent intent = new Intent(MainActivity.this, com.example.video_to_frame.Preference.class);
        startActivity(intent);
    }

    private String getLangCode(int languageIndex) {
        String langCode = null;
        if (languageIndex == 1) {
            langCode = "en";
        }
        if (languageIndex == 2) {
            langCode = "af";
        }
        if (languageIndex == 3) {
            langCode = "ar";
        }
        if (languageIndex == 4) {
            langCode = "az";
        }
        if (languageIndex == 5) {
            langCode = "bg";
        }
        if (languageIndex == 6) {
            langCode = "ca";
        }
        if (languageIndex == 7) {
            langCode = "zh";
        }
        if (languageIndex == 8) {
            langCode = "hr";
        }
        if (languageIndex == 9) {
            langCode = "cs";
        }
        if (languageIndex == 10) {
            langCode = "da";
        }
        if (languageIndex == 11) {
            langCode = "nl";
        }
        if (languageIndex == 12) {
            langCode = "et";
        }
        if (languageIndex == 13) {
            langCode = "fi";
        }
        if (languageIndex == 14) {
            langCode = "fr";
        }
        if (languageIndex == 15) {
            langCode = "gl";
        }
        if (languageIndex == 16) {
            langCode = "ka";
        }
        if (languageIndex == 17) {
            langCode = "de";
        }
        if (languageIndex == 18) {
            langCode = "el";
        }
        if (languageIndex == 19) {
            langCode = "hi";
        }
        if (languageIndex == 20) {
            langCode = "hu";
        }
        if (languageIndex == 21) {
            langCode = "is";
        }
        if (languageIndex == 22) {
            langCode = "id";
        }
        if (languageIndex == 23) {
            langCode = "it";
        }
        if (languageIndex == 24) {
            langCode = "ja";
        }
        if (languageIndex == 25) {
            langCode = "ko";
        }
        if (languageIndex == 26) {
            langCode = "lv";
        }
        if (languageIndex == 27) {
            langCode = "lt";
        }
        if (languageIndex == 28) {
            langCode = "ms";
        }
        if (languageIndex == 29) {
            langCode = "no";
        }
        if (languageIndex == 30) {
            langCode = "fa";
        }
        if (languageIndex == 31) {
            langCode = "pl";
        }
        if (languageIndex == 32) {
            langCode = "pt";
        }
        if (languageIndex == 33) {
            langCode = "ro";
        }
        if (languageIndex == 34) {
            langCode = "ru";
        }
        if (languageIndex == 35) {
            langCode = "sr";
        }
        if (languageIndex == 36) {
            langCode = "sk";
        }
        if (languageIndex == 37) {
            langCode = "sl";
        }
        if (languageIndex == 38) {
            langCode = "es";
        }
        if (languageIndex == 39) {
            langCode = "sv";
        }
        if (languageIndex == 40) {
            langCode = "th";
        }
        if (languageIndex == 41) {
            langCode = "tr";
        }
        if (languageIndex == 42) {
            langCode = "uk";
        }
        if (languageIndex == 43) {
            langCode = "vi";
        }
        if (languageIndex == 44) {
            langCode = "cy";
        }
        return langCode;


    }

    private void translateText(String langCode) {
        TranslatorOptions options = null;
        if (s4 == 1){
        options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
//                //to language
                .setTargetLanguage(langCode)
                .build();}
        else{

            targetLang = getLangCode(s4);
            options = new TranslatorOptions.Builder()
                    .setSourceLanguage(targetLang)
//                //to language
                    .setTargetLanguage(langCode)
                    .build();
        }

        final Translator translator = Translation.getClient(options);

        Log.d("DEBUG", txtview.getText().toString());
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                Log.d("translator", "downloaded lang  model");
                                translator.translate(txtview.getText().toString())
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String translatedText) {
                                                txtview.setText(translatedText);
                                                Log.d("translator", translatedText);

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("translator", "ERROR");

                                            }
                                        });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("translator", "FAILURE");
                            }
                        });
    }

    public void translateTextToLanguage() {
        //First identify the language of the entered text
        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage(txtview.getText().toString())
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode != "und") {
                                    Log.d("translator", "lang " + languageCode);
                                    //download translator for the identified language
                                    // and translate the entered text into english
                                    s4 =1;
                                    getSelectedLanguage(selectedLanguage);
                                    targetLang = getLangCode(selectedLanguage);
                                    translateText(targetLang);


                                } else {
                                    Toast.makeText(MainActivity.this,
                                            "Could not identify language of the text entered",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,
                                        "Problem in identifying language of the text entered",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
    }

    Bitmap Preprocess(Bitmap bitmap) {
        bitmapImageh = Bitmap.createScaledBitmap(bitmap, 299, 299, true);

        return bitmapImageh;
    }

    private String runImageCaption(Bitmap inputImage0h) throws IOException {
//        mModuleEncoder = PyTorchAndroid.loadModuleFromAsset(getAssets(), "encoder.pth");
//        mModuleDecoder = PyTorchAndroid.loadModuleFromAsset(getAssets(), "decoder.pth");
        mModuleGenerator = LiteModuleLoader.load(assetFilePath(MainActivity.this, "infer.ptl"));

        String json;
        JSONObject wrd2idx;
        JSONObject idx2wrd;
        try {
            InputStream is = getAssets().open("index2word_image.json");
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


        // running the model
//        final Tensor featureTensor = mModuleGenerator.forward(IValue.from(inputTensor)).toTensor();
        final Tensor featureTensor = mModuleGenerator.forward(IValue.from(inputTensor0)).toTensor();

        float[] result = featureTensor.getDataAsFloatArray();

        StringBuilder caption = new StringBuilder();
        try {
            for (float i : result) {
                if (i == 2.0) {
                    break;
                }
                caption.append(" ").append(idx2wrd.getString("" + Math.round(i)));
            }
        } catch (JSONException e) {
            android.util.Log.e("TAG", "JSONException ", e);
        }

        if (selectedLanguage == 41) {
            tts_custom.speak("Altyazı üretildi", TextToSpeech.QUEUE_FLUSH, null);
        } else {
            tts.speak("Caption is generated", TextToSpeech.QUEUE_FLUSH, null);
        }
        return caption.toString();


    }

    private void listenCaption(){
        if (selectedLanguage == 41) {
            tts_custom.speak(txtview.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        } else {
            tts.speak(txtview.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        }
    }

}

