package model;


import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by yugantjoshi on 4/18/17.
 */

public class Tag extends RealmObject{
    private String tagType, tagValue;

    public Tag(){
        this.tagType = "";
        this.tagValue ="";
    }

    public Tag(String tagType, String tagValue){
        this.tagType = tagType;
        this.tagValue = tagValue;
    }
    public String getTagType(){
        return this.tagType;
    }
    public String getTagValue(){
        return tagValue;
    }
    public void setTagType(String tagType){
        this.tagType = tagType;
    }
    public void setTagValue(String tagValue){
        this.tagValue = tagValue;
    }
    public String toString(){
        return this.tagType+": "+this.tagValue;
    }
}
