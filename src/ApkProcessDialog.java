import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class ApkProcessDialog extends JDialog {
    private String apkFile;
    private JLabel mainLabel;
    private JTextArea detailLabel;
    private JProgressBar progressBar;
    private JScrollPane scrollPane;
    private Document document;

    public void setApkFile(String apkFile) {
        this.apkFile = apkFile;
    }

    public ApkProcessDialog(Frame owner) {
        super(owner);
        JPanel panel = new JPanel();
        setTitle("Processing Apk...");
        panel.setLayout(new BorderLayout());
        setSize(400, 280);
        mainLabel = new JLabel("");
        progressBar = new JProgressBar();
        progressBar.setMaximum(100);
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(2, 1));
        top.add(mainLabel);
        top.add(progressBar);
        scrollPane = new JScrollPane();
        scrollPane.setSize(400, 300);
        detailLabel = new JTextArea("");
        detailLabel.setLineWrap(true);
        detailLabel.setWrapStyleWord(true);
        document = new DefaultStyledDocument();
        detailLabel.setDocument(document);
        scrollPane.setViewportView(detailLabel);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        setContentPane(panel);
        setLocationRelativeTo(getOwner());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.out.println("closing...");
                if (processThread != null) {
                    processThread.stop();
                }
            }
        });
    }

    private Thread processThread;
    ApkCrack apkCrack;

    public void start() {
        apkCrack = new ApkCrack();
        ApkConfig config = ConfigUtil.loadConfig();
        apkCrack.setApkFile(apkFile);
        apkCrack.setCertFile(config.certFile);
        apkCrack.setStoreFile(config.storeFile);
        apkCrack.setStorePassword(config.storePassword);
        apkCrack.setKeyAlias(config.keyAlias);
        apkCrack.setKeyPassword(config.keyPassword);
        apkCrack.setDebuggable(true);
        setVisible(true);
        processThread = new Thread(() -> {
            ProgressUtil.init();
            setProgressHandler();
            try {
                apkCrack.start();
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog(e);
            }
        });
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        processThread.start();
    }

    private void showErrorDialog(Exception e) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        try {
            document.insertString(document.getLength(), e.getMessage() + "\n", attrs);
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        mainLabel.setText("Process failed!");
    }

    private StringBuilder sb = new StringBuilder();

    private void setProgressHandler() {
        ProgressUtil.setMsgHandler(new ProgressUtil.ProgressHandler() {
            @Override
            public void progress(String msg1, String msg2, float progress) {

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (msg1 != null) {
                            SimpleAttributeSet attrs = new SimpleAttributeSet();
                            try {
                                document.insertString(document.getLength(), msg1 + "\n", attrs);
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        }
                        if (msg2 != null) {
                            mainLabel.setText(msg2);
                        }
                        if (progress > 0) {
                            progressBar.setValue((int) (progress * 100));
                        }
                        if ("done...".equals(msg1)) {
                            showDoneDialog();
                        }

                    }

                });

            }
        });
    }

    private void showDoneDialog() {
        int result = JOptionPane.showConfirmDialog(this, "Process finished!Open file in directory?", "Message", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == 0) {
            String outFile = apkCrack.getOutFile();
            File file = new File(outFile);
            try {
                Desktop.getDesktop().open(file.getParentFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
