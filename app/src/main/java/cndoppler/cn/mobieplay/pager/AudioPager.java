package cndoppler.cn.mobieplay.pager;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import cndoppler.cn.mobieplay.utils.BasePager;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class AudioPager extends BasePager {

    private TextView tv;

    public AudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        tv = new TextView(context);
        return tv;
    }

    @Override
    public void initData() {
        tv.setText("本地音乐");
    }
}
