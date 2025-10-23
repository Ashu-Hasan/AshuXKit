package com.ashu.ashuutils.fileUtils.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.ashu.ashuutils.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.jetbrains.annotations.Nullable;

public interface ImageHelperMethods {

    public static void loadImage(String TAG, Context context, String imageUrl, ImageView imageView) {
        try {
            Log.d(TAG, "🖼️ Trying to load image: " + imageUrl);

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.profile_icon) // your placeholder image
                    .error(R.drawable.profile_icon)             // image shown on error
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();

            Glide.with(context)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(
                                @Nullable GlideException e,
                                Object model,
                                Target<Drawable> target,
                                boolean isFirstResource
                        ) {
                            Log.e(TAG, "❌ Failed to load image: " + imageUrl, e);
                            return false; // allows Glide to handle error placeholder
                        }

                        @Override
                        public boolean onResourceReady(
                                Drawable resource,
                                Object model,
                                Target<Drawable> target,
                                DataSource dataSource,
                                boolean isFirstResource
                        ) {
                            Log.d(TAG, "✅ Image loaded successfully from: " + dataSource.name());
                            return false;
                        }
                    })
                    .into(imageView);

        } catch (Exception e) {
            Log.e(TAG, "⚠️ Exception while loading image: " + imageUrl, e);
        }
    }

}
