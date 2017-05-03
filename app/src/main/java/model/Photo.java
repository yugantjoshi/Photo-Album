package model;

import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;


/**
 * Created by yugantjoshi on 4/18/17.
 */

public class Photo extends RealmObject {
    private RealmList<Tag> tagArrayList;
    private String imageURI;
    private String belongToAlbum;
    private boolean isFirstTag = true;

    public Photo() {
        tagArrayList = new RealmList<>();
    }

    public Photo(Uri uri) {
        tagArrayList = new RealmList<>();
        this.imageURI = uri.toString();
    }

    public void addTag(Tag t) {
        tagArrayList.add(t);
    }

    public void editTag(String tagType, String tagValue, int index) {
        tagArrayList.get(index).setTagType(tagType);
        tagArrayList.get(index).setTagValue(tagValue);
    }

    public RealmList<Tag> getTagArrayList() {
        return this.tagArrayList;
    }

    public String getTagsString() {
        String output = "";
        for (int i = 0; i < tagArrayList.size(); i++) {
            output += tagArrayList.get(i).toString();
            if(tagArrayList.size()>1){
                if(i!=tagArrayList.size()-1){
                    output+=", ";
                }
            }
        }
        return output;
    }

    public void setBelongToAlbum(String belongToAlbum) {
        this.belongToAlbum = belongToAlbum;
    }

    public String getBelongToAlbum() {
        return this.belongToAlbum;
    }

    public int findTagIndex(String tagKey, String tagValue){
        for(int i=0; i<tagArrayList.size(); i++){
            if(tagArrayList.get(i).getTagType().equalsIgnoreCase(tagKey) && tagArrayList.get(i).getTagValue().equalsIgnoreCase(tagValue)){
                return i;
            }
        }

        return -1;
    }
    public void removeTag(int index){
        tagArrayList.remove(index);
    }

    public void setImageURI(Uri imageURI) {
        this.imageURI = imageURI.toString();
    }

    public Uri getImageURI() {
        return Uri.parse(this.imageURI);
    }


}
