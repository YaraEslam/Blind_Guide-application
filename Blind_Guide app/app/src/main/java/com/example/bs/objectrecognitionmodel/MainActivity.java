package com.example.bs.objectrecognitionmodel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
//import android.widget.ImageView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import static android.graphics.Camera.*;
import static com.example.bs.objectrecognitionmodel.Voice.SRec;
import static java.lang.StrictMath.abs;

public class MainActivity extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2
{

    // Initialize OpenCV manager.
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case BaseLoaderCallback.SUCCESS:
                    mOpenCvCameraView.enableView();
                    break;

                default:
                    super.onManagerConnected(status);
            }

        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug())
        {
            if (_Ok)
            {
                Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,
                        this, mLoaderCallback);
            }
        }
        else
        {
            if (_Ok)
            {
                Log.d(TAG, "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
        {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration)
    {
        super.onConfigurationChanged(configuration);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // button to speak
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Create a new intent to open the
                Intent Main_intent = new Intent(MainActivity.this, Voice.class);

                //start the new activity
                startActivity(Main_intent);

            }
        });


        toSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                {
                    current = getResources().getConfiguration().locale;
                    result = toSpeech.setLanguage(current);
                    //result=toSpeech.setLanguage(Locale.UK);

                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Feature not supported in your device", Toast.LENGTH_SHORT).show();
                }
            }
        });


        String permissions[] = new String[]
                {
                        Manifest.permission.CAMERA
                };


        _Ok = PermissionUtils.validate(this, 0, permissions);
        if (_Ok)
        {
            classNames = new String[]
                    {
                            "background", "person", "bicycle", "car", "motorcycle", "airplane",
                            "bus", "train", "truck", "boat", "traffic light", "fire hydrant", "N/A",
                            "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse",
                            "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "N/A",
                            "backpack", "umbrella", "N/A", "N/A", "handbag", "tie", "suitcase",
                            "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat",
                            "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle",
                            "N/A", "N//A", "cup", "fork", "knife", "spoon", "bowl", "banana",
                            "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza",
                            "donut", "cake", "chair", "couch", "potted plant", "bed", "N/A",
                            "dining table", "N/A", "N/A", "toilet", "N/A", "tv", "laptop", "mouse",
                            "remote", "keyboard", "cell phone", "microwave", "oven", "toaster",
                            "sink", "refrigerator", "N/A", "book", "clock", "vase", "scissors",
                            "teddy bear", "hair drier", "toothbrush"

                    };

            _Speeak = false;
            ALERTA01 = (String) getText(R.string.MSGALERT01);
            MSGRECOG01 = (String) getText(R.string.MSGRECOG01);

            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.CameraView);
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.disableFpsMeter();
            mOpenCvCameraView.setCvCameraViewListener(this);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            /* mOpenCvCameraView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     Log.v(TAG, "Attempting to autofocus camera.");
                     Toast.makeText(MainActivity.this, "pronunciar", Toast.LENGTH_LONG).show();
                     if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                         Toast.makeText(getApplicationContext(), "Feature not supported in your device", Toast.LENGTH_SHORT).show();
                     } else {
                         //text = editText.getText().toString();
                         //toSpeech.speak("feature in development",TextToSpeech.QUEUE_FLUSH,null);
                         //toSpeech.speak(ALERTA01,TextToSpeech.QUEUE_FLUSH,null);
                         //_Speeak = true;
                     }
                 }
             });
             */

        }
        else
        {
            Toast.makeText(MainActivity.this, "Permission denied",
                    Toast.LENGTH_LONG).show();

        }

        // use light sensor
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // if your phone does not have light sensor
        if (lightSensor == null)
        {
            Toast.makeText(MainActivity.this, "No Light Sensor! quit-",
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            max =  lightSensor.getMaximumRange();  // maximum range(value) of light sensor
            min =  0;                              // minimum value
            sensorManager.registerListener(lightSensorEventListener, lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        //light sensor

    }


    SensorEventListener lightSensorEventListener = new SensorEventListener()
    {
        // get the intensity of light for each frame(for all time)
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            if(event.sensor.getType()==Sensor.TYPE_LIGHT)
            {
                final float currentReading = event.values[0];
                if(currentReading <= min)
                    flash=1;
                else
                    flash=0;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults)
        {
            if (result == PackageManager.PERMISSION_DENIED)
            {
                Toast.makeText(MainActivity.this, "Permission denied",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(MainActivity.this, "Permission Granted",
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));
            }
        }
    }

    static int pixels_of_width = 634;

    public static void resolution(int x, int y)
    {
        pixels_of_width = (x * 634) / 1920;
    }

    public static int getScreenWidth()
    {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight()
    {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    //handler
    void thread (final String color)
    {
        // Set this up in the UI thread.

        Handler mHandler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message message)
            {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                // Toast.makeText(MainActivity.this, "color : "+color, Toast.LENGTH_LONG).show();
            }


        };
        // And this is how you call it from the worker thread:
        Message message = mHandler.obtainMessage();
        message.sendToTarget();


    }

    public void onCameraViewStarted(int width, int height)
    {

        frame = new Mat();
        mBlobColorHsv = new Scalar(255);
        mBlobColorRgba = new Scalar(255);
        // Load a network.
        String proto = getPath("deploy.prototxt", this);
        String weights = getPath("deploy.caffemodel", this);
        net = Dnn.readNetFromCaffe(proto, weights);
    }

    int f = 0;
    double[] d = new double[] {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0};

    public Mat onCameraFrame(CvCameraViewFrame inputFrame)
    {

        boolean _SpeeakNow = false;
        contador = new int[] {
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        if (_Speeak)
        {
            _SpeeakNow = true;
            _Speeak = false;
        }


        final int IN_WIDTH = 224;
        final int IN_HEIGHT = 224;
        final float WH_RATIO = (float) IN_WIDTH / IN_HEIGHT;
        final double IN_SCALE_FACTOR = 0.007843;
        final double MEAN_VAL = 127.5;
        final double THRESHOLD = 0.3;
        String phase;
        // Get a new frame
        frame = inputFrame.rgba();
        f++;

        //rotate the frame
        /*Core.transpose(frame,frame);
        Imgproc.resize(frame,frame,frame.size(),0,0,0);
        //Core.flip(frame,frame,1);

        float screenOrientation = mOpenCvCameraView.getRotation();

        switch ((int) screenOrientation)
        {
            case 180: case -180:
                Core.flip(frame,frame,0);
                break;
            case 90: case -270:
                Core.flip(frame,frame,1);
                break;
            case 270: case -90:
                Core.flip(frame,frame,0);
                break;
        }
        */
        if (f > 5)
        {
            f = 1;
        }

        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

        // Forward image through network.
        Mat blob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR,
                new Size(IN_WIDTH, IN_HEIGHT),
                new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), false);

        net.setInput(blob);
        Mat detections = net.forward();

        resolution(getScreenWidth(), getScreenHeight());

        int cols = frame.cols();
        int rows = frame.rows();
        int col = cols / 3;
        int row = rows / 3;
        Size cropSize;
        if ((float) cols / rows > WH_RATIO)
        {
            cropSize = new Size(rows * WH_RATIO, rows);
        }
        else
        {
            cropSize = new Size(cols, cols / WH_RATIO);
        }
        int y1 = (int) (rows - cropSize.height) / 2;
        int y2 = (int) (y1 + cropSize.height);
        int x1 = (int) (cols - cropSize.width) / 2;
        int x2 = (int) (x1 + cropSize.width);
        subFrame = frame.submat(y1, y2, x1, x2);
        cols = subFrame.cols();
        rows = subFrame.rows();
        col = cols / 3;
        row = rows / 3;

        int[] color = new int[3];

        //When color choosed
        if(Voice.color==1&& f==5)
        {
            color = detectColor();
            if(color != null)
            {
                String c = colorObj.getColorNameFromRgb(color[0], color[1], color[2]);
                String c1 = color[0]+" "+color[1]+" "+color[2]+" "+c;
                thread(c1);
                toSpeech.speak(c, TextToSpeech.QUEUE_FLUSH, null);
            }
        }

        detections = detections.reshape(1, (int) detections.total() / 7);


        // turn on flash in dark
        if(flash ==1)                       // dark
            JavaCameraView.turnOnFlash();
        else                                //light
            JavaCameraView.turnOffFlash();


        for (int i = 0; i < detections.rows(); ++i)
        {
            double confidence = detections.get(i, 2)[0];
            if (confidence > THRESHOLD)
            {
                int classId = (int) detections.get(i, 1)[0];

                int xLeftBottom = (int) (detections.get(i, 3)[0] * cols);
                int yLeftBottom = (int) (detections.get(i, 4)[0] * rows);
                int xRightTop = (int) (detections.get(i, 5)[0] * cols);
                int yRightTop = (int) (detections.get(i, 6)[0] * rows);
                x = abs(xLeftBottom + xRightTop) / 2;
                y = abs(yLeftBottom + yRightTop) / 2;
                widthObj = abs(xLeftBottom - xRightTop);
                heightObj = abs(yLeftBottom - yRightTop);

                int a = 0, b = 0;
                String out = "";

                if (width[classId] == -1)
                    width[classId] = (double) abs(xLeftBottom - xRightTop);
                else
                {
                    if (f == 3 && (1.0 - (width[classId] / (long) abs(xLeftBottom - xRightTop)))
                            != 0.0)
                    {
                        double s1 = abs(xLeftBottom - xRightTop) + 0.0;
                        double s = (double) ((double) width[classId] / (double) s1);
                        d[classId] = (double) (1.0 / abs(1.0 - s));
                        width[classId] = abs(xLeftBottom - xRightTop);
                    }

                }
                if (x <= col)
                {
                    a = 1;
                }
                else if (x <= col * 2)
                {
                    a = 2;
                }
                else
                {
                    a = 3;
                }
                if (y <= row)
                {
                    b = 1;
                }
                else if (y <= row * 2)
                {
                    b = 2;
                }
                else
                {
                    b = 3;
                }
                if (a == 2 && b == 2)
                {
                    out = "center";
                }
                else if (a == 2 && b == 1)
                {
                    out = "left";
                }
                else if (a == 2 && b == 3)
                {
                    out = "right";
                }
                else if (a == 1 && b == 1)
                {
                    out = "Front left";
                }
                else if (a == 1 && b == 2)
                {
                    out = "Front";
                }
                else if (a == 1 && b == 3)
                {
                    out = "Front right";
                }
                else if (a == 3 && b == 1)
                {
                    out = "left";
                }
                else if (a == 3 && b == 2)
                {
                    out = "Front";
                }
                else if (a == 3 && b == 3)
                {
                    out = "Right";
                }

                /*  if(Voice.color ==0 ){           //|| Voice.obstacle==1 || Voice.recognize==1) {
                      // Draw rectangle around detected object.
                      Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom),
                              new Point(xRightTop, yRightTop),
                              new Scalar(colorR[classId], colorG[classId], colorB[classId]), 6);
                  }
                  */

                // when distance choosed
                if(Voice.obstacle==1 && f==5)
                {
                    if (classId == 0 || classId == 1 || classId == 2 || classId == 3
                            || classId == 4 || classId == 5 || classId == 6 || classId == 7
                            || classId == 8 || classId == 9 || classId == 13 || classId == 14
                            || classId == 15 || classId == 17 || classId == 18 || classId == 19
                            || classId == 20 || classId == 21 || classId == 22 || classId == 23
                            || classId == 24 || classId == 25 || classId == 33 || classId == 34
                            || classId == 35 || classId == 62 || classId == 63 || classId == 64
                            || classId == 65 || classId == 67 || classId == 70 || classId == 72
                            || classId == 73 || classId == 78 || classId == 79 || classId == 82
                            || classId == 86)
                    {
                        if ((abs(xLeftBottom - xRightTop) >= pixels_of_width) &&
                                (a == 3 || (a == 2 && b == 2)))
                        {
                            toSpeech.speak("stop there is " + classNames[classId] + "in" +
                                    out, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }
                String label = classNames[classId] + ": " + confidence;//color;

                contador[classId] = contador[classId] + 1;

                int[] baseLine = new int[1];
                Size labelSize = Imgproc.getTextSize(label, Core.FONT_HERSHEY_DUPLEX,
                        1.00, 1, baseLine);

                // make a rectangle and write object name around object
                if(Voice.color ==0 )
                {
                    // Draw rectangle around detected object.
                    Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom),
                            new Point(xRightTop, yRightTop),
                            new Scalar(colorR[classId], colorG[classId], colorB[classId]),
                            6);

                    // Draw background for label.
                    Imgproc.rectangle(subFrame, new Point(xLeftBottom, yLeftBottom -
                                    labelSize.height),
                            new Point(xLeftBottom + labelSize.width, yLeftBottom +
                                    baseLine[0]),
                            new Scalar(255, 255, 255), Core.FILLED);

                    // Write class name and confidence.
                    Imgproc.putText(subFrame, label, new Point(xLeftBottom, yLeftBottom),
                            Core.FONT_HERSHEY_DUPLEX, 1.00, new Scalar(0, 0, 0));
                }

                // when recognize choosed
                if(Voice.recognize==1 && f==5)
                {
                    toSpeech.speak(classNames[classId] + "in" + out, TextToSpeech.QUEUE_FLUSH,
                            null);
                }
            }
        }

        if (_SpeeakNow)
        {

            phase = MSGRECOG01;
            for (int counter = 0; counter < contador.length; counter++)
            {
                if (contador[counter] != 0)
                {
                    Log.i(TAG, classNames[counter]);
                    phase = phase.concat(String.valueOf(contador[counter]));
                    phase = phase.concat(" ");
                    phase = phase.concat(classNames[counter]);
                    phase = phase.concat(",");

                }

            }
            _SpeeakNow = false;
            Log.i(TAG, phase);
            toSpeech.speak(phase, TextToSpeech.QUEUE_FLUSH, null);

        }
        return frame;
    }

    public void onCameraViewStopped()
    {
    }

    // Upload file to storage and return a path.
    private static String getPath(String file, Context context)
    {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try
        {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        }
        catch (IOException ex)
        {
            Log.i(TAG, "Failed to upload a file");
        }
        return "";
    }


    public int[] detectColor()
    {
        int cols = subFrame.cols();
        int rows = subFrame.rows();

        double yLow = (double) mOpenCvCameraView.getHeight() * 0.2401961;
        double yHigh = (double) mOpenCvCameraView.getHeight() * 0.7696078;

        double xScale = (double) cols / (double) mOpenCvCameraView.getWidth();
        double yScale = (double) rows / (yHigh - yLow);
        int subframe_center_x = cols/2 , subframe_center_y = rows/2;
        int xleft =  subframe_center_x - (cols / 8);
        int xRight = subframe_center_x + (cols / 8);
        int yTop =  subframe_center_y - (rows / 8);
        int yBottom = subframe_center_y + (rows / 8);
        int max =0;
        vector.clear();
        counter.clear();
        int []color_max= new int[3];
        Map< String,Integer> hm = new HashMap<>();

        if(flash==1)
             subFrame.convertTo(subFrame, -1, 1, -30); //decrease the brightness by 50
        else
             subFrame.convertTo(subFrame, -1, 1, -15); //increase the brightness by 25


        for (int i = xleft; i <= xRight; i++)
        {
            for (int a = yTop; a <= yBottom; a++)
            {

                int bool = 0;
                if ((i < 0) || (a < 0) || (i > cols) || (a > rows)) continue;

                double[] RGB =subFrame.get(i,a);

                if(RGB==null)
                {
                    return  null;
                }
                Double a1= RGB[0];
                int red = a1.intValue();
                Double a2= RGB[1];
                int green = a2.intValue();
                Double a3= RGB[2];
                int blue = a3.intValue();

                ss=Integer.toString(red)+" "+Integer.toString(green)+" "+Integer.toString(blue);


                if (bool == 0)
                {
                    if(hm.containsKey(ss))
                        hm.put(ss,hm.get(ss)+1);
                    else
                    {
                        hm.put(ss,1);
                    }

                    if(hm.get(ss)>max)
                    {
                        max = hm.get(ss);
                        color_max[0]=red;
                        color_max[1]=green;
                        color_max[2]=blue;
                    }

                }
            }
        }
        return color_max;
    }


    public void SpeechInput()
    {
        voice.getSpeechInput();
        voice.voiceBool = 1;
    }




    Locale current;
    private static final String TAG = "OpenCV/Sample/MobileNet";
    String[] classNames;
    int[] colorR = new int[] {14, 255, 128, 255, 1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 255, 0, 0, 128, 128, 11, 12, 13, 14, 15, 16, 17, 0, 144, 17, 17, 138, 255, 0, 18, 19, 20, 255, 255, 21, 22, 23, 24, 25, 255, 17, 17, 154, 47, 75, 210, 210, 205, 244, 222, 188, 140, 0, 64, 6, 140, 27, 180, 0, 29, 23, 17, 28, 17, 17, 46, 17, 66, 85, 91, 93, 139, 139, 255, 255, 26, 28, 220, 17, 0, 0, 128, 165, 27, 28, 128, 0, 255, 0, 255
    };
    int[] colorG = new int[] {14, 0, 9, 255, 1, 255, 2, 3, 4, 5, 255, 99, 255, 128, 0, 10, 11, 12, 13, 14, 15, 16, 17, 0, 238, 17, 17, 43, 255, 0, 18, 19, 20, 99, 69, 21, 22, 23, 24, 25, 140, 17, 17, 205, 79, 0, 105, 105, 133, 164, 184, 143, 141, 128, 0, 2, 64, 114, 118, 104, 162, 64, 17, 134, 17, 17, 71, 17, 44, 26, 156, 252, 58, 139, 0, 0, 26, 134, 20, 17, 128, 128, 128, 42, 27, 28, 0, 255, 0, 0, 0
    };
    int[] colorB = new int[] {14, 0, 9, 255, 1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 255, 139, 255, 128, 0, 10, 11, 12, 13, 14, 15, 16, 17, 0, 144, 17, 17, 226, 0, 0, 255, 18, 19, 20, 71, 0, 21, 22, 23, 24, 25, 0, 17, 17, 50, 79, 130, 144, 30, 63, 96, 135, 143, 80, 64, 64, 77, 0, 46, 163, 139, 55, 56, 17, 238, 17, 17, 59, 17, 47, 139, 100, 10, 58, 122, 255, 255, 26, 238, 60, 17, 0, 128, 0, 42, 27, 28, 128, 0, 0, 255, 0
    };
    double[] width = new double[]
            {
                    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
            };
    String ALERTA01;
    String MSGRECOG01;
    private Net net;
    private CameraBridgeViewBase mOpenCvCameraView;
    private JavaCameraView javaCameraView;
    boolean _Ok = false;
    TextToSpeech toSpeech;
    int result;
    int[] contador;
    boolean _Speeak;
    String color_max = null,ss;


    TextView touch_coordinates;
    ImageView imageView;

    Mat frame, subFrame;
    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;

    TextView touch_color;

    double x ;
    double y ;
    double widthObj, heightObj;

    Vector vector = new Vector();
    Vector counter = new Vector();
    Map< String,Integer> hm = null;

    Voice voice;
    ColorNames colorObj = new ColorNames();


    //light sensor
    float min ,max;
    int flash =0;

}
