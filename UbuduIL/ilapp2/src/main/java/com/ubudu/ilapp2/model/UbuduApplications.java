package com.ubudu.ilapp2.model;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mgasztold on 11/07/16.
 */
@JsonObject
public class UbuduApplications {
    @JsonField
    List<UbuduApplication> u_applications;

    public UbuduApplications(){

    }

    public List<UbuduApplication> getApplications(){
        return u_applications;
    }

    @Override
    public String toString() {
        String string = "";
        try {
            string = LoganSquare.serialize(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }

    public List<BottomSheetItem> getApplicationsBottomSheetItems() {
        List<BottomSheetItem> result = new ArrayList<>();
        for(UbuduApplication app : u_applications){
            result.add((BottomSheetItem)app);
        }
        return result;
    }
}
