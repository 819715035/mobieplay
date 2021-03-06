package cndoppler.cn.mobieplay.pager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.activity.VideoPlayActivity;
import cndoppler.cn.mobieplay.adapter.VideoPagerAdapter;
import cndoppler.cn.mobieplay.bean.VideoData;
import cndoppler.cn.mobieplay.utils.BasePager;
import cndoppler.cn.mobieplay.utils.LogUtils;
import cndoppler.cn.mobieplay.utils.ToastUtils;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class VideoPager extends BasePager {

    private TextView noneVideoTv;
    private ProgressBar loadingPb;
    private ListView videoLV;
    private ArrayList<VideoData> videos;
    private Handler handler = new MyHandler();

    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.fragment_video_layout, null);
        noneVideoTv = view.findViewById(R.id.nonevideo_tv);
        loadingPb = view.findViewById(R.id.loading_pb);
        videoLV = view.findViewById(R.id.video_lv);
        videoLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //调用系统的播放
                /*Intent intent = new Intent(context, VideoPlayActivity.class);
                intent.putExtra("uri",videos.get(i).getUrl());
                context.startActivity(intent);*/
                Intent intent = new Intent(context, VideoPlayActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("videolist",videos);
                bundle.putInt("position",i);
                intent.putExtras(bundle);
                context.startActivity(intent);
                /*Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"),"video*//*");
                context.startActivity(intent);*/

            }
        });
        return view;
    }

    @Override
    public void initData() {
        LogUtils.d("initdata");
        //从本地加载数据
        loadVideoLocal();
    }

    /**
     * 从本地加载数据
     */
    private void loadVideoLocal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] videoInfo = {
                        MediaStore.Video.Media.DISPLAY_NAME,  //视频名字
                        MediaStore.Video.Media.SIZE,          //视频大小
                        MediaStore.Video.Media.DATA,          //视频地址
                        MediaStore.Video.Media.ARTIST,        //视频作者
                        MediaStore.Video.Media.DURATION       //视频时长
                };
                Cursor cursor = resolver.query(uri, videoInfo, null, null, null);
                if (cursor != null) {
                    videos = new ArrayList<>();
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
                        videos.add(video);
                    }
                    //关闭cursor
                    cursor.close();
                }
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (videos!=null && videos.size()>0){
                //得到视频数据
                //把文本隐藏
                noneVideoTv.setVisibility(View.GONE);
                VideoPagerAdapter adapter = new VideoPagerAdapter(context,videos,true);
                videoLV.setAdapter(adapter);
            }else{
                //没有视频
                //把文本显示
                noneVideoTv.setVisibility(View.VISIBLE);
                ToastUtils.showToastShort(context,"没有数据");
            }
            //隐藏加载进度条
            loadingPb.setVisibility(View.GONE);
        }
    }
}