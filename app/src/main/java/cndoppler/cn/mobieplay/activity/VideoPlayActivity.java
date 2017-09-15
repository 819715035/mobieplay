package cndoppler.cn.mobieplay.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.utils.BaseActivity;
import cndoppler.cn.mobieplay.utils.ToastUtils;
import cndoppler.cn.mobieplay.utils.Utils;

public class VideoPlayActivity extends BaseActivity {

    private VideoView videoPlayView;
    private LinearLayout llTop;
    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwichPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSiwchScreen;
    private Handler handler;
    private boolean isUseSystem = true;
    /**
     * 视频进度的更新
     */
    private static final int PROGRESS = 1;
    /**
     * 隐藏控制面板
     */
    private static final int HIDE_MEDIACONTROLLER = 2;


    /**
     * 显示网络速度
     */
    private static final int SHOW_SPEED = 3;
    /**
     * 全屏
     */
    private static final int FULL_SCREEN = 1;
    /**
     * 默认屏幕
     */
    private static final int DEFAULT_SCREEN = 2;
    private Utils utils;

    @Override
    public void setContent() {
        setContentView(R.layout.activity_videoplay);
    }

    @Override
    public void initWidget() {
        llTop = findViewById( R.id.ll_top );
        tvName = findViewById( R.id.tv_name );
        ivBattery = findViewById( R.id.iv_battery );
        tvSystemTime = findViewById( R.id.tv_system_time );
        btnVoice = findViewById( R.id.btn_voice );
        seekbarVoice = findViewById( R.id.seekbar_voice );
        btnSwichPlayer = (Button)findViewById( R.id.btn_swich_player );
        llBottom = (LinearLayout)findViewById( R.id.ll_bottom );
        tvCurrentTime = (TextView)findViewById( R.id.tv_current_time );
        seekbarVideo = (SeekBar)findViewById( R.id.seekbar_video );
        tvDuration = (TextView)findViewById( R.id.tv_duration );
        btnExit = (Button)findViewById( R.id.btn_exit );
        btnVideoPre = (Button)findViewById( R.id.btn_video_pre );
        btnVideoStartPause = (Button)findViewById( R.id.btn_video_start_pause );
        btnVideoNext = (Button)findViewById( R.id.btn_video_next );
        btnVideoSiwchScreen = (Button)findViewById( R.id.btn_video_siwch_screen );
        videoPlayView = mFindViewById(R.id.videopaly_vv);
        setListener();
        handler = new PlayHandler();
        utils = new Utils();
        Intent intent = getIntent();
        String uri = intent.getStringExtra("uri");
        videoPlayView.setVideoURI(Uri.parse(uri));
    }

    public void setListener(){
        //加载监听
        videoPlayView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //开始播放
                videoPlayView.start();
                //设置名字
                // tvName.setText(mediaPlayer.getDrmInfo().);
                //设置时长
                seekbarVideo.setMax(mediaPlayer.getDuration());
                //更新文本时长
                tvDuration.setText(utils.stringForTime(mediaPlayer.getDuration()));
                //发送消息
                handler.sendEmptyMessage(PROGRESS);
            }
        });
        //完成播放
        videoPlayView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

            }
        });

        videoPlayView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                ToastUtils.showToastShort(VideoPlayActivity.this,"该视频无法播放");
                return false;
            }
        });
        //播放进度
        seekbarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b){
                    //视频播放进度
                    videoPlayView.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //点击退出
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //上一个视频
        btnVideoPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        //暂停播放
        btnVideoStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //是否播放
                if (videoPlayView.isPlaying()){
                    videoPlayView.pause();
                    //按钮状态设置播放
                    btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
                }else{
                    videoPlayView.start();
                    //按钮状态设置暂停
                    btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
                }
            }
        });
    }
    class PlayHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case PROGRESS:
                    //得到当前的进度
                    int currentPosition = videoPlayView.getCurrentPosition();
                    //更新进度
                    seekbarVideo.setProgress(currentPosition);
                    //更新文本播放进度
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    //每秒更新一次
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS,1000);
                    break;
            }
        }
    }
}
