package com.dueeeke.custom;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dueeeke.custom.custom_dkplayer.VideoViewRepository;
import com.dueeeke.custom.domain.Video;
import com.dueeeke.custom.domain.VideoPlayRecord;
import com.dueeeke.custom.domain.VideoPlayRecorder;
import com.dueeeke.custom.model.MainVM;
import com.dueeeke.videocontroller.StandardVideoController;
import com.dueeeke.videocontroller.component.CompleteView;
import com.dueeeke.videocontroller.component.ErrorView;
import com.dueeeke.videocontroller.component.GestureView;
import com.dueeeke.videocontroller.component.PrepareView;
import com.dueeeke.videocontroller.component.TitleView;
import com.dueeeke.videocontroller.component.VodControlView;
import com.dueeeke.videoplayer.player.VideoView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private VideoView playerVV;
    private TextView titleTV;
    private TextView descriptionTV;
    private LinearLayout subsetPickLL;
    private Button subset1BT;
    private Button subset2BT;
    private Button subset3BT;

    private PrepareView prepareView;
    private TitleView titleView;

    private Timer timer = new Timer();      // 用于周期性的记录播放位置

    private MainVM vm = new MainVM();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerVV = findViewById(R.id.playerVV);
        titleTV = findViewById(R.id.titleTV);
        descriptionTV = findViewById(R.id.descriptionTV);
        subsetPickLL = findViewById(R.id.subsetPickLL);
        subset1BT = findViewById(R.id.subset1BT);
        subset2BT = findViewById(R.id.subset2BT);
        subset3BT = findViewById(R.id.subset3BT);
        subset1BT.setOnClickListener(new OnClickImpl());
        subset2BT.setOnClickListener(new OnClickImpl());
        subset3BT.setOnClickListener(new OnClickImpl());

        init(getContext(), null);
        bind();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            playerVV.getLayoutParams().height = playerVV.getWidth() * 1080 / 1920;
            playerVV.requestLayout();
        }
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

        // 自动播放到下一集
        playerVV.addOnStateChangeListener(new VideoView.OnStateChangeListener() {
            @Override public void onPlayerStateChanged(int playerState) { }
            @Override public void onPlayStateChanged(int playState) {
                if (playState == VideoView.STATE_PLAYBACK_COMPLETED) {
                    if (vm.canMoveToNextSubset()) {
                        vm.moveToNextSubset();
                        resetPlayerUI(false, true);
                    }
                }
            }
        });

        VideoViewRepository.getInstance().addVideoView(playerVV);

        // 定时记录播放进度
        timer.schedule(new TimerTask() {
            @Override public void run() {
                if (playerVV.isPlaying()) {
                    VideoPlayRecorder.getInstance().updateVideoRecord(vm.videoId, vm.subsetIndex, playerVV.getCurrentPosition());
                }
            }
        }, new Date(), 5000);
    }

    private void bind() {
        resetPlayerUI(true, false);
        titleTV.setText(vm.title);
        descriptionTV.setText(vm.description);
        subsetPickLL.setVisibility(vm.showSubsetPicker() ? View.VISIBLE : View.GONE);
    }

    /**
     * 重新设置播放器
     * @param init  是否是首次重置
     * @param autoPlay 是否自动播放
     */
    private void resetPlayerUI(boolean init, boolean autoPlay) {
        Video video = vm.getCurrentSubset();

        // 首次重置设置播放记录：第几集
        if (init) {
            VideoPlayRecord record = VideoPlayRecorder.getInstance().getVideoRecordById(vm.videoId);
            if (record != null) {
                vm.subsetIndex = record.subsetIndex;
                playerVV.skipPositionWhenPlay(record.playOffset);     // 该方法参数为int，需要修改源码为long
            }
        }

        // 设置播放器状态
        if (video == null) {
            ((ImageView)prepareView.findViewById(R.id.thumb)).setImageResource(R.mipmap.ic_video_cover);
            playerVV.release();
        } else {
            playerVV.release();
            ((ImageView)prepareView.findViewById(R.id.thumb)).setImageResource(R.mipmap.ic_video_cover);
            titleView.setTitle(vm.getCurrentSubset().title);
            playerVV.setUrl(vm.getCurrentSubset().playUrl);
            if (autoPlay) {
                playerVV.start();
            }
        }

        // 设置选集按钮状态（选中为黑色；未选中为灰色）
        subset1BT.setTextColor(vm.subsetIndex == 0 ? Color.BLACK : Color.GRAY);
        subset2BT.setTextColor(vm.subsetIndex == 1 ? Color.BLACK : Color.GRAY);
        subset3BT.setTextColor(vm.subsetIndex == 2 ? Color.BLACK : Color.GRAY);
    }

    /**
     * 手动选集
     */
    private class OnClickImpl implements View.OnClickListener {
        @Override public void onClick(View v) {
            switch (v.getId()) {
                case R.id.subset1BT:
                    vm.moveToSubset(0);
                    break;
                case R.id.subset2BT:
                    vm.moveToSubset(1);
                    break;
                case R.id.subset3BT:
                    vm.moveToSubset(2);
                    break;
            }
            resetPlayerUI(false, true);
        }
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
        timer.cancel();     // 注意及时关闭定时器
    }
    @Override public void onBackPressed() {
        if (!VideoViewRepository.getInstance().onBackPressed(this)) {
            super.onBackPressed();
        }
    }
}
