package example.imagedownloaderapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

public class ActivityDownloadImage extends ActivityBase
{
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
    }

    /**
     * Hook method called after onCreate() or after onRestart() (when
     * the activity is being restarted from stopped state).  Should
     * re-acquire resources relinquished when activity was stopped
     * (onStop()) or acquire those resources for the first time after
     * onCreate().
     */
    @Override
    protected void onStart() {
        // Always call super class for necessary
        // initialization/implementation.
        super.onStart();

        // Make progress bar visible.
        m_pbDownloading.setVisibility(View.VISIBLE);

        //checkPermissions();
        asyncTaskDownloadImage();
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
