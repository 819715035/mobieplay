package cndoppler.cn.mobieplay.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;

import java.io.IOException;
import java.util.ArrayList;

import cndoppler.cn.mobieplay.IMusicPlayService;
import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.activity.AudioPlayerActivity;
import cndoppler.cn.mobieplay.bean.VideoData;
import cndoppler.cn.mobieplay.utils.CacheUtils;
import cndoppler.cn.mobieplay.utils.ToastUtils;

/**
 * Created by admin on 2017/10/11 0011.
 */

public class MusicPlayService extends Service
{
    private ArrayList<VideoData> audios;
    private VideoData musicInfo;
    private MediaPlayer mediaPlay;
    public static final String UPDATE_AUDIO_INFO = "update_audio_info";
    private NotificationManager notificationManager;
    /**
     * 顺序播放
     */
    public static final int REPEAT_NORMAL = 1;
    /**
     * 单曲循环
     */
    public static final int REPEAT_SINGLE = 2;
    /**
     * 全部循环
     */
    public static final int REPEAT_ALL = 3;

    /**
     * 播放模式
     */
    private int playmode = REPEAT_NORMAL;
    private int position;//播放位置

    @Override
    public void onCreate() {
        super.onCreate();
        //加载音乐列表
        getDataFromLocal();
        playmode = CacheUtils.getPlaymode(this,"playmode");
    }

