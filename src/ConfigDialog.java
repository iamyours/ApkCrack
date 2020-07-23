import javax.swing.*;
import java.awt.*;

public class ConfigDialog extends JDialog {
    private JTextField storeFileText;
    private JTextField storePasswordText;
    private JTextField keyAliasText;
    private JTextField keyPasswordText;
    private JTextField certFileText;

    public ConfigDialog(Frame owner) {
        super(owner);
        init();
    }

    private void init() {
        Box vBox = Box.createVerticalBox();
        setSize(400, 300);
        JPanel panel01 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel01.add(new JLabel("StoreFile"));
        setTitle("Config");
        storeFileText = new JTextField(20);
        panel01.add(storeFileText);
        JButton storeBtn = new JButton("Open");
        panel01.add(storeBtn);
        storeBtn.addActionListener(e -> {
            storeFileText.setText(openFile());
        });
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel2.add(new JLabel("StorePassword"));
        storePasswordText = new JPasswordField(10);
        panel2.add(storePasswordText);

        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel3.add(new JLabel("KeyAlias"));
        keyAliasText = new JTextField(15);
        panel3.add(keyAliasText);

        JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel4.add(new JLabel("KeyPassword"));
        keyPasswordText = new JPasswordField(15);
        panel4.add(keyPasswordText);

        JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel5.add(new JLabel("CertFile"));
        certFileText = new JTextField(20);
        panel5.add(certFileText);
        JButton certBtn = new JButton("Open");
        certBtn.addActionListener(e -> {
            certFileText.setText(openFile());
        });
        panel5.add(certBtn);

        JPanel panel6 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton configBtn = new JButton("Ok");
        configBtn.addActionListener(e -> saveConfig());
        panel6.add(configBtn);

        vBox.add(panel01);
        vBox.add(panel2);
        vBox.add(panel3);
        vBox.add(panel4);
        vBox.add(panel5);
        vBox.add(panel6);
        loadConfig();
        setContentPane(vBox);
        setLocationRelativeTo(getOwner());
    }

    private void loadConfig() {
        ApkConfig config = ConfigUtil.loadConfig();
        if (config == null) return;
        certFileText.setText(config.certFile);
        storeFileText.setText(config.storeFile);
        storePasswordText.setText(config.storePassword);
        keyAliasText.setText(config.keyAlias);
        keyPasswordText.setText(config.keyPassword);
    }

    private void saveConfig() {
        ApkConfig config = new ApkConfig();
        config.storeFile = storeFileText.getText();
        config.storePassword = storePasswordText.getText();
        config.keyAlias = keyAliasText.getText();
        config.keyPassword = keyPasswordText.getText();
        config.certFile = certFileText.getText();
        ConfigUtil.saveConfig(config);
        setVisible(false);
    }

    private String openFile() {
        FileDialog dialog = new FileDialog(new Frame(), "File");
        dialog.setVisible(true);
        return dialog.getDirectory() + dialog.getFile();
    }

}
