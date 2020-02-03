package com.dueeeke.custom.domain;

import android.os.Environment;

import com.dueeeke.custom.MainApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 视频播放记录器，该记录器只记录了每部视频集合中当前正在播放的选集进度
 * 目前只记录了视频的基本信息，如需记录额外信息（如视频封面，名字等）可自行修改VideoPlayRecord类
 */
public class VideoPlayRecorder implements Serializable {
    private static final long serialVersionUID = 3540948115525929193L;
    private Map<Long, VideoPlayRecord> videoPlayRecordMap = new HashMap<>();

    public static VideoPlayRecorder getInstance() {
        // 需要替换为自己的持久化框架（这里采用了序列化简单实现：演示使用）
        VideoPlayRecorder instance = null;

        try {
            File cacheFile = new File(MainApplication.context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "video_play_records");
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(cacheFile));
            instance = (VideoPlayRecorder) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (instance == null) {     // 确保一定有缓存管理对象
            instance = new VideoPlayRecorder();
            instance.updateToCache();
        }

        return instance;
    }

    /**
     * 更新视频播放记录
     * @param videoId       视频id
     * @param subsetIndex   当前播放的子集
     * @param playOffset    当前子集播放位置
     */
    public synchronized void updateVideoRecord(long videoId, int subsetIndex, long playOffset) {
        VideoPlayRecord record = videoPlayRecordMap.get(videoId);

        if (record == null) {
            record = new VideoPlayRecord();
            record.videoId = videoId;
            videoPlayRecordMap.put(videoId, record);
        }

        record.subsetIndex = subsetIndex;
        record.playOffset = playOffset;

        updateToCache();
    }

    /**
     * 获取视频播放记录
     * @param videoId   获取记录对应的视频id
     * @return  如果返回null说明目前还没有记录
     */
    public synchronized VideoPlayRecord getVideoRecordById(long videoId) {
        return videoPlayRecordMap.get(videoId);
    }

    private void updateToCache() {
        // 需要替换为自己的持久化框架（这里采用了序列化简单实现：演示使用）
        try {
            File cacheFile = new File(MainApplication.context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "video_play_records");
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(cacheFile));
            output.writeObject(this);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
