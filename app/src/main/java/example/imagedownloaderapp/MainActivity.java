package example.imagedownloaderapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.webkit.URLUtil;
import java.io.File;
import java.util.List;

import static example.imagedownloaderapp.UserInterfaceUtils.showToast;

public class MainActivity extends ActivityBase
{
    private static final String FILE_PROVIDER_AUTHORITY = "example.imagedownloaderapp.fileProvider";

    /**
     * EditText field for entering the desired URL to an image.
     */
    private EditText m_txtImageUrl;

    /**
     * Is the application already attempting a download
     */
    private boolean m_bIsDownloading = false;

    /**
     * A value that uniquely identifies the request to download an
     * image.
     */
    private static final int DOWNLOAD_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setMemberControls();
    }

    protected void setMemberControls()
    {
        m_txtImageUrl = (EditText) findViewById(R.id.txtImageUrl);
    }

    protected String GetUrlInput()
    {
        return m_txtImageUrl.getText().toString().trim();
    }

    public void btnDownloadOnClick(View view)
    {
        if (IsInputValid())
        {
            UserInterfaceUtils.hideKeyboard(this, m_txtImageUrl.getWindowToken());
            startDownloadImageActivity(Uri.parse(GetUrlInput()));
        }
    }

    public void btnSetDefaultOnClick(View view)
    {
        m_txtImageUrl.setText("http://40.media.tumblr.com/27fad26f418a40d3b84abb29b4bbf0d7/tumblr_npyv8a381H1swwwaso1_500.jpg");
    }

    private void startDownloadImageActivity(Uri url)
    {
        if (url != null)
        {
            // check if there's already a download in progress
            if (m_bIsDownloading)
            {
                UserInterfaceUtils.showToast(this, "Already downloading image " + url);
            }
            else
            {
                m_bIsDownloading = true;

                // create an intent to download the image
                final Intent intent = ActivityDownloadImage.createIntent(url, this);

                // start download activity
                startActivityForResult(intent, DOWNLOAD_IMAGE_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result)
    {
        // check if Activity result was successful
        if (resultCode == Activity.RESULT_OK)
        {
            // check if result is from expected activity
            if (requestCode == DOWNLOAD_IMAGE_REQUEST)
            {
                File imageFile = new File(result.getDataString());

                // create an intent to display image
                Intent intent = createGalleryIntent(imageFile);

                if (intent != null)
                {
                    // Start the default Android Gallery app image viewer.
                    startActivity(intent);
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED)
        {
            UserInterfaceUtils.showToast(this, "Download failed for " + GetUrlInput());
        }

        // allow user to click the download button again.
        m_bIsDownloading = false;
    }

    /**
     * Create an implicit image intent for the Gallery activity
     */
    protected Intent createGalleryIntent(File imageFile)
    {
        if (!imageFile.exists()) {
            UserInterfaceUtils.showToast(this, "createGalleryIntent() - Image file does not exist");
            return null;
        }

        String mimeType = FileUtils.GetFileMimeType(imageFile);

        Intent intent = null;

        try
        {
            intent = new Intent();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                Uri uriImage = FileProvider.getUriForFile(this,
                        this.getPackageName() + ".fileprovider",
                        imageFile);

                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(uriImage, mimeType);
            }
            else {
                intent.setDataAndType(Uri.fromFile(imageFile), mimeType);
            }

            // grant intent permissions
            grantUriPermissions(this, intent, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        catch(Exception e)
        {
            Log.e(TAG, "createGalleryIntent() - Failed to create intent from file. " + e.getMessage());
            return null;
        }
        return intent;
    }

    public static void grantUriPermissions(Context context, Intent intent, int permissions)
    {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
        {
            // Find all packages that support this intent and grant
            // them the specified permissions.
            List<ResolveInfo> resInfoList =
                    context.getPackageManager().queryIntentActivities(
                            intent, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList)
            {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(
                        packageName,
                        intent.getData(),
                        permissions);
            }
        } else {
            // Just grant permissions to all apps.
            intent.addFlags(permissions);
        }
    }

    protected boolean IsInputValid()
    {
        boolean bIsValid = true;
        String strUrlInput = m_txtImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(strUrlInput))
        {
            UserInterfaceUtils.showToast(this, "Please provide an image url");
            return false;
        }
        else if (!URLUtil.isValidUrl(strUrlInput))
        {
            UserInterfaceUtils.showToast(this, "The image url provided is not valid");
            return false;
        }

        return bIsValid;
    }
}
