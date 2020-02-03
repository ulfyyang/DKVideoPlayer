package com.dueeeke.custom;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dueeeke.custom.custom_dkplayer.VideoViewRepository;
import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videocontroller.component.CompleteView;
import com.dueeeke.videocontroller.component.ErrorView;
import com.dueeeke.videocontroller.component.GestureView;
import com.dueeeke.videocontroller.component.PrepareView;
import com.dueeeke.videocontroller.component.TitleView;
import com.dueeeke.videocontroller.component.VodControlView;
import com.dueeeke.videoplayer.player.VideoView;

public class MainActivity extends AppCompatActivity {
    private VideoView playerVV;
    private TextView titleTV;
    private TextView descriptionTV;
    private Button subset1BT;
    private Button subset2BT;
    private Button subset3BT;

    private PrepareView prepareView;
    private TitleView titleView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerVV = findViewById(R.id.playerVV);
        titleTV = findViewById(R.id.titleTV);
        descriptionTV = findViewById(R.id.descriptionTV);
        subset1BT = findViewById(R.id.subset1BT);
        subset2BT = findViewById(R.id.subset2BT);
        subset3BT = findViewById(R.id.subset3BT);

        init(getContext(), null);
        bind();
    }

    private void init(Context context, AttributeSet attrs) {
        prepareView = new PrepareView(getContext());
        prepareView.setClickStart();
        titleView = new TitleView(getContext());

        StandardVideoController videoController = new StandardVideoController(getContext());
        videoController.addControlComponent(prepareView);
        videoController.addControlComponent(new CompleteView(getContext()));
        videoController.addControlComponent(new ErrorView(getContext()));
        videoController.addControlComponent(titleView);
        videoController.addControlComponent(new VodControlView(getContext()));
        videoController.addControlComponent(new GestureView(getContext()));
        playerVV.setVideoController(videoController);

        VideoViewRepository.getInstance().addVideoView(playerVV);
    }

    private void bind() {
        ((ImageView)prepareView.findViewById(R.id.thumb)).setImageResource(R.mipmap.ic_video_cover);
        titleView.setTitle("超级小白");
        playerVV.setUrl("http://feifei.feifeizuida.com/20191013/20015_a3bffdd7/index.m3u8");
        // playerVV.start();   // 开始播放，不调用则不自动播放

        titleTV.setText("超级小白");
        descriptionTV.setText("动画《SUPER SHIRO》的设定为“非常普通的野原一家所养育的小狗小白，实际上是守护世界的超级英雄”。动画由霜山朋久担任总监制，上野贵美子负责剧本。具体播出时间尚未公开。");
    }

    private Context getContext() {
        return this;
    }

    @Override protected void onPause() {
        super.onPause();
        VideoViewRepository.getInstance().pause(this);
    }
    @Override protected void onResume() {
        super.onResume();
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
