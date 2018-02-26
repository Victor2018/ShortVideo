package com.victor.video.library.gpufilter;

import android.opengl.GLES20;
import android.view.MotionEvent;
import android.widget.Scroller;

import com.victor.video.library.App.App;
import com.victor.video.library.gpufilter.basefilter.GPUImageFilter;
import com.victor.video.library.gpufilter.helper.MagicFilterFactory;
import com.victor.video.library.gpufilter.helper.MagicFilterType;
import com.victor.video.library.utils.EasyGlUtils;


/**
 * Created by victor on 2017/4/26..
 * 滑动切换滤镜的控制类
 */

public class SlideGpuFilterGroup {
    private MagicFilterType[] types = new MagicFilterType[]{
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
            MagicFilterType.LOMO,
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
    private GPUImageFilter curFilter;
    private GPUImageFilter leftFilter;
    private GPUImageFilter rightFilter;
    private int width, height;
    private int[] fFrame = new int[1];
    private int[] fTexture = new int[1];
    private int curIndex = 0;
    private Scroller scroller;
    private OnFilterChangeListener mListener;

    public SlideGpuFilterGroup() {
        initFilter();
        scroller = new Scroller(App.getContext());
    }

    private void initFilter() {
        curFilter = getFilter(getCurIndex());
        leftFilter = getFilter(getLeftIndex());
        rightFilter = getFilter(getRightIndex());
    }

    private GPUImageFilter getFilter(int index) {
        GPUImageFilter filter = MagicFilterFactory.initFilters(types[index]);
        if (filter == null) {
            filter = new GPUImageFilter();
        }
        return filter;
    }

    public void init() {
        curFilter.init();
        leftFilter.init();
        rightFilter.init();
    }

    public void onSizeChanged(int width, int height) {
        this.width = width;
        this.height = height;
        GLES20.glGenFramebuffers(1, fFrame, 0);
        EasyGlUtils.genTexturesWithParameter(1, fTexture, 0, GLES20.GL_RGBA, width, height);
        onFilterSizeChanged(width, height);
    }

    private void onFilterSizeChanged(int width, int height) {
        curFilter.onInputSizeChanged(width, height);
        leftFilter.onInputSizeChanged(width, height);
        rightFilter.onInputSizeChanged(width, height);
        curFilter.onDisplaySizeChanged(width, height);
        leftFilter.onDisplaySizeChanged(width, height);
        rightFilter.onDisplaySizeChanged(width, height);
    }

    public int getOutputTexture() {
        return fTexture[0];
    }

    public void onDrawFrame(int textureId) {
        EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[0]);
        if (direction == 0 && offset == 0) {
            curFilter.onDrawFrame(textureId);
        } else if (direction == 1) {
            onDrawSlideLeft(textureId);
        } else if (direction == -1) {
            onDrawSlideRight(textureId);
        }
        EasyGlUtils.unBindFrameBuffer();
    }

    private void onDrawSlideLeft(int textureId) {
        if (locked && scroller.computeScrollOffset()) {
            offset = scroller.getCurrX();
            drawSlideLeft(textureId);
        } else {
            drawSlideLeft(textureId);
            if (locked) {
                if (needSwitch) {
                    reCreateRightFilter();
                    if (mListener != null) {
                        mListener.onFilterChange(types[curIndex]);
                    }
                }
                offset = 0;
                direction = 0;
                locked = false;
            }
        }
    }

    private void onDrawSlideRight(int textureId) {
        if (locked && scroller.computeScrollOffset()) {
            offset = scroller.getCurrX();
            drawSlideRight(textureId);
        } else {
            drawSlideRight(textureId);
            if (locked) {
                if (needSwitch) {
                    reCreateLeftFilter();
                    if (mListener != null) {
                        mListener.onFilterChange(types[curIndex]);
                    }
                }
                offset = 0;
                direction = 0;
                locked = false;
            }
        }
    }

    private void drawSlideLeft(int textureId) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0, 0, offset, height);
        leftFilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(offset, 0, width - offset, height);
        curFilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    private void drawSlideRight(int textureId) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(0, 0, width - offset, height);
        curFilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(width - offset, 0, offset, height);
        rightFilter.onDrawFrame(textureId);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    private void reCreateCenterFilter() {
        curFilter.destroy();
        curFilter = getFilter(curIndex);
        curFilter.init();
        curFilter.onDisplaySizeChanged(width, height);
        curFilter.onInputSizeChanged(width, height);
        needSwitch = false;
    }
    private void reCreateRightFilter() {
        decreaseCurIndex();
        rightFilter.destroy();
        rightFilter = curFilter;
        curFilter = leftFilter;
        leftFilter = getFilter(getLeftIndex());
        leftFilter.init();
        leftFilter.onDisplaySizeChanged(width, height);
        leftFilter.onInputSizeChanged(width, height);
        needSwitch = false;
    }

    private void reCreateLeftFilter() {
        increaseCurIndex();
        leftFilter.destroy();
        leftFilter = curFilter;
        curFilter = rightFilter;
        rightFilter = getFilter(getRightIndex());
        rightFilter.init();
        rightFilter.onDisplaySizeChanged(width, height);
        rightFilter.onInputSizeChanged(width, height);
        needSwitch = false;
    }

    public void destroy() {
        curFilter.destroy();
        leftFilter.destroy();
        rightFilter.destroy();
    }

    private int getLeftIndex() {
        int leftIndex = curIndex - 1;
        if (leftIndex < 0) {
            leftIndex = types.length - 1;
        }
        return leftIndex;
    }

    private int getRightIndex() {
        int rightIndex = curIndex + 1;
        if (rightIndex >= types.length) {
            rightIndex = 0;
        }
        return rightIndex;
    }

    private int getCurIndex() {
        return curIndex;
    }

    private void increaseCurIndex() {
        curIndex++;
        if (curIndex >= types.length) {
            curIndex = 0;
        }
    }

    private void decreaseCurIndex() {
        curIndex--;
        if (curIndex < 0) {
            curIndex = types.length - 1;
        }
    }

    int downX;
    int direction;//0为静止,-1为向左滑,1为向右滑
    int offset;
    boolean locked;
    boolean needSwitch;

    public void onTouchEvent(MotionEvent event) {
        if (locked) {
            return;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (downX == -1) {
                    return;
                }
                int curX = (int) event.getX();
                if (curX > downX) {
                    direction = 1;
                } else {
                    direction = -1;
                }
                offset = Math.abs(curX - downX);
                break;
            case MotionEvent.ACTION_UP:
                if (downX == -1) {
                    return;
                }
                if (offset == 0) {
                    return;
                }
                locked = true;
                downX = -1;
                if (offset > App.screenWidth / 3) {
                    scroller.startScroll(offset, 0, App.screenWidth - offset, 0, 100 * (1 - offset / App.screenWidth));
                    needSwitch = true;
                } else {
                    scroller.startScroll(offset, 0, -offset, 0, 100 * (offset / App.screenWidth));
                    needSwitch = false;
                }
                break;
        }
    }

    public void setOnFilterChangeListener(OnFilterChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnFilterChangeListener {
        void onFilterChange(MagicFilterType type);
    }

    public void setFilter (MagicFilterType type) {
        for (int i=0;i<types.length;i++) {
            if (type == types[i]) {
                curIndex = i;
                reCreateCenterFilter();
                break;
            }
        }
    }
    public void setFilter (int filterIndex) {
        curIndex = filterIndex;
        reCreateCenterFilter();
    }

    public GPUImageFilter getCurFilter () {
        return curFilter;
    }
}
