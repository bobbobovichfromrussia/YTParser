import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLOutput;
import java.util.Observable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.*;
import java.util.concurrent.*;

public class FileChooser extends JFrame implements ActionListener {
    private static JLabel jLabel;
    private String path;

    private JFrame jFrame;
    private JButton openButton;
    private JPanel jPanel;
    private JButton nextButton;
    private JProgressBar progressBar;

    public FileChooser(){
        // frame to contains GUI elements
        jLabel = new JLabel("No File Selected");

        jFrame = new JFrame("File Chooser");
        jFrame.setSize(500, 200);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        openButton = new JButton("Select List");
        openButton.addActionListener(this);

        nextButton = new JButton("Next");
        nextButton.addActionListener(this);


        jPanel = new JPanel();
        jPanel.add(openButton);
        jPanel.add(jLabel);

        jFrame.add(jPanel);
        jFrame.show();
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource()==openButton){
            JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            int response = fileChooser.showOpenDialog(null);

            if (response == JFileChooser.APPROVE_OPTION) {
                path = fileChooser.getSelectedFile().getAbsolutePath();
                jLabel.setText("File Selected: " + path);
                jPanel.add(nextButton);
            } else jLabel.setText("No File Selected");
        } else if(evt.getSource()==nextButton){
            openButton.setVisible(false);
            nextButton.setVisible(false);
            String updatedPath = getUpdatedPath(path);
            jLabel.setText("Trying");

            try {
                MyTest.execute(path,updatedPath);
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUpdatedPath(String fullPath){
        int index = fullPath.lastIndexOf("/");
        String fileName = fullPath.substring(index + 1);
        String updatedName = fileName.replace(".","-UPDATED.");
        return fullPath.replace(fileName,updatedName);
    }


}