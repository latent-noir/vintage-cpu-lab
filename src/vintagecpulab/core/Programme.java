package vintagecpulab.core;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/* Programme - Fenêtre de visualisation du programme assemblé */
public class Programme extends JFrame {

    private static final long serialVersionUID = 1L;

    // ═══════════ PALETTE THÈME SOMBRE VERT ═══════════
    private static final Color BG_DARK = new Color(20, 28, 20);
    private static final Color BG_DARKER = new Color(15, 22, 15);
    private static final Color GREEN_PRIMARY = new Color(80, 200, 120);
    private static final Color GREEN_ACCENT = new Color(100, 220, 140);
    private static final Color TEXT_LIGHT = new Color(220, 220, 220);
    private static final Color TEXT_GREEN = new Color(120, 240, 150);
    private static final Color GRID_COLOR = new Color(60, 70, 60);
    private static final Color SELECTION_BG = new Color(80, 200, 120, 80);
    private static final Color HEADER_BG = new Color(35, 45, 35);
    private static final Color HIGHLIGHT_BG = new Color(80, 200, 120, 150);
    private static final Color BREAKPOINT_BG = new Color(220, 50, 50, 100);

    private final DefaultTableModel model;
    private final JTable table;
    private int currentLineIndex = -1;
    private final Map<Integer, Integer> addressToRowIndex;
    private JLabel statusLabel;

    public Programme() {
        setTitle("⚡ Programme - Visualisation");
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(600, 80, 280, 400);

        model = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        model.addColumn("Adresse");
        model.addColumn("Instruction");

        table = new JTable(model);
        addressToRowIndex = new HashMap<>();

        createUI();
    }

