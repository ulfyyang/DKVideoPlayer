package com.dueeeke.custom.domain;

import java.io.Serializable;

public class VideoPlayRecord implements Serializable {
    private static final long serialVersionUID = -7927065953937542215L;
    public long videoId;        // 视频id
    public int subsetIndex;     // 当前播放到第几集
    public long playOffset;     // 当前集播放的位置
}
