package aniltallam.tracer;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vimeo.stag.generated.Stag;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anil on 7/10/16.
 */

public class CaptureUtil {
    public static TracerData previewData;

    static String tracerString, prevTracerString;
    public static void saveData(ArrayList<Point> points, ArrayList<Integer> strokes) {
//        prevTracerData = tracerData;
//        tracerData = new TracerData(points, strokes);

        prevTracerString = tracerString;
        tracerString = getGson().toJson(new TracerData(points, strokes), TracerData.class);
        Log.d("CaptureUtil", "Json data => \n " + tracerString);
    }

    public static String toJson(TracerData tracerData) {
        String temp = getGson().toJson(tracerData, TracerData.class);
        Log.d("CaptureUtil", "Json data => \n " + temp);
        return temp;
    }

    public static TracerData getData(){
        return getGson().fromJson(tracerString, TracerData.class);
    }

    public static TracerData getPrevData(){
        return getGson().fromJson(prevTracerString, TracerData.class);
    }

    private static Gson gson = null;

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapterFactory(new Stag.Factory())
                    .create();
        }
        return gson;
    }
}
