package aniltallam.tracer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CaptureActivity extends AppCompatActivity {
    CurveCaptureView cview;
    TextView letterTxt;
    Button letterBtn;
    CharSequence[] letters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        letterBtn = (Button) findViewById(R.id.letterBtn);
        letterTxt = (TextView) findViewById(R.id.letterTxt);

        cview = (CurveCaptureView) findViewById(R.id.cview);
        fillLetters();
        setLetter(0);
    }

    private void fillLetters() {
        letters = new CharSequence[53];
        for (int i = 0; i < letters.length; i++) {
            char c = (char) (2309 + i);
            letters[i] = "" + c;
        }
    }

    private void setLetter(int pos) {
        letterTxt.setText(letters[pos]);
        letterBtn.setText(letters[pos]);

        cview.new1();
    }

    public void changeLetter(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose letter")
                .setItems(letters, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setLetter(which);
                    }
                })
                .show();
    }

    public void new1(View view) {
        cview.new1();
    }

    public void clear(View view) {
        cview.clear();
    }

    public void preview(View view) {
        CaptureUtil.previewData = cview.getData();
        TracerUtil.calculateMinMax(CaptureUtil.previewData);

        Intent intent = new Intent(this, TracerActivity.class);
        startActivity(intent);
    }

    public void email(View view) {
        TracerData tracerData = cview.getData();
        TracerUtil.calculateMinMax(tracerData);

        String letter = letterTxt.getText().toString();
        String data = CaptureUtil.toJson(tracerData);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Tracer Data: " + letter);
        emailIntent.putExtra(Intent.EXTRA_TEXT, data);

        //emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(listVideos.get(position).getVideoPath())));//path of video
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public void save(View view) {
        cview.save();

        Intent intent = new Intent(this, TracerActivity.class);
        startActivity(intent);
    }

    // Temp
    public void normal(View view) {
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.setLineDrawStyle(0);
    }

    public void vert(View view) {
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.setLineDrawStyle(1);
    }

    public void horiz(View view) {
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.setLineDrawStyle(2);
    }

    public void breakS(View view) {
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.breakStroke();
    }

    public void joinS(View view) {
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.continueStroke();
    }

    public void delete(View view) {
        CaptureView cview = (CaptureView) findViewById(R.id.cview);
        cview.undo();
    }
}
