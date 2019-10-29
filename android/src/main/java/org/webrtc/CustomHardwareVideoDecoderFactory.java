//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.os.Build.VERSION;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.webrtc.EglBase.Context;

public class CustomHardwareVideoDecoderFactory implements VideoDecoderFactory {
    private static final String TAG = "HWVideoDecoderFactory";
    private final Context sharedContext;

    /** @deprecated */
    @Deprecated
    public CustomHardwareVideoDecoderFactory() {
        this((Context)null);
    }

    public CustomHardwareVideoDecoderFactory(Context sharedContext) {
        this.sharedContext = sharedContext;
    }

    @Nullable
    public VideoDecoder createDecoder(VideoCodecInfo codecType) {
        VideoCodecType type = VideoCodecType.valueOf(codecType.getName());
        MediaCodecInfo info = this.findCodecForType(type);
        if (info == null) {
            return null;
        } else {
            CodecCapabilities capabilities = info.getCapabilitiesForType(type.mimeType());
            return new AndroidVideoDecoder(new MediaCodecWrapperFactoryImpl(), info.getName(), type, MediaCodecUtils.selectColorFormat(MediaCodecUtils.DECODER_COLOR_FORMATS, capabilities), this.sharedContext);
        }
    }

    public VideoCodecInfo[] getSupportedCodecs() {
        List<VideoCodecInfo> supportedCodecInfos = new ArrayList();
        VideoCodecType[] var2 = new VideoCodecType[]{VideoCodecType.VP8, VideoCodecType.VP9, VideoCodecType.H264};
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            VideoCodecType type = var2[var4];
            MediaCodecInfo codec = this.findCodecForType(type);
            if (codec != null) {
                String name = type.name();
                if (type == VideoCodecType.H264 && this.isH264HighProfileSupported(codec)) {
                    supportedCodecInfos.add(new VideoCodecInfo(name, MediaCodecUtils.getCodecProperties(type, true)));
                }

                supportedCodecInfos.add(new VideoCodecInfo(name, MediaCodecUtils.getCodecProperties(type, false)));
            }
        }

        return (VideoCodecInfo[])supportedCodecInfos.toArray(new VideoCodecInfo[supportedCodecInfos.size()]);
    }

    @Nullable
    private MediaCodecInfo findCodecForType(VideoCodecType type) {
        if (VERSION.SDK_INT < 19) {
            return null;
        } else {
            for(int i = 0; i < MediaCodecList.getCodecCount(); ++i) {
                MediaCodecInfo info = null;

                try {
                    info = MediaCodecList.getCodecInfoAt(i);
                } catch (IllegalArgumentException var5) {
                    Logging.e("HardwareVideoDecoderFactory", "Cannot retrieve encoder codec info", var5);
                }

                if (info != null && !info.isEncoder() && this.isSupportedCodec(info, type)) {
                    return info;
                }
            }

            return null;
        }
    }

    private boolean isSupportedCodec(MediaCodecInfo info, VideoCodecType type) {
        boolean is_supported = false;

        if (!MediaCodecUtils.codecSupportsType(info, type)) {
            is_supported = false;
            Log.d(TAG, "isSupportedCodec:" + info.getName() + " for " + type + " -> " + is_supported);
        } else if(info.getName().equalsIgnoreCase("OMX.google.h264.decoder") && type == VideoCodecType.H264) {
            is_supported = true;
            Log.d(TAG, "isSupportedCodec:" + info.getName() + " for " + type + " -> " + is_supported);
        } else {
            boolean is_match_color = (MediaCodecUtils.selectColorFormat(MediaCodecUtils.DECODER_COLOR_FORMATS, info.getCapabilitiesForType(type.mimeType())) != null);
            is_supported = is_match_color ? this.isHardwareSupported(info, type) : false;
            Log.d(TAG, "isSupportedCodec:" + info.getName() + " for " + type + " -> " + is_supported + " color:" + is_match_color);
        }

        return is_supported;
    }

    private boolean isHardwareSupported(MediaCodecInfo info, VideoCodecType type) {
        String name = info.getName();
        boolean is_supported = false;

        switch(type) {
            case VP8:
                is_supported = name.startsWith("OMX.qcom.") || name.startsWith("OMX.Intel.") || name.startsWith("OMX.Exynos.") || name.startsWith("OMX.Nvidia.");
                break;
            case VP9:
                is_supported = name.startsWith("OMX.qcom.") || name.startsWith("OMX.Exynos.");
                break;
            case H264:
                is_supported = name.startsWith("OMX.MTK.")
                        || name.startsWith("OMX.hisi.")
                        || name.startsWith("OMX.qcom.")
                        || name.startsWith("OMX.Intel.")
                        || name.startsWith("OMX.Exynos.")
                        || name.startsWith("OMX.IMG.")
                        || name.startsWith("OMX.hantro.")
                        || name.startsWith("OMX.Nvidia.");
                break;
            default:
                is_supported = false;
                break;
        }

        Log.d(TAG, "isHardwareSupported:" + name + " -> " + is_supported);
        return is_supported;
    }

    private boolean isH264HighProfileSupported(MediaCodecInfo info) {
        String name = info.getName();
        if (VERSION.SDK_INT >= 21 && name.startsWith("OMX.qcom.")) {
            return true;
        } else {
            return VERSION.SDK_INT >= 23 && name.startsWith("OMX.Exynos.");
        }
    }
}
