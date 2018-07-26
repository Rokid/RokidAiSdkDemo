package com.rokid.ai.sdkdemo.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.ai.sdkdemo.R;


/**
 * Create By HaiyuKing
 * Used 自定义Toast显示风格，基于系统Toast【可以控制显示样式、位置，不可以控制显示时间、动画，不可触发】
 * 注意 Toast布局在源码中的布局是采用LinearLayout
 */
public class RokidToast {

    private static RokidToast RokidToast;
    private Toast toast;

    public static RokidToast makeText(Context context, CharSequence text, int duration){
        LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.rokid_toast_view, null);
        TextView tv = (TextView)view.findViewById(R.id.rokid_toast);
        tv.setText(text);
            if (RokidToast == null) {
            RokidToast = new RokidToast();
        }
        RokidToast.toast = new Toast(context);
        RokidToast.toast.setView(view);
        RokidToast.toast.setDuration(duration);

        return RokidToast;
    }

    public static RokidToast makeText(Context context, int resId, int duration){
        return RokidToast.makeText(context,context.getResources().getString(resId),duration);
    }

    public void show(){
        toast.show();
    }

    /**
     * 1、gravity是输入Toast需要显示的位置，例如CENTER_VERTICAL（垂直居中）、CENTER_HORIZONTAL（水平居中）、TOP（顶部）等等。
     * 2、xOffset则是决定Toast在水平方向（x轴）的偏移量，偏移量单位为，大于0向右偏移，小于0向左偏移
     * 3、yOffset决定Toast在垂直方向（y轴）的偏移量，大于0向下偏移，小于0向上偏移，想设大值也没关系，反正Toast不会跑出屏幕。*/
    public void setGravity(int gravity, int xOffset, int yOffset) {
        toast.setGravity(gravity, xOffset, yOffset);
    }

    public void setText(CharSequence s){
        TextView tv = (TextView) toast.getView().findViewById(R.id.rokid_toast);
        tv.setText(s);
    }

    public void setText(int resId){
        TextView tv = (TextView) toast.getView().findViewById(R.id.rokid_toast);
        tv.setText(resId);
    }
}
