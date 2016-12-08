package com.tmbao.shpictures.forms;

import com.sun.tools.javac.util.Pair;
import com.tmbao.shpictures.server.ClientIdentifier;
import com.tmbao.shpictures.server.SharedPicture;
import com.tmbao.shpictures.utils.ImageFilter;
import com.tmbao.shpictures.utils.Serializer;
import com.tmbao.shpictures.utils.Settings;
import com.tmbao.shpictures.server.Package;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class DialogUpload extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tfOwner;
    private JTextField tfDesc;
    private JTextField tfPath;
    private JButton buttonBrowse;
    private InetAddress serverAddress;
    private int portNumber;
    private String clientName;
    private static Socket clientSocket;
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private final JFileChooser fc = new JFileChooser();
    //private SharedPicture picture;
    private ClientIdentifier identifier = null;

    public DialogUpload(InetAddress serverAddress, int portNumber, String clientName) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        fc.addChoosableFileFilter(new ImageFilter());
        fc.setAcceptAllFileFilterUsed(false);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.serverAddress = serverAddress;
        this.portNumber = portNumber;
        this.clientName = clientName;
        buttonBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(DialogUpload.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    tfPath.setText(file.getAbsolutePath());
                }
            }
        });
    }

    private void onOK() {
        try {
            this.setTitle("Please wait...");
            buttonBrowse.setEnabled(false);
            buttonCancel.setEnabled(false);
            buttonOK.setEnabled(false);

            SharedPicture picture = getNewPicture();

            if (picture != null) {
                // First, get a session id
                identifier = getClientIdenfier();

                if (identifier != null) {
                    this.setTitle("Successfully connected with ID " + identifier.getClientId());

                    // Serialize and split into chunks
                    List<Pair<Integer, byte[]>> serializedSharedPics = Serializer.serialize(picture, Settings.BUFFER_SIZE);
                    boolean success = true;
                    for (Pair<Integer, byte[]> serializedSharedPic : serializedSharedPics) {
                        Package pkg = new Package(identifier, Package.PackageType.UPLOAD,
                                serializedSharedPic.snd, serializedSharedPic.fst);
                        // Send a chunk
                        outputStream.write(Serializer.serialize(pkg));
                        outputStream.flush();
                        // Wait for response
                        byte[] bytes = new byte[Settings.BUFFER_CAPACITY];

                        try {
                            int length = inputStream.read(bytes);
                            pkg = (Package) Serializer.deserialize(bytes);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (pkg.getType() != Package.PackageType.SUCCESS) {
                            JOptionPane.showMessageDialog(this, "Upload Failed.");
                            success = false;
                            break;
                        }
                    }
                    if(success) {
                        JOptionPane.showMessageDialog(this, "Upload Success.");
                    }
                }
            }
            closeConnection(identifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private ClientIdentifier getClientIdenfier() throws IOException {
        // Initiate socket
        clientSocket = new Socket(serverAddress, portNumber);
        inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();

        ClientIdentifier identifier = new ClientIdentifier(clientName, "");
        Package pkg = new Package(identifier, Package.PackageType.INITIATE);
        // Send an initial package to get a session id
        try {
            outputStream.write(Serializer.serialize(pkg));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes = new byte[Settings.BUFFER_CAPACITY];
        try {
            int length = inputStream.read(bytes);
            pkg = (Package) Serializer.deserialize(bytes);

            if (pkg.getType() == Package.PackageType.SUCCESS) {
                System.out.printf("Successfully connected.\nClient Id: %s\n", pkg.getClientIdentifier().getClientId());
                return pkg.getClientIdentifier();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SharedPicture getNewPicture() {
        String ownerName = tfOwner.getText();
        if (ownerName.equals("")) {
            ownerName = clientName;
        }

        String description = tfDesc.getText();


        File inputFile = new File(tfPath.getText());
        BufferedImage image = null;
        try {
            image = ImageIO.read(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new SharedPicture(ownerName, description, "", image);
    }

    private static void closeConnection(ClientIdentifier identifier) throws IOException {
        if (identifier != null) {
            Package pkg = new Package(identifier, Package.PackageType.CLOSE);
            try {
                outputStream.write(Serializer.serialize(pkg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inputStream.close();
        outputStream.close();
        clientSocket.close();
    }
}
