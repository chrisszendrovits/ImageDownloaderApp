package example.imagedownloaderapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by acc on 2016-12-17.
 */
public class FileUtils
{
    protected static String TAG = String.valueOf(FileUtils.class);

    private FileUtils() { throw new AssertionError(); }

    public static Bitmap downloadBitmap(Uri urlImage)
    {
        Bitmap bitmap = null;

        try {
            URL url = new URL(urlImage.toString());
            InputStream stream = url.openStream();
            bitmap = BitmapFactory.decodeStream(stream);

            stream.close();
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "downloadImage() - Malformed URL. " + e.toString());
        }
        catch(IOException e) {
            Log.e(TAG, "downloadImage() - IO stream exception occurred. " + e.toString());
        }
        catch(Exception e) {
            Log.e(TAG, "downloadImage() - Unable to download image. " + e.toString());
        }
        return bitmap;
    }

    public static Uri saveBitmap(Context context, Bitmap bitmap, String fileName, String folderName)
    {
        if (!isExternalStorageWritable()) {
            UserInterfaceUtils.showToast(context, "external storage is not writable");
            return null;
        }

        // create a directory in the public storage
        File directory = getPublicStorageDir(folderName);

        if (directory == null) {
            UserInterfaceUtils.showToast(context, "Unable to create a directory for download.");
            return null;
        }

        // make a new temporary file name
        File file = new File(directory, getTemporaryFilename(fileName) + ".jpg");

        if (file.exists())
        {
            // delete the file if it already exists
            file.delete();
        }

        // save the image to the output file
        try (FileOutputStream outputStream = new FileOutputStream(file))
        {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        }
        catch (Exception e) {
            Log.e(TAG, "saveBitmap() - Exception while saving the output stream. " + e.toString());
            return null;
        }

        // get the absolute path of the image
        String absolutePathToImage = file.getAbsolutePath();

        // add metadata to the image
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, fileName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis ());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", absolutePathToImage);

        // get the content resolver for this context
        ContentResolver cr = context.getContentResolver();

        // store the metadata for the image into the Gallery content provider
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Log.d(TAG, "Image saved at: " + absolutePathToImage);

        // return the absolute path of the image file
        return Uri.parse(absolutePathToImage);
    }

    /**
     * This method checks if we can write image to external storage
     * @return true if an image can be written, and false otherwise
     */
    public static boolean isExternalStorageWritable()
    {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getPublicStorageDir(String directoryName)
    {
        // get the public storage directory
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), directoryName);

        if (!directory.exists())
        {
            if (!directory.mkdirs())
            {
                Log.e(TAG, "getPublicStorageDir() - Unable to create a public storage directory");
                return null;
            }
        }
        return directory;
    }

    private static String getTemporaryFilename(final String url)
    {
        return Base64.encodeToString((url.toString() + System.currentTimeMillis()).getBytes(),
                Base64.NO_WRAP);
    }

    protected static String GetFileMimeType(File file)
    {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        return mime.getMimeTypeFromExtension(ext);
    }
}
