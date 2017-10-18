package cndoppler.cn.mobieplay.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.bean.VideoData;
import cndoppler.cn.mobieplay.utils.BaseActivity;
import cndoppler.cn.mobieplay.utils.ToastUtils;
import cndoppler.cn.mobieplay.utils.Utils;
import cndoppler.cn.mobieplay.widget.MyVideoView;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;

public class VideoPlayActivity extends BaseActivity {

    private MyVideoView videoPlayView;
    private LinearLayout llTop;
    private TextView tvName;
    private TextView tvBattery;
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
    private View controlLayout;
    private View bufferLayout;
    private View loadingLayout;
    private TextView loadingTv;
    private TextView bufferTv;
    private Handler handler;
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
    private Utils utils;
    private BroadcastReceiver batteryReceiver;
    /**
     * 当前视频位置
     */
    private int videoPosition;
    /**
     * 视频列表
     */
    private List<VideoData> videoDatas;
    /**
     * 是否全屏，默认是否
     */
    private boolean isFullScreen;
    private int screenHeight;
    private int screenWidth;
    private int videoHeight;
    private int videoWidth;
    private GestureDetector detector;
    /**
     * 调用声音
     */
    private AudioManager am;
    private int currentAudio;
    private int maxAudio;
    private int tempVolume;//临时音量
    /**
     * 静音
     */
    private boolean isMute;
    private Uri uri; //视频链接
    private boolean isNetUri;
    private int precurrentPosition;
    private boolean isUseSystem = true;//是否使用系统的监听卡

    @Override
    public void setContent() {
        setContentView(R.layout.activity_videoplay);
    }

