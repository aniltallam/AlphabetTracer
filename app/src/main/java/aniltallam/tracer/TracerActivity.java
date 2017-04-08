package aniltallam.tracer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TracerActivity extends AppCompatActivity {
    TracerView tracerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracer);

        tracerView = (TracerView) findViewById(R.id.tview);
        tracerView.setData(CaptureUtil.previewData);
    }

    public void draw(View view){
        tracerView.setData(CaptureUtil.previewData);
    }

    public void drawPrev(View view){
        //tracerView.setData(TracerUtil.getPrevData());
    }

    public void clearPath(View view){
        tracerView.clearPath();
    }

    public void clearPoints(View view){
        tracerView.clearPoints();
    }
}
