package com.victor.video.library.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.victor.video.library.camera.CameraController;
import com.victor.video.library.drawer.CameraDrawer;
import com.victor.video.library.gpufilter.SlideGpuFilterGroup;
import com.victor.video.library.gpufilter.basefilter.GPUImageFilter;
import com.victor.video.library.gpufilter.filter.MagicBeautyFilter;
import com.victor.video.library.gpufilter.helper.MagicFilterType;
import com.victor.video.library.gpufilter.utils.OpenGlUtils;
import com.victor.video.library.gpufilter.utils.Rotation;
import com.victor.video.library.gpufilter.utils.TextureRotationUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private Context mContext;

    private CameraDrawer mCameraDrawer;
    private CameraController mCamera;
    private MagicBeautyFilter beautyFilter;

    private int dataWidth=0,dataHeight=0;

    private boolean isSetParm = false;

    private int cameraId;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        /**初始化OpenGL的相关信息*/
        setEGLContextClientVersion(2);//设置版本
        setRenderer(this);//设置Renderer
        setRenderMode(RENDERMODE_WHEN_DIRTY);//主动调用渲染
        setPreserveEGLContextOnPause(true);//保存Context当pause时
        setCameraDistance(100);//相机距离

        /**初始化Camera的绘制类*/
        mCameraDrawer = new CameraDrawer(getResources());
        /**初始化相机的管理类*/
        mCamera = new CameraController();

    }
    private void open(int cameraId){
        mCamera.close();
        mCamera.open(cameraId);
        mCameraDrawer.setCameraId(cameraId);
        final Point previewSize=mCamera.getPreviewSize();
        dataWidth=previewSize.x;
        dataHeight=previewSize.y;
        SurfaceTexture texture = mCameraDrawer.getTexture();
        texture.setOnFrameAvailableListener(this);
        mCamera.setPreviewTexture(texture);
        mCamera.preview();
    }
    public void switchCamera(){
        cameraId = cameraId==0?1:0;
        open(cameraId);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraDrawer.onSurfaceCreated(gl,config);
        if (!isSetParm){
            open(cameraId);
            stickerInit();
        }
        mCameraDrawer.setPreviewSize(dataWidth,dataHeight);
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraDrawer.onSurfaceChanged(gl,width,height);
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onDrawFrame(GL10 gl) {
        if (isSetParm){
            mCameraDrawer.onDrawFrame(gl);
        }
    }
    /**
     * 每次Activity onResume时被调用,第一次不会打开相机
     */
    @Override
    public void onResume() {
        super.onResume();
        if(isSetParm){
            open(cameraId);
        }
    }
    public void onDestroy(){
        if (mCamera != null){
            mCamera.close();
        }
    }

    /**
     * 摄像头聚焦
     * */
    public void onFocus(Point point, Camera.AutoFocusCallback callback){
        mCamera.onFocus(point,callback);
    }

    public int getCameraId(){
        return cameraId;
    }
    public int getBeautyLevel() {
        return mCameraDrawer.getBeautyLevel();
    }

    public void changeBeautyLevel(final int level) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.changeBeautyLevel(level);
            }
        });
    }
    public void setFilter(final MagicFilterType type) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.setFilter(type);
            }
        });
    }
    public void setFilter(final int filterIndex) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.setFilter(filterIndex);
            }
        });
    }
    public void startRecord(){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.startRecord();
            }
        });
    }

    public void stopRecord(){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.stopRecord();
            }
        });
    }
    public void setSavePath(String path) {
        mCameraDrawer.setSavePath(path);
    }
    public void resume(final boolean auto) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.onResume(auto);
            }
        });
    }
    public void pause(final boolean auto) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.onPause(auto);
            }
        });
    }
    public void onTouch(final MotionEvent event){
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.onTouch(event);
            }
        });
    }
    /*public void takePicture(){
        if (mCamera != null) {
            if (mCamera.getCamera() != null) {
                mCamera.getCamera().takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(final byte[] data, Camera camera) {
                        final SavePictureTask savePictureTask = new SavePictureTask(getOutputMediaFile(),null);
//                        CameraEngine.stopPreview();
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                final Bitmap photo = drawPhoto(bitmap,mCamera.isFontCamera());
                                GLES20.glViewport(0, 0, dataWidth, dataHeight);
                                if (photo != null) {
                                    savePictureTask.execute(photo);
                                }
                            }
                        });
//                        CameraEngine.startPreview();
                    }
                });
            }
        }

    }*/
    public void setOnFilterChangeListener(SlideGpuFilterGroup.OnFilterChangeListener listener){
        mCameraDrawer.setOnFilterChangeListener(listener);
    }

    private void stickerInit(){
        if(!isSetParm&&dataWidth>0&&dataHeight>0) {
            isSetParm = true;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }


    private Bitmap drawPhoto(Bitmap bitmap,boolean isRotated){
        GPUImageFilter filter = null;
        if(mCameraDrawer != null) {
            filter = mCameraDrawer.getCurFilter();
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] mFrameBuffers = new int[1];
        int[] mFrameBufferTextures = new int[1];
        if(beautyFilter == null)
            beautyFilter = new MagicBeautyFilter();
        beautyFilter.init();
        beautyFilter.onDisplaySizeChanged(width, height);
        beautyFilter.onInputSizeChanged(width, height);

        if (filter != null) {
            filter.onInputSizeChanged(width, height);
            filter.onDisplaySizeChanged(width, height);
        }
        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        GLES20.glGenTextures(1, mFrameBufferTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);

        GLES20.glViewport(0, 0, width, height);
        int textureId = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, true);

        FloatBuffer gLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        FloatBuffer gLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);
        if(isRotated)
            gLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, false)).position(0);
        else
            gLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)).position(0);


        if(filter == null){
            beautyFilter.onDrawFrame(textureId, gLCubeBuffer, gLTextureBuffer);
        }else{
            beautyFilter.onDrawFrame(textureId);
            filter.onDrawFrame(mFrameBufferTextures[0], gLCubeBuffer, gLTextureBuffer);
        }
        IntBuffer ib = IntBuffer.allocate(width * height);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.copyPixelsFromBuffer(ib);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
        GLES20.glDeleteFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);
        GLES20.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);

        beautyFilter.destroy();
        beautyFilter = null;
        if(filter != null) {
            filter.onDisplaySizeChanged(width, height);
            filter.onInputSizeChanged(width, height);
        }
        return result;
    }

    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MagicCamera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

}
