import java.io.File;

public class Main {

    public static void main(String[] args) {
        String parent = new File("/yanxx/test/test.file").getName();
        System.out.println(parent);
    }
}
