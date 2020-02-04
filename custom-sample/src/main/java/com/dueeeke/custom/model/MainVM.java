package com.dueeeke.custom.model;

import com.dueeeke.custom.R;
import com.dueeeke.custom.domain.Video;

import java.util.ArrayList;

public class MainVM {
    public Video video;                     // 视频信息
    /*
    采用两个变量来记录播放集数，一个表示当前正在播放第几集，一个表示下次播放第几集
    当设置播放集数的时候会先设置下次播放第几集，在播放器设置部分会根据播放状态切换
     */
    public int currentSubsetIndex = 0;      // 当前播放到第几集
    public int pendingSubsetIndex = 0;      // 即将播放第几集（用于设置下次播放的集数）

    public MainVM() {
        // 此处为后台加载的内容演示

        video = new Video();
        video.id = 1;
        video.cover = R.mipmap.ic_video_cover;
        video.title = "超级小白";
        video.description = "动画《SUPER SHIRO》的设定为“非常普通的野原一家所养育的小狗小白，实际上是守护世界的超级英雄”。动画由霜山朋久担任总监制，上野贵美子负责剧本。具体播出时间尚未公开。";
        video.subsetList = new ArrayList<>();

        Video.Subset subset1 = new Video.Subset();
        subset1.title = "第一集";
        subset1.playUrl = "http://feifei.feifeizuida.com/20191013/20015_a3bffdd7/index.m3u8";
        video.subsetList.add(subset1);

        Video.Subset subset2 = new Video.Subset();
        subset2.title = "第二集";
        subset2.playUrl = "http://meng.wuyou-zuida.com/20191020/20450_ea5f0a75/index.m3u8";
        video.subsetList.add(subset2);

        Video.Subset subset3 = new Video.Subset();
        subset3.title = "第三集";
        subset3.playUrl = "http://hong.tianzhen-zuida.com/20191028/12084_a38866c1/index.m3u8";
        video.subsetList.add(subset3);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 和选集相关的服务方法 start
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 是否含有可用的播放子集
     */
    public boolean haveValidSubset() {
        return video.subsetList != null && video.subsetList.size() > 0;
    }

    /**
     * 是否显示子集选择界面
     */
    public boolean showSubsetPicker() {
        /*
        具体规则需要根据业务来定
            1. 有的是根据类型来的：电影不显示，电视剧显示（即使只有一集）
            2. 有的是根据集数来的：只有一集不显示，多于一集显示
         */
        return video.subsetList != null && video.subsetList.size() > 1;
    }

    /**
     * 是否可以移动到下一集
     */
    public boolean canMoveToNextSubset() {
        return video.subsetList != null && currentSubsetIndex < video.subsetList.size() - 1;
    }

    /**
     * 移动到下一集
     */
    public void moveToNextSubset() {
        if (video.subsetList != null && currentSubsetIndex < video.subsetList.size() - 1) {
            pendingSubsetIndex++;
        }
    }

    /**
     * 移动到指定集
     */
    public void moveToSubset(int subsetIndex) {
        if (video.subsetList != null && subsetIndex < video.subsetList.size()) {
            this.pendingSubsetIndex = subsetIndex;
        }
    }

    /**
     * 获取当前即将播放或正在播放的视频
     *      如果获取不到说明没有播放源，界面应该显示相应的错误
     */
    public Video.Subset getCurrentSubset() {
        if (video.subsetList == null) {
            return null;
        } else {
            return currentSubsetIndex < video.subsetList.size() ? video.subsetList.get(currentSubsetIndex) : null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 和选集相关的服务方法 end
    ///////////////////////////////////////////////////////////////////////////
}
