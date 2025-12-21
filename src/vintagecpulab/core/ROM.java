package vintagecpulab.core;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/* ROM - Read Only Memory (Mémoire Programme) */
public class ROM extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final int ROM_START = 0xFC00;
    private static final int ROM_END = 0xFFFF;
    private static final int ROM_SIZE = ROM_END - ROM_START + 1;
    private static final String DEFAULT_VALUE = "FF";
    private static final Color BG_DARK = new Color(25, 25, 28);
    private static final Color BG_DARKER = new Color(18, 18, 20);
    private static final Color ORANGE_PRIMARY = new Color(255, 140, 0);
    private static final Color ORANGE_ACCENT = new Color(255, 165, 0);
    private static final Color TEXT_LIGHT = new Color(220, 220, 220);
    private static final Color TEXT_ORANGE = new Color(255, 180, 50);
    private static final Color GRID_COLOR = new Color(60, 60, 65);
    private static final Color SELECTION_BG = new Color(255, 140, 0, 50);
    private static final Color HEADER_BG = new Color(40, 40, 45);
    private final DefaultTableModel model;
    private final JTable table;


    public ROM() {
        setTitle(" ROM - Mémoire Programme");
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(310, 80, 280, 400);

        model = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        initializeROM();
        table = new JTable(model);
        createUI();
    }

    private void initializeROM() {
        model.addColumn("Adresse");
        model.addColumn("Donnée");

        for (int i = ROM_START; i <= ROM_END; i++) {
            String address = intToHex(i, 4);
            model.addRow(new Object[]{address, DEFAULT_VALUE});
        }
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
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(ORANGE_ACCENT);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(ORANGE_PRIMARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 28));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ORANGE_PRIMARY));

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);

        DefaultTableCellRenderer addrRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("JetBrains Mono", Font.BOLD, 10));

                if (!isSelected) {
                    setBackground(BG_DARKER);
                    setForeground(TEXT_ORANGE);
                }
                return c;
            }
        };

        DefaultTableCellRenderer dataRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("JetBrains Mono", Font.BOLD, 11));

                if (!isSelected) {
                    setBackground(BG_DARK);
                    if ("FF".equals(value)) {
                        setForeground(new Color(100, 100, 105));
                    } else {
                        setForeground(ORANGE_ACCENT);
                    }
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(addrRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(dataRenderer);

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
        panel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, ORANGE_PRIMARY));

        JLabel iconLabel = new JLabel("💾");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        panel.add(iconLabel);

        JLabel infoLabel = new JLabel(String.format(
                "0x%04X → 0x%04X • %d bytes",
                ROM_START, ROM_END, ROM_SIZE
        ));
        infoLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 9)); 
        infoLabel.setForeground(TEXT_ORANGE);
        panel.add(infoLabel);

        return panel;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public void write(int address, String value) {
        int row = addressToRow(address);

        if (!isValidRow(row)) {
            System.err.println("ROM: Adresse invalide: 0x" +
                    Integer.toHexString(address).toUpperCase());
            return;
        }

        value = value.toUpperCase().trim();

        if (value.length() == 2) {
            model.setValueAt(value, row, 1);
        } else if (value.length() == 4) {
            if (isValidRow(row + 1)) {
                model.setValueAt(value.substring(0, 2), row, 1);
                model.setValueAt(value.substring(2, 4), row + 1, 1);
            }
        } else {
            System.err.println("ROM: Valeur invalide: " + value);
        }
    }

    public String read(int address) {
        int row = addressToRow(address);

        if (!isValidRow(row)) {
            return DEFAULT_VALUE;
        }

        return (String) model.getValueAt(row, 1);
    }



    public void clear() {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(DEFAULT_VALUE, i, 1);
        }
    }


    private int addressToRow(int address) {
        return address - ROM_START;
    }

    private boolean isValidRow(int rowIndex) {
        return rowIndex >= 0 && rowIndex < model.getRowCount();
    }

    private String intToHex(int value, int digits) {
        String hex = Integer.toHexString(value).toUpperCase();
        while (hex.length() < digits) {
            hex = "0" + hex;
        }
        return hex;
    }

    @Deprecated
    public static void setStringToColumn1(int rowIndex, String value) {
        System.err.println("ATTENTION: Méthode statique dépréciée utilisée!");
    }

    @Deprecated
    public static void ved_rom() {
        System.err.println("ATTENTION: Méthode statique dépréciée utilisée!");
    }
}
