package com.example.u.dressup;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import java.io.ByteArrayOutputStream;
import android.support.v7.widget.ShareActionProvider;
import android.widget.Toast;
import android.os.Environment;
import android.graphics.Bitmap.CompressFormat;
import android.content.Context;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class MainActivity extends ActionBarActivity implements LongPressCB, ShareActionProvider.OnShareTargetSelectedListener, SelfieUpdateCB, ServerInterface.ImageLoadedCB
{

    private static final String TAG = "Dressup::MainActivity";

    private static ImageView mySelfyView, myDressView;
    private static DressedupView myMergedView;
    private static final int SELECT_PHOTO = 100;
    private static final int TAKE_PICTURE = 101;
    private static final int SELECT_DRESS = 102;

    private static int mySelectionImageNo = 0;

    private static Bitmap mySelfieBmp, myDressBmp;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void modifyImageView(ImageView aView_in)
    {
        aView_in.setColorFilter(0x7700FF00, PorterDuff.Mode.MULTIPLY);

        aView_in.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView) v;
                        //overlay is black with transparency of 0x77 (119)
                        view.getDrawable().setColorFilter(0x77000000,PorterDuff.Mode.SRC_ATOP);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        //clear the overlay
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }

                return false;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mySelfyView = (ImageView) MainActivity.this.findViewById(R.id.imageSelfie);
        myDressView = (ImageView) MainActivity.this.findViewById(R.id.imageDress);
        myMergedView = (DressedupView) MainActivity.this.findViewById(R.id.imageMerged);

        //mySelfyView.setColorFilter(0x7700FF00, PorterDuff.Mode.MULTIPLY);
        //myDressView.setColorFilter(0x7700FF00, PorterDuff.Mode.MULTIPLY);

        modifyImageView(mySelfyView);
        modifyImageView(myDressView);

        shareIntent.setType("image/*");

        run();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        cleanDir(getCacheDir());
    }
    @Override
    public void press()
    {
        Intent anIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        anIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        startActivityForResult(anIntent, TAKE_PICTURE);
    }
    private void run()
    {
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        myMergedView.setCurrentActivityDims(screenSize);

        myMergedView.registerPressCB(this);
        myMergedView.registerSelfieUpdateCB(this);

        mySelfyView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                mySelectionImageNo = 1;

            }
        });
        myDressView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                /*Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);*/
                Intent intent = new Intent(MainActivity.this, ArmoireDemoActivity.class);
                startActivityForResult(intent, SELECT_DRESS);

                mySelectionImageNo = 2;
            }
        });

    }

    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent=new Intent(Intent.ACTION_SEND);

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //getMenuInflater().inflate(R.menu.share_menu, menu);



        MenuItem item = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(item);

        mShareActionProvider.setOnShareTargetSelectedListener(this);

        return(super.onCreateOptionsMenu(menu));
        //return true;
    }
    @Override
    public boolean onShareTargetSelected(ShareActionProvider source, Intent intent)
    {
        /*Toast.makeText(this, intent.getComponent().toString(),
                Toast.LENGTH_LONG).show();*/

        if (mShareActionProvider != null)
        {
            //mShareActionProvider.setShareIntent(shareIntent);
            //setShareIntent(intent);
        }

        return(false);
    }

    private Timer myTimer = new Timer();
    private Runnable Timer_Tick = new Runnable() {
        public void run() {

            //This method runs in the same thread as the UI.

            //Do something to the UI thread here

            setShareIntent();

        }
    };


    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        Log.i(TAG, "in DressedupView::TimerMethod myCBCameAgain = " + myCBCameAgain);
        if(myCBCameAgain)
        {
            myCBCameAgain = false;
            triggerTimer();
        }
        else
        {
            myTimerTriggered = false;
            this.runOnUiThread(Timer_Tick);
        }
    }

    private void triggerTimer()
    {
        myTimerTriggered = true;
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 1000);
    }


    boolean myCBCameAgain = false;
    boolean myTimerTriggered = false;
    public void selfieUpdated()
    {
        Log.i(TAG, "in DressedupView::selfieUpdated myCBCameAgain = " + myCBCameAgain);

        if(myTimerTriggered == false)
            triggerTimer();
        else
            myCBCameAgain = true;

        if (mShareActionProvider != null)
        {
            //mShareActionProvider.setShareIntent(shareIntent);
            //setShareIntent(shareIntent);
        }
    }

    private static void cleanDir(File dir)
    {
        File[] files = dir.listFiles();

        for (File file : files)
        {
            file.delete();
        }
    }
    // Call to update the share intent
    private void setShareIntent()
    {
        if (mShareActionProvider != null)
        {
            View content = findViewById(R.id.imageMerged);
            content.setDrawingCacheEnabled(true);

            Bitmap bitmap = content.getDrawingCache();

            try
            {
                File cachePath = File.createTempFile("SelfieDressed", "jpg");

                FileOutputStream ostream = new FileOutputStream(cachePath);
                bitmap.compress(CompressFormat.JPEG, 100, ostream);

                ostream.flush();

                cachePath.setReadable(true, false);
                ostream.close();

                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(cachePath));
                mShareActionProvider.setShareIntent(shareIntent);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            content.setDrawingCacheEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent call = new Intent(MainActivity.this, null);
            call.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            call.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(call);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent newIntent) {

        super.onNewIntent(newIntent);

        run();
    }


    void setSelfie(Bitmap aBMP_in)
    {
        mySelfyView.setImageBitmap(aBMP_in);
        //mySelfieBmp = aBMP_in;
        mySelfyView.invalidate();

        myMergedView.setImageBitmap(aBMP_in);
        myMergedView.invalidate();
        myMergedView.setSelfieBMP(aBMP_in);
    }

    private void setDress(Bitmap aDressBitmap_in)
    {
        myDressView.setImageBitmap(aDressBitmap_in);
        myDressBmp = aDressBitmap_in;
        myDressView.invalidate();
        myMergedView.setDressBMP(myDressBmp);

        myCBCameAgain = false;
        myTimerTriggered = false;

        setShareIntent();
    }
    public void imageLoaded(Bitmap result)
    {
        result = Bitmap.createScaledBitmap(result, 200, 300, true);
        setDress(result);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode)
        {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK)
                {
                    Uri selectedImage = imageReturnedIntent.getData();

                    InputStream imageStream = null;
                    try
                    {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    }
                    catch (FileNotFoundException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    BitmapFactory.Options bitmapSize = new BitmapFactory.Options();
                    bitmapSize.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(imageStream, null, bitmapSize);
                    try
                    {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    Log.i("onActivityResult", "Original bitmap size is = [" + bitmapSize.outHeight + ", " + bitmapSize.outWidth + "]");

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.outHeight = 300;
                    //options.outWidth = 200;
                    options.inSampleSize = Math.min(bitmapSize.outHeight / 300, bitmapSize.outWidth / 200);
                    //options.inJustDecodeBounds = false;
                    Log.i("onActivityResult", "options.inSampleSize is = [" + options.inSampleSize + "]");

                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream, null, options);
                    yourSelectedImage = Bitmap.createScaledBitmap(yourSelectedImage, 200, 300, true);

                    if (mySelectionImageNo == 1)
                    {
                        setSelfie(yourSelectedImage);
                    }
                    else if (mySelectionImageNo == 2)
                    {
                        /*myDressView.setImageBitmap(yourSelectedImage);
                        myDressBmp = yourSelectedImage;
                        myDressView.invalidate();
                        myMergedView.setDressBMP(myDressBmp);

                        myCBCameAgain = false;
                        myTimerTriggered = false;

                        setShareIntent();*/
                        setDress(yourSelectedImage);

                    }
                }
                break;
            case SELECT_DRESS:

                String aDressURL = imageReturnedIntent.getStringExtra("DRESS_URL");
                Log.i("onActivityResult", "Selected Dress URL is = " + aDressURL);

                try
                {
                    URL url = new URL(aDressURL);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.connect();
                    int file_size = urlConnection.getContentLength();
                    Log.i("onActivityResult", "Size of " + aDressURL + " is : " + file_size);
                }
                catch (MalformedURLException e)
                {
                    Log.i("onActivityResult", "MalformedURLException. " + aDressURL);
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                new ServerInterface.ImageLoadTask(aDressURL, this).execute();

                break;
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK)
                {
                    Bitmap yourSelectedImage = (Bitmap)imageReturnedIntent.getExtras().get("data");
                    yourSelectedImage = Bitmap.createScaledBitmap(yourSelectedImage, 200, 300, true);

                    setSelfie(yourSelectedImage);
                }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, this, mLoaderCallback);
    }
}
