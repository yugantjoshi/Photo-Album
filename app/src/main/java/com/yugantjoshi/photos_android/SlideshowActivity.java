package com.yugantjoshi.photos_android;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.Album;
import model.Photo;
import model.Tag;

public class SlideshowActivity extends AppCompatActivity {
    private Album selectedAlbum;
    private Photo p;
    Realm realm;
    private RealmList<Photo> photoRealmList;

    private Button addTag, deleteTag, deletePhoto, nextButton, previousButton, movePhotoButton;
    private TextView photoTags, photoCaption;
    private ImageView photoImage;
    private String selectedPhotoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        Toolbar toolbar = (Toolbar) findViewById(R.id.slideshow_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

        Bundle bundle = getIntent().getExtras();

        String selectedAlbumName = bundle.getString("selectedAlbumName");
        selectedPhotoURI = bundle.getString("selectedPhotoURI");


        RealmQuery<Album> albumRealmQuery = realm.where(Album.class);
        albumRealmQuery.equalTo("albumName", selectedAlbumName);
        RealmResults<Album> albumRealmResults = albumRealmQuery.findAll();
        selectedAlbum = albumRealmResults.get(0);
        photoRealmList = selectedAlbum.getPhotosArrayList();

        findPhoto(selectedPhotoURI);

        addTag = (Button) findViewById(R.id.add_tag_button);
        deleteTag = (Button) findViewById(R.id.delete_tag_button);
        deletePhoto = (Button) findViewById(R.id.delete_photo_button);
        nextButton = (Button) findViewById(R.id.next_button);
        previousButton = (Button) findViewById(R.id.previous_button);
        photoTags = (TextView) findViewById(R.id.slide_tags_textview);
        photoImage = (ImageView) findViewById(R.id.photo_slide);
        photoCaption = (TextView) findViewById(R.id.slide_caption_textview);
        movePhotoButton = (Button) findViewById(R.id.move_photo_button);

        photoCaption.setText(selectedPhotoURI);


        toolbar.setTitle(selectedAlbumName.substring(0, 1).toUpperCase() + selectedAlbumName.substring(1).toLowerCase() + ": Slideshow");

        photoTags.setText(p.getTagsString());
        photoImage.setImageURI(p.getImageURI());


        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTagHandle();
            }
        });
        deleteTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTagHandle();
            }
        });
        deletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePhotoHandle();
            }
        });
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousButtonHandle();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButtonHandle();
            }
        });
        movePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movePhotoHandle();
            }
        });
    }

    public void findPhoto(String selectedPhotoURI) {
        Photo foundPhoto = null;

        for (int i = 0; i < photoRealmList.size(); i++) {
            if (photoRealmList.get(i).getImageURI().toString().equals(selectedPhotoURI.toString())) {
                foundPhoto = photoRealmList.get(i);
                break;
            }
        }
        if (foundPhoto != null) {
            this.p = foundPhoto;
        }
    }

    private void addTagHandle() {
        //Create Dialog for input, take both inputs, create new Tag, display it in  textview
        LayoutInflater layoutInflater = LayoutInflater.from(SlideshowActivity.this);
        View promptView = layoutInflater.inflate(R.layout.photo_tagdialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SlideshowActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText tagKey = (EditText) promptView.findViewById(R.id.photo_tagkey);
        final EditText tagValue = (EditText) promptView.findViewById(R.id.photo_tagvalue);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //final String newAlbumName = editText.getText().toString();
                        final String tagKeyText = tagKey.getText().toString();
                        final String tagValueText = tagValue.getText().toString();
                        if (tagKeyText.length() < 1 || tagValueText.length() < 1) {
                            Toast.makeText(SlideshowActivity.this, "Must have a valid name", Toast.LENGTH_LONG).show();
                        } else {
                            if (isUniqueTag(tagKeyText, tagValueText)) {
                                if (tagKeyText.equalsIgnoreCase("person") || tagKeyText.equalsIgnoreCase("location")) {

                                    Log.d("Adding Key Value", tagKeyText + ": " + tagValueText);

                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            Tag t = new Tag(tagKeyText.substring(0, 1).toUpperCase() + tagKeyText.substring(1).toLowerCase(), tagValueText);
                                            p.addTag(t);
                                            photoTags.setText(p.getTagsString());
                                        }
                                    });
                                } else {
                                    Toast.makeText(SlideshowActivity.this, "Not a valid tag key", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(SlideshowActivity.this, "This Tag already exists", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void movePhotoHandle() {
        final Photo movePhoto = p;
        //TODO: Create dialog with options to move photo

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        RealmQuery<Album> albumRealmQuery = realm.where(Album.class);
        RealmResults<Album> albumRealmResults = albumRealmQuery.findAll();

        final CharSequence albumNames[] = new CharSequence[albumRealmResults.size()];

        for (int i = 0; i < albumRealmResults.size(); i++) {
            albumNames[i] = albumRealmResults.get(i).getAlbumName();
        }

        adb.setSingleChoiceItems(albumNames, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, final int n) {

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        String moveToAlbum = albumNames[n].toString();
                        Log.d("Moving to", moveToAlbum);
                        int photoIndex = 0;
                        for (int i = 0; i < photoRealmList.size(); i++) {
                            if (photoRealmList.get(i).getImageURI().toString().equals(selectedPhotoURI.toString())) {
                                photoIndex = i;
                                break;
                            }
                        }
                        photoRealmList.remove(photoIndex);

                        RealmQuery<Album> moveAlbumQuery = realm.where(Album.class);
                        moveAlbumQuery.equalTo("albumName", moveToAlbum);
                        RealmResults<Album> realmMoveAlbum = moveAlbumQuery.findAll();
                        Album moveAlbum = realmMoveAlbum.get(0);
                        Log.d("Removing Photo", movePhoto.getImageURI().toString());

                        if (isUniquePhoto(moveAlbum, movePhoto)) {
                            moveAlbum.insertPhoto(movePhoto);
                            Intent i = new Intent(SlideshowActivity.this, PhotoActivity.class);
                            i.putExtra("albumName",selectedAlbum.getAlbumName());
                            startActivity(i);
                        } else {
                            Toast.makeText(SlideshowActivity.this, "This photo already exists in that Album", Toast.LENGTH_LONG).show();
                        }
                    }

                });
                d.dismiss();
            }

        });
        adb.setNegativeButton("Cancel", null);
        adb.setTitle("Choose an Album to move");
        adb.show();

    }

    private void deleteTagHandle() {
        LayoutInflater layoutInflater = LayoutInflater.from(SlideshowActivity.this);
        View promptView = layoutInflater.inflate(R.layout.photo_delete_tag_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SlideshowActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText tagKey = (EditText) promptView.findViewById(R.id.photo_tag_delete_key);
        final EditText tagValue = (EditText) promptView.findViewById(R.id.photo_tag_delete_value);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //final String newAlbumName = editText.getText().toString();
                        final String tagKeyText = tagKey.getText().toString();
                        final String tagValueText = tagValue.getText().toString();
                        if (tagKeyText.length() < 1 || tagValueText.length() < 1) {
                            Toast.makeText(SlideshowActivity.this, "Must have a valid name", Toast.LENGTH_LONG).show();
                        } else {
                            if (tagKeyText.equalsIgnoreCase("person") || tagKeyText.equalsIgnoreCase("location")) {

                                Log.d("Deleting Key Value", tagKeyText + ": " + tagValueText);
                                final int tagIndexFound = p.findTagIndex(tagKeyText, tagValueText);
                                if (tagIndexFound > -1) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            //TODO: remove tag
                                            p.removeTag(tagIndexFound);
                                            photoTags.setText(p.getTagsString());
                                        }
                                    });
                                } else {
                                    Toast.makeText(SlideshowActivity.this, "This tag doesn't exist", Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(SlideshowActivity.this, "Not a valid tag key value", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void deletePhotoHandle() {
        //Create Prompt to make sure for delete, need to go back to the PhotoActivity once deleted
        LayoutInflater layoutInflater = LayoutInflater.from(SlideshowActivity.this);
        View promptView = layoutInflater.inflate(R.layout.photo_deletedialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SlideshowActivity.this);
        alertDialogBuilder.setView(promptView);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                selectedAlbum.removePhoto(p);
                                Intent i = new Intent(SlideshowActivity.this, PhotoActivity.class);
                                i.putExtra("albumName", selectedAlbum.getAlbumName());
                                startActivity(i);
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void nextButtonHandle() {
        //Get the index of the next Photo and display all the revelant information
        //If at the last index go back to index 0
        Log.d("Calling Next", "Going to the next photo");
        int index = -100;
        Log.d("Photo URI", p.getImageURI().toString());
        for (int i = 0; i < photoRealmList.size(); i++) {
            Log.d("RealmList URI", photoRealmList.get(i).getImageURI().toString());
            if (photoRealmList.get(i).getImageURI().toString().equals(p.getImageURI().toString())) {
                Log.d("Found matching", i + "");
                index = i;
                break;
            }
        }

        index += 1;

        if (index < photoRealmList.size()) {
            Log.d("Going to photo at", index + "");
            Photo nextPhoto = photoRealmList.get(index);
            photoTags.setText(nextPhoto.getTagsString());
            photoImage.setImageURI(nextPhoto.getImageURI());
            photoCaption.setText(nextPhoto.getImageURI().toString());
            p = nextPhoto;
        } else {
            if (photoRealmList.size() == 1) {
                Toast.makeText(SlideshowActivity.this, "Try adding more photos first", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(SlideshowActivity.this, "Reached the end, try clicking previous", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void previousButtonHandle() {
        Log.d("Calling Previous", "Going to the previous photo");
        int index = -100;
        Log.d("Photo URI", p.getImageURI().toString());
        for (int i = 0; i < photoRealmList.size(); i++) {
            Log.d("RealmList URI", photoRealmList.get(i).getImageURI().toString());
            if (photoRealmList.get(i).getImageURI().toString().equals(p.getImageURI().toString())) {
                Log.d("Found matching", i + "");
                index = i;
                break;
            }
        }

        index -= 1;

        if (index > -1) {
            Log.d("Going to photo at", index + "");
            Photo previousPhoto = photoRealmList.get(index);
            photoTags.setText(previousPhoto.getTagsString());
            photoImage.setImageURI(previousPhoto.getImageURI());
            photoCaption.setText(previousPhoto.getImageURI().toString());
            p = previousPhoto;
        } else {
            if (photoRealmList.size() == 1) {
                Toast.makeText(SlideshowActivity.this, "Try adding more photos first", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(SlideshowActivity.this, "Reached the beginning, try clicking next", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isUniqueTag(String key, String value) {

        for (int i = 0; i < p.getTagArrayList().size(); i++) {
            if (p.getTagArrayList().get(i).getTagType().equals(key) && p.getTagArrayList().get(i).getTagValue().equals(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra("albumName", selectedAlbum.getAlbumName());
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isUniquePhoto(Album a, Photo photo) {
        for (int i = 0; i < a.getPhotosArrayList().size(); i++) {
            if (a.getPhotosArrayList().get(i).getImageURI().toString().equals(photo.getImageURI().toString())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Closing Application", "Closing Realm");
        realm.close();
    }
}
