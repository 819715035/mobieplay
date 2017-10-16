package cndoppler.cn.mobieplay.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;

import cndoppler.cn.mobieplay.bean.Lyric;
import cndoppler.cn.mobieplay.utils.DensityUtil;

/**
 * Created by admin on 2017/10/13 0013.
 */

public class ShowLyricView extends TextView
{
    /**
     * 歌词列表
     */
    private ArrayList<Lyric> lyrics;
    private int playPosition;
    private int width;
    private int height;
    private Paint greenPaint;
    private int textHeight;
    private Paint whitePaint;
    /**
     * 当前播放进度
     */
    private float currentPositon;
    /**
     * 高亮显示的时间或者休眠时间
     */
    private float sleepTime;
    /**
     * 时间戳，什么时刻到高亮哪句歌词
     */
    private float timePoint;
    public ShowLyricView(Context context)
    {
        this(context,null);
    }

    public ShowLyricView(Context context, AttributeSet attrs)
    {
        this(context, attrs,0);
    }

    public ShowLyricView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 初始化
     */
    private void initView(Context context)
    {
        textHeight = DensityUtil.dip2px(context,20);
        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setTextSize(DensityUtil.dip2px(context,20));
        greenPaint.setTextAlign(Paint.Align.CENTER);
        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);
        whitePaint.setTextSize(DensityUtil.dip2px(context,18));
        whitePaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (lyrics!=null && lyrics.size()>0){

            //往上移动
            float plush = 0;
            if(sleepTime ==0){
                plush = 0;
            }else{
                //平移
                //这一句所花的时间 ：休眠时间 = 移动的距离 ： 总距离（行高）
                //移动的距离 =  (这一句所花的时间 ：休眠时间)* 总距离（行高）
                float moveDistance = (currentPositon-timePoint)/sleepTime*textHeight;
                //屏幕的的坐标 = 行高 + 移动的距离
                plush = textHeight + moveDistance;
            }
            canvas.translate(0,-plush);
            if (playPosition>=lyrics.size()){
                return;
            }
            canvas.drawText(lyrics.get(playPosition).getContent(),width/2,height/2,greenPaint);
            // 绘制前面部分
            float tempY = height / 2;//Y轴的中间坐标
            for (int i = playPosition-1;i>0;i--){
                tempY = tempY - textHeight;
                if (tempY<0){
                    break;
                }
                canvas.drawText(lyrics.get(i).getContent(),width/2,tempY, whitePaint);
            }
            // 绘制后面部分
            // 绘制后面部分
            tempY = height / 2;//Y轴的中间坐标
            for (int i = playPosition+1;i<lyrics.size();i++){
                tempY = tempY + textHeight;
                if (tempY>height){
                    break;
                }
                canvas.drawText(lyrics.get(i).getContent(),width/2,tempY, whitePaint);
            }
        }else{
            canvas.drawText("没有歌词",width/2,height/2,greenPaint);
        }
    }

    /**
     * 设置歌词列表
     *
     * @param lyrics
     */
    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * 根据当前播放的位置，找出该高亮显示哪句歌词
     *
     * @param currentPosition
     */
    public void setshowNextLyric(int currentPosition) {
        this.currentPositon = currentPosition;
        if (lyrics==null || lyrics.size()==0){
            playPosition = 0;
            return;
        }
        for (int i =1;i<lyrics.size();i++){
            if(currentPosition < lyrics.get(i).getTimePoint()){

                int tempIndex = i - 1;

                if(currentPosition >= lyrics.get(tempIndex).getTimePoint()){
                    //当前正在播放的哪句歌词
                    playPosition = tempIndex;
                    sleepTime = lyrics.get(playPosition).getSleepTime();
                    timePoint = lyrics.get(playPosition).getTimePoint();
                    break;
                }

            }
        }
        //重新绘制
        invalidate();//在主线程中
        //子线程
//        postInvalidate();
    }
}
