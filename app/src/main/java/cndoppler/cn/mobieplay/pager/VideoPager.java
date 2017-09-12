package cndoppler.cn.mobieplay.pager;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import cndoppler.cn.mobieplay.utils.BasePager;
import cndoppler.cn.mobieplay.utils.LogUtils;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class VideoPager extends BasePager {

    private TextView tv;

    public VideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
       tv = new TextView(context);
        LogUtils.d("initview");
        return tv;
    }

    @Override
    public void initData() {
        tv.setText("本地视频");
        LogUtils.d("initdata");
    }
}
