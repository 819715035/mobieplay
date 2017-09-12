package cndoppler.cn.mobieplay.utils;

import android.app.Activity;
import android.app.Application;

import java.util.Stack;

/**
 * Created by Administrator on 2017/8/28 0028.
 */

public class BaseApplication extends Application {
    public static BaseApplication application;


    private Stack<Activity> activityStack;

    public static BaseApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        application = this;
        //CrashHandler.getInstance().init(this);
    }


    /***************************** activity管理 start **********************************/

    /** add Activity 添加Activity到栈 */
    public synchronized void addActivity(Activity activity) {

        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);

    }

    /** get current Activity 获取当前Activity（栈中最后一个压入的） */
    public synchronized Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /** 结束当前Activity（栈中最后一个压入的） */
    public synchronized void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /** 结束指定的Activity */
    public synchronized void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    public synchronized void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
        }
    }

    /** 结束指定类名的Activity */
    public synchronized void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
                return;
            }
        }
    }

    /** 结束所有Activity */
    public synchronized void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();

    }

    /** 退出应用程序 */
    public void AppExit() {
        try {
            finishAllActivity();
        } catch (Exception e) {
        }
    }

    /***************************** activity管理 end **********************************/

}
