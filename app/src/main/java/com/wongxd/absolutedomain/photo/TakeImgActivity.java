package com.wongxd.absolutedomain.photo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.model.TakePhotoOptions;
import com.orhanobut.logger.Logger;
import com.wongxd.absolutedomain.R;

import java.io.File;



public class TakeImgActivity extends TakePhotoActivity {


    private String imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_take_photo);
        Button btnTake = (Button) findViewById(R.id.btn_take_photo);
        Button btnGet = (Button) findViewById(R.id.btn_pick_photo);
        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //图片裁剪
        final CropOptions.Builder builder = new CropOptions.Builder();
        builder
//                .setOutputX(500).setOutputY(500)
                .setWithOwnCrop(false);

        //是否使用自带
        TakePhotoOptions.Builder b = new TakePhotoOptions.Builder();
        b.setWithOwnGallery(true);
        b.setCorrectImage(true);//纠正拍照的照片旋转角度


//        File file=new File(Environment.getExternalStorageDirectory(), "/temp/"+System.currentTimeMillis() + ".jpg");
        File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + "temp.jpg");
        if (file.exists()) file.delete();
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        final Uri imageUri = Uri.fromFile(file);

        takePhoto = getTakePhoto();
        takePhoto.onEnableCompress(config, false);
        takePhoto.setTakePhotoOptions(b.create());

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto.onPickFromCaptureWithCrop(imageUri, builder.create());
            }
        });

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto.onPickMultipleWithCrop(1, builder.create());
            }
        });
    }


    //    #############################图片压缩##############
    CompressConfig config = new CompressConfig.Builder()
//            .setMaxSize()
            .setMaxPixel(500)
            .enableReserveRaw(true)
            .create();
    private TakePhoto takePhoto;

    @Override
    public void takeCancel() {
        super.takeCancel();
        Logger.e("取消取图");
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
        Logger.e(msg);
    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
//        showImg(result.getImages());
        imgPath = result.getImage().getCompressPath();
        Logger.e(imgPath);
        if (!TextUtils.isEmpty(result.getImage().getCompressPath())) {
            Intent intent = new Intent();
            intent.putExtra("path", imgPath);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

}
