package one.inve.contract.util;

/**
 * @author Clare
 * @date   2018/7/30 0030.
 */
public class PathUtils {
    public static String dataFilePath;

    public static String getDataFileDir() {
        String osName = System.getProperty("os.name");
        String userHome = System.getProperty("user.home");
        if (osName != null && osName.toLowerCase().contains("win")) {
            dataFilePath = userHome + "\\AppData\\Local\\Hashnet\\";
        } else if (osName != null && osName.toLowerCase().contains("linux")) {
            dataFilePath = userHome + "/.config/Hashnet/";
        } else if (osName != null && osName.toLowerCase().contains("mac")) {
            dataFilePath = userHome + "/Library/Application Support/Hashnet/";
        }
        return dataFilePath;
    }
}