    /**
     * 得到本地的音乐列表
     */
    private void getDataFromLocal()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                audios = new ArrayList<>();
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] videoInfo = {
                        MediaStore.Audio.Media.DISPLAY_NAME,  //视频名字
                        MediaStore.Audio.Media.SIZE,          //视频大小
                        MediaStore.Audio.Media.DATA,          //视频地址
                        MediaStore.Audio.Media.ARTIST,        //视频作者
                        MediaStore.Audio.Media.DURATION       //视频时长
                };
                Cursor cursor = resolver.query(uri, videoInfo, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        VideoData video = new VideoData();
                        String name = cursor.getString(0);
                        video.setName(name);
                        long size = cursor.getLong(1);
                        video.setSize(size);
                        String data = cursor.getString(2);
                        video.setUrl(data);
                        String artist = cursor.getString(3);
                        video.setArtist(artist);
                        long duration = cursor.getLong(4);
                        video.setTime(duration);
                        audios.add(video);
                    }
                    //关闭cursor
                    cursor.close();
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return stub;
    }

    private IMusicPlayService.Stub stub = new IMusicPlayService.Stub(){

        MusicPlayService musicService = MusicPlayService.this;

        @Override
        public void openAudio(int position) throws RemoteException
        {
            musicService.openAudio(position);
        }

        @Override
        public void start() throws RemoteException
        {
            musicService.start();
        }

        @Override
        public void pause() throws RemoteException
        {
            musicService.pause();
        }

        @Override
        public void stop() throws RemoteException
        {
            musicService.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException
        {
            return musicService.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException
        {
            return musicService.getDuration();
        }

        @Override
        public String getArtist() throws RemoteException
        {
            return musicService.getArtist();
        }

        @Override
        public String getName() throws RemoteException
        {
            return musicService.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException
        {
            return musicService.getAudioPath();
        }

        @Override
        public void next() throws RemoteException
        {
            musicService.next();
        }

        @Override
        public void pre() throws RemoteException
        {
            musicService.pre();
        }

        @Override
        public void setPlayMode(int playmode) throws RemoteException
        {
            musicService.setPlayMode(playmode);
        }

        @Override
        public int getPlayMode() throws RemoteException
        {
            return musicService.getPlayMode();
        }

        @Override
        public boolean isPlaying() throws RemoteException
        {
            return musicService.isPlaying();
        }

        @Override
        public void setSeekTo(int progress) throws RemoteException
        {
            musicService.setSeekTo(progress);
        }
    };


    /**
     * 根据位置打开对应的音频文件,并且播放
     *
     * @param position
     */
    private void openAudio(int position) {
        this.position = position;
        if (audios!=null && audios.size()>0){
            musicInfo =  audios.get(position);
            if (mediaPlay!=null){
                mediaPlay.reset();
            }
            try
            {
                mediaPlay = new MediaPlayer();
                //设置监听：播放出错，播放完成，准备好
                mediaPlay.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
                {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer)
                    {
                        //通知activity更新歌名
                        notificationActivity();
                        start();
                    }
                });
                mediaPlay.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer)
                    {
                        next();
                    }
                });
                mediaPlay.setOnErrorListener(new MediaPlayer.OnErrorListener()
                {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1)
                    {
                        next();
                        return true;
                    }
                });
                mediaPlay.setDataSource(musicInfo.getUrl());
                mediaPlay.prepareAsync();
                if(playmode==MusicPlayService.REPEAT_SINGLE){
                    //单曲循环播放-不会触发播放完成的回调
                    mediaPlay.setLooping(true);
                }else{
                    //不循环播放
                    mediaPlay.setLooping(false);
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        } else {
           ToastUtils.showToastShort(MusicPlayService.this, "还没有数据");
        }
    }

    /**
     * 播放音乐
     */
    private void start() {
        mediaPlay.start();
        //显示通知栏
        showNotification();
    }


    /**
     * 播暂停音乐
     */
    private void pause() {
        if (mediaPlay!=null && mediaPlay.isPlaying()){
            mediaPlay.pause();
        }
        if (notificationManager!=null){
            notificationManager.cancel(1);
        }
    }

    /**
     * 停止
     */
    private void stop() {

    }

    /**
     * 得到当前的播放进度
     *
     * @return
     */
    private int getCurrentPosition() {
        return mediaPlay.getCurrentPosition();
    }


    /**
     * 得到当前音频的总时长
     *
     * @return
     */
    private int getDuration() {
        return mediaPlay.getDuration();
    }

    /**
     * 得到艺术家
     *
     * @return
     */
    private String getArtist() {
        return musicInfo.getArtist();
    }

    /**
     * 得到歌曲名字
     *
     * @return
     */
    private String getName() {
        return musicInfo.getName();
    }


    /**
     * 得到歌曲播放的路径
     *
     * @return
     */
    private String getAudioPath() {
        return musicInfo.getUrl();
    }

    /**
     * 播放下一个音频
     */
    private void next() {
        //1.根据当前的播放模式，设置下一个的位置
        setNextPosition();
    }

    /**
     * 播放上一个音频
     */
    private void pre() {
        //1.根据当前的播放模式，设置上一个的位置
        setPrePosition();
    }

    /**
     * 设置播放模式
     *
     * @param playmode
     */
    private void setPlayMode(int playmode) {
        this.playmode = playmode;
        //保存到本地
        CacheUtils.putPlaymode(this,"playmode",playmode);
        if(playmode==MusicPlayService.REPEAT_SINGLE){
            //单曲循环播放-不会触发播放完成的回调
            mediaPlay.setLooping(true);
        }else{
            //不循环播放
            mediaPlay.setLooping(false);
        }
    }

    /**
     * 得到播放模式
     *
     * @return
     */
    private int getPlayMode() {
        return playmode;
    }


    /**
     * 是否在播放音频
     * @return
     */
    private boolean isPlaying(){
        return mediaPlay.isPlaying();
    }

    /**
     * 设置播放进度
     */
    private void setSeekTo(int progress){
        mediaPlay.seekTo(progress);
    }

    /**
     * 发送更新歌名广播
     */
    private void notificationActivity()
    {
        Intent intent = new Intent();
        intent.setAction(UPDATE_AUDIO_INFO);
        sendBroadcast(intent);
    }

    /**
     * 显示通知栏
     */
    private void showNotification()
    {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this,AudioPlayerActivity.class);
        intent.putExtra("notification",true);//标识来自状态拦
        //PendingIntent.FLAG_UPDATE_CURRENT双击的时候以最后一次为准
        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("321播放器")
                .setContentText("正在播放："+musicInfo.getName())
                .setContentIntent(pendingIntent)
                .build();
        notificationManager.notify(1, notification);
    }

    /**
     * 设置下一个音频
     */
    private void setNextPosition()
    {
        switch (playmode){
            case REPEAT_SINGLE:
                position++;
                if(position >=audios.size()){
                    position = 0;
                }
                //正常范围
                openAudio(position);
                break;
            case REPEAT_ALL:
                position++;
                if(position >=audios.size()){
                    position = 0;
                }
                //正常范围
                openAudio(position);
                break;
            case REPEAT_NORMAL:
                position++;
                if(position < audios.size()){
                    //正常范围
                    openAudio(position);
                }else{
                    position = audios.size()-1;
                }
                break;
            default:
                position++;
                if(position < audios.size()){
                    //正常范围
                    openAudio(position);
                }else{
                    position = audios.size()-1;
                }
                break;
        }
    }

    /**
     * 设置上一个音频
     */
    private void setPrePosition()
    {
        switch (playmode){
            case REPEAT_SINGLE:
                position--;
                if(position <0){
                    position = audios.size()-1;
                }
                openAudio(position);
                break;
            case REPEAT_ALL:
                position--;
                if(position <0){
                    position = audios.size()-1;
                }
                openAudio(position);
                break;
            case REPEAT_NORMAL:
                position--;
                if(position >=0){
                    //正常范围
                    openAudio(position);
                }else{
                    position = 0;
                }
                break;
            default:
                position--;
                if(position >=0){
                    //正常范围
                    openAudio(position);
                }else{
                    position = 0;
                }
                break;
        }
    }
}
