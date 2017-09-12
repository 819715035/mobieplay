package cndoppler.cn.mobieplay;

import android.os.Handler;
import android.view.MotionEvent;

import cndoppler.cn.mobieplay.utils.BaseActivity;

public class WelcomActivity extends BaseActivity {

    private Handler handler = new Handler();
    private boolean isMainActivity = false;
    @Override
    public void setContent() {
        setContentView(R.layout.activity_welcom);
    }

    @Override
    public void initWidget() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //开始跳转
                startMainActivity();
            }
        },2000);
    }

    private void startMainActivity() {
        if (!isMainActivity){
            isMainActivity = true;
            openActivity(MainActivity.class);
            finish();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startMainActivity();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
