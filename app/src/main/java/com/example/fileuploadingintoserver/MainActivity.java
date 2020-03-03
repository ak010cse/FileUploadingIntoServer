package com.example.fileuploadingintoserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class MainActivity extends AppCompatActivity {
    private ImageView attachment, uploaded_image;
    private TextView fileName, uploaded_pdf;
    private Button upload_files;
    private LinearLayout choose_files, uploaded_pdf_layout;
    static final int PICK_IMAGE_REQUEST = 1;
    private String displayName = "";
    private Uri filePath;
    private RequestQueue rQueue;
    String REQUEST_QUOTES_URL = "http://onqanet.net/dev05/carpenter/api/user/request_quote";
    private Button download_button, show_button;
//    String imageUrl="https://flipspot.com/resources/uploads/users/small/d002b123fc7e6c32e1dcdcc3e4f22c65.JPEG";
    String imageUrl="https://flipspot.com/resources/uploads/users/small/eeb74f9fd7d0ed12d3f764bdda6faf5e.png";
String filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        clickEvents();
    }

    public void initViews() {
        choose_files = (LinearLayout) findViewById(R.id.choose_files_linearLayout);
        uploaded_pdf_layout = (LinearLayout) findViewById(R.id.uploaded_pdf_linearLayout);
        attachment = (ImageView) findViewById(R.id.attachment_imageView);
        uploaded_image = (ImageView) findViewById(R.id.file_uploaded_imageView);
        fileName = (TextView) findViewById(R.id.fileName_TextView);
        uploaded_pdf = (TextView) findViewById(R.id.pdf_uploaded_TextView);
        upload_files = (Button) findViewById(R.id.upload_files_button);
        download_button = (Button) findViewById(R.id.download_button);
        show_button = (Button) findViewById(R.id.show_button);

    }

    public void clickEvents() {
        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA}, PICK_IMAGE_REQUEST);
                } else {

                    downloadFile(imageUrl);

                }
