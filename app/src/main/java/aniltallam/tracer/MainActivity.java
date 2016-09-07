package aniltallam.tracer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void draw(View view){
        ConnectDotsView cdview = (ConnectDotsView) findViewById(R.id.cdview);
        cdview.fillPoints();
    }
    public void clearPath(View view){
        ConnectDotsView cdview = (ConnectDotsView) findViewById(R.id.cdview);
        cdview.clearPath();
    }

    public void clearPoints(View view){
        ConnectDotsView cdview = (ConnectDotsView) findViewById(R.id.cdview);
        cdview.clearPoints();
    }
}
