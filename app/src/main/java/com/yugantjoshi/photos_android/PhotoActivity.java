package com.yugantjoshi.photos_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.Album;
import model.Photo;
import model.Tag;

public class PhotoActivity extends AppCompatActivity {

    Realm realm;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    Album selectedAlbum;
    private PhotoAdapter photoAdapter;
    private RealmList<Photo> photoRealmList;
    public static final int KITKAT_VALUE = 1002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        realm = Realm.getDefaultInstance();
        recyclerView = (RecyclerView) findViewById(R.id.photo_recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        Bundle bundle = getIntent().getExtras();
        String selectedAlbumName = bundle.getString("albumName");
        Log.d("Album Name", selectedAlbumName);


        RealmQuery<Album> albumRealmQuery = realm.where(Album.class);
        albumRealmQuery.equalTo("albumName", selectedAlbumName);
        RealmResults<Album> albumRealmResults = albumRealmQuery.findAll();


        selectedAlbum = albumRealmResults.get(0);
        photoRealmList = selectedAlbum.getPhotosArrayList();
        photoAdapter = new PhotoAdapter(photoRealmList);
        recyclerView.setAdapter(photoAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Album: " + selectedAlbumName.substring(0, 1).toUpperCase() + selectedAlbumName.substring(1));


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                }
                intent.setType("*/*");
                startActivityForResult(intent, KITKAT_VALUE);
                Log.d("Photo", "Choosing Photo");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == KITKAT_VALUE) {
            if (resultCode == Activity.RESULT_OK) {
                boolean alreadyExists = false;
                Uri selectedImage = data.getData();
                //Check if selected photo is already in this album
                for (int i = 0; i < photoRealmList.size(); i++) {
                    if (photoRealmList.get(i).getImageURI().equals(selectedImage)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (alreadyExists == false) {
                    Log.d("Activit Result", "Calling onActivityResult");
                    final Photo p = new Photo(selectedImage);
                    Log.d("ImageURI", "" + selectedImage);
                    p.setBelongToAlbum(selectedAlbum.getAlbumName());

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            selectedAlbum.insertPhoto(p);
                            photoRealmList = selectedAlbum.getPhotosArrayList();
                            photoAdapter = new PhotoAdapter(photoRealmList);
                            recyclerView.setAdapter(photoAdapter);
                        }
                    });
                } else {
                    Log.d("Photo Already Exists!","DUPLICATE");
                    Toast.makeText(PhotoActivity.this, "This photo already exists", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView)menuItem.getActionView();

        final RealmList<Photo> matchingPhotos = new RealmList<>();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("Submitted Query", query);
                if(!query.contains(":")){
                    Toast.makeText(PhotoActivity.this, "Not a valid search, try searching TagKey: TagValue", Toast.LENGTH_LONG).show();
                }else{
                    String parsedKey = query.substring(0, query.indexOf(":"));
                    String parsedValue = query.substring(query.indexOf(":"));

                    parsedKey.trim();
                    parsedValue.trim();

                    Log.d("parsedKey",parsedKey);
                    Log.d("parsedValue",parsedValue);

                    for(Photo p: photoRealmList){
                        RealmList<Tag> tempTags = p.getTagArrayList();
                        for(int i=0; i<tempTags.size(); i++){
                            String tagKey = tempTags.get(i).getTagType();
                            String tagValue = tempTags.get(i).getTagValue();

                            tagKey.trim();
                            tagValue.trim();

                            Log.d("TagKey",tagKey);
                            Log.d("TagValue",tagValue);

                            parsedKey = parsedKey.toLowerCase();
                            parsedValue = parsedValue.toLowerCase();
                            tagKey = tagKey.toLowerCase();
                            tagValue = tagValue.toLowerCase();

                            Log.d("parsedKey",parsedKey);
                            Log.d("parsedValue",parsedValue);
                            Log.d("TagKey",tagKey);
                            Log.d("TagValue",tagValue);

                            if(parsedKey.equals(tagKey)) {
                                Log.d("Matching key", parsedKey);
                                if (parsedValue.equals(parsedValue)) {
                                    Log.d("Matching value", parsedValue);
                                    //Add to matchingphotos
                                    matchingPhotos.add(p);
                                }
                            }
                        }
                    }
                }
                if(matchingPhotos.size()>0){
                    //TODO: display in Recyclerview
                    photoAdapter = new PhotoAdapter(matchingPhotos);
                    recyclerView.setAdapter(photoAdapter);
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                            linearLayoutManager.getOrientation());
                    recyclerView.addItemDecoration(dividerItemDecoration);
                    return true;
                }
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("Query Changed", newText);
                Log.d("Submitted Query", newText);
                if(!newText.contains(":")){
                    Toast.makeText(PhotoActivity.this, "Not a valid search, try searching TagKey: TagValue", Toast.LENGTH_LONG).show();
                }else{
                    String parsedKey = newText.substring(0, newText.indexOf(":"));
                    String parsedValue = newText.substring(newText.indexOf(":"));

                    parsedKey.trim();
                    parsedValue.trim();

                    Log.d("parsedKey",parsedKey);
                    Log.d("parsedValue",parsedValue);

                    for(Photo p: photoRealmList){
                        RealmList<Tag> tempTags = p.getTagArrayList();
                        for(int i=0; i<tempTags.size(); i++){
                            String tagKey = tempTags.get(i).getTagType();
                            String tagValue = tempTags.get(i).getTagValue();

                            tagKey.trim();
                            tagValue.trim();

                            Log.d("TagKey",tagKey);
                            Log.d("TagValue",tagValue);

                            parsedKey = parsedKey.toLowerCase();
                            parsedValue = parsedValue.toLowerCase();
                            tagKey = tagKey.toLowerCase();
                            tagValue = tagValue.toLowerCase();

                            Log.d("parsedKey",parsedKey);
                            Log.d("parsedValue",parsedValue);
                            Log.d("TagKey",tagKey);
                            Log.d("TagValue",tagValue);

                            if(parsedKey.equals(tagKey)) {
                                Log.d("Matching key", parsedKey);
                                if (parsedValue.equals(parsedValue)) {
                                    Log.d("Matching value", parsedValue);
                                    //Add to matchingphotos
                                    matchingPhotos.add(p);
                                }
                            }
                        }
                    }
                }
                if(matchingPhotos.size()>0){
                    //TODO: display in Recyclerview
                    photoAdapter = new PhotoAdapter(matchingPhotos);
                    recyclerView.setAdapter(photoAdapter);
                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                            linearLayoutManager.getOrientation());
                    recyclerView.addItemDecoration(dividerItemDecoration);
                    return true;
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                Log.d("Up Nagivation", "Navigating back to Albums");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Closing Application", "Closing Realm");
        realm.close();
    }
}

