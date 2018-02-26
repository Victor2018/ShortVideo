package com.victor.video.library.jni;

/**
 * Created by victor on 2017/4/26.
 * desc
 */

public class AudioJniUtils {


    static {
        System.loadLibrary("native-lib");
    }
    public static native byte[] audioMix(byte[] sourceA,byte[] sourceB,byte[] dst,float firstVol , float secondVol);
}
