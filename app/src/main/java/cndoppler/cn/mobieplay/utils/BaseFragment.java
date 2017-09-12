package cndoppler.cn.mobieplay.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2017/9/12 0012.
 */

public class BaseFragment extends Fragment {

    private BasePager pager;

    public BaseFragment(BasePager pager) {
        this.pager = pager;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (pager!=null){
            return pager.rootView;
        }
        return null;
    }
}
