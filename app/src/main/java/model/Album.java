package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


/**
 * Created by yugantjoshi on 4/18/17.
 */

public class Album extends RealmObject{

    private RealmList<Photo> photoArrayList;

   private String albumName;

    public Album(){
        this.albumName = "";
        photoArrayList = new RealmList<>();
    }

    public Album(String albumName){
        this.albumName = albumName;
        photoArrayList = new RealmList<>();
    }
    public RealmList<Photo> getPhotosArrayList(){
        return this.photoArrayList;
    }
    public String getAlbumName(){
        return this.albumName;
    }
    public void setAlbumName(String albumName){
        this.albumName = albumName;
    }
    public void insertPhoto(Photo p){
        photoArrayList.add(p);
    }
    public void removePhoto(Photo p){
        photoArrayList.remove(p);
    }
    public void removePhoto(int index){
        photoArrayList.remove(index);
    }
    public String toString(){
        return this.albumName;
    }


}
