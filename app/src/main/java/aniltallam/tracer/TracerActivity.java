package aniltallam.tracer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TracerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracer);
    }

    public void draw(View view){
        TracerView tracerView = (TracerView) findViewById(R.id.tview);
        tracerView.setData(TracerUtil.tracerData);
    }

    public void drawPrev(View view){
        TracerView tracerView = (TracerView) findViewById(R.id.tview);
        tracerView.setData(TracerUtil.prevTracerData);
    }

    public void clearPath(View view){
        TracerView tracerView = (TracerView) findViewById(R.id.tview);
        tracerView.clearPath();
    }

    public void clearPoints(View view){
        TracerView tracerView = (TracerView) findViewById(R.id.tview);
        tracerView.clearPoints();
    }
}
