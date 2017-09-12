package cndoppler.cn.mobieplay.utils;

import android.content.Context;
import android.view.View;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public abstract class BasePager{
    public View rootView;
    public Context context;
    public boolean isData;

    public BasePager(Context context) {
        this.context = context;
        rootView = initView();
    }

    /**
     * 绑定布局
     * @return
     */
    public abstract View initView();

    /**
     * 初始化数据
     */
    public abstract  void initData();
}
