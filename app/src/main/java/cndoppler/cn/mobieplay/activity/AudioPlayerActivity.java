package cndoppler.cn.mobieplay.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.IBinder;
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
import cndoppler.cn.mobieplay.utils.ToastUtils;

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
                    //打开音乐
                    musicService.openAudio(position);
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

    @Override
    public void setContent()
    {
        setContentView(R.layout.activity_audio_player);
    }

    @Override
    public void initWidget()
    {
        ivIcon = findViewById( R.id.iv_icon );
        ivIcon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable rocketAnimation = (AnimationDrawable) ivIcon.getBackground();
        rocketAnimation.start();
        tvArtist = (TextView)findViewById( R.id.tv_artist );
        tvName = (TextView)findViewById( R.id.tv_name );
        tvTime = (TextView)findViewById( R.id.tv_time );
        seekbarAudio = (SeekBar)findViewById( R.id.seekbar_audio );
        btnAudioPlaymode = (Button)findViewById( R.id.btn_audio_playmode );
        btnAudioPre = (Button)findViewById( R.id.btn_audio_pre );
        btnAudioStartPause = (Button)findViewById( R.id.btn_audio_start_pause );
        btnAudioNext = (Button)findViewById( R.id.btn_audio_next );
        btnLyrc = (Button)findViewById( R.id.btn_lyrc );

        btnAudioPlaymode.setOnClickListener( this );
        btnAudioPre.setOnClickListener( this );
        btnAudioStartPause.setOnClickListener( this );
        btnAudioNext.setOnClickListener( this );
        btnLyrc.setOnClickListener( this );
        getData();
        bindAndStartService();
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
        position = getIntent().getIntExtra("position",0);
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
        }
    }
}
