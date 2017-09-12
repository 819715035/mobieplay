package cndoppler.cn.mobieplay.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/8/28 0028.
 */

public abstract class BaseActivity extends FragmentActivity{
    // 是否允许全屏
    private boolean mAllowFullScreen = true;
    private Toast toast;

    //设置xml文件
    public abstract  void setContent();
    //初始化控件
    public abstract void initWidget();


    //是否全屏
    public void setAllowFullScreen(boolean allowFullScreen) {
        this.mAllowFullScreen = allowFullScreen;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 横屏锁定
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //竖屏锁定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (mAllowFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE); // 取消标题
        }
        setContent();
        initWidget();
        // 添加Activity到堆栈
        BaseApplication.getInstance().addActivity(this);
    }


    /**
     * 简化findViewById()
     *
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T extends View> T mFindViewById(int id) {
        // return 返回view时，加上泛型T
        return (T) findViewById(id);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.getInstance().removeActivity(this);
    }

    /**
     * 某个activity变得“容易”被系统销毁时，该activity的onSaveInstanceState就会被执行，
     * 除非该activity是被用户主动销毁的。
     * (按下HOME键？长按HOME键，选择运行其他的程序?按下电源按键?activity切换？屏幕方向切换？)
     * 此方法常常用来做一些应用中非持久性的存储
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 该activity被恢复时执行(前提是该activity的确已被销毁,即此方法与onSaveInstanceState()方法不一定对等成对调用)
     * ,且savedInstanceState参数还会传递到onCreate()内。 此方法常常用来做一些应用中非持久性的存储的恢复。
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 此方法与清单中android:configChanges对等调用(不指定,配置改变时将重调用onCreate方法,指定后,
     * 指定情况下将不调用onCreate方法而来调用此方法,故你懂的啦)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * toast 短 int
     *
     * @param pResId 字符串的id
     */
    protected void showToast(int pResId) {
        showToast(getString(pResId));
    }

    /**
     * toast 长 int
     *
     * @param pResId 字符串的id
     */
    protected void showLongToast(int pResId) {
        showLongToast(getString(pResId));
    }

    /**
     * toast 短 String
     *
     * @param pMsg
     */
    protected void showToast(String pMsg) {
        if (toast == null) {
            initToast();
        }
        TextView view = (TextView) toast.getView();
        view.setText(pMsg);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * toast 长 string
     *
     * @param pMsg
     */
    protected void showLongToast(String pMsg) {
        if (toast == null) {
            initToast();
        }
        TextView view = (TextView) toast.getView();
        view.setText(pMsg);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    @SuppressLint("ShowToast")
    private void initToast() {
        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
    }

    /**
     * Activity跳转
     *
     * @param pClass
     */
    protected void openActivity(Class<?> pClass) {
        openActivity(pClass, null);
    }

    /**
     * Activity跳转
     *
     * @param pClass
     * @param pBundle
     */
    protected void openActivity(Class<?> pClass, Bundle pBundle) {
        openActivity(pClass, pBundle, null);
    }

    /**
     * Activity跳转
     *
     * @param pClass
     * @param pBundle
     * @param uri
     */
    protected void openActivity(Class<?> pClass, Bundle pBundle, Uri uri) {
        Intent intent = new Intent(this, pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
    }

    /**
     * Activity跳转
     *
     * @param pAction
     */
    protected void openActivity(String pAction) {
        openActivity(pAction, null);
    }

    /**
     * Activity跳转
     *
     * @param pAction
     * @param pBundle
     */
    protected void openActivity(String pAction, Bundle pBundle) {
        openActivity(pAction, pBundle, null);
    }

    /**
     * Activity跳转
     *
     * @param pAction
     * @param pBundle
     * @param uri
     */
    protected void openActivity(String pAction, Bundle pBundle, Uri uri) {
        Intent intent = new Intent(pAction);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
    }

    /**
     * intent
     *
     * @return
     */
    protected Intent getPrevIntent() {
        return getIntent();
    }

    /**
     * bundle
     *
     * @return
     */
    protected Bundle getPrevExtras() {
        return getPrevIntent().getExtras();
    }

    /** 关闭应用 */
    public void finishDefault() {
        finish();
    }


    /**
     * 检查当前网络是否可用
     * @return
     */

    protected boolean isNetworkAvailable(Activity activity) {
        Context context = activity.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
