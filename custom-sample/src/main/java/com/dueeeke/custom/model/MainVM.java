package com.dueeeke.custom.model;

import com.dueeeke.custom.domain.Video;

import java.util.ArrayList;
import java.util.List;

public class MainVM {
    public long videoId;            // 视频ID
    public String title;            // 视频标题
    public String description;      // 视频秒数
    public List<Video> videoList = new ArrayList<>();       // 视频列表
    public int subsetIndex = 0;     // 当前播放到第几集

    public MainVM() {
        // 此处为后台加载的内容
        videoId = 1;
        title = "超级小白";
        description = "动画《SUPER SHIRO》的设定为“非常普通的野原一家所养育的小狗小白，实际上是守护世界的超级英雄”。动画由霜山朋久担任总监制，上野贵美子负责剧本。具体播出时间尚未公开。";
        videoList.add(new Video("第一集", "http://feifei.feifeizuida.com/20191013/20015_a3bffdd7/index.m3u8"));
        videoList.add(new Video("第二集", "http://meng.wuyou-zuida.com/20191020/20450_ea5f0a75/index.m3u8"));
        videoList.add(new Video("第三集", "http://hong.tianzhen-zuida.com/20191028/12084_a38866c1/index.m3u8"));
    }

    ///////////////////////////////////////////////////////////////////////////
    // 和选集相关的服务方法 start
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 是否显示子集选择界面
     */
    public boolean showSubsetPicker() {
        /*
        具体规则需要根据业务来定
            1. 有的是根据类型来的：电影不显示，电视剧显示（即使只有一集）
            2. 有的是根据集数来的：只有一集不显示，多于一集显示
         */
        return videoList.size() > 1;
    }

    /**
     * 是否可以移动到下一集
     */
    public boolean canMoveToNextSubset() {
        return subsetIndex < videoList.size() - 1;
    }

    /**
     * 移动到下一集
     */
    public void moveToNextSubset() {
        if (subsetIndex < videoList.size() - 1) {
            subsetIndex ++;
        }
    }

    /**
     * 移动到指定集
     */
    public void moveToSubset(int subsetIndex) {
        if (subsetIndex < videoList.size() - 1) {
            this.subsetIndex = subsetIndex;
        }
    }

    /**
     * 获取当前即将播放或正在播放的视频
     *      如果获取不到说明没有播放源，界面应该显示相应的错误
     */
    public Video getCurrentSubset() {
        return subsetIndex < videoList.size() ? videoList.get(subsetIndex) : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 和选集相关的服务方法 end
    ///////////////////////////////////////////////////////////////////////////
}
