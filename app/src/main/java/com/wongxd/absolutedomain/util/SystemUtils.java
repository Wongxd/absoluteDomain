package com.wongxd.absolutedomain.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.ClipboardManager;

import java.util.List;

/**
 * Created by Xiamin on 2017/2/12.
 */

public class SystemUtils {
    private SystemUtils() {

    }

    public static void share(Context context, String text, String title) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, title));
    }

    public static void copyText(Context context, String text) {
        ClipboardManager c = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
        c.setText(text);
    }

    public static void openUrlByBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    /**
     * 判断当前应用是否在后台
     */
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {  
                /* 
                BACKGROUND=400 EMPTY=500 FOREGROUND=100 
                GONE=1000 PERCEPTIBLE=130 SERVICE=300 ISIBLE=200 
                 */
//                Log.i(context.getPackageName(), "此appimportace ="
//                        + appProcess.importance
//                        + ",context.getClass().getName()="
//                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                    Log.i(context.getPackageName(), "处于后台"
//                            + appProcess.processName);
//                    ToastUtil.cT(context, context.getPackageName() + "--" + appProcess.processName + "处于后台");
                    return true;
                } else {
//                    Log.i(context.getPackageName(), "处于前台"
//                            + appProcess.processName);
//                    ToastUtil.cT(context, context.getPackageName() + "--" + appProcess.processName + "处于前台");
                    return false;
                }
            }
        }
        return false;
    }
}