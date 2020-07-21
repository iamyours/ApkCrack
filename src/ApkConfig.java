import org.apache.commons.lang3.StringUtils;

public class ApkConfig {
    public String storeFile;
    public String storePassword;
    public String keyAlias;
    public String keyPassword;
    public String certFile;

    public boolean isInvalid() {
        return StringUtils.isEmpty(storeFile) || StringUtils.isEmpty(storePassword) || StringUtils.isEmpty(keyAlias) || StringUtils.isEmpty(keyPassword) || StringUtils.isEmpty(certFile);
    }
}
