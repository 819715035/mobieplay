package cndoppler.cn.mobieplay.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import cndoppler.cn.mobieplay.IMusicPlayService;
import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.service.MusicPlayService;
import cndoppler.cn.mobieplay.utils.BaseActivity;
import cndoppler.cn.mobieplay.utils.CacheUtils;
import cndoppler.cn.mobieplay.utils.ToastUtils;
import cndoppler.cn.mobieplay.utils.Utils;

public class AudioPlayerActivity extends BaseActivity implements View.OnClickListener
{

    private int position;
    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private IMusicPlayService musicService;
    private ServiceConnection conn = new ServiceConnection()
    {
        //连接成功
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            musicService = IMusicPlayService.Stub.asInterface(iBinder);
            if (musicService != null){
                try
                {
                    if (!isFromNotification){
                        //打开音乐
                        musicService.openAudio(position);
                    }else{
                        //更新信息
                        updateAudio();
                    }
                } catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }
        }

        //断开连接
        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            try{
                if (musicService!=null){

                    //停止播放
                    musicService.stop();
                    musicService = null;

                }
            } catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    };
    /**
     * 播放歌曲后更新歌名跟歌手的信息及进度条的广播
     */
    private BroadcastReceiver receiver;
    private final int UPDATE_MUSIC_TIME = 1;
    private PlayHandler playHandler = new PlayHandler();
    private Utils utils;
    private boolean isFromNotification; //是否来自通知栏
    private int playMode; //播放模式

    @Override
    public void setContent()
    {
        setContentView(R.layout.activity_audio_player);
        utils = new Utils();
        registerUpdateReceiver();
    }

    @Override
    public void initWidget()
    {
        ivIcon = findViewById( R.id.iv_icon );
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable rocketAnimation = (AnimationDrawable) ivIcon.getBackground();
        rocketAnimation.start();
        tvArtist = findViewById( R.id.tv_artist );
        tvName = findViewById( R.id.tv_name );
        tvTime = findViewById( R.id.tv_time );
        seekbarAudio = findViewById( R.id.seekbar_audio );
        btnAudioPlaymode = findViewById( R.id.btn_audio_playmode );
        btnAudioPre = findViewById( R.id.btn_audio_pre );
        btnAudioStartPause = findViewById( R.id.btn_audio_start_pause );
        btnAudioNext = findViewById( R.id.btn_audio_next );
        btnLyrc = findViewById( R.id.btn_lyrc );
        btnAudioPlaymode.setOnClickListener( this );
        btnAudioPre.setOnClickListener( this );
        btnAudioStartPause.setOnClickListener( this );
        btnAudioNext.setOnClickListener( this );
        btnLyrc.setOnClickListener( this );
        getData();
        bindAndStartService();
        setListener();

    }


    private void setListener()
    {
        seekbarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser)
            {
                if (fromUser){
                    try
                    {
                        musicService.setSeekTo(i);
                    } catch (RemoteException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
    }

    //注册更新歌名广播
    private void registerUpdateReceiver()
    {
        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                //更新歌曲信息
                updateAudio();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayService.UPDATE_AUDIO_INFO);
        registerReceiver(receiver,intentFilter);
    }

    /**
     * 更新歌曲信息
     */
    private void updateAudio()
    {
        try
        {
            tvArtist.setText(musicService.getArtist());
            tvName.setText(musicService.getName());
            seekbarAudio.setMax(musicService.getDuration());
            playHandler.sendEmptyMessage(UPDATE_MUSIC_TIME);
        } catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }

    //绑定服务
    private void bindAndStartService()
    {
        Intent intentService = new Intent(this, MusicPlayService.class);
        intentService.setAction("android.intent.musicService");
        bindService(intentService,conn, Context.BIND_AUTO_CREATE);
        startService(intentService);
    }

    //得到数据
    private void getData()
    {
        isFromNotification = getIntent().getBooleanExtra("notification",false);
        if (!isFromNotification){
            position = getIntent().getIntExtra("position",0);
        }
        playMode = CacheUtils.getPlaymode(this,"playmode");
        checkPlayMode();
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId()){
            case R.id.btn_audio_start_pause:
                //暂停播放
                if(musicService != null){
                    try {
                        if(musicService.isPlaying()){
                            //暂停
                            musicService.pause();
                            //按钮-播放
                            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                        }else{
                            //播放
                            musicService.start();
                            //按钮-暂停
                            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_audio_next:
                //下一首
                try
                {
                    if (musicService!=null){
                        playHandler.removeMessages(UPDATE_MUSIC_TIME);
                        musicService.next();
                    }
                } catch (RemoteException e)
                {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_audio_pre:
                //上一首
                try
                {
                    if (musicService!=null){
                        playHandler.removeMessages(UPDATE_MUSIC_TIME);
                        musicService.pre();
                    }
                } catch (RemoteException e)
                {
                    e.printStackTrace();
                }
                break;
            case  R.id.btn_audio_playmode:
                //播放模式
                setPlayMode();
                break;
            case R.id.btn_lyrc:
                //歌词
                break;
        }
    }

    /**
     *
     * 设置播放模式
     */
    private void setPlayMode()
    {
        try
        {
            playMode = musicService.getPlayMode();
            if(playMode==MusicPlayService.REPEAT_NORMAL){
                playMode = MusicPlayService.REPEAT_SINGLE;
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
                ToastUtils.showToastShort(this, "单曲循环");
            }else if(playMode == MusicPlayService.REPEAT_SINGLE){
                playMode = MusicPlayService.REPEAT_ALL;
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
                ToastUtils.showToastShort(this, "全部循环");
            }else if(playMode ==MusicPlayService.REPEAT_ALL){
                playMode = MusicPlayService.REPEAT_NORMAL;
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                ToastUtils.showToastShort(this, "顺序播放");
            }else{
                playMode = MusicPlayService.REPEAT_NORMAL;
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                ToastUtils.showToastShort(this, "顺序播放");
            }
            //保持
            musicService.setPlayMode(playMode);
        } catch (RemoteException e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     * 检查播放模式
     */
    private void checkPlayMode()
    {
        if(playMode==MusicPlayService.REPEAT_NORMAL){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
        }else if(playMode == MusicPlayService.REPEAT_SINGLE){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
        }else if(playMode ==MusicPlayService.REPEAT_ALL){
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
        }else{
            btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
        if (playHandler!=null){
            playHandler.removeCallbacksAndMessages(null);
        }
        if (conn!=null){
            unbindService(conn);
        }
    }

    class PlayHandler extends Handler{
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what){
                case UPDATE_MUSIC_TIME:
                    try
                    {
                        //更新播放进度
                        seekbarAudio.setProgress(musicService.getCurrentPosition());
                        //更新时间文本
                        tvTime.setText(utils.stringForTime(musicService.getCurrentPosition())+"/"+utils.stringForTime(musicService.getDuration()));
                        //每秒发送一次
                        playHandler.sendEmptyMessageDelayed(UPDATE_MUSIC_TIME,1000);
                    } catch (RemoteException e)
                    {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
