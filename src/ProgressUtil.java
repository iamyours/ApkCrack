import brut.androlib.Androlib;
import brut.androlib.res.AndrolibResources;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ProgressUtil {
    private static String[] detailMsgs = new String[]{
            "remove build.....",
            //decode
            "Loading resource table...",
            "Decoding AndroidManifest.xml with resources..",
            "Loading resource table from file: ",
            "Regular manifest package...",
            "Decoding values */* XMLs...",
            "Copying raw classes",
            "Copying assets and libs...",
            "Copying unknown files...",
            "Copying original files...",
            //hook
            "start hook apk...",

            //build
            "Building resources...",
            "Copying libs... (/lib)",
            "Building apk file...",
            "Copying unknown files/dir...",
            "Built apk...",
            //sign
            "start sign apk...",
            "done..."

    };
    private static String[] msgs = new String[]{
            "init config...",
            "start decode apk...",
            "remove build.....",
            "start hook apk...",
            "start build unsigned apk...",
            "start sign apk...",
            "done..."
    };
    private static Class[] LoggerClasses = new Class[]{
            AndrolibResources.class,
            Androlib.class,
            ApkCrack.class
    };

    public interface ProgressHandler {
        void progress(String msg1, String msg2, float progress);
    }

    public static void setMsgHandler(ProgressHandler progressHandler) {
        for (Class cls : LoggerClasses) {
            Logger logger = Logger.getLogger(cls.getName());
            logger.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    String msg = record.getMessage();
                    String msg2 = null;
                    float progress = 0f;

                    for (int i = 0; i < detailMsgs.length; i++) {
                        if (msg.startsWith(detailMsgs[i])) {
                            progress = (i + 1f) / detailMsgs.length;
                            break;
                        }
                    }
                    for (String m : msgs) {
                        if (msg.startsWith(m)) {
                            msg2 = msg;
                            break;
                        }
                    }
                    progressHandler.progress(msg, msg2, progress);
                }

                @Override
                public void flush() {

                }

                @Override
                public void close() throws SecurityException {

                }
            });
        }
    }
}
