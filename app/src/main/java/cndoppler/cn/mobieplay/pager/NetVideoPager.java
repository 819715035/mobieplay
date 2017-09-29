package cndoppler.cn.mobieplay.pager;

import android.content.Context;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.adapter.NetVideoPagerAdapter;
import cndoppler.cn.mobieplay.bean.VideoData;
import cndoppler.cn.mobieplay.utils.BasePager;
import cndoppler.cn.mobieplay.utils.Constants;
import cndoppler.cn.mobieplay.utils.LogUtils;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class NetVideoPager extends BasePager {

    @ViewInject(R.id.listview)
    private ListView mListView;
    @ViewInject(R.id.tv_nonet)
    private TextView nonetTv;
    @ViewInject(R.id.pb_loading)
    private ProgressBar loadingPb;
    private ArrayList<VideoData> videos;
    private NetVideoPagerAdapter adapter;

    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager,null);
        //第一个参数是：NetVideoPager.this,第二个参数：布局
        x.view().inject(NetVideoPager.this,view);
        return view;
    }

    @Override
    public void initData() {
        LogUtils.e("网络视频的数据被初始化了。。。");
        getDataFromNet();
    }

    /**
     * 从网络中得到数据
     */
    private void getDataFromNet()
    {
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                LogUtils.e("联网成功==" + result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback)
            {
                LogUtils.e("联网失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex)
            {
                LogUtils.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished()
            {
                LogUtils.e("onFinished==");
            }
        });
    }


    private void processData(String json)
    {
        //解析json数据
        videos = parseJson(json);
        showData();
    }


    private void showData()
    {
        //设置适配器
        if(videos != null && videos.size() >0){
            //有数据
            //设置适配器
            adapter = new NetVideoPagerAdapter(context,videos);
            mListView.setAdapter(adapter);
            //把文本隐藏
            nonetTv.setVisibility(View.GONE);
        }else{
            //没有数据
            //文本显示
            nonetTv.setVisibility(View.VISIBLE);
        }

        //ProgressBar隐藏
        loadingPb.setVisibility(View.GONE);
    }

    /**
     * 解决json数据：
     * 1.用系统接口解析json数据
     * 2.使用第三方解决工具（Gson,fastjson）
     * @param json
     * @return
     */
    private ArrayList<VideoData> parseJson(String json)
    {
        ArrayList<VideoData> videoDatas = new ArrayList<>();
        try
        {
            JSONObject job = new JSONObject(json);
            JSONArray jsonArray = job.optJSONArray("trailers");
            if (jsonArray!=null && jsonArray.length()>0){
                for (int i=0;i<jsonArray.length();i++){
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    if (obj!=null){
                        VideoData videoData = new VideoData();
                        String name = obj.optString("movieName");
                        videoData.setName(name);
                        String coverImg = obj.optString("coverImg");
                        videoData.setImageUrl(coverImg);
                        String videoTitle = obj.optString("videoTitle");//desc
                        videoData.setDesc(videoTitle);
                        String hightUrl = obj.optString("hightUrl");
                        videoData.setUrl(hightUrl);
                        videoDatas.add(videoData);
                    }
                }
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return videoDatas;
    }
}
