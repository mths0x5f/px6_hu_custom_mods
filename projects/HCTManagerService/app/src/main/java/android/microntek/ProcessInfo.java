package android.microntek;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInfo {

    public String[] pkgNameList;
    private int pid;
    private int uid;
    private int memSize;
    private String processName;

}
