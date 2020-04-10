package com.example.androidfilters;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewEventAdapter.ThumbnailsAdapterListener{

    ImageView imageView;
    Bitmap originalImage;
    Bitmap FilterImage;
    RecyclerView recyclerView;
    public static final String IMAGE_NAME = "bag.jpg";
    LinearLayout ll;
    List<ThumbnailItem> list;
    ViewEventAdapter mAdapter;
    LinearLayoutManager mLinearLayoutManager;
    public  static final  int SELECT_GALLERY_IMAGE=22;

    static {
        System.loadLibrary("NativeImageProcessor");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ll=findViewById(R.id.ll);
        imageView = findViewById(R.id.image_preview);
        recyclerView = findViewById(R.id.recycler_view);
        list=new ArrayList<>();

        mAdapter = new ViewEventAdapter( this,list, this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(mLinearLayoutManager);

        LoadImage();
        recyclerView.setAdapter(mAdapter);
        prepare(null);


        findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(MainActivity.this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if (report.areAllPermissionsGranted()) {
                                    Intent intent = new Intent(Intent.ACTION_PICK);
                                    intent.setType("image/*");
                                    startActivityForResult(intent, SELECT_GALLERY_IMAGE);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Permissions are not granted!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                           PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();

            }
        });


        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveImageToGallery();

            }
        });
    }

    public void saveImageToGallery(){

        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), FilterImage,
                                    System.currentTimeMillis() + "_profile.jpg", null);
                            if (!TextUtils.isEmpty(path)) {
                                Snackbar snackbar = Snackbar
                                        .make(ll, "Image saved to gallery!", Snackbar.LENGTH_LONG)
                                        .setAction("OPEN", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openImage(path);
                                            }
                                        });

                                snackbar.show();
                            } else {
                                Snackbar snackbar = Snackbar
                                        .make(ll, "Unable to save image!", Snackbar.LENGTH_LONG);

                                snackbar.show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }
    public void prepare(Bitmap bitmap){

        Bitmap thumbImage;
        ThumbnailsManager.clearThumbs();
        list.clear();

        // add normal bitmap first
        thumbImage = (Bitmap) BitmapUtils.GetFromAssetFolder(MainActivity.this,
                MainActivity.IMAGE_NAME);
        ThumbnailItem thumbnailItem = new ThumbnailItem();
        thumbnailItem.image = thumbImage;
        thumbnailItem.filterName = "Normal";
        ThumbnailsManager.addThumb(thumbnailItem);

        List<Filter> filters = FilterPack.getFilterPack(MainActivity.this);

        for (Filter filter : filters) {
            ThumbnailItem tI = new ThumbnailItem();
            tI.image = thumbImage;
            tI.filter = filter;
            tI.filterName = filter.getName();
            ThumbnailsManager.addThumb(tI);
        }
        list.addAll(ThumbnailsManager.processThumbs(MainActivity.this));
        mAdapter.notifyDataSetChanged();
    }

    private void LoadImage() {
        originalImage=BitmapUtils.GetFromAssetFolder(MainActivity.this,IMAGE_NAME);
        FilterImage=originalImage.copy(Bitmap.Config.ARGB_8888,true);
        imageView.setImageBitmap(originalImage);

    }

    @Override
    public void onFilterSelected(Filter filter) {
        FilterImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        imageView.setImageBitmap(filter.processFilter(FilterImage));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this,
                    data.getData());

            originalImage.recycle();
//
            FilterImage.recycle();
//
            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            FilterImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

            Uri imageUri=data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    public void openImage(String path){
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path),"image/*");
        startActivity(intent);
    }
}
