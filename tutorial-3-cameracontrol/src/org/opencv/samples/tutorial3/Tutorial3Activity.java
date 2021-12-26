package org.opencv.samples.tutorial3;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgcodecs.Imgcodecs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Tutorial3Activity extends CameraActivity implements CvCameraViewListener2, View.OnClickListener, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    public native void Color_Detector_FromJNI(long Mat_ptr, int color_flag, boolean IsGray);  // 不用管报错 直接build 应该是static声明不匹配？

    public native void Save_IMG(long Mat_ptr, String filename_ptr);

    /**
     * Called when the user clicks the Button named btn
     */
    public void showMsg(View view) {
        Toast.makeText(Tutorial3Activity.this, "btn is clicked!", Toast.LENGTH_SHORT).show();
    }

    private Button ChangeColor, shoot_btn, ToGray;

    private int COLOR_FLAG = 0, count = 0;
    private boolean IsGary = false;
    private Mat CurFrame;


    private Tutorial3View mOpenCvCameraView;
    private List<Size> mResolutionList;
    private Menu mMenu;
    private boolean mCameraStarted = false;
    private boolean mMenuItemsCreated = false;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;

    //动态申请权限
    public static boolean isGrantExternalRW(Tutorial3Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(Tutorial3Activity.this);

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
            System.loadLibrary("ColorDetector");
        }
    };

    public Tutorial3Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial3_surface_view);

        shoot_btn = findViewById(R.id.shoot_btn);
        ToGray = findViewById(R.id.ToGray);
        ChangeColor = findViewById(R.id.ChangeColor);

        //mOpenCvCameraView = findViewById(R.id.tutorial3_activity_java_surface_view);
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);

        ToGray.setVisibility(View.VISIBLE);
        shoot_btn.setVisibility(View.VISIBLE);
        ChangeColor.setVisibility(View.VISIBLE);

        mOpenCvCameraView.setVisibility(View.VISIBLE);


        ChangeColor.setOnClickListener(this);
        shoot_btn.setOnClickListener(this);
        ToGray.setOnClickListener(this);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mCameraStarted = true;
        setupMenuItems();
    }

    public void onCameraViewStopped() {
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();         // frame在这里 调用C++后返回
        long Mat_ptr = frame.getNativeObjAddr();   // 获得帧地址，传给c++处理
        Color_Detector_FromJNI(Mat_ptr, COLOR_FLAG, IsGary);   // 执行检测
        CurFrame = frame;
        return frame;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        setupMenuItems();
        return true;
    }

    private void setupMenuItems() {
        if (mMenu == null || !mCameraStarted || mMenuItemsCreated) {
            return;
        }
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return;
        }

        mColorEffectsMenu = mMenu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        for (String effect : effects) {
            mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, effect);
            idx++;
        }

        mResolutionMenu = mMenu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        idx = 0;
        for (Size resolution : mResolutionList) {
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString());
            idx++;
        }
        mMenuItemsCreated = true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1) {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        } else if (item.getGroupId() == 2) {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG, "onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime + ".jpg";
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        return false;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ToGray:     // 改变颜色的按键

                count += 1;
                COLOR_FLAG = count % 4;

                if(COLOR_FLAG == 0) Toast.makeText(this, "检测蓝色",Toast.LENGTH_SHORT).show();
                else if(COLOR_FLAG == 1) Toast.makeText(this, "检测黄色",Toast.LENGTH_SHORT).show();
                else if(COLOR_FLAG == 2) Toast.makeText(this, "检测红色",Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "检测绿色",Toast.LENGTH_SHORT).show();

                break;

            case R.id.shoot_btn:
                Mat photo = CurFrame.clone();
                long Mat_ptr = photo.getNativeObjAddr();   // 获得帧地址，传给c++处理

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentDateandTime = sdf.format(new Date());
                String filePath = "sdcard/" + "IMG_ColorDetector/";  // 巨坑！ android10只允许操作sdcard下的文件夹
                String filename = currentDateandTime + ".jpg";

                Save_IMG(Mat_ptr, filename);
                Toast.makeText(this, filePath + filename + " saved", Toast.LENGTH_SHORT).show();
                break;

            case R.id.ChangeColor:    // 二值化预览的按键
                IsGary = !IsGary;

            default:
                break;
        }

    }
}

