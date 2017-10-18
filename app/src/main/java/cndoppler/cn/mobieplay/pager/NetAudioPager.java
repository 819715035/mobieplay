package cndoppler.cn.mobieplay.pager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.List;

import cndoppler.cn.mobieplay.R;
import cndoppler.cn.mobieplay.adapter.NetAudioPagerAdapter;
import cndoppler.cn.mobieplay.bean.NetAudioPagerData;
import cndoppler.cn.mobieplay.utils.BasePager;
import cndoppler.cn.mobieplay.utils.CacheUtils;
import cndoppler.cn.mobieplay.utils.Constants;
import cndoppler.cn.mobieplay.utils.LogUtils;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class NetAudioPager extends BasePager {
    private List<NetAudioPagerData.ListEntity> datas;

    @ViewInject(R.id.listview)
    private ListView mListView;
    @ViewInject(R.id.tv_nonet)
    private TextView tv_nonet;
    @ViewInject(R.id.pb_loading)
    private ProgressBar pb_loading;
    private NetAudioPagerAdapter adapter;

    public NetAudioPager(Context context) {
        super(context);
    }

    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.netaudio_pager,null);
        x.view().inject(NetAudioPager.this, view);
        return view;
    }

    @Override
    public void initData() {
        LogUtils.e("网络音频的数据被初始化了。。。");
        //缓存到本地
        String savaJson = CacheUtils.getString(context, Constants.ALL_RES_URL);
        if (!TextUtils.isEmpty(savaJson)){
            //解析数据
            processData(savaJson);
        }
        //联网
        getDataFromNet();

    }

    private void getDataFromNet() {
        RequestParams params = new RequestParams(Constants.ALL_RES_URL);
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                LogUtils.e("请求数据成功==" + result);
                //缓存到本地
                CacheUtils.putString(context,Constants.ALL_RES_URL,result);
                processData(result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                LogUtils.e("请求数据失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                LogUtils.e("onCancelled==" + cex.getMessage());
            }

            @Override
            public void onFinished() {
                LogUtils.e("onFinished==");
            }
        });
    }

    /**
     * 解析json数据和显示数据
     * 解析数据：1.GsonFormat生成bean对象；2.用gson解析数据
     */
    private void processData(String json) {
        //解析数据
        NetAudioPagerData data = parsedJson(json);
        if (data!=null){
            datas = data.getList();
            if(datas != null && datas.size() >0 ){
                //有数据
                tv_nonet.setVisibility(View.GONE);
                //设置适配器
                adapter = new NetAudioPagerAdapter(context,datas);
                mListView.setAdapter(adapter);
            }else{
                tv_nonet.setText("没有对应的数据...");
                //没有数据
                tv_nonet.setVisibility(View.VISIBLE);
            }

            pb_loading.setVisibility(View.GONE);
        }

    }

    /**
     * Gson解析数据
     * @param json
     * @return
     */
    private NetAudioPagerData parsedJson(String json) {
        return new Gson().fromJson(json,NetAudioPagerData.class);
    }
}
