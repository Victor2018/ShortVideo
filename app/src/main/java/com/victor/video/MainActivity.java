package com.victor.video;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.victor.video.adapter.FilterAdapter;
import com.victor.video.library.App.App;
import com.victor.video.library.camera.SensorControler;
import com.victor.video.library.gpufilter.SlideGpuFilterGroup;
import com.victor.video.library.gpufilter.helper.MagicFilterType;
import com.victor.video.library.utils.Constants;
import com.victor.video.library.widget.CameraView;
import com.victor.video.library.widget.CircularProgressView;
import com.victor.video.library.widget.FocusImageView;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import permission.victor.com.library.OnPermissionCallback;
import permission.victor.com.library.PermissionHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnTouchListener, SensorControler.CameraFocusListener,
        SlideGpuFilterGroup.OnFilterChangeListener,OnPermissionCallback{

    private String TAG = "MainActivity";

    private CameraView mCameraView;
    private CircularProgressView mCapture;
    private FocusImageView mFocus;
    private ImageView mBeautyBtn;
    private ImageView mFilterBtn;
    private ImageView mCameraChange;
    private LinearLayout mFilterLayout;
    private RecyclerView mFilterListView;
    private ImageView mIvCloseFilter;

    private FilterAdapter mAdapter;

    private static final int maxTime = 20000;//最长录制20s
    private boolean pausing = false;
    private boolean recordFlag = false;//是否正在录制

    private int WIDTH = 720,HEIGHT = 1280;

    private long timeStep = 50;//进度条刷新的时间
    long timeCount = 0;//用于记录录制时间
    private boolean autoPausing = false;
    ExecutorService executorService;
    private SensorControler mSensorControler;

    private PermissionHelper permissionHelper;
    private String[] neededPermission;
    private AlertDialog builder;
    private final static String[] MULTI_PERMISSIONS = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_WIFI_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_main);

        executorService = Executors.newSingleThreadExecutor();
        mSensorControler = SensorControler.getInstance();
        mSensorControler.setCameraFocusListener(this);
        initView();
    }

    private void initView() {
        mCameraView = (CameraView) findViewById(R.id.camera_view);
        mCapture = (CircularProgressView) findViewById(R.id.mCapture);
        mFocus = (FocusImageView) findViewById(R.id.focusImageView);
        mBeautyBtn = (ImageView) findViewById(R.id.btn_camera_beauty);
        mFilterBtn = (ImageView) findViewById(R.id.btn_camera_filter);
        mCameraChange = (ImageView) findViewById(R.id.btn_camera_switch);

        mFilterLayout = (LinearLayout)findViewById(R.id.layout_filter);
        mFilterListView = (RecyclerView) findViewById(R.id.filter_listView);
        mIvCloseFilter = (ImageView) findViewById(R.id.btn_camera_closefilter);

        mBeautyBtn.setOnClickListener(this);
        mFilterBtn.setOnClickListener(this);
        mIvCloseFilter.setOnClickListener(this);
        mCameraView.setOnTouchListener(this);
        mCameraView.setOnFilterChangeListener(this);
        mCameraChange.setOnClickListener(this);
        mCapture.setTotal(maxTime);
        mCapture.setOnClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterListView.setLayoutManager(linearLayoutManager);

        mAdapter = new FilterAdapter(this, types);
        mFilterListView.setAdapter(mAdapter);
        mAdapter.setOnFilterChangeListener(onFilterChangeListener);

        permissionHelper = PermissionHelper.getInstance(this);
        permissionHelper
                .setForceAccepting(false) // default is false. its here so you know that it exists.
                .request(MULTI_PERMISSIONS);

    }

    public AlertDialog getAlertDialog(final String[] permissions, final String permissionName) {
        if (builder == null) {
            builder = new AlertDialog.Builder(this)
                    .setTitle("Permission Needs Explanation")
                    .create();
        }
        builder.setButton(DialogInterface.BUTTON_POSITIVE, "Request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionHelper.requestAfterExplanation(permissions);
            }
        });
        builder.setMessage("Permissions need explanation (" + permissionName + ")");
        return builder;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_camera_switch:
                mCameraView.switchCamera();
                if (mCameraView.getCameraId() == 1){
                    //前置摄像头 使用美颜
                    mCameraView.changeBeautyLevel(3);
                }else {
                    //后置摄像头不使用美颜
                    mCameraView.changeBeautyLevel(0);
                }
                break;
            case R.id.mCapture:
                if (!recordFlag) {
                    executorService.execute(recordRunnable);
                } else if (!pausing) {
                    mCameraView.pause(false);
                    pausing = true;
                } else {
                    mCameraView.resume(false);
                    pausing = false;
                }
                break;
            case R.id.btn_camera_beauty:

                if (mCameraView.getCameraId() == 0){
                    Toast.makeText(this, "后置摄像头 不使用美白磨皮功能", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setSingleChoiceItems(new String[]{"关闭", "1", "2", "3", "4", "5"}, mCameraView.getBeautyLevel(),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        mCameraView.changeBeautyLevel(which);
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton("取消", null)
                        .show();
                break;
            case R.id.btn_camera_closefilter:
                hideFilters();
                break;
            case R.id.btn_camera_filter:
                showFilters();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mCameraView.onTouch(event);
        if (mCameraView.getCameraId() == 1) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                float sRawX = event.getRawX();
                float sRawY = event.getRawY();
                float rawY = sRawY * App.screenWidth / App.screenHeight;
                float temp = sRawX;
                float rawX = rawY;
                rawY = (App.screenWidth - temp) * App.screenHeight / App.screenWidth;

                Point point = new Point((int) rawX, (int) rawY);
                mCameraView.onFocus(point, callback);
                mFocus.startFocus(new Point((int) sRawX, (int) sRawY));
        }
        return true;
    }

    @Override
    public void onFocus() {
        if (mCameraView.getCameraId() == 1) {
            return;
        }
        Point point = new Point(App.screenWidth / 2, App.screenHeight / 2);
        mCameraView.onFocus(point, callback);
    }

    @Override
    public void onFilterChange(final MagicFilterType type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == MagicFilterType.NONE){
                    Toast.makeText(getApplicationContext(),"当前没有设置滤镜--"+type,Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"当前滤镜切换为--"+type,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    Camera.AutoFocusCallback callback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            Log.e("hero","----onAutoFocus===="+success);
            if (success) {
                mFocus.onFocusSuccess();
            } else {
                //聚焦失败显示的图片
                mFocus.onFocusFailed();

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
        Toast.makeText(this,R.string.change_filter,Toast.LENGTH_SHORT).show();
        if (recordFlag && autoPausing) {
            mCameraView.resume(true);
            autoPausing = false;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (recordFlag && !pausing) {
            mCameraView.pause(true);
            autoPausing = true;
        }
        mCameraView.onPause();
    }

    @Override
    public void onBackPressed() {
        if (recordFlag) {
            recordFlag = false;
        } else {
            super.onBackPressed();
        }
    }

    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            recordFlag = true;
            pausing = false;
            autoPausing = false;
            timeCount = 0;
            long time = System.currentTimeMillis();
            String savePath = Constants.getPath("record/", time + ".mp4");

            try {
                mCameraView.setSavePath(savePath);
                mCameraView.startRecord();
                while (timeCount <= maxTime && recordFlag) {
                    if (pausing || autoPausing) {
                        continue;
                    }
                    mCapture.setProcess((int) timeCount);
                    Thread.sleep(timeStep);
                    timeCount += timeStep;
                }
                recordFlag = false;
                mCameraView.stopRecord();
                if (timeCount < 2000) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "录像时间太短", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    recordComplete(savePath);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private void recordComplete(final String path) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCapture.setProcess(0);
                Toast.makeText(MainActivity.this, "文件保存路径：" + path, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private FilterAdapter.onFilterChangeListener onFilterChangeListener = new FilterAdapter.onFilterChangeListener(){

        @Override
        public void onFilterChanged(MagicFilterType filterType) {
            mCameraView.setFilter(filterType);
        }
    };

    private void showFilters(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", mFilterLayout.getHeight(), 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                mFilterLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();
    }

    private void hideFilters(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", 0 ,  mFilterLayout.getHeight());
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                mFilterLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub
                mFilterLayout.setVisibility(View.INVISIBLE);
            }
        });
        animator.start();
    }

    private final MagicFilterType[] types = new MagicFilterType[]{
            MagicFilterType.NONE,
            MagicFilterType.FAIRYTALE,
            MagicFilterType.SUNRISE,
            MagicFilterType.SUNSET,
            MagicFilterType.WHITECAT,
            MagicFilterType.BLACKCAT,
            MagicFilterType.SKINWHITEN,
            MagicFilterType.HEALTHY,
            MagicFilterType.SWEETS,
            MagicFilterType.ROMANCE,
            MagicFilterType.SAKURA,
            MagicFilterType.WARM,
            MagicFilterType.ANTIQUE,
            MagicFilterType.NOSTALGIA,
            MagicFilterType.CALM,
            MagicFilterType.LATTE,
            MagicFilterType.TENDER,
            MagicFilterType.COOL,
            MagicFilterType.EMERALD,
            MagicFilterType.EVERGREEN,
            MagicFilterType.CRAYON,
            MagicFilterType.SKETCH,
            MagicFilterType.AMARO,
            MagicFilterType.BRANNAN,
            MagicFilterType.BROOKLYN,
            MagicFilterType.EARLYBIRD,
            MagicFilterType.FREUD,
            MagicFilterType.HEFE,
            MagicFilterType.HUDSON,
            MagicFilterType.INKWELL,
            MagicFilterType.KEVIN,
//            MagicFilterType.LOMO,
            MagicFilterType.N1977,
            MagicFilterType.NASHVILLE,
            MagicFilterType.PIXAR,
            MagicFilterType.RISE,
            MagicFilterType.SIERRA,
            MagicFilterType.SUTRO,
            MagicFilterType.TOASTER2,
            MagicFilterType.VALENCIA,
            MagicFilterType.WALDEN,
            MagicFilterType.XPROII
    };

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.d(TAG, "onPermissionGranted-Permission(s) " + Arrays.toString(permissionName) + " Granted");
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {
        Log.d(TAG, "Permission(s) " + Arrays.toString(permissionName) + " Declined");
    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {
        Log.d(TAG, "Permission( " + permissionsName + " ) preGranted");
    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String s) {
        neededPermission = PermissionHelper.declinedPermissions(this, MULTI_PERMISSIONS);
        StringBuilder builder = new StringBuilder(neededPermission.length);
        if (neededPermission.length > 0) {
            for (String permission : neededPermission) {
                builder.append(permission).append("\n");
            }
        }
        AlertDialog alert = getAlertDialog(neededPermission, builder.toString());
        if (!alert.isShowing()) {
            alert.show();
        }
    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {
        Log.d(TAG, "Permission " + permissionName + " can only be granted from settingsScreen");
    }

    @Override
    public void onNoPermissionNeeded() {
        Log.d(TAG, "Permission(s) not needed");
    }
}
