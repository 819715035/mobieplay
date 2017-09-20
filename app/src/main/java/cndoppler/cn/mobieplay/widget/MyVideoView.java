package cndoppler.cn.mobieplay.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.VideoView;

/**
 * Created by Administrator on 2017/9/19 0019.
 */

public class MyVideoView extends VideoView {
    public MyVideoView(Context context) {
        super(context,null);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    /**
     * 设置播放器大小
     * @param width 视频宽度
     * @param height 视频高度
     */
    public void setVideoViewSize(int width,int height){
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = width;
        params.height = height;
        setLayoutParams(params);
    }
}
