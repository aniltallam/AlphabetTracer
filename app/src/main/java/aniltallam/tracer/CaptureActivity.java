package aniltallam.tracer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CaptureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
    }

    public void normal(View view){
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.setLineDrawStyle(0);
    }
    public void vert(View view){
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.setLineDrawStyle(1);
    }
    public void horiz(View view){
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.setLineDrawStyle(2);
    }
    public void breakS(View view){
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.breakStroke();
    }
    public void joinS(View view){
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.continueStroke();
    }
    public void delete(View view){
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.undo();
    }

    public void new1(View view){
        CurveCaptureView cview = (CurveCaptureView) findViewById(R.id.cview);
        cview.new1();
    }

    public void clear(View view){
        CurveCaptureView cview = (CurveCaptureView) findViewById(R.id.cview);
        cview.clear();
    }

    public void save(View view){
        CurveCaptureView cview = (CurveCaptureView) findViewById(R.id.cview);
        cview.save();

        Intent intent = new Intent(this, TracerActivity.class);
        startActivity(intent);
    }
}
