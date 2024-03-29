package com.example.usedbook;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class MainActivity extends AppCompatActivity {

    private static final String CLOUD_VISION_API_KEY = "API_KEY";  // google cloud vision api key
    public static final String FILE_NAME = "temp2.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private TextView mImageDetails;
    private TextView mBookTitle;//보이지 않는 텍스트뷰
    private TextView finPrice;
    private ImageView mMainImage;
    private String imageFilePath;
    public int selectMode=1;//1이면 책판별, 2이면 텍스트 판별
    public String rgstring1="x";
    public int sel1=0;
    public String rgstring2="x";
    public int sel2=0;
    public String rgstring3="x";
    public int sel3=0;
    public boolean book_check = false;
    public boolean is_picture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //테스트 완료
        //카메라+앨범추저 유지
        //책판별 해독->조건문 판별
        //서버 통신시작
        //selectMode
        //부분 지워야함

        /*
        A - 얼룩짐 및 낙서
        1.매우심함
        2.보통
        3.매우깔끔

        B - 훼손도(찢어짐 등)
        1.매우심함
        2.보통
        3.매우깔끔

        C - 구입시기
        1.10년 이내
        2.5년 이내
        3.1년 이내
         */

        RadioGroup rg1 = findViewById(R.id.rgp1);
        RadioGroup rg2 = findViewById(R.id.rgp2);
        RadioGroup rg3 = findViewById(R.id.rgp3);


        //A 얼룩짐 및 낙서
        rg1.setOnCheckedChangeListener((radioGroup, i) -> {
            if(i == R.id.rop1_1){
                rgstring1 = "매우심함";
                sel1 = 1;
            }
            else if( i == R.id.rop1_2){
                rgstring1 = "보통";
                sel1 = 2;
            }
            else if( i == R.id.rop1_3){
                rgstring1 = "매우깔끔";
                sel1=3;
            }
        });

        //B 훼손도
        rg2.setOnCheckedChangeListener((radioGroup, i) -> {
            if(i == R.id.rop2_1){
                rgstring2 = "매우심함";
                sel2 = 1;
            }
            else if( i == R.id.rop2_2){
                rgstring2 = "보통";
                sel2 = 2;
            }
            else if( i == R.id.rop2_3){
                rgstring2 = "매우깔끔";
                sel2 = 3;
            }
        });

        //C 구입시기
        rg3.setOnCheckedChangeListener((radioGroup, i) -> {
            if(i == R.id.rop3_1){
                rgstring3 = "10년 이내";
                sel3 = 1;
            }
            else if( i == R.id.rop3_2){
                rgstring3 = "5년 이내";
                sel3 = 2;
            }
            else if( i == R.id.rop3_3){
                rgstring3 = "1년 이내";
                sel3 = 3;
            }
        });

        //책인지 아닌지 판별
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            selectMode=1;
            builder
                    .setMessage("카메라를 실행합니다.")
                    .setNegativeButton("취소",
                            (dialog, whichButton) -> {
                                // Cancel 버튼 클릭시
                            })
                    .setPositiveButton("확인", (dialog, which) -> startCamera());
            builder.show();
        });

        FloatingActionButton fab1 = findViewById(R.id.fab1);
        fab1.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            selectMode=2;
            if(is_picture==false){
                builder
                        .setTitle("오류")
                        .setMessage("사진이 없습니다.\n책을 촬영해주세요.")
                        .setPositiveButton("확인", (dialog, which) -> {
                        });
                builder.show();
            }
            else if(book_check==false) {
                builder
                        .setTitle("오류")
                        .setMessage("책이 아닙니다.\n책을 촬영해주세요.")
                        .setPositiveButton("확인", (dialog, which) -> {
                        });
                builder.show();
            }
            else{

                startActivity(intent);
            }
        });

        mImageDetails = findViewById(R.id.image_details);
        mMainImage = findViewById(R.id.main_image);
        //보이지 않는 텍스트뷰
        mBookTitle = findViewById(R.id.labeldetection);
        finPrice = findViewById(R.id.main_finprice);
    }

    //사진 회전하기
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
    //사진 회전하기
    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            //이미지 회전을 위해 임시저장경로
            File imgt = getCameraFile();
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", imgt);
            imageFilePath= imgt.getAbsolutePath();//사진회전을 위해 저장
            uploadImage(photoUri);
            is_picture = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
                //이미지 회전 함수
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(imageFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch(IllegalArgumentException e){
                    e.printStackTrace();
                }

                int exifOrientation;
                int exifDegree;

                if (exif != null) {
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    exifDegree = exifOrientationToDegrees(exifOrientation);
                } else {
                    exifDegree = 0;
                }

                mMainImage.setImageBitmap(rotate(bitmap,exifDegree));

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                if(selectMode == 1)
                {
                    labelDetection.setType("LABEL_DETECTION");
                }
                else if(selectMode == 2) {
                    labelDetection.setType("TEXT_DETECTION");
                }
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    //책판별
    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;
        ProgressDialog asyncDialog = new ProgressDialog(MainActivity.this);


        LableDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }
        @Override
        protected  void onPreExecute(){
            if(asyncDialog !=null){
                asyncDialog.dismiss();
            }
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다...");

            //show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();

                return convertResponseToStringbook(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();
            asyncDialog.dismiss();

            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.image_details);
                TextView isbookDet = activity.findViewById(R.id.isbookdetection);
                imageDetail.setText(result);
                if(result.contains("사진")){
                    isbookDet.setText("1");
                }
                //여기가 구글 클라우드 비전에서 결과들어오는곳
            }
        }
    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        if(selectMode==1) {
            mImageDetails.setText("잠시만 기다려 주세요. 판별중입니다..");
        }

        // Do the real work in an async task, because we need to use the network anyway
        try {
            if(selectMode==1) {//책 판별
                AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
                labelDetectionTask.execute();
            }
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        Integer originalWidth = bitmap.getWidth();
        Integer originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    //책판별일경우
    private String convertResponseToStringbook(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("잠시만 기다려 주세요. 판별중입니다..\n\n");

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

        if (labels != null) {
            for (EntityAnnotation label : labels) {

                String ss = String.format(Locale.US, "%.3f", label.getScore());
                float ft = Float.parseFloat(ss);
                ft=ft*100;
                ft = (int)ft;
                if( (label.getDescription().equals("Paper") || label.getDescription().equals("Text") || label.getDescription().equals("Font")
                        || label.getDescription().equals("Book")) && ft>=80 )  {

                    message = new StringBuilder("이 사진은" + ft + "% 확률로 책입니다!\n");
                    message.append("->버튼을 눌러 바코드를 촬영해 주세요.");

                    book_check = true;
                    break;
                }
                else{
                    //책이 아니거나 사진이 없습니다.
                    message = new StringBuilder("책이 아닙니다. 다시 정확하게 촬영해 주세요");
                }
            }
        } else {
            //책이 아니거나 사진이 없습니다.
            message = new StringBuilder("책이 아닙니다. 다시 정확하게 촬영해 주세요");
        }

        return message.toString();
    }

    //뒤로가기 2번 누를시 종료
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            finish();
        }
        else
        {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }


}
