package win.lioil.bluetooth.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.widget.BrushView;

public class BrushViewActivityActivity extends AppCompatActivity {
    private BrushView brushView;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brush_view_activity);
        initView();
    }

    private void initView() {
        brushView = findViewById(R.id.brushView);
        iv = findViewById(R.id.ivBitmap);
        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brushView.saveBitmap(BrushViewActivityActivity.this);
            }
        });
        findViewById(R.id.btnLoadBitmap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brushView.setVisibility(View.GONE);
                iv.setVisibility(View.VISIBLE);
                loadBitmap();
            }
        });

        findViewById(R.id.btnRenew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv.setVisibility(View.GONE);
                brushView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void loadBitmap() {
        String bitmapPath;
        Bitmap bitmap;
        //sd卡存在
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            bitmapPath = Environment.getExternalStorageDirectory() + "/" + "win.lioil.bluetooth" + "/resource/";
        } else {
            bitmapPath = "/data/data/win.lioil.bluetooth/resource";
        }
        bitmapPath = bitmapPath + "/2.png";
        File file = new File(bitmapPath);
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(bitmapPath);
            iv.setImageBitmap(bitmap);
        }
    }
}
