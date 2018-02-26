package com.victor.video.library.gpufilter.helper;


import com.victor.video.library.gpufilter.basefilter.GPUImageFilter;
import com.victor.video.library.gpufilter.filter.MagicAmaroFilter;
import com.victor.video.library.gpufilter.filter.MagicAntiqueFilter;
import com.victor.video.library.gpufilter.filter.MagicBlackCatFilter;
import com.victor.video.library.gpufilter.filter.MagicBrannanFilter;
import com.victor.video.library.gpufilter.filter.MagicBrooklynFilter;
import com.victor.video.library.gpufilter.filter.MagicCalmFilter;
import com.victor.video.library.gpufilter.filter.MagicCoolFilter;
import com.victor.video.library.gpufilter.filter.MagicCrayonFilter;
import com.victor.video.library.gpufilter.filter.MagicEarlyBirdFilter;
import com.victor.video.library.gpufilter.filter.MagicEmeraldFilter;
import com.victor.video.library.gpufilter.filter.MagicEvergreenFilter;
import com.victor.video.library.gpufilter.filter.MagicFairytaleFilter;
import com.victor.video.library.gpufilter.filter.MagicFreudFilter;
import com.victor.video.library.gpufilter.filter.MagicHealthyFilter;
import com.victor.video.library.gpufilter.filter.MagicHefeFilter;
import com.victor.video.library.gpufilter.filter.MagicHudsonFilter;
import com.victor.video.library.gpufilter.filter.MagicImageAdjustFilter;
import com.victor.video.library.gpufilter.filter.MagicInkwellFilter;
import com.victor.video.library.gpufilter.filter.MagicKevinFilter;
import com.victor.video.library.gpufilter.filter.MagicLatteFilter;
import com.victor.video.library.gpufilter.filter.MagicLomoFilter;
import com.victor.video.library.gpufilter.filter.MagicN1977Filter;
import com.victor.video.library.gpufilter.filter.MagicNashvilleFilter;
import com.victor.video.library.gpufilter.filter.MagicNostalgiaFilter;
import com.victor.video.library.gpufilter.filter.MagicPixarFilter;
import com.victor.video.library.gpufilter.filter.MagicRiseFilter;
import com.victor.video.library.gpufilter.filter.MagicRomanceFilter;
import com.victor.video.library.gpufilter.filter.MagicSakuraFilter;
import com.victor.video.library.gpufilter.filter.MagicSierraFilter;
import com.victor.video.library.gpufilter.filter.MagicSketchFilter;
import com.victor.video.library.gpufilter.filter.MagicSkinWhitenFilter;
import com.victor.video.library.gpufilter.filter.MagicSunriseFilter;
import com.victor.video.library.gpufilter.filter.MagicSunsetFilter;
import com.victor.video.library.gpufilter.filter.MagicSutroFilter;
import com.victor.video.library.gpufilter.filter.MagicSweetsFilter;
import com.victor.video.library.gpufilter.filter.MagicTenderFilter;
import com.victor.video.library.gpufilter.filter.MagicToasterFilter;
import com.victor.video.library.gpufilter.filter.MagicValenciaFilter;
import com.victor.video.library.gpufilter.filter.MagicWaldenFilter;
import com.victor.video.library.gpufilter.filter.MagicWhiteCatFilter;
import com.victor.video.library.gpufilter.filter.MagicXproIIFilter;
import com.victor.video.library.gpufilter.filter.base.gpuimage.GPUImageBrightnessFilter;
import com.victor.video.library.gpufilter.filter.base.gpuimage.GPUImageContrastFilter;
import com.victor.video.library.gpufilter.filter.base.gpuimage.GPUImageExposureFilter;
import com.victor.video.library.gpufilter.filter.base.gpuimage.GPUImageHueFilter;
import com.victor.video.library.gpufilter.filter.base.gpuimage.GPUImageSaturationFilter;
import com.victor.video.library.gpufilter.filter.base.gpuimage.GPUImageSharpenFilter;

public class MagicFilterFactory {

    private static MagicFilterType filterType = MagicFilterType.NONE;

    public static GPUImageFilter initFilters(MagicFilterType type) {
        if (type == null) {
            return null;
        }
        filterType = type;
        switch (type) {
            case WHITECAT:
                return new MagicWhiteCatFilter();
            case BLACKCAT:
                return new MagicBlackCatFilter();
            case SKINWHITEN:
                return new MagicSkinWhitenFilter();
            case ROMANCE:
                return new MagicRomanceFilter();
            case SAKURA:
                return new MagicSakuraFilter();
            case AMARO:
                return new MagicAmaroFilter();
            case WALDEN:
                return new MagicWaldenFilter();
            case ANTIQUE:
                return new MagicAntiqueFilter();
            case CALM:
                return new MagicCalmFilter();
            case BRANNAN:
                return new MagicBrannanFilter();
            case BROOKLYN:
                return new MagicBrooklynFilter();
            case EARLYBIRD:
                return new MagicEarlyBirdFilter();
            case FREUD:
                return new MagicFreudFilter();
            case HEFE:
                return new MagicHefeFilter();
            case HUDSON:
                return new MagicHudsonFilter();
            case INKWELL:
                return new MagicInkwellFilter();
            case KEVIN:
                return new MagicKevinFilter();
            case LOMO:
                return new MagicLomoFilter();
            case N1977:
                return new MagicN1977Filter();
            case NASHVILLE:
                return new MagicNashvilleFilter();
            case PIXAR:
                return new MagicPixarFilter();
            case RISE:
                return new MagicRiseFilter();
            case SIERRA:
                return new MagicSierraFilter();
            case SUTRO:
                return new MagicSutroFilter();
            case TOASTER2:
                return new MagicToasterFilter();
            case VALENCIA:
                return new MagicValenciaFilter();
            case XPROII:
                return new MagicXproIIFilter();
            case EVERGREEN:
                return new MagicEvergreenFilter();
            case HEALTHY:
                return new MagicHealthyFilter();
            case COOL:
                return new MagicCoolFilter();
            case EMERALD:
                return new MagicEmeraldFilter();
            case LATTE:
                return new MagicLatteFilter();
            case WARM:
                return new MagicWarmFilter();
            case TENDER:
                return new MagicTenderFilter();
            case SWEETS:
                return new MagicSweetsFilter();
            case NOSTALGIA:
                return new MagicNostalgiaFilter();
            case FAIRYTALE:
                return new MagicFairytaleFilter();
            case SUNRISE:
                return new MagicSunriseFilter();
            case SUNSET:
                return new MagicSunsetFilter();
            case CRAYON:
                return new MagicCrayonFilter();
            case SKETCH:
                return new MagicSketchFilter();
            //image adjust
            case BRIGHTNESS:
                return new GPUImageBrightnessFilter();
            case CONTRAST:
                return new GPUImageContrastFilter();
            case EXPOSURE:
                return new GPUImageExposureFilter();
            case HUE:
                return new GPUImageHueFilter();
            case SATURATION:
                return new GPUImageSaturationFilter();
            case SHARPEN:
                return new GPUImageSharpenFilter();
            case IMAGE_ADJUST:
                return new MagicImageAdjustFilter();
            default:
                return null;
        }
    }

    public MagicFilterType getCurrentFilterType() {
        return filterType;
    }

    private static class MagicWarmFilter extends GPUImageFilter {
    }
}
