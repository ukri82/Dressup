package com.example.u.dressup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import org.opencv.android.Utils;

import android.graphics.Point;


import android.view.ScaleGestureDetector;
import android.view.GestureDetector;
import android.support.v4.view.GestureDetectorCompat;
import android.graphics.RectF;

import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;

interface ImageTransformCB
{
    public void zoom();
    public void pan();
}

interface LongPressCB
{
    public void press();
}

/**
 * TODO: document your custom view class.
 */
public class DressedupView extends ImageView implements ImageTransformCB {

    private static final String TAG = "Dressup::DressedupView";

    private ScaleGestureDetector myScaleDetector;
    private ScaleListener myListener;
    private GestureDetectorCompat myGestureDetector;
    private GestureListener myGestureListener;

    private Mat mySelfieMat;
    private Mat myDressMat;

    private Mat myDressOnlyMat;
    private Mat myDressOnlyMatGrayScale;


    private Bitmap myMergedBMP;



    public DressedupView(Context context) {
        super(context);

        init(context);
    }
    public DressedupView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public void init(Context context)
    {
        Log.i(TAG, "in DressedupView::context");
        myListener = new ScaleListener();
        myListener.registerCB(this);
        myScaleDetector = new ScaleGestureDetector(context, myListener);

        myGestureListener = new GestureListener();
        myGestureListener.registerCB(this);
        myGestureDetector = new GestureDetectorCompat(context, myGestureListener);

    }

