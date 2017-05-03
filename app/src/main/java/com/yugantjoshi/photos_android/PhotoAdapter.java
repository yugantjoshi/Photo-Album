package com.yugantjoshi.photos_android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.IOException;

import io.realm.RealmList;
import model.Photo;

/**
 * Created by yugantjoshi on 4/25/17.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder> {
    private RealmList<Photo> photoRealmList;

    public static class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView photoImageView;
        private TextView tagsTextView;
        private Photo photo;
        private TextView captionText;

        public PhotoHolder(View v) {
            super(v);
            photoImageView = (ImageView) v.findViewById(R.id.photo_image);
            tagsTextView = (TextView) v.findViewById(R.id.tags_textview);
            captionText = (TextView)v.findViewById(R.id.photo_list_caption);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d("RecyclerView", "CLICK!");
            Context context = itemView.getContext();
            Intent intent = new Intent(context, SlideshowActivity.class);
            intent.putExtra("selectedPhotoURI",photo.getImageURI().toString());
            intent.putExtra("selectedAlbumName",photo.getBelongToAlbum());
            Log.d("Starting Slideshow",photo.getImageURI().toString());
            context.startActivity(intent);
        }

        public void bindPhoto(Photo photo) {
            this.photo = photo;
            tagsTextView.setText(photo.getTagsString());
            captionText.setText(photo.getImageURI().toString());
            Uri selectedURI = photo.getImageURI();
            Log.d("ImageURI", "Setting Image to URI");
            photoImageView.setImageURI(selectedURI);

        }
    }

    public PhotoAdapter(RealmList<Photo> photoRealmList) {
        this.photoRealmList = photoRealmList;
    }


    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false);
        return new PhotoHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(final PhotoHolder holder, int position) {
        Photo selectedPhoto = photoRealmList.get(position);
        holder.bindPhoto(selectedPhoto);
        Log.d("Binding Photo", "at position " + position);
        Log.d("image path", selectedPhoto.getImageURI().toString());
    }

    @Override
    public int getItemCount() {
        return this.photoRealmList.size();
    }
}
