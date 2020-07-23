import brut.androlib.Androlib;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkOptions;
import brut.common.BrutException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

public class ApkCrack {
    private boolean debuggable;
    private boolean networkSecurityConfig;
    private String certFile;
    private String certName;
    private String certFileName;
    private String storeFile;
    private String storePassword;
    private String keyAlias;
    private String keyPassword;
    private String apkFile;
    private String outFile;

    private String buildPath;

    private static final String ANDROID_MANIFEST_PATH = "AndroidManifest.xml";
    private static final String ATTR_NETCONFIG = "android:networkSecurityConfig";
    private static final String ATTR_DEBUG = "android:debuggable";
    private static final String ATTR_CERT = "certificates";
    private static final String ATTR_TRUST = "trust-anchors";
    private long s;
    private static final Logger LOG = Logger.getLogger(ApkCrack.class.getName());

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public void setStoreFile(String storeFile) {
        this.storeFile = storeFile;
    }

    public void setStorePassword(String storePassword) {
        this.storePassword = storePassword;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public void setApkFile(String apkFile) {
        this.apkFile = apkFile;
    }

    public String getOutFile() {
        return outFile;
    }

    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    public void start() throws Exception {
        s = System.currentTimeMillis();
        if (storeFile != null) {
            networkSecurityConfig = true;

            setNetWork();
            setOutFile();
        } else {
            initConfig();
        }
        setBuildPath();
        decodeApk();
        hook();

        generateApk();
        signApk();
        LOG.info("done...");
    }

    private void setBuildPath() {
        File file = new File(apkFile);
        buildPath = new File(file.getParent(), "tmpBuild").getAbsolutePath();
    }

    private void setOutFile() {
        File file = new File(apkFile);
        String parent = file.getParent();
        String fileName = file.getName();
        outFile = new File(parent, "out-" + fileName).getAbsolutePath();
    }

    private void signApk() {
        String cmd = "jarsigner -verbose -keystore " + storeFile + " -storepass " + storePassword + " -keypass " + keyPassword + " " + outFile + " " + keyAlias;
        LOG.info("start sign apk...");
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String s = br.readLine();
                if (s == null)
                    break;
                LOG.info(s);
            }
            br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info(">>>> finished....." + (System.currentTimeMillis() - s) + "ms");
    }

    //generate apk with apktool
    private void generateApk() throws Exception {
        LOG.info("start build unsigned apk...");
        ApkOptions opt = new ApkOptions();
        new Androlib(opt).build(new File(buildPath), new File(outFile));

    }

    //add debuggable ,networkSecurityConfig
    private void hook() throws Exception {
        LOG.info("start hook apk...");
        LOG.info(">>>>>parsing AndroidManifest.xml.....");
        File file = new File(buildPath, ANDROID_MANIFEST_PATH);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        Node application = document.getElementsByTagName("application").item(0);

        NamedNodeMap namedNodeMap = application.getAttributes();
        if (networkSecurityConfig) {
            addCertFile();
            LOG.info(">>>>>adding networkSecurityConfig attribute...");
            Attr attr = document.createAttribute(ATTR_NETCONFIG);
            attr.setValue("@xml/network_security_config");
            namedNodeMap.setNamedItem(attr);
            File xmlPath = new File("build/res/xml/");
            if (!xmlPath.exists()) {
                LOG.info(">>>>> mkdirs xml path...");
                xmlPath.mkdirs();
            }
            addNetConfig(buildPath + "/res/xml/network_security_config.xml");
        }

        if (debuggable) {
            LOG.info(">>>>>adding debuggable attribute...");
            Attr attr = document.createAttribute(ATTR_DEBUG);
            attr.setValue("true");
            namedNodeMap.setNamedItem(attr);
        }
        if (debuggable || networkSecurityConfig) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult reStreamResult = new StreamResult(file);
            transformer.transform(domSource, reStreamResult);
            File tmp = new File(buildPath, ANDROID_MANIFEST_PATH + ".tmp");
            StreamResult consoleResult = new StreamResult(tmp);
            transformer.transform(domSource, consoleResult);
            if (file.exists()) file.delete();
            tmp.renameTo(file);
        }
    }

    private void addNetConfig(String outFile) throws Exception {
        LOG.info(">>>adding certificate to " + outFile);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(StringUtil.netConfigStream());
        Node node = document.getElementsByTagName(ATTR_TRUST).item(0);
        Element element = document.createElement(ATTR_CERT);
        element.setAttribute("src", "@raw/" + certName);
        node.appendChild(element);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        File tmp = new File(buildPath + "netConfig.tmp");
        StreamResult consoleResult = new StreamResult(tmp);
        transformer.transform(domSource, consoleResult);
        File out = new File(outFile);
        File parent = out.getParentFile();
        if (!parent.exists()) parent.mkdirs();
        if (out.exists()) out.delete();
        tmp.renameTo(out);
    }

    private void addCertFile() {
        try {
            FileUtils.copyFile(new File(certFile), new File(buildPath + "/res/raw/", certFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initConfig() {
        LOG.info("init config...");
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(new File("config/config.properties")));
            debuggable = Boolean.parseBoolean(p.getProperty("debuggable"));
            networkSecurityConfig = Boolean.parseBoolean(p.getProperty("networkSecurityConfig"));
            certFile = p.getProperty("certFile");
            storeFile = p.getProperty("storeFile");
            storePassword = p.getProperty("storePassword");
            keyAlias = p.getProperty("keyAlias");
            keyPassword = p.getProperty("keyPassword");
            apkFile = p.getProperty("apkFile");
            outFile = p.getProperty("outFile");

        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info(configValue());
        setNetWork();
        if (!new File(storeFile).exists()) {
            LOG.info("error: storeFile not exist");
        }
        if (!new File(apkFile).exists()) {
            LOG.info("error: apkFile not exist");
        }
    }

    private void setNetWork() {
        if (networkSecurityConfig) {
            File c = new File(certFile);
            certFileName = c.getName();
            certName = certFileName.substring(0, certFileName.lastIndexOf("."));
            if (!c.exists()) {
                LOG.info(certName);
                LOG.info("error: certFile not exist");
            }

        }
    }

    private void decodeApk() {
        File build = new File(buildPath);
        if (build.exists()) {
            LOG.info("remove build.....");
            deleteDir(build);
        }
        LOG.info("start decode apk...");
        LOG.info(">>>>decode apk....");
        ApkDecoder decoder = new ApkDecoder();
        try {
            decoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_NONE);
            decoder.setApkFile(new File(apkFile));
            decoder.setOutDir(build);
            decoder.decode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private String configValue() {
        return ">>>>>config {" +
                "debuggable=" + debuggable +
                ", networkSecurityConfig=" + networkSecurityConfig +
                ", certFile='" + certFile + '\'' +
                ", storeFile='" + storeFile + '\'' +
                ", storePassword='" + storePassword + '\'' +
                ", keyAlias='" + keyAlias + '\'' +
                ", keyPassword='" + keyPassword + '\'' +
                ", apkFile='" + apkFile + '\'' +
                ", outFile='" + outFile + '\'' +
                '}';
    }
}
