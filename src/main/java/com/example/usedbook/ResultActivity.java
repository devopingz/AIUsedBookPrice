package com.example.usedbook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    public ImageView imgv;
    private TextView con1_det;
    private TextView con2_det;
    private TextView con3_det;
    private TextView finresprice;
    private ImageButton backbt;

    public String con1;
    public String con2;
    public String con3;
    public String uprice;

    public String finprice;
    public String wonprice;
    public String usedprice;

    public static final double DIRTY = 0.5;
    public static final double OLD = 0.25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imgv = findViewById(R.id.book_img);
        con1_det = findViewById(R.id.con1_det);
        con2_det = findViewById(R.id.con2_det);
        con3_det = findViewById(R.id.con3_det);
        finresprice = findViewById(R.id.fin_price);
        backbt = findViewById(R.id.backb);

        Intent intent = getIntent();
        con1 = intent.getExtras().getString("con1");
        con2 = intent.getExtras().getString("con2");
        con3 = intent.getExtras().getString("con3");
        uprice = intent.getExtras().getString("uprice");

        imgv.setImageBitmap(rotateImage(BitmapFactory
                .decodeFile(getExternalFilesDir("Pictures/temp2.jpg")
                        .toString()),90));

        con1_det.setText(con1);
        con2_det.setText(con2);
        con3_det.setText(con3);

        UPAlgoritm();

        backbt.setOnClickListener(view -> {
            finish();
        });

    }

    @Override
    public void onBackPressed() { //스마트폰에 있는 뒤로가기 터치시 finish 시켜준다.
        finish();
    }

    // 이미지 회전 함수
    public Bitmap rotateImage(Bitmap src, float degree) {

        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }


    public void UPAlgoritm(){
        String[] tem = uprice.split("x");
        usedprice = tem[0];
        wonprice = tem[1];
        usedprice = usedprice.replaceAll("\\,","");
        wonprice = wonprice.replaceAll("\\,","");
        int won = Integer.parseInt(wonprice);
        int used = Integer.parseInt(usedprice);

        int upalgo = used + (int)(((won-used)/5)*Xprice(con1,con2,con3));
        finprice = Integer.toString(upalgo) + "원";
        finresprice.setText(finprice);
    }
    public double Xprice(String a, String b, String c){
        Log.d("price sum is ",Double.toString(retA(a) + retB(b) + retC(c)));
        return retA(a) + retB(b) + retC(c);
    }

    public double retA(String a){
        switch (a){
            case "매우깔끔": return 1;
            case "보통": return 0.5;
            case "매우심함": return 0.25;
            default: return 0;
        }
    }

    public double retB(String b){
        switch (b){
            case "매우깔끔": return 1;
            case "보통": return 0.5;
            case "매우심함": return 0.25;
            default: return 0;
        }
    }

    public double retC(String c){
        switch (c){
            case "1년 이내": return 0.5;
            case "5년 이내": return 0.25;
            case "10년 이내": return 0.125;
            default: return 0;
        }
    }



}
