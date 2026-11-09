package io.github.javiewer.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

public final class Intents {

    private Intents() {
    }

    public static boolean openUri(Context context, String uri) {
        if (context == null || uri == null || uri.trim().isEmpty()) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.trim()));
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                Toast.makeText(context, "没有可打开的应用", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }

    public static boolean isAlive(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
            return false;
        }
        return true;
    }
}
