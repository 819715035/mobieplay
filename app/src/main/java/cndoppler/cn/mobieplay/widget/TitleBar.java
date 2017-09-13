package cndoppler.cn.mobieplay.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.utils.ToastUtils;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class TitleBar extends LinearLayout{
    private Context context;
    public TitleBar(Context context) {
        super(context);
        this.context = context;
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.search_tv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showToastShort(context,"search");
            }
        });
        findViewById(R.id.game_iv).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showToastShort(context,"game");
            }
        });
        findViewById(R.id.record).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showToastShort(context,"record");
            }
        });
    }
}
