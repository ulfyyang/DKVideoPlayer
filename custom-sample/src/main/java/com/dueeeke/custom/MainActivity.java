package com.dueeeke.custom;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
    private FrameLayout subsetPickFL;
    private LinearLayout subsetPickLL;
    private LinearLayout subsetEmptyLL;
    private Button subset1BT;
    private Button subset2BT;
    private Button subset3BT;

    private PrepareView prepareView;
    private TitleView titleView;

    private Timer timer = new Timer();      // 用于周期性的记录播放位置

    private MainVM vm = new MainVM();

    /*
        测试用例：
            1. 没有选集时界面显示：播放器显示封面但是无法播放，不显示选集而显示一个无播放资源的提示
            2. 有子集不显示子集时不显示选集模块，点击播放器可以播放
            3. 有子集显示子集时显示选集模块，点击播放器可以播放
            4. 首次进入点击或重复点击当前选集按钮视频正常播放不切换、不重置
            5. 切换选集时新的选集从开始播放
            6. 记录播放历史后重新进入页面点击播放视频从历史记录位置播放
            7. 记录播放历史后重新进入页面切换到其它选集从开始播放
            8. 当前集播放结束切换到下一集视频从开始播放
            9. 最后一集播放结束不自动切换，显示重播按钮
     */

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerVV = findViewById(R.id.playerVV);
        titleTV = findViewById(R.id.titleTV);
        descriptionTV = findViewById(R.id.descriptionTV);
        subsetPickFL = findViewById(R.id.subsetPickFL);
        subsetPickLL = findViewById(R.id.subsetPickLL);
        subsetEmptyLL = findViewById(R.id.subsetEmptyLL);
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
                        updatePlayerUI(false, true);
                    }
                }
            }
        });

        VideoViewRepository.getInstance().addVideoView(playerVV);

        // 定时记录播放进度
        timer.schedule(new TimerTask() {
            @Override public void run() {
                if (playerVV.isPlaying()) {
                    VideoPlayRecorder.getInstance().updateVideoRecord(vm.video.id, vm.currentSubsetIndex, playerVV.getCurrentPosition());
                }
            }
        }, new Date(), 5000);
    }

    private void bind() {
        updatePlayerUI(true, false);
        updateContentUI();
    }

    /**
     * 更新播放器设置
     * @param init  是否是首次重置
     * @param autoPlay 是否自动播放
     */
    private void updatePlayerUI(boolean init, boolean autoPlay) {
        boolean subsetChanged;      // 播放的选集是否发生了变化

        // 设置播放历史记录
        if (init) {     // init表示首次设置，该行为发生在首次进入页面时。首次进入页面设置播放记录：第几集、以及播放位置
            subsetChanged = true;       // 首次进入子集必然发生变化
            VideoPlayRecord record = VideoPlayRecorder.getInstance().getVideoRecordById(vm.video.id);
            if (record != null) {
                vm.currentSubsetIndex = vm.pendingSubsetIndex = record.subsetIndex;
                playerVV.skipPositionWhenPlay(record.playOffset);     // 该方法参数为int，需要修改源码为long
            }
        } else {        // 如果不是首次进入页面则：根据当前选集是否变化来决定是否重置播放位置
            subsetChanged = vm.currentSubsetIndex != vm.pendingSubsetIndex;
            if (subsetChanged) {
                vm.currentSubsetIndex = vm.pendingSubsetIndex;
                playerVV.skipPositionWhenPlay(0);
            }
        }

        // 设置播放器
        if (subsetChanged) {        // 只有当播放的选集发生变化后才设置相关的播放页面
            // 设置播放器状态
            Video.Subset subset = vm.getCurrentSubset();        // 获取当前选集
            if (subset == null) {
                // 网络加载的时候不要用占位图，否则会引起闪烁
                ((ImageView)prepareView.findViewById(R.id.thumb)).setImageResource(vm.video.cover);
                playerVV.release();
            } else {
                playerVV.release();
                // 网络加载的时候不要用占位图，否则会引起闪烁
                ((ImageView)prepareView.findViewById(R.id.thumb)).setImageResource(vm.video.cover);
                titleView.setTitle(subset.title);
                playerVV.setUrl(subset.playUrl);
                if (autoPlay) {
                    playerVV.start();
                }
            }

            // 设置选集区域
            subset1BT.setTextColor(vm.currentSubsetIndex == 0 ? Color.BLACK : Color.GRAY);
            subset2BT.setTextColor(vm.currentSubsetIndex == 1 ? Color.BLACK : Color.GRAY);
            subset3BT.setTextColor(vm.currentSubsetIndex == 2 ? Color.BLACK : Color.GRAY);
        } else {                    // 如果选集没有变化则根据是否自动播放设置播放状态
            if (autoPlay && !playerVV.isPlaying()) {
                playerVV.start();
            }
        }
    }

    /**
     * 更新页面内容
     */
    private void updateContentUI() {
        titleTV.setText(vm.video.title);
        descriptionTV.setText(vm.video.description);
        if (vm.haveValidSubset()) {     // 有子集时根据是否显示选集设置选集
            if (vm.showSubsetPicker()) {
                subsetPickFL.setVisibility(View.VISIBLE);
                subsetPickLL.setVisibility(View.VISIBLE);
            } else {
                subsetPickFL.setVisibility(View.GONE);
            }
            subsetEmptyLL.setVisibility(View.GONE);
        } else {                        // 没子集时一定显示一个错误提示
            subsetPickFL.setVisibility(View.VISIBLE);
            subsetPickLL.setVisibility(View.GONE);
            subsetEmptyLL.setVisibility(View.VISIBLE);
        }
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
            updatePlayerUI(false, true);
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
