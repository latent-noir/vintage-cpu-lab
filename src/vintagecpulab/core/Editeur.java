
package vintagecpulab.core;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Editeur {

    private final JFrame editorFrame;
    private final JFrame viewerFrame;
    private final JTextArea editorArea;
    private final JTextArea viewerArea;

    public Editeur() {
        editorFrame = new JFrame("Éditeur de Programme");
        editorFrame.setSize(600, 500);
        editorFrame.setLayout(new BorderLayout());

        editorArea = new JTextArea();
        editorArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane editorScroll = new JScrollPane(editorArea);
        editorScroll.setBorder(BorderFactory.createTitledBorder("Éditeur"));
        editorFrame.add(editorScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton copyBtn = new JButton("Copier vers Visualisation");
        copyBtn.addActionListener(e -> copyToViewer());
        bottom.add(copyBtn);

        JButton closeBtn = new JButton("Fermer");
        closeBtn.addActionListener(e -> editorFrame.setVisible(false));
        bottom.add(closeBtn);
        editorFrame.add(bottom, BorderLayout.SOUTH);

        // Viewer
        viewerFrame = new JFrame("Programme - Visualisation");
        viewerFrame.setSize(600, 500);
        viewerFrame.setLayout(new BorderLayout());

        viewerArea = new JTextArea();
        viewerArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        viewerArea.setEditable(false);
        JScrollPane viewerScroll = new JScrollPane(viewerArea);
        viewerScroll.setBorder(BorderFactory.createTitledBorder("Visualisation"));
        viewerFrame.add(viewerScroll, BorderLayout.CENTER);

        JPanel bottomV = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton updateBtn = new JButton("Mettre à jour depuis éditeur");
        updateBtn.addActionListener(e -> copyToViewer());
        bottomV.add(updateBtn);

        JButton closeVBtn = new JButton("Fermer");
        closeVBtn.addActionListener(e -> viewerFrame.setVisible(false));
        bottomV.add(closeVBtn);
        viewerFrame.add(bottomV, BorderLayout.SOUTH);
    }


    public void setEditorText(String text) {
        editorArea.setText(text);
    }

    public void clearEditor() {
        editorArea.setText("");
    }

    public void clearViewer() {
        viewerArea.setText("");
    }

    public void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Fichiers Assembleur (.s, .asm, .txt)", "s", "asm", "txt"));
        int res = chooser.showOpenDialog(editorFrame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
                editorArea.setText(sb.toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(editorFrame, "Erreur lecture fichier: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadFromPath() {
        String path = JOptionPane.showInputDialog(editorFrame, "Entrez le chemin complet du fichier ou une URL :", "Charger depuis chemin", JOptionPane.PLAIN_MESSAGE);
        if (path == null || path.trim().isEmpty()) return;
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append("\n");
                viewerArea.setText(sb.toString());
                viewerArea.setEditable(false);
                viewerFrame.setVisible(true);
                JOptionPane.showMessageDialog(editorFrame, "Programme chargé depuis le chemin donné.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(editorFrame, "Erreur lecture fichier: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(editorFrame, "Fichier introuvable : " + path, "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void copyToViewer() {
        viewerArea.setText(editorArea.getText());
        viewerArea.setEditable(false);
        viewerFrame.setVisible(true);
        viewerFrame.toFront();
    }
}
