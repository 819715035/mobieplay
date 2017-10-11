package cndoppler.cn.mobieplay.service;

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
import cndoppler.cn.mobieplay.bean.VideoData;
import cndoppler.cn.mobieplay.utils.ToastUtils;

/**
 * Created by admin on 2017/10/11 0011.
 */

public class MusicPlayService extends Service
{
    private ArrayList<VideoData> audios;
    private VideoData musicInfo;
    private MediaPlayer mediaPlay;

    @Override
    public void onCreate() {
        super.onCreate();
        //加载音乐列表
        getDataFromLocal();
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
    };

    /**
     * 根据位置打开对应的音频文件,并且播放
     *
     * @param position
     */
    private void openAudio(int position) {
        if (audios!=null && audios.size()>0){
            musicInfo =  audios.get(position);
            if (mediaPlay!=null){
                mediaPlay.release();
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
                        start();
                    }
                });
                mediaPlay.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer)
                    {

                    }
                });
                mediaPlay.setOnErrorListener(new MediaPlayer.OnErrorListener()
                {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1)
                    {
                        return true;
                    }
                });
                mediaPlay.setDataSource(musicInfo.getUrl());
                mediaPlay.prepareAsync();
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
    }

    /**
     * 播暂停音乐
     */
    private void pause() {
        if (mediaPlay!=null && mediaPlay.isPlaying()){
            mediaPlay.pause();
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
        return 0;
    }


    /**
     * 得到当前音频的总时长
     *
     * @return
     */
    private int getDuration() {
        return 0;
    }

    /**
     * 得到艺术家
     *
     * @return
     */
    private String getArtist() {
        return "";
    }

    /**
     * 得到歌曲名字
     *
     * @return
     */
    private String getName() {
        return "";
    }


    /**
     * 得到歌曲播放的路径
     *
     * @return
     */
    private String getAudioPath() {
        return "";
    }

    /**
     * 播放下一个视频
     */
    private void next() {

    }


    /**
     * 播放上一个视频
     */
    private void pre() {

    }

    /**
     * 设置播放模式
     *
     * @param playmode
     */
    private void setPlayMode(int playmode) {

    }

    /**
     * 得到播放模式
     *
     * @return
     */
    private int getPlayMode() {
        return 0;
    }


    /**
     * 是否在播放音频
     * @return
     */
    private boolean isPlaying(){
        return mediaPlay.isPlaying();
    }
}
