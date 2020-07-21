import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainUI {
    public static void main(String[] args) {
        openMainPanel();
    }

    private static JTextField apkFileText;

    private static void openMainPanel() {
        JFrame jf = new JFrame("ApkCrack");
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // 第 1 个 JPanel, 使用默认的浮动布局
        JPanel panel01 = new JPanel();
        panel01.add(new JLabel("ApkFile"));
        apkFileText = new JTextField(20);
        panel01.add(apkFileText);
        JButton fileBtn = new JButton("Open");
        fileBtn.addActionListener(e -> apkFileText.setText(openFile()));
        panel01.add(fileBtn);


        // 第 3 个 JPanel, 使用浮动布局, 并且容器内组件居中显示
        JPanel panel03 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton configBtn = new JButton("Config");
        configBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openConfigDialog(jf);
            }
        });
        panel03.add(configBtn);
        JButton goBtn = new JButton("GO");
        goBtn.addActionListener(e -> {
            processApk(jf);
        });
        panel03.add(goBtn);

        Box vBox = Box.createVerticalBox();
        vBox.add(panel01);
        vBox.add(panel03);

        jf.setContentPane(vBox);

        jf.pack();
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);
    }

    private static String openFile() {
        FileDialog dialog = new FileDialog(new Frame(), "File");
        dialog.setVisible(true);
        return dialog.getDirectory() + dialog.getFile();
    }

    private static void processApk(JFrame jf) {
        ApkConfig config = ConfigUtil.loadConfig();
        if (config == null || config.isInvalid()) {
            int result = JOptionPane.showConfirmDialog(jf, "Please config Store File and Cert File!", "Message", JOptionPane.OK_OPTION);
            if (result == 0) {
                openConfigDialog(jf);
            }
            return;
        }
        String apkFile = apkFileText.getText();

        if (apkFile == null || "".equals(apkFile) || !new File(apkFile).exists()) {
            int result = JOptionPane.showConfirmDialog(jf, "Please select apk file", "Message", JOptionPane.OK_OPTION);
            if (result == 0) {
                apkFileText.setText(openFile());
            }
            return;
        }

        ApkProcessDialog dialog = new ApkProcessDialog(jf);
        dialog.setApkFile(apkFile);
        dialog.start();
    }

    private static void openConfigDialog(JFrame jf) {
        ConfigDialog dialog = new ConfigDialog(jf);
        dialog.setVisible(true);
    }
}