    @Override
    public void initWidget() {
        loadingLayout = findViewById(R.id.loading_layout);
        loadingTv = findViewById(R.id.tv_laoding_netspeed);
        bufferLayout = findViewById(R.id.buffer_ll);
        bufferTv = findViewById(R.id.buffer_tv);
        controlLayout = findViewById(R.id.video_control);
        llTop = findViewById( R.id.ll_top );
        tvName = findViewById( R.id.tv_name );
        tvBattery = findViewById( R.id.tv_battery );
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
        getData();
        setData();
        //注册电量监听
        batteryReceiver = new ElectricityReceiver();
        IntentFilter fifter = new IntentFilter();
        fifter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver,fifter);
        //获取屏幕的大小
        //过时的方法
        //screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        //screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        //得到音量
        currentAudio = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentAudio==0){
            isMute = true;
        }
        maxAudio = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        seekbarVoice.setMax(maxAudio);
        seekbarVoice.setProgress(currentAudio);
        //开始更新网络速度
        handler.sendEmptyMessage(SHOW_SPEED);
    }

    /**
     * 得到播放数据
     * @return
     */
    public void getData() {
        Intent intent = getIntent();
        videoDatas = (List<VideoData>) intent.getSerializableExtra("videolist");
        videoPosition = intent.getIntExtra("position",0);
        uri = getIntent().getData();//文件夹，图片浏览器，QQ空间
    }

    /**
     * 设置播放的数据
     */
    private void setData() {
        if (videoDatas != null && videoDatas.size()>0){
            playVideo();
        }else if(uri!=null){
            tvName.setText(uri.toString());//设置视频的名称
            isNetUri = utils.isNetUri(uri.toString());
            videoPlayView.setVideoURI(uri);
        }else{
            ToastUtils.showToastShort(this, "帅哥你没有传递数据");
        }
        setButtonState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver!=null){
            unregisterReceiver(batteryReceiver);
            batteryReceiver = null;
        }
        if(handler!=null){
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    public void setListener(){
        //加载监听
        videoPlayView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoWidth = mediaPlayer.getVideoWidth();
                videoHeight = mediaPlayer.getVideoHeight();
                //默认屏幕
                setVideoSwitchScreen();
                //开始播放
                videoPlayView.start();
                //设置时长
                seekbarVideo.setMax(mediaPlayer.getDuration());
                //更新文本时长
                tvDuration.setText(utils.stringForTime(mediaPlayer.getDuration()));
                //隐藏加载页
                loadingLayout.setVisibility(View.GONE);
                //发送消息
                handler.sendEmptyMessage(PROGRESS);
                hideControlLayout();
            }
        });
        //完成播放
        videoPlayView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                handler.removeMessages(PROGRESS);
                seekbarVideo.setProgress(seekbarVideo.getMax());
                //更新文本播放进度
                tvCurrentTime.setText(utils.stringForTime(seekbarVideo.getMax()));
                if (videoDatas!=null && videoDatas.size()>0){
                    nextVideo();
                }
            }
        });

        videoPlayView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                ToastUtils.showToastShort(VideoPlayActivity.this,"该视频无法播放");
                loadingLayout.setVisibility(View.GONE);
                return true;
            }
        });
        //播放进度
        seekbarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b){
                    //视频播放进度
                    videoPlayView.seekTo(i);
                    //更新文本播放进度
                    tvCurrentTime.setText(utils.stringForTime(i));
                    handler.removeMessages(PROGRESS);
                    videoPlayView.pause();
                    btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(HIDE_MEDIACONTROLLER);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoPlayView.start();
                btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
                handler.sendEmptyMessage(PROGRESS);
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,3000);
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
                preVideo();
            }
        });

        //下一个视频
        btnVideoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextVideo();
            }
        });

        //暂停播放
        btnVideoStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playOrPause();

            }
        });

        /**
         * 切换全屏或默认
         */
        btnVideoSiwchScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVideoSwitchScreen();
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                hideControlLayout();
            }
        });
        //得到手势识别
        detector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            //双击
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setVideoSwitchScreen();
                return super.onDoubleTap(e);
            }
            //单击
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (controlLayout.getVisibility()==View.VISIBLE){
                    controlLayout.setVisibility(View.GONE);
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                }else{
                    hideControlLayout();
                }
                return super.onSingleTapConfirmed(e);
            }
            //长按
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                playOrPause();
            }
        });

        //设置音量
        seekbarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentAudio = i;
                am.setStreamVolume(AudioManager.STREAM_MUSIC,i,0);
                if (i<=0){
                    isMute = true;
                }else{
                    isMute = false;
                    tempVolume = i;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(HIDE_MEDIACONTROLLER);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,3000);
            }
        });

        /**
         * 是否静音
         */
        btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMute){
                    seekbarVoice.setProgress(tempVolume);
                    isMute = false;
                }else{
                    seekbarVoice.setProgress(0);
                    isMute = true;
                }
                hideControlLayout();
            }
        });

        /**
         * 监听卡
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            videoPlayView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int what, int i1) {
                    switch (what){
                        case MEDIA_INFO_BUFFERING_START:
                            bufferLayout.setVisibility(View.VISIBLE);
                            break;
                        case MEDIA_INFO_BUFFERING_END:
                            bufferLayout.setVisibility(View.GONE);
                            break;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * 暂停或者播放视频
     */
    private void playOrPause() {
        //是否播放
        if (videoPlayView.isPlaying()){
            videoPlayView.pause();
            //按钮状态设置播放
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        }else{
            videoPlayView.start();
            //按钮状态设置暂停
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
            //发送消息
            handler.sendEmptyMessage(PROGRESS);
        }
    }

    /**
     * 设置屏幕大小
     */
    private void setVideoSwitchScreen() {
        int width;
        int height;
        if (isFullScreen){
            //切换成全屏
            width = screenWidth;
            height = screenHeight;
            btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_default_selector);
            isFullScreen = false;
        }else{
            //切换成默认
            float x = videoWidth*1.0f/screenWidth;
            float y = videoHeight*1.0f/screenHeight;
            //得到最小的比例
            float rote = Math.max(x,y);
            width = (int) (videoWidth/rote);
            height = (int) (videoHeight/rote);
            btnVideoSiwchScreen.setBackgroundResource(R.drawable.btn_video_siwch_screen_full_selector);
            isFullScreen = true;
        }
        videoPlayView.setVideoViewSize(width,height);
    }

    /**
     * 播放下一个视频
     */
    private void nextVideo() {
        videoPosition++;
        playVideo();
    }



    /**
     * 播放上一个视频
     */
    private void preVideo() {
        videoPosition--;
        playVideo();
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        if (videoPosition<0 || videoPosition>videoDatas.size()-1){
            return;
        }
        loadingLayout.setVisibility(View.VISIBLE);
        VideoData video = videoDatas.get(videoPosition);
        videoPlayView.setVideoURI(Uri.parse(video.getUrl()));
        //设置名字
        tvName.setText(video.getName());
        setButtonState();
        hideControlLayout();
    }

    /**
     * 隐藏控制面板
     */
    private void hideControlLayout() {
        if (controlLayout.getVisibility()== View.GONE){
            controlLayout.setVisibility(View.VISIBLE);
        }
        //移除隐藏面板消息
        handler.removeMessages(HIDE_MEDIACONTROLLER);
        //发送隐藏
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,3000);
    }
    /**
     * 设置上一个、下一个视频的显示状态
     */
    private void setButtonState() {
        if (videoDatas!=null && videoDatas.size()>0){
            if(videoPosition ==videoDatas.size()-1){
                if (videoDatas.size()>1){
                    btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                    btnVideoPre.setEnabled(true);
                }else{
                    btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                    btnVideoPre.setEnabled(false);
                }
                btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                btnVideoNext.setEnabled(false);

            }else if (videoPosition ==0){
                if (videoDatas.size() ==1){
                    btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                    btnVideoNext.setEnabled(false);
                }else{
                    btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                    btnVideoNext.setEnabled(true);
                }
                btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                btnVideoPre.setEnabled(false);

            }else {
                btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
                btnVideoPre.setEnabled(true);
                btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                btnVideoNext.setEnabled(true);
            }
        }else{
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            btnVideoPre.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoNext.setEnabled(false);
        }
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
                    //设置系统时间
                    tvSystemTime.setText(getSysteTime());
                    //缓存进度的更新
                    if (isNetUri) {
                        //只有网络资源才有缓存效果
                        int buffer = videoPlayView.getBufferPercentage();//0~100
                        int totalBuffer = buffer * seekbarVideo.getMax();
                        int secondaryProgress = totalBuffer / 100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    } else {
                        //本地视频没有缓冲效果
                        seekbarVideo.setSecondaryProgress(0);
                    }

                    //监听卡
                    if (!isUseSystem) {

                        if(videoPlayView.isPlaying()){
                            int buffer = currentPosition - precurrentPosition;
                            if (buffer < 10) {
                                //视频卡了
                                bufferLayout.setVisibility(View.VISIBLE);
                            } else {
                                //视频不卡了
                                bufferLayout.setVisibility(View.GONE);
                            }
                        }else{
                            bufferLayout.setVisibility(View.GONE);
                        }

                    }
                    precurrentPosition = currentPosition;
                    handler.removeMessages(PROGRESS);
                    //每秒更新一次
                    handler.sendEmptyMessageDelayed(PROGRESS,20);
                    break;
                case HIDE_MEDIACONTROLLER:
                    //隐藏控制面板
                    controlLayout.setVisibility(View.GONE);
                    break;
                case SHOW_SPEED://显示网速
                    //1.得到网络速度
                    String netSpeed = utils.getNetSpeed(VideoPlayActivity.this);

                    //显示网络速
                    loadingTv.setText("玩命加载中..."+netSpeed);
                    bufferTv.setText("缓存中..."+netSpeed);

                    //2.每两秒更新一次
                    handler.removeMessages(SHOW_SPEED);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED, 2000);

                    break;
            }
        }
    }

    /**
     * 得到系统时间
     * @return
     */
    private String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    class ElectricityReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())){
                ToastUtils.showToastShort(context,"电量过低，请及时充电");
            }
            if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())){
                ToastUtils.showToastShort(context,"电量已恢复，请放心使用");
            }
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                //当前电量
                int current = intent.getIntExtra("level",0);
                //总电量
                int max = intent.getIntExtra("scale",100);
                String point = current*100/max+"%";
                tvBattery.setText(point);
                setBattery(current);
            }

        }
    }

    //设置电量图片
    private void setBattery(int current) {
        if (current<=0){
            tvBattery.setBackgroundResource(R.drawable.ic_battery_0);
        }else if (current<10){
            tvBattery.setBackgroundResource(R.drawable.ic_battery_10);
        }else if (current<20){
            tvBattery.setBackgroundResource(R.drawable.ic_battery_20);
        }else if (current<40){
            tvBattery.setBackgroundResource(R.drawable.ic_battery_40);
        }else if (current<60){
            tvBattery.setBackgroundResource(R.drawable.ic_battery_60);
        }else if (current<80){
            tvBattery.setBackgroundResource(R.drawable.ic_battery_80);
        }else if (current<=100){
            tvBattery.setBackgroundResource(R.drawable.ic_battery_100);
        }
    }
    float startY = 0;
    float toundRang = 0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                toundRang = Math.min(screenWidth,screenHeight);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                float spaceY = endY - startY;
                float endX = event.getX();
                float distanceY = startY - endY;
                if(endX < screenWidth/2){
                    //左边屏幕-调节亮度
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;
                    if (distanceY > FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        Log.e(TAG, "up");
                        setBrightness(20);
                    }
                    if (distanceY < FLING_MIN_DISTANCE
                            && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
//                        Log.e(TAG, "down");
                        setBrightness(-20);
                    }
                }else{
                    //音量调节
                    currentAudio = (int) Math.min(tempVolume - spaceY / toundRang * maxAudio, maxAudio);
                    updateVolume();
                }

                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    private  Vibrator vibrator;
    /*
    /*
        *
        * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
        */
    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        // if (lp.screenBrightness <= 0.1) {
        // return;
        // }
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 10, 200 }; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        } else if (lp.screenBrightness < 0.2) {
            lp.screenBrightness = (float) 0.2;
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] pattern = { 10, 200 }; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, -1);
        }
//        Log.e(TAG, "lp.screenBrightness= " + lp.screenBrightness);
        getWindow().setAttributes(lp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //音量减少
                currentAudio--;
                updateVolume();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                //音量增加
                currentAudio++;
                updateVolume();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置音量
     */
    private void updateVolume() {
        am.setStreamVolume(AudioManager.STREAM_MUSIC,currentAudio,0);
        seekbarVoice.setProgress(currentAudio);
        hideControlLayout();
    }
}
