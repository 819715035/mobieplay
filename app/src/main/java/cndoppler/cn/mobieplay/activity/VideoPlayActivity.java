package cndoppler.cn.mobieplay.activity;

import android.content.Intent;
import android.net.Uri;
import android.widget.VideoView;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.utils.BaseActivity;

public class VideoPlayActivity extends BaseActivity {

    private VideoView videoPlayView;

    @Override
    public void setContent() {
        setContentView(R.layout.activity_videoplay);
    }

    @Override
    public void initWidget() {
        videoPlayView = mFindViewById(R.id.videopaly_vv);
        Intent intent = getIntent();
        String uri = intent.getStringExtra("uri");
        videoPlayView.setVideoURI(Uri.parse(uri));
        videoPlayView.start();
    }

}
