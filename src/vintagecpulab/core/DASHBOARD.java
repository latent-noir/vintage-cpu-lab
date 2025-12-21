package vintagecpulab.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.print.PrinterException;

public class DASHBOARD extends JFrame {

    private static final Color DARK_BG = new Color(20, 20, 30);
    private static final Color PANEL_BG = new Color(30, 35, 45);
    private static final Color BLUE_ACCENT = new Color(70, 130, 200);
    private static final Color BLUE_LIGHT = new Color(100, 160, 230);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color TEXT_LIGHT = new Color(220, 220, 220);
    private static final Color BORDER_COLOR = new Color(60, 70, 85);
    private static final Color CONSOLE_BG = new Color(15, 15, 20);
    private static final Color CONSOLE_TEXT = new Color(100, 220, 120);
    private static final Color BTN_PRIMARY = new Color(70, 130, 200);
    private static final Color BTN_SUCCESS = new Color(50, 150, 80);
    private static final Color BTN_WARNING = new Color(200, 140, 50);
    private static final Color BTN_DANGER = new Color(180, 60, 60);

    private final CPU cpu;
    private final ROM romWindow;
    private final RAM ramWindow;
    private final CPUView cpuView;
    private final Editeur editeur;
    private final Programme programmeWindow;
    private ProgramManager programManager;

    private JTextArea assemblerCodeArea;
    private JTextArea consoleOutput;