//                downloadIMAGE();
            }
        });
        choose_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA}, PICK_IMAGE_REQUEST);
                } else {

                    showPictureDialog();

                }
            }
        });

        upload_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileName.getText().toString().trim().isEmpty()) {

                    Toast.makeText(MainActivity.this, "Please select at least one files", Toast.LENGTH_SHORT).show();
                } else {

                    uploadsFilesIntoServer();

                }
            }
        });
    }

    public String downloadIMAGE(){
        try
        {
            URL url = new URL(imageUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            File SDCardRoot = Environment.getExternalStorageDirectory().getAbsoluteFile();
            String filename="downloadedFile.png";
            Log.i("Local filename:",""+filename);
            File file = new File(SDCardRoot,filename);
            if(file.createNewFile())
            {
                file.createNewFile();
            }
            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ( (bufferLength = inputStream.read(buffer)) > 0 )
            {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                Log.i("Progress:","downloadedSize:"+downloadedSize+"totalSize:"+ totalSize) ;
            }
            fileOutput.close();
            if(downloadedSize==totalSize) filepath=file.getPath();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            filepath=null;
            e.printStackTrace();
        }
        Log.i("filepath:"," "+filepath) ;
        return filepath;
    }

    public void downloadFile(String uRl) {
        File direct = new File(Environment.getExternalStorageDirectory()+ "/apkTest");
        Log.e( "downloadFile: ", String.valueOf(direct));
        if (!direct.exists()) {
            direct.mkdirs();
            Log.e( "downloadFile1: ", String.valueOf(direct.mkdirs()));
        }else {
            File file = new File(direct, "fileName.jpg");
            boolean deleted = file.delete();
            Log.e( "downloadFile2: ", String.valueOf(deleted));
//            direct.delete();
        }

        DownloadManager mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI| DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir("/apkTest", "fileName.jpg");


        mgr.enqueue(request);

        // Open Download Manager to view File progress
        Toast.makeText(this, "Downloading...", Toast.LENGTH_LONG).show();
        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));

    }


    private void showPictureDialog() {

        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select from files",
                "Capture from camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                chooseFromFiles();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }


                    }
                });
        pictureDialog.show();
    }

    private void chooseFromFiles() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT), chooser = null;
        intent.setType("*/*");
        chooser = Intent.createChooser(intent, "Find file to Print");
        startActivityForResult(chooser, PICK_IMAGE_REQUEST);
    }

    private void takePhotoFromCamera() {

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PICK_IMAGE_REQUEST) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
//                    Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the Uri of the selected file
            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            getRealPathFromURI_API19(this, uri);

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        fileName.setText(displayName);
                        Log.d("nameeeee>>>>  ", displayName);
                        Log.d("nameeeee>>uri>>  ", uri.toString());
                        filePath = uri;

                        final String fileExt = MimeTypeMap.getFileExtensionFromUrl(filePath.toString());
                        Log.d("nameeeee>>fileExt>>: ", fileExt);

                        if (fileExt.contentEquals("pdf") || fileExt.contentEquals("docx")) {
                            uploaded_image.setVisibility(View.INVISIBLE);
                            uploaded_pdf_layout.setVisibility(View.VISIBLE);
                            uploaded_pdf.setText(displayName);


                        } else {
                            uploaded_pdf_layout.setVisibility(View.INVISIBLE);
                            uploaded_image.setVisibility(View.VISIBLE);

                            Glide.with(MainActivity.this)
                                    .load(filePath)
                                    .apply(new RequestOptions().placeholder(R.drawable.border).error(R.drawable.border))
                                    .into(uploaded_image);
                        }

                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
                Log.d("nameeeee>>file>>  ", displayName);
                Log.d("nameeeee>>file_uri>>  ", uri.toString());
                filePath = uri;
                fileName.setText(displayName);
                final String fileExt = MimeTypeMap.getFileExtensionFromUrl(filePath.toString());
                Log.d("nameeeee>>fileExt>>: ", fileExt);

                if (fileExt.contentEquals("pdf") || fileExt.contentEquals("docx")) {
                    uploaded_image.setVisibility(View.INVISIBLE);
                    uploaded_pdf_layout.setVisibility(View.VISIBLE);
                    uploaded_pdf.setText(displayName);


                } else {
                    uploaded_pdf_layout.setVisibility(View.INVISIBLE);
                    uploaded_image.setVisibility(View.VISIBLE);
                    Glide.with(MainActivity.this).load(filePath).apply(new RequestOptions().placeholder(R.drawable.border).error(R.drawable.border)).into(uploaded_image);
                }
            }
        }

    }


    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        if (uri.getHost().contains("com.android.providers.media")) {
            // Image pick from recent
            String wholeID = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                wholeID = DocumentsContract.getDocumentId(uri);
            }

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
            Log.e("nameeeee>>filepath>> ", filePath);
            return filePath;


        } else {
            // image pick from gallery
            return getRealPathFromURI_BelowAPI11(context, uri);
        }

    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public String compressImage(String imageUri) {

        final String filePath1 = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath1, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath1, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath1);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        File file = new File(filename);

        MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("file_media_scanner", uri.toString());

                        filePath = uri;

                    }
                });
        Log.i("filename", filename);
        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private void uploadsFilesIntoServer() {
        final ProgressDialog pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("loading");
        pd.show();

        final String fileExt = MimeTypeMap.getFileExtensionFromUrl(filePath.toString());
        Log.d("nameeeee>>fileExt1>>: ", fileExt);

        InputStream iStream = null;
        try {

            Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3AapkTest%2FfileName.jpg");
            Log.d("onCreate: ", uri.toString());


            iStream = getContentResolver().openInputStream(uri);
            final byte[] inputData = getBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, REQUEST_QUOTES_URL,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            Log.d("ressssssoo", new String(response.data));
                            rQueue.getCache().clear();
                            try {
                                pd.dismiss();

                                JSONObject obj = new JSONObject(new String(response.data));
                                String code = obj.getString("code");
                                String message = obj.getString("message");
                                if (code.contentEquals("success")) {

                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> params = new HashMap<>();
                    params.put("name", "test");
                    params.put("email", "xyz@gmail.com");
                    params.put("contact_no", "1234567890");
                    params.put("brand", "test");
                    params.put("product_name", "test");
                    params.put("thickness", "4.2");
                    params.put("sq_feet", "5.4");
                    params.put("quantity", "2");
                    params.put("altire_challenge", "yes");
                    System.out.println("params" + Arrays.asList(params));
                    return params;
                }


                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    if (fileExt.contentEquals("pdf")) {
                        params.put("banner_picture", new DataPart(displayName, inputData, "application/pdf"));
                    } else if (fileExt.contentEquals("docx")) {
                        params.put("banner_picture", new DataPart(displayName, inputData, "application/msword"));
//                        params.put("banner_picture", new DataPart(displayName, inputData, "application/docx"));
                    } else {
                        params.put("banner_picture", new DataPart(displayName, inputData, "image/jpeg"));

                    }
                    System.out.println("params1" + Arrays.asList(params));

                    return params;
                }
            };


            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            rQueue = Volley.newRequestQueue(this);
            rQueue.add(volleyMultipartRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
