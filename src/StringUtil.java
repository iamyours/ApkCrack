import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StringUtil {
    private static final String NET_CONFIG_XML =
            "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n" +
                    "<network-security-config>\n" +
                    "    <base-config cleartextTrafficPermitted=\"true\">\n" +
                    "        <trust-anchors>\n" +
                    "            <certificates src=\"system\"/>\n" +
                    "            <certificates src=\"user\"/>\n" +
                    "        </trust-anchors>\n" +
                    "    </base-config>\n" +
                    "</network-security-config>";

    public static InputStream netConfigStream() {
        return new ByteArrayInputStream(NET_CONFIG_XML.getBytes());
    }
}
