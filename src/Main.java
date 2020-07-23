public class Main {

    public static void main(String[] args) {
        ProgressUtil.init();
        try {
            new ApkCrack().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
