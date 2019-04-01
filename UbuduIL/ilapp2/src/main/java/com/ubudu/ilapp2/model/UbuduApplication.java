package com.ubudu.ilapp2.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.ubudu.ilapp2.R;

/**
 * Created by mgasztold on 11/07/16.
 */
@JsonObject
public class UbuduApplication extends BottomSheetItem {

    @JsonField
    int id;

    @JsonField
    String name;

    @JsonField
    String namespace_uid;

    @JsonField
    String environment;

    @JsonField
    String normal_proximity_uuid;

    @JsonField
    String secure_proximity_uuid;

    @JsonField
    String service_proximity_uuid;

    @JsonField
    String anti_hacking_protocol;

    @JsonField
    String url;

    public UbuduApplication(){
        super(-1, R.drawable.ic_star_border_black_24dp, "");
    }

    @OnJsonParseComplete
    protected void onParseComplete() {
        setId(id);
        setTitle(name);
        if(environment.equals("production")) {
            setDrawableRes(R.drawable.ic_star_black_24dp);
        } else {
            setDrawableRes(R.drawable.ic_star_border_black_24dp);
        }
    }

    public UbuduApplication(int id, int drawable, String title) {
        super(id, drawable, title);
        name = title;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNamespace_uid() {
        return namespace_uid;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getNormal_proximity_uuid() {
        return normal_proximity_uuid;
    }

    public String getSecure_proximity_uuid() {
        return secure_proximity_uuid;
    }

    public String getService_proximity_uuid() {
        return service_proximity_uuid;
    }

    public String getAnti_hacking_protocol() {
        return anti_hacking_protocol;
    }

    public String getUrl() {
        return url;
    }
}
