package com.dueeeke.custom;

import android.content.Context;
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
import com.dueeeke.custom.model.MainVM;
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
    private LinearLayout subsetPickLL;
    private Button subset1BT;
    private Button subset2BT;
    private Button subset3BT;

    private PrepareView prepareView;
    private TitleView titleView;

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
                        resetPlayerUI(true);
                    }
                }
            }
        });

        VideoViewRepository.getInstance().addVideoView(playerVV);
    }

    private void bind() {
        resetPlayerUI(false);
        titleTV.setText(vm.title);
        descriptionTV.setText(vm.description);
        subsetPickLL.setVisibility(vm.showSubsetPicker() ? View.VISIBLE : View.GONE);
    }

    /**
     * 重新设置播放器
     */
    private void resetPlayerUI(boolean autoPlay) {
        Video video = vm.getCurrentSubset();
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
            resetPlayerUI(true);
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
    }
    @Override public void onBackPressed() {
        if (!VideoViewRepository.getInstance().onBackPressed(this)) {
            super.onBackPressed();
        }
    }
}