    public void registerPressCB(LongPressCB aCB_in)
    {
        if(myGestureListener != null)
        {
            myGestureListener.registerCB(aCB_in);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }

    public void setSelfieBMP(Bitmap aBMP_in)
    {
        mySelfieMat = new Mat();
        Utils.bitmapToMat(aBMP_in, mySelfieMat);

        mySelfieMat.convertTo(mySelfieMat, -1, 1, 30);
    }

    public void setDressBMP(Bitmap aBMP_in)
    {
        myDressOnlyMat = null;
        myDressMat = null;
        myDressOnlyMatGrayScale = null;
        myMergedBMP = null;

        myDressMat = new Mat();
        Utils.bitmapToMat(aBMP_in, myDressMat);
        myListener.reset();
        myGestureListener.reset();
        initializeImages();
        //updateDisplay();
    }

    @Override
    public void zoom()
    {
        updateDisplay();
    }
    @Override
    public void pan()
    {
        updateDisplay();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        // Let the ScaleGestureDetector inspect all events.
        myScaleDetector.onTouchEvent(ev);
        myGestureDetector.onTouchEvent(ev);
        return true;
    }


    public void initializeImages()
    {
        /*if (myDressMat != null)
        {
            myDressOnlyMatGrayScale = Mat.zeros(myDressMat.size(), CvType.CV_8U);
            Imgproc.cvtColor(myDressMat, myDressOnlyMatGrayScale, Imgproc.COLOR_BGRA2GRAY);

            ImageProcessor anImageProc = new ImageProcessor(myDressOnlyMatGrayScale);
            anImageProc.regionGrow();

            myDressOnlyMatGrayScale.put(0, 0, anImageProc.getVisitedMatrix());
            Mat aDressOnlyMatGrayScaleInv = Mat.zeros(myDressOnlyMatGrayScale.size(), myDressOnlyMatGrayScale.type());
            aDressOnlyMatGrayScaleInv.setTo(new Scalar(255));

            Core.subtract(aDressOnlyMatGrayScaleInv, myDressOnlyMatGrayScale, aDressOnlyMatGrayScaleInv);


            myDressOnlyMat = Mat.zeros(myDressMat.size(), myDressMat.type());
            myDressMat.copyTo(myDressOnlyMat, aDressOnlyMatGrayScaleInv);

            myMergedBMP = Bitmap.createBitmap(myDressMat.cols(), myDressMat.rows(), Bitmap.Config.ARGB_8888);


        }*/


        if (myDressMat != null)
        {
            myDressOnlyMatGrayScale = Mat.zeros(myDressMat.size(), CvType.CV_8U);
            Imgproc.cvtColor(myDressMat, myDressOnlyMatGrayScale, Imgproc.COLOR_BGR2GRAY);

            ImageProcessor anImageProc = new ImageProcessor(myDressOnlyMatGrayScale);
            anImageProc.regionGrow();

            myDressOnlyMatGrayScale = anImageProc.getContourImage();
            myDressOnlyMat = anImageProc.getContourImage();
            //Mat aDressOnlyMatGrayScaleInv = new Mat(myDressOnlyMatGrayScale.size(), myDressOnlyMatGrayScale.type(), new Scalar(255));

            //Core.subtract(aDressOnlyMatGrayScaleInv, myDressOnlyMatGrayScale, aDressOnlyMatGrayScaleInv);


            //myDressOnlyMat = new Mat(myDressMat.size(), myDressMat.type());
            //myDressMat.copyTo(myDressOnlyMat, aDressOnlyMatGrayScaleInv);

            myMergedBMP = Bitmap.createBitmap(myDressMat.cols(), myDressMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(myDressOnlyMat, myMergedBMP);
            this.setImageBitmap(myMergedBMP);
            this.invalidate();
        }
    }

    public Mat getDressTransformationMatrix()
    {
        //Log.i(TAG, "Current Zoom is : (" + myListener.getXZoom() + ", " + myListener.getYZoom() + ")");
        //Log.i(TAG, "Current Pan is : " + myGestureListener.getXPan() + ", " + myGestureListener.getYPan());

        Mat aTransformMatrix = new Mat(2, 3, CvType.CV_32FC1);
        double[] aTransMat = new double[6];
        aTransMat[0] = myListener.getXZoom();
        aTransMat[4] = myListener.getYZoom();

        int aCenterX = myDressOnlyMat.cols() / 2;
        int aScaledCenterX = (int) (myDressOnlyMat.cols() * myListener.getXZoom() / 2);
        int aCenterDiffX = aCenterX - aScaledCenterX;

        int aCenterY = myDressOnlyMat.rows() / 2;
        int aScaledCenterY = (int) (myDressOnlyMat.rows() * myListener.getYZoom() / 2);
        int aCenterDiffY = aCenterY - aScaledCenterY;

        int aPanX = (int) (myGestureListener.getXPan() * myListener.getXZoom());
        int aPanY = (int) (myGestureListener.getYPan() * myListener.getYZoom());

        aTransMat[2] = aCenterDiffX + aPanX;
        aTransMat[5] = aCenterDiffY + aPanY;

        aTransformMatrix.put(0, 0, aTransMat);

        //Log.i(TAG, "Current Matrix is : [" + aTransMat[0] + "," + aTransMat[1] + "," + aTransMat[2] + "," + aTransMat[3] + "," + aTransMat[4] + "," + aTransMat[5] + "]");

        return aTransformMatrix;
    }
    public void updateDisplay()
    {
        if (mySelfieMat != null && myDressOnlyMat != null) {
            Mat aTransformMatrix = getDressTransformationMatrix();

            Mat aTransformedDressImage = Mat.zeros(myDressOnlyMat.size(), myDressOnlyMat.type());

            Imgproc.warpAffine(myDressOnlyMat, aTransformedDressImage, aTransformMatrix, myDressOnlyMat.size());


            Mat aTransformedDressContourImage = Mat.zeros(myDressOnlyMat.size(), myDressOnlyMat.type());
            Imgproc.warpAffine(myDressOnlyMatGrayScale, aTransformedDressContourImage, aTransformMatrix, myDressOnlyMat.size(), Imgproc.INTER_LINEAR, Imgproc.BORDER_CONSTANT, new Scalar(255));

            Mat aPartialSelfieImage = Mat.zeros(myDressOnlyMat.size(), mySelfieMat.type());
            mySelfieMat.copyTo(aPartialSelfieImage, aTransformedDressContourImage);

            Mat aBlendedImage = Mat.zeros(myDressOnlyMat.size(), mySelfieMat.type());
            Core.addWeighted(aPartialSelfieImage, 1, aTransformedDressImage, 1, 0, aBlendedImage);


            Utils.matToBitmap(aBlendedImage, myMergedBMP);

            this.setImageBitmap(myMergedBMP);
            this.invalidate();
        }
    }


    public void setCurrentActivityDims(Point anActivityDims)
    {
        Log.i(TAG, "in DressedupView::setCurrentActivityDims");
        myGestureListener.setCurrentActivityDims(anActivityDims);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        myGestureListener.setViewDims(new Point(w, h));
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {

        private float myXZoom = 1.f;
        private float myYZoom = 1.f;

        private ImageTransformCB myTransformCB;


        public void registerCB(ImageTransformCB aCB_in)
        {
            myTransformCB = aCB_in;
        }


        public void reset()
        {
            myXZoom = 1.f;
            myYZoom = 1.f;
        }

        public float getXZoom()
        {
            return myXZoom;
        }
        public float getYZoom()
        {
            return myYZoom;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            //Log.i(TAG, "in DressedupView::onScale");

            //mScaleFactor *= detector.getScaleFactor();

            double anXZoom = detector.getCurrentSpanX() / detector.getPreviousSpanX();
            myXZoom *= anXZoom;

            // Don't let the object get too small or too large.
            myXZoom = Math.max(0.1f, Math.min(myXZoom, 5.0f));

            double anYZoom = detector.getCurrentSpanY() / detector.getPreviousSpanY();
            myYZoom *= anYZoom;

            // Don't let the object get too small or too large.
            myYZoom = Math.max(0.1f, Math.min(myYZoom, 5.0f));

            if(myTransformCB != null)
            {
                myTransformCB.zoom();
            }
            return true;
        }
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {

        private ImageTransformCB myTransformCB;
        private LongPressCB myPressCB;
        private int myXPan = 0;
        private int myYPan = 0;
        private Point myCurrentActivityDims;
        private Point myCurrentViewDims;

        public void registerCB(ImageTransformCB aCB_in)
        {
            myTransformCB = aCB_in;
        }
        public void registerCB(LongPressCB aCB_in)
        {
            myPressCB = aCB_in;
        }
        public void reset()
        {
            myXPan = 0;
            myYPan = 0;
        }

        public float getXPan()
        {
            return myXPan;
        }

        public float getYPan()
        {
            return myYPan;
        }

        public void setCurrentActivityDims(Point anActivityDims)
        {
            myCurrentActivityDims = anActivityDims;
        }

        public void setViewDims(Point aViewDims)
        {
            myCurrentViewDims = aViewDims;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            //Log.e("", "OnScroll: deltaX=" + String.valueOf(e2.getX() - e1.getX()) + ", deltaY=" + String.valueOf(e2.getY() - e1.getY()));

            distanceX = distanceX * myCurrentViewDims.x / myCurrentActivityDims.x;
            distanceY = distanceY * myCurrentViewDims.y / myCurrentActivityDims.y;

            myXPan = myXPan - (int)(distanceX * 0.3);
            myYPan = myYPan - (int)(distanceY * 0.3);

            if(myTransformCB != null)
            {
                myTransformCB.pan();
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            Log.e("", "onSingleTapUp: X=" + String.valueOf(e.getX()) + ", Y=" + String.valueOf(e.getY()));
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e)
        {
            Log.e("", "onLongPress: X=" + String.valueOf(e.getX()) + ", Y=" + String.valueOf(e.getY()));
            if(myPressCB != null)
            {
                myPressCB.press();
            }
        }

        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }
    }
}
