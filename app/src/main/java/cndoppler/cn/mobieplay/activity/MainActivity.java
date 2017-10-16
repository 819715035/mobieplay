package cndoppler.cn.mobieplay.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.pager.AudioPager;
import cndoppler.cn.mobieplay.pager.NetAudioPager;
import cndoppler.cn.mobieplay.pager.NetVideoPager;
import cndoppler.cn.mobieplay.pager.VideoPager;
import cndoppler.cn.mobieplay.utils.BaseActivity;
import cndoppler.cn.mobieplay.utils.BaseFragment;
import cndoppler.cn.mobieplay.utils.BasePager;
import cndoppler.cn.mobieplay.utils.ToastUtils;

public class MainActivity extends BaseActivity {

    private RadioGroup mainRg;
    private List<BasePager> pagers = new ArrayList<>();
    private int position;
    @Override
    public void setContent() {
        setContentView(R.layout.activity_main);
        pagers.add(new VideoPager(this));
        pagers.add(new AudioPager(this));
        pagers.add(new NetVideoPager(this));
        pagers.add(new NetAudioPager(this));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //没有授权
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    @Override
    public void initWidget() {
        mainRg = mFindViewById(R.id.main_rg);
        mainRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
                    case R.id.video_rb:
                        //本地视频
                        position = 0;
                        break;
                    case R.id.audio_rb:
                        //本地音乐
                        position = 1;
                        break;
                    case R.id.netvideo_rb:
                        //网络视频
                        position = 2;
                        break;
                    case R.id.netaudio_rb:
                        //网络音频
                        position = 3;
                        break;
                    default:
                        position = 0;
                        break;
                }
                setFrameLayout();
            }
        });
        //默认选中本地视频
        mainRg.check(R.id.video_rb);
    }

    /**
     * 设置具体某个子页面
     */
    private void setFrameLayout() {
        FragmentManager fragmentManager =  getSupportFragmentManager();
        //得到fragment事务管理
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //替换页面
        transaction.replace(R.id.main_content_fl,new BaseFragment(getPager()));
        //提交事物
        transaction.commit();
    }

    private BasePager getPager() {
        BasePager pager = pagers.get(position);
        if (pager!=null){
            //如果已经更新过则不在加载数据
            if (!pager.isData){
                pager.initData();
                pager.isData = true;
            }
            return pager;
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                ToastUtils.showToastShort(this,"请先授权");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 是否已经退出
     */
    private boolean isExit = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(position != 0){//不是第一页面
                position = 0;
                mainRg.check(R.id.video_rb);//首页
                return true;
            }else  if(!isExit){
                isExit = true;
                ToastUtils.showToastShort(MainActivity.this,"再按一次推出");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit  = false;
                    }
                },2000);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
