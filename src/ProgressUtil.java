import brut.androlib.Androlib;
import brut.androlib.res.AndrolibResources;

import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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

    private static ProgressHandler progressHandler;

    private static ProgressHandler getHandler() {
        return progressHandler;
    }

    private static Executor executor = Executors.newSingleThreadExecutor();


    public static void init() {
        for (Class cls : LoggerClasses) {
            Logger logger = Logger.getLogger(cls.getName());
            clearHandler(logger);
            logger.addHandler(new Handler() {
                @Override
                public void publish(LogRecord record) {
                    System.out.println(record.getMessage());
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (getHandler() == null) return;
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
                            getHandler().progress(msg, msg2, progress);
                        }
                    });
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

    private static void clearHandler(Logger logger) {
        try {
            Field field = logger.getClass().getDeclaredField("handlers");
            field.setAccessible(true);
            CopyOnWriteArrayList list = (CopyOnWriteArrayList) field.get(logger);
            list.clear();
            System.out.println("handler size:" + list.size());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setMsgHandler(ProgressHandler h) {
        progressHandler = h;
    }

    public static void main(String[] args) {
        setMsgHandler(new ProgressHandler() {
            @Override
            public void progress(String msg1, String msg2, float progress) {
                System.out.println(msg1 + "," + msg2 + "," + progress);
            }
        });
        Logger.getLogger(Androlib.class.getName()).info("test");
    }
}
