package android.microntek;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RunServiceModel {

    private Drawable appIcon;
    private String appLabel;
    private Intent intent;
    private int pid;
    private String pkgName;
    private String processName;
    private String serviceName;
    private int uid;

}
