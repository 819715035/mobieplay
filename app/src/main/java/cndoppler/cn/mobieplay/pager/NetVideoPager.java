package cndoppler.cn.mobieplay.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.activity.VideoPlayActivity;
import cndoppler.cn.mobieplay.adapter.NetVideoPagerAdapter;
import cndoppler.cn.mobieplay.bean.VideoData;
import cndoppler.cn.mobieplay.utils.BasePager;
import cndoppler.cn.mobieplay.utils.CacheUtils;
import cndoppler.cn.mobieplay.utils.Constants;
import cndoppler.cn.mobieplay.utils.LogUtils;
import cndoppler.cn.mobieplay.widget.XListView;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class NetVideoPager extends BasePager {

    @ViewInject(R.id.listview)
    private XListView mListView;
    @ViewInject(R.id.tv_nonet)
    private TextView nonetTv;
    @ViewInject(R.id.pb_loading)
    private ProgressBar loadingPb;
    private ArrayList<VideoData> videos;
    private NetVideoPagerAdapter adapter;
    private boolean isLoadMore;

    public NetVideoPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netvideo_pager,null);
        //第一个参数是：NetVideoPager.this,第二个参数：布局
        x.view().inject(NetVideoPager.this,view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                //3.传递列表数据-对象-序列化
                Intent intent = new Intent(context,VideoPlayActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("videolist",videos);
                intent.putExtras(bundle);
                intent.putExtra("position",position-1);
                context.startActivity(intent);
            }
        });
        mListView.setPullLoadEnable(true);
        setListener();
        return view;
    }

    private void setListener()
    {
        mListView.setXListViewListener(new XListView.IXListViewListener()
        {
            @Override
            public void onRefresh()
            {
                if (videos!=null && videos.size()>0){
                    videos.clear();
                }
                getDataFromNet();
            }

            @Override
            public void onLoadMore()
            {
                getMoreDataFromNet();
            }
        });
    }



    private void onLoad() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mListView.setRefreshTime("更新时间:"+getSysteTime());
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    public String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    @Override
    public void initData() {
        LogUtils.e("网络视频的数据被初始化了。。。");
        String saveJson = CacheUtils.getString(context,Constants.NET_URL);
        if(!TextUtils.isEmpty(saveJson)){
            processData(saveJson);
        }
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
                CacheUtils.putString(context,Constants.NET_URL,result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback)
            {
                LogUtils.e("联网失败==" + ex.getMessage());
                showData();

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

    /**
     * 加载更多数据
     */
    private void getMoreDataFromNet()
    {
        //视频内容
        RequestParams params = new RequestParams(Constants.NET_URL);
        x.http().get(params, new Callback.CommonCallback<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                LogUtils.e("联网成功==" + result);
                isLoadMore = true;
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback)
            {
                LogUtils.e("联网失败==" + ex.getMessage());
                isLoadMore = false;
                showData();
            }

            @Override
            public void onCancelled(CancelledException cex)
            {
                LogUtils.e("onCancelled==" + cex.getMessage());
                isLoadMore = false;
            }

            @Override
            public void onFinished()
            {
                LogUtils.e("onFinished==");
                isLoadMore = false;
            }
        });
    }

    private void processData(String json)
    {

        if(!isLoadMore){
            //解析json数据
            videos = parseJson(json);
            showData();
            adapter.notifyDataSetChanged();
            onLoad();
        }else{
            //加载更多
            //要把得到更多的数据，添加到原来的集合中
//            ArrayList<MediaItem> moreDatas = parseJson(json);
            isLoadMore = false;
            videos.addAll(parseJson(json));
            //刷新适配器
            adapter.notifyDataSetChanged();
            onLoad();
        }
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
        onLoad();
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
