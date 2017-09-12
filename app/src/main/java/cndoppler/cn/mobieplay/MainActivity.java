package cndoppler.cn.mobieplay;

import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;
import java.util.ArrayList;
import java.util.List;
import cndoppler.cn.mobieplay.pager.AudioPager;
import cndoppler.cn.mobieplay.pager.NetAudioPager;
import cndoppler.cn.mobieplay.pager.NetVideoPager;
import cndoppler.cn.mobieplay.pager.VideoPager;
import cndoppler.cn.mobieplay.utils.BaseActivity;
import cndoppler.cn.mobieplay.utils.BaseFragment;
import cndoppler.cn.mobieplay.utils.BasePager;

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
        if (pager!=null && pager.isData){
            pager.initData();
            pager.isData = true;
            return pager;
        }
        return null;
    }
}
