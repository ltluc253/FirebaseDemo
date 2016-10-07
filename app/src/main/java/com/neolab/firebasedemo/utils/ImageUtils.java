package com.neolab.firebasedemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

/**
 * Created by LucLe on 05/10/2016.
 */
public class ImageUtils {

    private static final DiskCacheStrategy sCacheStrategy = DiskCacheStrategy.RESULT;

    private static final float THUMBNAIL_SIZE_MULTIPLIER = 0.5f;

    /**
     * Load thumbnail image
     *
     * @param context             current context
     * @param imageView           ImageView
     * @param url                 image url
     * @param placeHolderResource Place Holder Resource ID
     */
    public static void loadThumbnail(Context context, ImageView imageView, String url, int placeHolderResource) {
        if (url != null && !url.isEmpty()) {
            Glide.with(context).load(url)
                    .asBitmap()
                    .thumbnail(THUMBNAIL_SIZE_MULTIPLIER)
                    .centerCrop()
                    .placeholder(placeHolderResource)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            if (resource != null) {
                                getView().setImageBitmap(resource);
                            }
                        }
                    });
        } else {
            imageView.setImageResource(placeHolderResource);
        }
    }

    /**
     * Load online image
     *
     * @param context             current context
     * @param imageView           ImageView
     * @param url                 image url
     * @param placeHolderResource Place Holder Resource ID
     */
    public static void loadImage(Context context, ImageView imageView, String url, int placeHolderResource) {
        if (url != null && !url.isEmpty()) {
            Glide.with(context).load(url)
                    .asBitmap()
                    .placeholder(placeHolderResource)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            if (resource != null) {
                                getView().setImageBitmap(resource);
                            }
                        }
                    });
        } else {
            imageView.setImageResource(placeHolderResource);
        }
    }

}
