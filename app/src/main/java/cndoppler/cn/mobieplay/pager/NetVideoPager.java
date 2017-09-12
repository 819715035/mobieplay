package cndoppler.cn.mobieplay.pager;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import cndoppler.cn.mobieplay.utils.BasePager;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class NetVideoPager extends BasePager {
    private TextView textView;
    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        textView = new TextView(context);
        return textView;
    }

    @Override
    public void initData() {
        textView.setText("网络视频");
    }
}
