package constants;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Html;

/**
 * Created by eashaan on 1/6/17.
 */

public class Methods {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String fromHtml(String htmlDescription){
        if ((Build.VERSION.SDK_INT) >= 24) {
            return Html.fromHtml(htmlDescription, Html.FROM_HTML_MODE_LEGACY).toString(); // SDK >= Android N
        } else {
            return Html.fromHtml(htmlDescription).toString();
        }
    }
}
