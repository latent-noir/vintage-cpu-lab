package vintagecpulab.core;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.util.HashMap;

/* RAM - Random Access Memory (Mémoire Données) */
public class RAM extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int RAM_START = 0x0000;
    private static final int RAM_END = 0x03FF;
    private static final int RAM_SIZE = 1024;
    private static final String DEFAULT_VALUE = "00";
    private static final Color BG_DARK = new Color(20, 20, 30);
    private static final Color BG_DARKER = new Color(15, 15, 25);
    private static final Color PANEL_BG = new Color(30, 35, 45);
    private static final Color TABLE_BG = new Color(25, 30, 40);
    private static final Color TABLE_ALT = new Color(35, 40, 50);
    private static final Color BLUE_ACCENT = new Color(70, 130, 200);
    private static final Color BLUE_LIGHT = new Color(120, 180, 240);
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color HEADER_BG = new Color(50, 60, 75);
    private static final Color GRID_COLOR = new Color(60, 70, 85);
    private static final Color SELECTION_BG = new Color(70, 130, 200, 50);
    private static final HashMap<Integer, String> memory = new HashMap<>();
    private final DefaultTableModel model;
    private final JTable table;

    public RAM() {
        setTitle("RAM - Mémoire Données");
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(20, 80, 280, 400);

        model = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        };

        model.addColumn("Adresse");
        model.addColumn("Donnée");

        initializeMemory();

        table = new JTable(model);

        model.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();

                if (col == 1 && row >= 0) {
                    String newValue = ((String) model.getValueAt(row, 1)).toUpperCase();

                    if (newValue.matches("[0-9A-Fa-f]{2}")) {
                        int address = RAM_START + row;
                        memory.put(address, newValue);
                        model.setValueAt(newValue, row, 1);
                    } else {
                        int address = RAM_START + row;
                        model.setValueAt(memory.get(address), row, 1);
                    }
                }
            }
        });

        createUI();
    }

    private void initializeMemory() {
        for (int i = 0; i < RAM_SIZE; i++) {
            int address = RAM_START + i;
            memory.put(address, DEFAULT_VALUE);
        }

        for (int i = 0; i < RAM_SIZE; i++) {
            int address = RAM_START + i;
            String addrHex = intToHex(address, 4);
            model.addRow(new Object[]{addrHex, DEFAULT_VALUE});
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
        table.setBackground(TABLE_BG);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(BLUE_LIGHT);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setFont(new Font("JetBrains Mono", Font.BOLD, 11));
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(BLUE_ACCENT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 28));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BLUE_ACCENT));

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
                    setForeground(BLUE_LIGHT);
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
                    setBackground(TABLE_BG);
                    if ("00".equals(value)) {
                        setForeground(new Color(100, 105, 120));
                    } else {
                        setForeground(BLUE_LIGHT);
                    }
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(addrRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(dataRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(TABLE_BG);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel infoPanel = createInfoPanel();
        contentPane.add(infoPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));
        panel.setBackground(HEADER_BG);
        panel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BLUE_ACCENT));

        JLabel iconLabel = new JLabel("💾");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        panel.add(iconLabel);

        JLabel infoLabel = new JLabel(String.format(
                "0x%04X → 0x%04X • %d bytes",
                RAM_START, RAM_END, RAM_SIZE
        ));
        infoLabel.setFont(new Font("JetBrains Mono", Font.BOLD, 9));
        infoLabel.setForeground(BLUE_LIGHT);
        panel.add(infoLabel);

        return panel;
    }


    public void write(int address, String value) {
        if (!isValidAddress(address)) {
            System.err.println("RAM: Adresse invalide: 0x" +
                    Integer.toHexString(address).toUpperCase());
            return;
        }

        value = value.toUpperCase().trim();

        if (!value.matches("[0-9A-F]{2}")) {
            System.err.println("RAM: Valeur invalide: " + value);
            return;
        }

        memory.put(address, value);
        int row = addressToRow(address);
        model.setValueAt(value, row, 1);
    }

    public static String read(String addressHex) {
        try {
            int address = hexToInt(addressHex);
            return read(address);
        } catch (Exception e) {
            System.err.println("RAM: Erreur de lecture avec adresse: " + addressHex);
            return DEFAULT_VALUE;
        }
    }

    public static String read(int address) {
        if (!isValidAddress(address)) {
            return DEFAULT_VALUE;
        }
        return memory.getOrDefault(address, DEFAULT_VALUE);
    }

    public void clear() {
        for (int i = 0; i < RAM_SIZE; i++) {
            int address = RAM_START + i;
            memory.put(address, DEFAULT_VALUE);
            model.setValueAt(DEFAULT_VALUE, i, 1);
        }
    }

    public DefaultTableModel getModel() {
        return model;
    }

    private static boolean isValidAddress(int address) {
        return address >= RAM_START && address <= RAM_END;
    }

    private int addressToRow(int address) {
        return address - RAM_START;
    }

    private int rowToAddress(int row) {
        return RAM_START + row;
    }

    private String intToHex(int value, int digits) {
        String hex = Integer.toHexString(value).toUpperCase();
        while (hex.length() < digits) {
            hex = "0" + hex;
        }
        return hex;
    }

    private static int hexToInt(String hexValue) {
        if (hexValue.startsWith("0x") || hexValue.startsWith("0X")) {
            hexValue = hexValue.substring(2);
        }

        try {
            return Integer.parseInt(hexValue, 16);
        } catch (NumberFormatException e) {
            System.err.println("RAM: Valeur hexadécimale invalide: " + hexValue);
            return RAM_START;
        }
    }

    @Deprecated
    public void setData(String value, int rowIndex) {
        int address = rowToAddress(rowIndex);
        write(address, value);
    }

    @Deprecated
    public static String getData(String addressHex) {
        return read(addressHex);
    }

    @Deprecated
    public String getDatapre(String addressHex) {
        int address = hexToInt(addressHex) + 1;
        return read(address);
    }
}
