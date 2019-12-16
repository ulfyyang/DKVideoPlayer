package com.dueeeke.dkplayer.widget.controller;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dueeeke.videoplayer.controller.BaseVideoController;

/**
 * 抖音
 * Created by dueeeke on 2018/1/6.
 */

public class TikTokController extends BaseVideoController {

    public TikTokController(Context context) {
        super(context);
    }

    public TikTokController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TikTokController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    public boolean showNetWarning() {
        //不显示移动网络播放警告
        return false;
    }
}
