package example.imagedownloaderapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class ActivityDownloadImage extends ActivityBase
{
    private static final int PERMISSION_REQUEST_CODE = 1;

    private Button m_btnPermission;

    /**
     * Name of the Intent Action that wills start this Activity.
     */
    public static String ACTION_DOWNLOAD_IMAGE = "example.imagedownloaderapp.action.DOWNLOAD_IMAGE";

    /**
     * Display progress.
     */
    protected ProgressBar m_pbDownloading;

    /**
     * AsyncTask used to download an image in the background.
     */
    protected AsyncTask<Uri, Void, Uri> m_TaskDownloadImage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_image);

        // store the ProgressBar in a field for fast access.
        m_pbDownloading = (ProgressBar)findViewById(R.id.pbDownloading);

        if (Build.VERSION.SDK_INT >= 23 && !checkPermission())
        {
            // request permission to write to external storage
            requestPermission();
        }
        else
        {
            asyncTaskDownloadImage();
        }
    }

    protected boolean checkPermission()
    {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    protected void requestPermission()
    {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    asyncTaskDownloadImage();
                }
                else
                {
                    UserInterfaceUtils.showToast(this, "Unable to continue without the required permissions.");

                    // Set the result of the Activity.
                    setActivityResult(ActivityDownloadImage.this, null);

                    // Stop the Activity from running.
                    ActivityDownloadImage.this.finish();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        // Always call super class for necessary
        // initialization/implementation.
        super.onStart();

        // Make progress bar visible.
        m_pbDownloading.setVisibility(View.VISIBLE);
    }

    protected void asyncTaskDownloadImage()
    {
        // download an image in the background
        m_TaskDownloadImage = new AsyncTask<Uri, Void, Uri>()
        {
            protected Uri doInBackground(Uri ...url)
            {
                Uri uriSavedImage = null;

                // download the image at the given url
                Bitmap bitmap = FileUtils.downloadBitmap(url[0]);

                if (bitmap != null) {
                    uriSavedImage = FileUtils.saveBitmap(getApplicationContext(), bitmap, url[0].toString(), "ImageDir");
                }
                else {
                    UserInterfaceUtils.showToast(getApplicationContext(),
                            "startDownloadTask() - Unable to download image from Url");
                    this.cancel(true);
                }
                return uriSavedImage;
            }

            protected void onPostExecute(Uri imagePath) {
                // Set the result of the Activity.
                setActivityResult(ActivityDownloadImage.this, imagePath);

                // Stop the Activity from running.
                ActivityDownloadImage.this.finish();
            }
        };

        // start the AsyncTask
        m_TaskDownloadImage.execute(getIntent().getData());
    }

    public static void setActivityResult(Activity activity, Uri imagePath)
    {
        if (imagePath == null)
        {
            // Indicate why the operation on the content was unsuccessful or was cancelled.
            activity.setResult(Activity.RESULT_CANCELED,
                                new Intent("").putExtra("reason", "download failed"));
        }
        else
        {
            // Set the result of the Activity to designate the path to
            // the content file resulting from a successful operation.
            activity.setResult(Activity.RESULT_OK, new Intent("", imagePath));
        }
    }

    public static Intent createIntent(Uri url, Context context)
    {
        return new Intent(ACTION_DOWNLOAD_IMAGE, url, context, ActivityDownloadImage.class);
    }
}