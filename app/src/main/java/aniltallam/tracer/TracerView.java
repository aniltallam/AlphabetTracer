package aniltallam.tracer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by anil on 7/9/16.
 */
public class TracerView extends View {
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath, arrowPath;
    private Paint mPathPaint, arrowPaint, mPointsPaint, mSpecialPointPaint, mBitmapPaint;

    TracerDataHandler dataHandler;

    public TracerView(Context context) {
        super(context);
        init();
    }

    public TracerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TracerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mCanvas = new Canvas();
        mPath = new Path();
        arrowPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setColor(Color.GRAY);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeWidth(12);

        mPointsPaint = new Paint();
        mPointsPaint.setAntiAlias(true);
        mPointsPaint.setDither(true);
        mPointsPaint.setColor(Color.BLACK);
        mPointsPaint.setStyle(Paint.Style.STROKE);
        mPointsPaint.setStrokeJoin(Paint.Join.ROUND);
        mPointsPaint.setStrokeCap(Paint.Cap.ROUND);
        mPointsPaint.setStrokeWidth(18);

        mSpecialPointPaint = new Paint(mPointsPaint);
        mSpecialPointPaint.setColor(Color.RED);

        arrowPaint = new Paint(mPathPaint);
        arrowPaint.setColor(Color.rgb(255, 165, 0));
        arrowPaint.setStrokeWidth(5);
    }

    public void setDataHandler(TracerDataHandler tracerDataHandler) {
        dataHandler = tracerDataHandler;
        invalidate();
    }

    boolean pathStarted = false;

    private void touch_start(float x, float y) {
        if (dataHandler != null && dataHandler.hasUndrawnStroke()) {

            TracerDataHandler.Stroke currStroke = dataHandler.getCurrStroke();
            if (currStroke.hasUnconnectedPoint() && currStroke.isCurrPoint(x, y)) {
                mPath.reset();
                pathStarted = true;
            }
        }
    }

    private void touch_move(float x, float y) {
        if (pathStarted) {
            TracerDataHandler.Stroke currStroke = dataHandler.getCurrStroke();
            if (currStroke.isNextPoint(x, y)) {
                mPath.reset();
                mPath.moveTo(currStroke.getCurrPoint().x, currStroke.getCurrPoint().y);
                mPath.lineTo(currStroke.getNextPoint().x, currStroke.getNextPoint().y);
                mCanvas.drawPath(mPath, mPathPaint); //save to bitmap
                mPath.reset();

                currStroke.moveToNextPoint();
                if (!currStroke.hasUnconnectedPoint()) {
                    dataHandler.moveToNextStroke();
                    pathStarted = false;
                }
            } else {
                mPath.reset();
                mPath.moveTo(currStroke.getCurrPoint().x, currStroke.getCurrPoint().y);
                mPath.lineTo(x, y);
            }
        }
    }

    private void touch_up() {
        if (pathStarted) {
            mPath.reset();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPathPaint);

        if (dataHandler != null) {
            for (Point point : dataHandler.getAllPoints()) {
                canvas.drawPoint(point.x, point.y, mPointsPaint);
            }
            if (dataHandler.hasUndrawnStroke() && dataHandler.getCurrStroke().hasUnconnectedPoint()) {
                Point cp = dataHandler.getCurrStroke().getCurrPoint();
                canvas.drawPoint(cp.x, cp.y, mSpecialPointPaint);

                Point np = dataHandler.getCurrStroke().getNextPoint();

                fillArrow(canvas, cp.x,cp.y,np.x,np.y);
            }
        }
    }

    private void fillArrow(Canvas canvas, float x0, float y0, float x1, float y1) {

        int arrowHeadLenght = 20;
        int arrowHeadAngle = 30;
        float[] linePts = new float[] {x1 - arrowHeadLenght, y1, x1, y1};
        float[] linePts2 = new float[] {x1, y1, x1, y1 + arrowHeadLenght};
        Matrix rotateMat = new Matrix();

        //get the center of the line
        float centerX = x1;
        float centerY = y1;

        //set the angle
        double angle = Math.atan2(y1 - y0, x1 - x0) * 180 / Math.PI + arrowHeadAngle;

        //rotate the matrix around the center
        rotateMat.setRotate((float) angle, centerX, centerY);
        rotateMat.mapPoints(linePts);
        rotateMat.mapPoints(linePts2);

        arrowPath.reset();
        arrowPath.moveTo(x0, y0);
        arrowPath.lineTo(x1, y1);

        arrowPath.moveTo(linePts [0], linePts [1]);
        arrowPath.lineTo(linePts [2], linePts [3]);

//        arrowPath.moveTo(linePts2 [0], linePts2 [1]);
//        arrowPath.lineTo(linePts2 [2], linePts2 [3]);
        arrowPath.offset(15, -15);
        canvas.drawPath(arrowPath, arrowPaint);
    }

    public void clear() {
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        invalidate();
    }

    public void clearPath() {
        if (dataHandler != null) {
            dataHandler.rewind();
        }
        clear();
    }

    public void clearPoints() {
        dataHandler = null;
        clearPath();
    }


}
