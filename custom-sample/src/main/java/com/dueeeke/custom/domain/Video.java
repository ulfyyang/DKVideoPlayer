package com.dueeeke.custom.domain;

import java.util.List;

public class Video {
    public int id;                      // 视频id
    public int cover;                   // 视频封面：这里用本地资源代替
    public String title;                // 视频标题
    public String description;          // 视频描述
    public List<Subset> subsetList;     // 视频子集列表

    public static class Subset {
        public String title;
        public String playUrl;
    }
}
