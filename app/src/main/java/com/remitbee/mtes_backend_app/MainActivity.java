package com.remitbee.mtes_backend_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.remitbee.mtes_backend_app.model.User;
import com.remitbee.mtes_backend_app.parsers.UserJSONParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends ActionBarActivity {
    private ProgressDialog pd = null;
    User user_detail = new User("","","","","","","","","","","","","","","");

    TextView name;
    TextView email;
    TextView phone;
    TextView unique_id;

    TextView name_lbl;
    TextView email_lbl;
    TextView phone_lbl;
    TextView unique_id_lbl;

    ImageView cus_id_1;
    ImageView cus_id_2;
    ImageView cus_id_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_scan_qr_code = (Button) findViewById(R.id.btn_scan_qr_code);

        unique_id_lbl = (TextView) findViewById(R.id.label_id);
        name_lbl = (TextView) findViewById(R.id.label_name);
        phone_lbl = (TextView) findViewById(R.id.label_phone);
        email_lbl = (TextView) findViewById(R.id.label_email);

        unique_id = (TextView) findViewById(R.id.content_id);
        name = (TextView) findViewById(R.id.content_name);
        phone = (TextView) findViewById(R.id.content_phone);
        email = (TextView) findViewById(R.id.content_email);

        cus_id_1 = (ImageView) findViewById(R.id.cus_id_1);
        cus_id_2 = (ImageView) findViewById(R.id.cus_id_2);
        cus_id_3 = (ImageView) findViewById(R.id.cus_id_3);

        btn_scan_qr_code.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                        integrator.initiateScan();
                    }
                }
        );


        cus_id_1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                zoomImageFromThumb(cus_id_1, cus_id_1.getDrawable());
            }
        });
        cus_id_2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                zoomImageFromThumb(cus_id_2, cus_id_2.getDrawable());
            }
        });
        cus_id_3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                zoomImageFromThumb(cus_id_3, cus_id_3.getDrawable());
            }
        });

    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        System.out.println(scanResult);
        if (scanResult.getContents() != null) {
            // Check validity of the QR code:
            String qr_code = scanResult.getContents();
            // Process transaction:
            if (isOnline()) {
                String uri = "https://mtesapp.azurewebsites.net/backend"; // Get App specific constants
                RequestPackage p = new RequestPackage();
                p.setMethod("GET");
                p.setUri(uri);
                p.setParams("method", "check_qr_code");
                p.setParams("format", "json");
                p.setParams("qr_code", qr_code);

                new AsyncTaskCheckQRCode(MainActivity.this).execute(p);
            } else {
                Toast.makeText(getApplicationContext(), "Network isn't available", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "No QR Code found", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public class AsyncTaskCheckQRCode extends AsyncTask<RequestPackage, String, String> {
        Context context;
        public AsyncTaskCheckQRCode(Context context){
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            Log.v("a", "START");
            // Show the ProgressDialog on this thread
            MainActivity.this.pd = ProgressDialog.show(context, "Processing...", "Validating Code", true, false);
        }

        @Override
        protected String doInBackground(RequestPackage... params) {
            String content = HttpManager.getData(params[0]);
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            user_detail = UserJSONParser.parseUserProfileFeed(result);

            if(user_detail.getCus_unique_id()!=""){
                Toast.makeText(getApplicationContext(), user_detail.getCus_unique_id(), Toast.LENGTH_LONG).show();

                // Set basic detail:
                unique_id.setText(user_detail.getCus_unique_id());
                name.setText(user_detail.getCus_firstname() + " " + user_detail.getCus_lastname());
                email.setText(user_detail.getCus_email());
                phone.setText(user_detail.getCus_phone1() + " / " + user_detail.getCus_phone2());

                unique_id_lbl.setText("Unique ID");
                name_lbl.setText("Name");
                email_lbl.setText("Email");
                phone_lbl.setText("Phone");

                // Set Images:
                if(!user_detail.getCus_id_1().equals("")){
                    new RetrievePic("id_1").execute(user_detail.getCus_id_1());
                }
                if(!user_detail.getCus_id_2().equals("")){
                    new RetrievePic("id_2").execute(user_detail.getCus_id_2());
                }
                if(!user_detail.getCus_id_3().equals("")){
                    new RetrievePic("id_3").execute(user_detail.getCus_id_3());
                }
            }
            else{
                Toast.makeText(getApplicationContext(), "Please try again", Toast.LENGTH_LONG).show();
            }

            if (MainActivity.this.pd != null) {
                MainActivity.this.pd.dismiss();
            }
        }
    }


    class RetrievePic extends AsyncTask<String, Void, Bitmap> {
        String trigger;
        public RetrievePic(String which_pic){
            trigger = which_pic;
        }

        private Exception exception;
        protected Bitmap doInBackground(String... urls) {
            try {
                URL newurl = null;
                try {
                    newurl = new URL(urls[0]);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                assert newurl != null;
                Bitmap mIcon_val = null;
                    try {
                    URLConnection connection = newurl.openConnection();
                    connection.setUseCaches(true);
                    mIcon_val = BitmapFactory.decodeStream(connection.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return mIcon_val;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            if(trigger.equals("id_1")){
                cus_id_1.setImageBitmap(result);
            }
            else if(trigger.equals("id_2")){
                cus_id_2.setImageBitmap(result);
            }
            else if(trigger.equals("id_3")){
                cus_id_3.setImageBitmap(result);
            }
        }
    }

    protected boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if(netInfo != null && netInfo.isConnectedOrConnecting()){
            return true;
        }
        else{
            return false;
        }
    }




    // Hold a reference to the current animator,
    // so that it can be canceled mid-way.
    private Animator mCurrentAnimator;

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private int mShortAnimationDuration=100;
    //image zoom

    private void zoomImageFromThumb(final View thumbView, Drawable imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        final ImageView expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        expandedImageView.setImageDrawable(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}
