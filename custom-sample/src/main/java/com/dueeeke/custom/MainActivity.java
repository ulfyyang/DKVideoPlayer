package com.dueeeke.custom;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dueeeke.custom.custom.VideoViewRepository;
import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videocontroller.component.CompleteView;
import com.dueeeke.videocontroller.component.ErrorView;
import com.dueeeke.videocontroller.component.GestureView;
import com.dueeeke.videocontroller.component.PrepareView;
import com.dueeeke.videocontroller.component.TitleView;
import com.dueeeke.videocontroller.component.VodControlView;
import com.dueeeke.videoplayer.player.VideoView;

public class MainActivity extends AppCompatActivity {
    private VideoView videoVV;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoVV = findViewById(R.id.videoVV);
        VideoViewRepository.getInstance().addVideoView(videoVV);

        setupVideoController();

        videoVV.setUrl("http://34.92.158.191:8080/test-hd.mp4");
        videoVV.start();
    }

    private void setupVideoController() {
        StandardVideoController controller = new StandardVideoController(this);
        //根据屏幕方向自动进入/退出全屏
        controller.setEnableOrientation(true);
        PrepareView prepareView = new PrepareView(this);//准备播放界面
        controller.addControlComponent(prepareView);
        controller.addControlComponent(new CompleteView(this));//自动完成播放界面
        controller.addControlComponent(new ErrorView(this));//错误界面
        TitleView titleView = new TitleView(this);//标题栏
        controller.addControlComponent(titleView);
        VodControlView vodControlView = new VodControlView(this);//点播控制条
        //是否显示底部进度条。默认显示
//                vodControlView.showBottomProgress(false);
        controller.addControlComponent(vodControlView);
        GestureView gestureControlView = new GestureView(this);//滑动控制视图
        controller.addControlComponent(gestureControlView);
        titleView.setTitle("视频标题");
        videoVV.setVideoController(controller);
    }

    @Override protected void onPause() {
        super.onPause();
        VideoViewRepository.getInstance().pause(this);
    }

    @Override protected void onPostResume() {
        super.onPostResume();
        VideoViewRepository.getInstance().resume(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        VideoViewRepository.getInstance().releaseVideoView(this);
    }

    @Override public void onBackPressed() {
        if (!VideoViewRepository.getInstance().onBackPressed(this)) {
            super.onBackPressed();
        }
    }
}
