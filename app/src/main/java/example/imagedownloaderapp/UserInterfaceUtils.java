package example.imagedownloaderapp;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by acc on 2016-12-17.
 */
public class UserInterfaceUtils
{
    protected static String TAG = String.valueOf(FileUtils.class);

    private UserInterfaceUtils() {}

    public static void showToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboard(Activity activity, IBinder windowToken)
    {
        InputMethodManager mgr = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }
}
