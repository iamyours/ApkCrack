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

    private static final String BUILD_PATH = "build";
    private static final String RAW_PATH = "build/res/raw/";
    private static final String XML_PATH = "build/res/xml/";
    private static final String ANDROID_MANIFEST_PATH = "build/AndroidManifest.xml";
    private static final String ATTR_NETCONFIG = "android:networkSecurityConfig";
    private static final String ATTR_DEBUG = "android:debuggable";
    private static final String ATTR_CERT = "certificates";
    private static final String ATTR_TRUST = "trust-anchors";
    private long s;

    public void start() {
        s = System.currentTimeMillis();
        initConfig();
        decodeApk();
        try {
            hook();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        generateApk();
        signApk();
    }

    private void signApk() {
        String cmd = "jarsigner -verbose -keystore " + storeFile + " -storepass " + storePassword + " -keypass " + keyPassword + " " + outFile + " " + keyAlias;
        System.out.println(">>>>> sign apk ....\n+cmd");
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String s = br.readLine();
                if (s == null)
                    break;
                System.out.println(s);
            }
            br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(">>>> finished....." + (System.currentTimeMillis() - s) + "ms");
    }

    //generate apk with apktool
    private void generateApk() {
        System.out.println(">>>> generate unsigned apk....");
        ApkOptions opt = new ApkOptions();
        try {
            new Androlib(opt).build(new File(BUILD_PATH), new File(outFile));
        } catch (BrutException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //add debuggable ,networkSecurityConfig
    private void hook() throws Exception {
        System.out.println(">>>>>parsing AndroidManifest.xml.....");
        File file = new File(ANDROID_MANIFEST_PATH);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        Node application = document.getElementsByTagName("application").item(0);

        NamedNodeMap namedNodeMap = application.getAttributes();
        Node node = namedNodeMap.getNamedItem(ATTR_NETCONFIG);
        if (networkSecurityConfig) {
            addCertFile();
            if (node != null) {
                String path = "build/res/xml" + node.getNodeValue().substring(4) + ".xml";
                System.out.println(">>>>>find networkSecurityConfig:" + path);

                addNetConfig("config/network_security_config.xml", path);

            } else {//新增文件：network_security_config.xml
                System.out.println(">>>>>adding networkSecurityConfig attribute...");
                Attr attr = document.createAttribute(ATTR_NETCONFIG);
                attr.setValue("@xml/network_security_config");
                namedNodeMap.setNamedItem(attr);
                File xmlPath = new File("build/res/xml/");
                if (!xmlPath.exists()) {
                    System.out.println(">>>>> mkdirs xml path...");
                    xmlPath.mkdirs();
                }
                addNetConfig("config/network_security_config.xml", "build/res/xml/network_security_config.xml");
            }
        }

        if (debuggable) {
            System.out.println(">>>>>adding debuggable attribute...");
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
            File tmp = new File(ANDROID_MANIFEST_PATH + ".tmp");
            StreamResult consoleResult = new StreamResult(tmp);
            transformer.transform(domSource, consoleResult);
            if(file.exists())file.delete();
            tmp.renameTo(file);
        }
    }

    private void addNetConfig(String netConfigXml, String outFile) throws Exception {
        System.out.println(">>>adding certificate to " + outFile);
        File file = new File(netConfigXml);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        Node node = document.getElementsByTagName(ATTR_TRUST).item(0);
        Element element = document.createElement(ATTR_CERT);
        element.setAttribute("src", "@raw/" + certName);
        node.appendChild(element);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        File tmp = new File(netConfigXml + ".tmp");
        StreamResult consoleResult = new StreamResult(tmp);
        transformer.transform(domSource, consoleResult);
        File out = new File(outFile);
        if(out.exists())out.delete();
        tmp.renameTo(out);
    }

    private void addCertFile() {
        try {
            FileUtils.copyFile(new File(certFile), new File(RAW_PATH, certFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initConfig() {
        System.out.println(">>>>>init config...");
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
        System.out.println(configValue());
        if (networkSecurityConfig) {
            File c = new File(certFile);
            certFileName = c.getName();
            certName = certFileName.substring(0, certFileName.lastIndexOf("."));
            if (!c.exists()) {
                System.out.println(certName);
                throw new RuntimeException("certFile not exist");
            }

        }
        if (!new File(storeFile).exists()) {
            throw new RuntimeException("storeFile not exist");
        }
        if (!new File(apkFile).exists()) {
            throw new RuntimeException("apkFile not exist");
        }
    }

    private void decodeApk() {
        File build = new File("build");
        if (build.exists()) {
            System.out.println(">>>>remove build.....");
            deleteDir(build);
        }
        System.out.println(">>>>decode apk....");
        ApkDecoder decoder = new ApkDecoder();
        decoder.setApkFile(new File(apkFile));
        try {
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
