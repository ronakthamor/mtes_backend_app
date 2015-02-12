package com.remitbee.mtes_backend_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.remitbee.mtes_backend_app.model.User;
import com.remitbee.mtes_backend_app.parsers.UserJSONParser;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends ActionBarActivity {
    // Camera:
    final int TAKE_PHOTO_CODE = 0;
    //File upload
    final int UPLOAD_PHOTO_CODE = 1;

    final int VIEW_PHOTO_CODE =2;
    String which_photo = "";
    int serverResponseCode = 0;

    String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/remitbee/";
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

    String user_id_1 = "";
    String user_id_2 = "";
    String user_id_3 = "";

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



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //UPLOAD IMAGE
        String upLoadServerUri = null;

        if (resultCode == MainActivity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (requestCode != IntentIntegrator.REQUEST_CODE) {
                File imgFile = null;


                Bitmap myBitmap = null;

                if (requestCode == TAKE_PHOTO_CODE) {
                    Log.d("CameraDemo", "Pic saved");
                    imgFile = new File(dir + "remitbee_profile_pic.jpg");
                    myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    Bitmap newResult = Bitmap.createScaledBitmap(myBitmap, 250, 250, false);
                    make_uri_and_update(which_photo,newResult, imgFile);
                }
                //If photo was uploaded from gallery
                else if (requestCode == UPLOAD_PHOTO_CODE) {
                    final Uri selectedImageuri = intent.getData();
                    new AsyncTaskMakePic(MainActivity.this, which_photo, selectedImageuri, this.getContentResolver()).execute();
                }

                } else {


                    //SCANNER
                Log.v("HERE", "SCANNER");

                    if (scanResult != null) {
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
                    } else {
                        Toast.makeText(getApplicationContext(), "No QR Code found", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }


        //Listener for uploading a new picture

    public void upload_picture(View v) {

        int which_pic = Integer.parseInt(v.getTag().toString());
        switch (which_pic) {
            case 0:
                which_photo = "profile_pic";
                break;
            case 1:
                which_photo = "id_1";
                break;
            case 2:
                which_photo = "id_2";
                break;
            case 3:
                which_photo = "id_3";
                break;


        }
        CharSequence choice[] = new CharSequence[]{"Picture From Camera", "Upload From Gallery", "View Photo"};
        final ImageView img = (ImageView) v;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload a new picture");
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on [which]
                switch (which) {
                    case TAKE_PHOTO_CODE:
                        camera_picture();
                        break;
                    case UPLOAD_PHOTO_CODE:
                        openGallery();
                        break;
                    case VIEW_PHOTO_CODE:
                        zoomImageFromThumb(img, img.getDrawable());
                        break;

                }
            }
        });
        builder.show();


    }

    public void camera_picture() {
        String file = dir + "remitbee_profile_pic.jpg";
        File newfile = new File(file);
        try {
            newfile.createNewFile();
        } catch (IOException e) {
        }

        Uri outputFileUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);

    }

    public void openGallery() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), UPLOAD_PHOTO_CODE);


    }

    public void make_uri_and_update(String trigger, Bitmap result, File imgFile){
        String uri = null;
        if (trigger.equals("id_1")) {
            uri = "https://mtesapp.azurewebsites.net/user?method=upload_id_pic&cus_unique_id=" + user_detail.getCus_unique_id() + "&format=json&which_id=" + user_id_1;
            cus_id_1.setImageBitmap(result);
        } else if (trigger.equals("id_2")) {
            uri = "https://mtesapp.azurewebsites.net/user?method=upload_id_pic&cus_unique_id=" + user_detail.getCus_unique_id() + "&format=json&which_id=" + user_id_2;
            cus_id_2.setImageBitmap(result);
        } else if (trigger.equals("id_3")) {
            uri = "https://mtesapp.azurewebsites.net/user?method=upload_id_pic&cus_unique_id=" + user_detail.getCus_unique_id() + "&format=json&which_id=" + user_id_3;
            cus_id_3.setImageBitmap(result);
        }
        // Retrieve file:
        if (imgFile.isFile()) {
            final String finalUpLoadServerUri = uri;
            new Thread(new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Show the ProgressDialog on this thread
                            //pd = ProgressDialog.show(EditProfileActivity.this, "Processing", "Uploading Image...", true, false);
                        }
                    });
                    uploadFile(dir + "remitbee_profile_pic.jpg", finalUpLoadServerUri);
                }
            }).start();
        } else {
            Log.v("Not a file", "PROBLEM!!!");
        }

    }


    class AsyncTaskMakePic extends AsyncTask<String, Void, Bitmap> {
        String trigger;
        Uri selectedImageuri;
        Bitmap newResult = null;
        ContentResolver cr;
        Context context;
        File imgFile;

        public AsyncTaskMakePic(Context context, String which_pic, Uri uri, ContentResolver cr) {
            this.context = context;
            trigger = which_pic;
            selectedImageuri = uri;
            this.cr = cr;
        }

        private Exception exception;
        @Override
        protected void onPreExecute() {
            Log.v("a", "START");
            // Show the ProgressDialog on this thread
            MainActivity.this.pd = ProgressDialog.show(context, "Processing...", "Uploading Image", true, false);
        }
        protected Bitmap doInBackground(String... urls) {
           imgFile = new File(dir + "remitbee_profile_pic.jpg");
            Bitmap myBitmap = null;
            System.out.println("PATH: " + selectedImageuri);
            try {
                myBitmap = MediaStore.Images.Media.getBitmap(cr, selectedImageuri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(imgFile);
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            newResult = Bitmap.createScaledBitmap(myBitmap, 250, 250, false);
            return newResult;

        }


        protected void onPostExecute(Bitmap result) {
            make_uri_and_update(trigger, result, imgFile);
            if (MainActivity.this.pd != null) {
                MainActivity.this.pd.dismiss();
            }
        }
    }
    public int uploadFile(String sourceFileUri, String upLoadServerUri) {
        String fileName = sourceFileUri;
        System.out.println("Uploading FIle");
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            pd.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    Log.e("ee","Ee");
                }
            });
            return 0;
        }
        else
        {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                System.out.println("URL" + upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                // Get server response:
                InputStream in = null;
                try {
                    in = conn.getInputStream();
                    byte[] buf = new byte[1024];
                    int read;
                    while ((read = in.read(buf)) > 0) {
                        System.out.println(new String(buf, 0, read, "utf-8"));
                    }
                } finally {
                    in.close();
                }

                if(serverResponseCode == 200){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "File Upload Complete.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                pd.dismiss();
                ex.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "MalformedURLException",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                pd.dismiss();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : "
                        + e.getMessage(), e);
            }
            pd.dismiss();
            return serverResponseCode;
        } // End else block*/
    }


    public class AsyncTaskCheckQRCode extends AsyncTask<RequestPackage, String, String> {
        Context context;

        public AsyncTaskCheckQRCode(Context context) {
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

            if (user_detail.getCus_unique_id() != "") {
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

                cus_id_1.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.moneybag));
                cus_id_2.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.moneybag));
                cus_id_3.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.moneybag));

                // Set Images:
                user_id_1 = user_detail.getCus_id_1();
                if (!user_id_1.equals("")) {
                    new RetrievePic("id_1").execute(user_id_1);
                }
                user_id_2 = user_detail.getCus_id_2();
                if (!user_id_2.equals("")) {
                    new RetrievePic("id_2").execute(user_id_2);
                }
                user_id_3 = user_detail.getCus_id_3();
                if (!user_id_3.equals("")) {
                    new RetrievePic("id_3").execute(user_id_3);
                }
            } else {
                Toast.makeText(getApplicationContext(), "This is not a valid customer", Toast.LENGTH_LONG).show();
            }

            if (MainActivity.this.pd != null) {
                MainActivity.this.pd.dismiss();
            }
        }
    }


    class RetrievePic extends AsyncTask<String, Void, Bitmap> {
        String trigger;

        public RetrievePic(String which_pic) {
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
