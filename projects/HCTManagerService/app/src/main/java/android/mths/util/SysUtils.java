package android.mths.util;

import android.content.Context;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;

import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SysUtils {

    /**
     * Checks if a GPS card is mounted and readable.
     *
     * <p>This method iterates through all available storage volumes and checks if any of them
     * is a public, readable SD card with the description "GPS".
     *
     * @param context The context to use for accessing system services.
     * @return {@code true} if a GPS card is mounted and readable, {@code false} otherwise.
     */
    public boolean isGpsCardMounted(Context context) {
        StorageManager storageManager = context.getSystemService(StorageManager.class);
        List<VolumeInfo> volumes = storageManager.getVolumes();
        for (VolumeInfo vol : volumes) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC && vol.isMountedReadable()) {
                DiskInfo disk = vol.getDisk();
                if (disk.isSd() && storageManager.getBestVolumeDescription(vol).equals("GPS")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the device type (e.g., "SD", "USB") from the given storage path.
     *
     * @param context The context to use for accessing system services.
     * @param path The storage path to check.
     * @return The device type as a string, or an empty string if not found.
     */
    public String getDeviceTypeFromPath(Context context, String path) {
        StorageManager storageManager = context.getSystemService(StorageManager.class);
        List<VolumeInfo> volumes = storageManager.getVolumes();
        for (VolumeInfo vol : volumes) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC && vol.isMountedReadable() &&
                vol.getPath().getPath().equals(path)) {
                DiskInfo disk = vol.getDisk();
                if (disk.isSd()) {
                    return storageManager.getBestVolumeDescription(vol);
                } else if (disk.isUsb()) {
                    return "USB";
                }
            }
        }
        return "";
    }

}
