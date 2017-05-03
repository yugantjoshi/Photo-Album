package com.yugantjoshi.photos_android;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import model.Album;
import model.Photo;

public class AlbumActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_READ = 10001;
    private static final int REQUEST_EXTERNAL_WRITE = 10002;
    private static final int REQUEST_MANAGE_DOCUMENTS = 10003;

    ArrayList<Album> albumArrayList = new ArrayList<>();
    private ListView albumListView;
    private ArrayList<String> albumNames = new ArrayList<>();
    Realm realm;

    ArrayAdapter<String> albumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        permissionChecks();
        realm = Realm.getDefaultInstance();

        RealmQuery realmQuery = realm.where(Album.class);
        RealmResults results = realmQuery.findAll();

        //Initial Read
        for (Object o : results) {
            Album a = (Album) o;
            albumArrayList.add(a);
            albumNames.add(a.getAlbumName());
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAlbumDialog();
            }
        });


        albumAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albumNames);
        albumListView = (ListView) findViewById(R.id.album_listview);
        albumListView.setAdapter(albumAdapter);

        registerForContextMenu(albumListView);

        albumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(AlbumActivity.this, PhotoActivity.class);
                String albumSelectedName = (String) (albumListView.getItemAtPosition(i));
                Log.d("Selected Album", albumSelectedName);
                intent.putExtra("albumName", albumSelectedName);
                startActivity(intent);

            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_album, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_edit:
                editAlbum(info);
                return true;
            case R.id.action_delete:
                deleteAlbum(info);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private void editAlbum(AdapterView.AdapterContextMenuInfo info) {
        final int oldAlbumIndex = info.position;
        LayoutInflater layoutInflater = LayoutInflater.from(AlbumActivity.this);
        View promptView = layoutInflater.inflate(R.layout.album_inputdialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AlbumActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.album_inputText);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String newAlbumName = editText.getText().toString();
                        if (newAlbumName.length() < 1) {
                            Toast.makeText(AlbumActivity.this, "Must have a valid name", Toast.LENGTH_LONG).show();
                        } else {
                            if (isUniqueAlbum(newAlbumName)) {
                                Log.d("Changed Album Name", newAlbumName);

                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        Album oldAlbum = albumArrayList.get(oldAlbumIndex);
                                        String oldAlbumName = oldAlbum.getAlbumName();

                                        RealmQuery<Album> query = realm.where(Album.class);
                                        query.equalTo("albumName", oldAlbumName);

                                        RealmResults<Album> results = query.findAll();
                                        Log.d("Results Size on Edit", results.size() + "");
                                        results.get(0).setAlbumName(newAlbumName);

                                        albumNames.set(oldAlbumIndex, newAlbumName);
                                        albumAdapter.notifyDataSetChanged();
                                    }
                                });

                            } else {
                                Toast.makeText(AlbumActivity.this, "This album already exists", Toast.LENGTH_LONG).show();
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

    private void deleteAlbum(AdapterView.AdapterContextMenuInfo info) {

        final int index = info.position;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Album a = albumArrayList.get(index);
                RealmQuery<Album> query = realm.where(Album.class);
                query.equalTo("albumName", a.getAlbumName());
                RealmResults results = query.findAll();
                Log.d("Deleting From Realm", "Removing " + a.getAlbumName());
                results.deleteAllFromRealm();

                albumAdapter.remove(albumAdapter.getItem(index));
                albumArrayList.remove(index);
            }
        });

        albumAdapter.notifyDataSetChanged();
    }

    private void addAlbumDialog() {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(AlbumActivity.this);
        View promptView = layoutInflater.inflate(R.layout.album_inputdialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AlbumActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.album_inputText);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final String newAlbumName = editText.getText().toString();
                        if (newAlbumName.length() < 1) {
                            Toast.makeText(AlbumActivity.this, "Must have a valid name", Toast.LENGTH_LONG).show();
                        } else {
                            if (isUniqueAlbum(newAlbumName)) {
                                Log.d("Album Name", newAlbumName);
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        Log.d("Executing Realm write", ">Executing<");
                                        Album a = realm.createObject(Album.class);
                                        a.setAlbumName(newAlbumName);
                                        albumArrayList.add(a);
                                        albumNames.add(a.getAlbumName());
                                        albumAdapter.notifyDataSetChanged();
                                    }
                                });


                            } else {
                                Toast.makeText(AlbumActivity.this, "This album already exists", Toast.LENGTH_LONG).show();
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

    private boolean isUniqueAlbum(String albumName) {
        for (int i = 0; i < albumArrayList.size(); i++) {
            if (albumArrayList.get(i).getAlbumName().equals(albumName)) {
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

    public void permissionChecks() {
        Log.d("Connected", "Check Permissions");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(AlbumActivity.this, "Need to access Storage", Toast.LENGTH_LONG).show();
                }
                Log.d("Connected", "REQUEST PERMISSIONS");
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_READ);
            }
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(AlbumActivity.this, "Need to access Storage", Toast.LENGTH_LONG).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_WRITE);

            }
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MANAGE_DOCUMENTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.MANAGE_DOCUMENTS)) {
                    Toast.makeText(AlbumActivity.this, "Need to access Storage", Toast.LENGTH_LONG).show();
                }
                requestPermissions(new String[]{Manifest.permission.MANAGE_DOCUMENTS}, REQUEST_MANAGE_DOCUMENTS);
            }

        }
    }

}

