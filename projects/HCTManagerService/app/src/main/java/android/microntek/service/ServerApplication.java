package android.microntek.service;

import android.app.Application;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServerApplication extends Application {

    private int state = 0;

}