    public DASHBOARD() {
        this.cpu = new CPU();
        this.romWindow = new ROM();
        this.ramWindow = new RAM();
        this.cpuView = new CPUView();
        this.editeur = new Editeur();
        this.programmeWindow = new Programme();

        romWindow.setVisible(false);
        ramWindow.setVisible(false);
        programmeWindow.setVisible(false);

        this.programManager = new ProgramManager(cpu, cpuView,
                ramWindow.getModel(), romWindow.getModel());

        setTitle("Vintage CPU Lab - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLayout(new BorderLayout());
        getContentPane().setBackground(DARK_BG);

        setJMenuBar(createMenuBarWithButtons());
        add(createLeftPanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
        updateAllDisplays();
    }

    private JMenuBar createMenuBarWithButtons() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PANEL_BG);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BLUE_ACCENT));

        JMenu menuFile = createStyledMenu("Fichier");
        JMenuItem itemNew = createStyledMenuItem("Nouveau", KeyEvent.VK_N);
        itemNew.addActionListener(e -> newFileAction());
        JMenuItem itemOpen = createStyledMenuItem("Ouvrir...", KeyEvent.VK_O);
        itemOpen.addActionListener(e -> editeur.loadFromFile());
        JMenuItem itemSave = createStyledMenuItem("Enregistrer", KeyEvent.VK_S);
        itemSave.addActionListener(e -> saveFileAction());
        JMenuItem itemLoadPath = createStyledMenuItem("Charger (Chemin ou URL)", 0);
        itemLoadPath.addActionListener(e -> editeur.loadFromPath());
        JMenuItem itemPrint = createStyledMenuItem("Imprimer...", KeyEvent.VK_P);
        itemPrint.addActionListener(e -> printAction());
        JMenuItem itemQuit = createStyledMenuItem("Quitter", KeyEvent.VK_Q);
        itemQuit.addActionListener(e -> System.exit(0));

        menuFile.add(itemNew);
        menuFile.add(itemOpen);
        menuFile.add(itemSave);
        menuFile.addSeparator();
        menuFile.add(itemLoadPath);
        menuFile.addSeparator();
        menuFile.add(itemPrint);
        menuFile.addSeparator();
        menuFile.add(itemQuit);

        JMenu menuView = createStyledMenu("Visualisation");

        JMenuItem itemRom = createStyledMenuItem("ROM (Mémoire Programme)", 0);
        itemRom.addActionListener(e -> {
            romWindow.setVisible(true);
            romWindow.toFront();
        });

        JMenuItem itemRam = createStyledMenuItem("RAM (Mémoire Données)", 0);
        itemRam.addActionListener(e -> {
            ramWindow.setVisible(true);
            ramWindow.toFront();
        });

        JMenuItem itemProgramme = createStyledMenuItem("Programme (Instructions Assemblées)", 0);
        itemProgramme.addActionListener(e -> {
            programmeWindow.setVisible(true);
            programmeWindow.toFront();
        });

        menuView.add(itemRom);
        menuView.add(itemRam);
        menuView.add(itemProgramme);
        menuView.addSeparator();

        JMenu menuHelp = createStyledMenu("Aide");
        JMenuItem itemAbout = createStyledMenuItem("À propos", 0);
        itemAbout.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Simulateur Motorola 6809 — Projet Module\n" +
                                "Auteur: latent-noir\n\n" +
                                "Architecture MVC Refactorisée",
                        "À propos",
                        JOptionPane.INFORMATION_MESSAGE));
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuView);
        menuBar.add(menuHelp);
        menuBar.add(Box.createHorizontalGlue());

        JButton btnAssemble = createStyledButton(" Assembler", BTN_PRIMARY);
        btnAssemble.addActionListener(this::handleAssemble);

        JButton btnRun = createStyledButton(" Exécuter", BTN_SUCCESS);
        btnRun.addActionListener(this::handleRun);

        JButton btnStep = createStyledButton(" Pas à Pas", BTN_WARNING);
        btnStep.addActionListener(this::handleStep);

        JButton btnReset = createStyledButton(" Reset", BTN_DANGER);
        btnReset.addActionListener(this::handleReset);

        menuBar.add(btnAssemble);
        menuBar.add(Box.createHorizontalStrut(8));
        menuBar.add(btnRun);
        menuBar.add(Box.createHorizontalStrut(8));
        menuBar.add(btnStep);
        menuBar.add(Box.createHorizontalStrut(8));
        menuBar.add(btnReset);
        menuBar.add(Box.createHorizontalStrut(10));

        return menuBar;
    }

    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(TEXT_COLOR);
        menu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return menu;
    }

    private JMenuItem createStyledMenuItem(String text, int keyEvent) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (keyEvent != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyEvent, InputEvent.CTRL_DOWN_MASK));
        }
        return item;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFocusable(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(TEXT_COLOR);
        btn.setBackground(bgColor);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setPreferredSize(new Dimension(450, getHeight()));
        leftPanel.setBackground(DARK_BG);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 8));

        assemblerCodeArea = new JTextArea(" ");
        assemblerCodeArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 15));
        assemblerCodeArea.setBackground(PANEL_BG);
        assemblerCodeArea.setForeground(TEXT_LIGHT);
        assemblerCodeArea.setCaretColor(BLUE_LIGHT);
        assemblerCodeArea.setSelectionColor(BLUE_ACCENT);
        assemblerCodeArea.setLineWrap(false);
        assemblerCodeArea.setTabSize(4);

        JScrollPane scrollPane = new JScrollPane(assemblerCodeArea);
        scrollPane.setBackground(PANEL_BG);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        "Éditeur - Code Assembleur 6809",
                        0,
                        0,
                        new Font("Segoe UI", Font.BOLD, 14),
                        BLUE_LIGHT
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        leftPanel.add(scrollPane, BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBackground(DARK_BG);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 8, 15, 15));

        JPanel cpuPanel = new JPanel(new BorderLayout());
        cpuPanel.setBackground(Color.BLACK);
        cpuPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BLUE_ACCENT, 2),
                        "Architecture Interne du CPU Motorola 6809",
                        0,
                        0,
                        new Font("Segoe UI", Font.BOLD, 15),
                        BLUE_LIGHT
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        Container cpuContentPane = cpuView.getContentPane();
        if (cpuContentPane instanceof JComponent) {
            ((JComponent) cpuContentPane).setOpaque(true);
            cpuContentPane.setBackground(Color.BLACK);
        }

        cpuPanel.add(cpuContentPane, BorderLayout.CENTER);
        centerPanel.add(cpuPanel, BorderLayout.CENTER);

        consoleOutput = new JTextArea("Console Système 6809 - Prêt à simuler\n");
        consoleOutput.setEditable(false);
        consoleOutput.setRows(6);
        consoleOutput.setFont(new Font("Consolas", Font.PLAIN, 13));
        consoleOutput.setBackground(CONSOLE_BG);
        consoleOutput.setForeground(CONSOLE_TEXT);
        consoleOutput.setCaretColor(Color.GREEN);

        JScrollPane consoleScrollPane = new JScrollPane(consoleOutput);
        consoleScrollPane.setBackground(CONSOLE_BG);
        consoleScrollPane.getViewport().setBackground(CONSOLE_BG);
        consoleScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(BLUE_ACCENT, 2),
                        " Sortie Console ",
                        0,
                        0,
                        new Font("Segoe UI", Font.BOLD, 14),
                        BLUE_LIGHT
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        centerPanel.add(consoleScrollPane, BorderLayout.SOUTH);
        return centerPanel;
    }

    private void newFileAction() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Créer un nouveau fichier ? L'éditeur sera vidé.",
                "Nouveau", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            assemblerCodeArea.setText("");
            editeur.clearEditor();
            logConsole("Nouveau fichier créé.");
        }
    }


    private void saveFileAction() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("program.asm"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write(assemblerCodeArea.getText());
                logConsole("✓ Fichier enregistré : " + f.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erreur sauvegarde : " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printAction() {
        try {
            boolean complete = assemblerCodeArea.print();
            if (complete) {
                logConsole(" Impression terminée.");
            } else {
                logConsole(" Impression annulée.");
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur impression: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAssemble(ActionEvent e) {
        String code = assemblerCodeArea.getText();
        logConsole("Assemblage du code en cours...");

        programmeWindow.clear();

        if (programManager.loadProgram(code)) {
            logConsole("Programme chargé: " + programManager.getProgramLines().size() + " lignes");

            if (programManager.assemble()) {
                logConsole(" Assemblage terminé avec succès");

            
                logConsole("CONTENU DE LA ROM (OCTETS GÉNÉRÉS):");

                int totalBytes = 0;
                int startAddr = cpu.getPC();

                for (int addr = startAddr; addr < startAddr + 100; addr++) {
                    String byteValue = romWindow.read(addr);

                    if (byteValue != null && !byteValue.equals("FF")) {
                        totalBytes++;
                        logConsole(String.format("  $%04X: %s", addr, byteValue));
                    } else {
                        
                        break;
                    }
                }

                logConsole(" TOTAL: " + totalBytes + " octets en ROM");
                logConsole("  Octets assemblés (ProgramManager): " +
                        programManager.getLastAssembledBytes());
                editeur.setEditorText(code);

                programmeWindow.loadFromProgramManager(programManager, cpu);

                programmeWindow.highlightFromCPU(cpu);

                programmeWindow.setVisible(true);
                programmeWindow.toFront();

                updateAllDisplays();

                logConsole("Programme chargé dans la visualisation");
                logConsole("PC initial: $" + CPU.decimalToHex(cpu.getPC(), 4));
            } else {
                logConsole("Erreur d'assemblage");
            }
        } else {
            logConsole("Erreur de chargement");
        }
    }


    private void handleRun(ActionEvent e) {
        logConsole("Exécution complète (Run)");
        try {
            programManager.runProgram();

            programmeWindow.highlightFromCPU(cpu);

            logConsole("Programme terminé");
            logConsole("État final:");
            logConsole("PC=$" + CPU.decimalToHex(cpu.getPC(), 4) +
                    " A=$" + CPU.decimalToHex(cpu.getA(), 2) +
                    " B=$" + CPU.decimalToHex(cpu.getB(), 2));

            updateAllDisplays();
        } catch (Exception ex) {
            logConsole(" ERREUR: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleStep(ActionEvent e) {
        try {
            int pcBefore = cpu.getPC();

            if (programManager.step()) {
                int pcAfter = cpu.getPC();

                logConsole("PC: $" + CPU.decimalToHex(pcBefore, 4) +
                        " $" + CPU.decimalToHex(pcAfter, 4));
                logConsole("   A=$" + CPU.decimalToHex(cpu.getA(), 2) +
                        " B=$" + CPU.decimalToHex(cpu.getB(), 2) +
                        " X=$" + CPU.decimalToHex(cpu.getX(), 4));

                programmeWindow.highlightFromCPU(cpu);

                programmeWindow.setVisible(true);
                programmeWindow.toFront();

                updateAllDisplays();
            } else {
                logConsole("Programme terminé ou erreur");
                logConsole("PC final: $" + CPU.decimalToHex(cpu.getPC(), 4));
            }
        } catch (Exception ex) {
            logConsole(" ERREUR Step: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du Pas à Pas : " + ex.getMessage(),
                    "Erreur CPU", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReset(ActionEvent e) {
        programManager.reset();

        if (romWindow != null) {
            romWindow.clear();
        }

        if (ramWindow != null) {
            ramWindow.clear();
        }

        if (programmeWindow != null) {
            programmeWindow.resetHighlight();
            if (programManager.isProgramLoaded()) {
                
                programmeWindow.loadFromProgramManager(programManager, cpu);
                programmeWindow.highlightFromCPU(cpu);
            } else {
                programmeWindow.clear();
            }
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Réinitialiser aussi le programme visible (Visualisation) ?",
                "Réinitialiser", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            editeur.clearViewer();
        }

        cpuView.resetDisplay();
        updateAllDisplays();

        logConsole("Simulateur réinitialisé");
        logConsole("PC: $" + CPU.decimalToHex(cpu.getPC(), 4));
    }

    private void updateAllDisplays() {
        cpuView.updateFromCPU(cpu);
        cpuView.repaint();
    }

    private void logConsole(String message) {
        consoleOutput.append(message + "\n");
        consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
    }
}