    private void createUI() {
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setBackground(BG_DARKER);
        setContentPane(contentPane);

        table.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        table.setRowHeight(20);
        table.setShowGrid(true);
        table.setGridColor(GRID_COLOR);
        table.setBackground(BG_DARK);
        table.setForeground(TEXT_LIGHT);
        table.setSelectionBackground(HIGHLIGHT_BG);
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(GREEN_PRIMARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 28));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, GREEN_PRIMARY));

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(145);

        DefaultTableCellRenderer addrRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("JetBrains Mono", Font.BOLD, 10));

                if (row == currentLineIndex) {
                    setBackground(HIGHLIGHT_BG);
                    setForeground(Color.WHITE);
                    setFont(new Font("JetBrains Mono", Font.BOLD, 11));
                } else if (!isSelected) {
                    setBackground(BG_DARKER);
                    setForeground(TEXT_GREEN);
                } else {
                    setBackground(SELECTION_BG);
                    setForeground(Color.WHITE);
                }
                return c;
            }
        };

        DefaultTableCellRenderer instrRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.LEFT);
                setFont(new Font("JetBrains Mono", Font.BOLD, 11));

                if (row == currentLineIndex) {
                    setBackground(HIGHLIGHT_BG);
                    setForeground(Color.WHITE);
                    setFont(new Font("JetBrains Mono", Font.BOLD, 12));
                } else if (!isSelected) {
                    setBackground(BG_DARK);
                    setForeground(GREEN_ACCENT);
                } else {
                    setBackground(SELECTION_BG);
                    setForeground(Color.WHITE);
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(addrRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(instrRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_DARK);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel infoPanel = createInfoPanel();
        contentPane.add(infoPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panel.setBackground(HEADER_BG);
        panel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, GREEN_PRIMARY));

        JLabel iconLabel = new JLabel("▶️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        panel.add(iconLabel);

        statusLabel = new JLabel("Instructions assemblées");
        statusLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 9));
        statusLabel.setForeground(TEXT_GREEN);
        panel.add(statusLabel);

        return panel;
    }

    public void addInstruction(String address, String instruction) {
        int rowIndex = model.getRowCount();
        model.addRow(new Object[]{address, instruction});

        try {
            String addrStr = address.replace("$", "").trim();
            int addr = Integer.parseInt(addrStr, 16);
            addressToRowIndex.put(addr, rowIndex);
        } catch (NumberFormatException e) {
            System.err.println(" Adresse invalide: " + address);
        }
    }

    public void clear() {
        model.setRowCount(0);
        addressToRowIndex.clear();
        currentLineIndex = -1;
        updateStatus("Programme vide");
    }

    public void highlightLine(int lineIndex) {
        if (lineIndex >= 0 && lineIndex < model.getRowCount()) {
            currentLineIndex = lineIndex;
            table.repaint();
            table.scrollRectToVisible(table.getCellRect(lineIndex, 0, true));

            String addr = (String) model.getValueAt(lineIndex, 0);
            String instr = (String) model.getValueAt(lineIndex, 1);
            updateStatus(String.format("Ligne %d: %s %s", lineIndex + 1, addr, instr));
        }
    }

    public void highlightByAddress(int pcAddress) {
        Integer rowIndex = addressToRowIndex.get(pcAddress);

        if (rowIndex != null) {
            highlightLine(rowIndex);
        } else {
            currentLineIndex = -1;
            table.repaint();
            updateStatus(String.format("PC=%04X (hors programme)", pcAddress));
        }
    }

    public void highlightFromCPU(CPU cpu) {
        if (cpu != null) {
            highlightByAddress(cpu.getPC());
        }
    }


    public void resetHighlight() {
        currentLineIndex = -1;
        table.repaint();
        updateStatus("Instructions assemblées");
    }

    public void loadFromProgramManager(ProgramManager programManager, CPU cpu) {
        clear();

        if (programManager == null || !programManager.isProgramLoaded()) {
            updateStatus("Aucun programme chargé");
            return;
        }

        java.util.List<String> sourceLines = programManager.getProgramLines();
        int address = 0;
        boolean orgFound = false;

        System.out.println("\nProgramme.loadFromProgramManager() - Début");

        // PRIORITÉ 1: Chercher ORG dans le code source
        for (String line : sourceLines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }

            if (line.toUpperCase().startsWith("ORG")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    try {
                        String addrStr = parts[1].replace("$", "").trim();
                        address = Integer.parseInt(addrStr, 16);
                        orgFound = true;
                        System.out.println("ORG trouvé dans le code: $" +
                                Integer.toHexString(address).toUpperCase());
                        break;
                    } catch (Exception e) {
                        System.err.println("Erreur parse ORG: " + line);
                    }
                }
            }
        }

        // PRIORITÉ 2: Si pas de ORG, utiliser le PC du CPU
        if (!orgFound && cpu != null) {
            address = cpu.getPC();
            System.out.println("Aucun ORG trouvé, utilisation du PC du CPU: $" +
                    Integer.toHexString(address).toUpperCase());
        } else if (!orgFound) {
            System.out.println("Aucun ORG trouvé et pas de CPU, démarrage à $0000");
        }

        int startAddress = address;

        // Charger TOUTES les instructions (y compris END)
        for (String line : sourceLines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }

            // Ignorer ORG dans l'affichage (mais on l'a déjà traité)
            if (line.toUpperCase().startsWith("ORG")) {
                continue;
            }

            //Afficher END (ne plus break )
            String displayLine = line;
            String instructionPart = line;

            // Gérer les étiquettes
            if (InstructionDecoder.hasLabel(line)) {
                displayLine = line; // Garder l'étiquette pour l'affichage
                instructionPart = InstructionDecoder.removeLabel(line);

                if (instructionPart.trim().isEmpty()) {
                    continue; // Étiquette seule
                }
            }

            // Format sans $
            String addrStr = String.format("%04X", address);
            addInstruction(addrStr, displayLine);

            System.out.println(String.format("   %s: %s", addrStr, displayLine));

            //  Si c'est END, arrêter APRÈS l'avoir affiché
            if (line.toUpperCase().equals("END")) {
                System.out.println(" END affiché et traité");
                break;
            }

            // Calculer la taille pour la prochaine instruction
            try {
                InstructionDecoder.DecodedInstruction instr =
                        InstructionDecoder.decode(instructionPart);
                if (instr != null) {
                    int size = programManager.getExecutor().computeInstructionSize(instr);
                    address += size;
                }
            } catch (Exception e) {
                address += 1;
            }
        }

        int instrCount = model.getRowCount();
        updateStatus(String.format("%d instructions assemblées", instrCount));

        System.out.println(String.format(" %d instructions chargées depuis $%04X",
                instrCount, startAddress));
        System.out.println("Mapping adresse→ligne:");
        addressToRowIndex.forEach((addr, row) ->
                System.out.println(String.format("   $%04X → ligne %d", addr, row))
        );
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

}