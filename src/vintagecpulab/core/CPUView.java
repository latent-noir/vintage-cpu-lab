package vintagecpulab.core;
import javax.swing.*;
import java.awt.*;

/**
 * Classe CPUView :  Vue graphique pour afficher l'état du CPU
 */
public class CPUView extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JLabel lblPC;
    private JLabel lblInstruction;
    private JLabel lblS;
    private JLabel lblU;
    private JLabel lblA;
    private JLabel lblB;
    private JLabel lblDP;
    private JLabel lblX;
    private JLabel lblY;
    private JLabel lblE;
    private JLabel lblF;
    private JLabel lblH;
    private JLabel lblI;
    private JLabel lblN;
    private JLabel lblZ;
    private JLabel lblV;
    private JLabel lblC;

    public CPUView() {
        initializeUI();
    }

    private void initializeUI() {
        setResizable(false);
        setAlwaysOnTop(true);
        setTitle("ARCHITECTURE INTERNE DU 6809");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(70, 200, 300, 500);
        contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        lblPC = createRegisterLabel("FC00", 95, 15, 89, 26);
        lblInstruction = createRegisterLabel("", 13, 50, 202, 31);
        lblS = createRegisterLabel("0000", 40, 85, 70, 30);
        lblU = createRegisterLabel("0000", 145, 85, 70, 30);
        lblA = createRegisterLabel("00", 40, 141, 46, 30);
        lblB = createRegisterLabel("00", 40, 225, 46, 30);
        lblDP = createRegisterLabel("00", 45, 281, 46, 30);
        lblX = createRegisterLabel("0000", 30, 352, 76, 30);
        lblY = createRegisterLabel("0000", 145, 353, 76, 30);
        lblE = createFlagLabel("0", 92, 281, 13, 31);
        lblF = createFlagLabel("0", 105, 281, 13, 31);
        lblH = createFlagLabel("0", 117, 281, 13, 31);
        lblI = createFlagLabel("0", 130, 281, 13, 31);
        lblN = createFlagLabel("0", 143, 281, 13, 31);
        lblZ = createFlagLabel("1", 156, 281, 13, 31);
        lblV = createFlagLabel("0", 169, 281, 13, 31);
        lblC = createFlagLabel("0", 182, 281, 13, 31);

        // Image de fond
        JLabel lblBackground = new JLabel();
        lblBackground.setIcon(new ImageIcon(getClass().getResource("UAL.png")));
        lblBackground.setBounds(2, 2, 210, 380);
        contentPane.add(lblBackground);
    }

    private JLabel createRegisterLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Tahoma", Font.PLAIN, 23));
        label.setForeground(Color.BLUE);
        label.setBounds(x, y, width, height);
        contentPane.add(label);
        return label;
    }

    private JLabel createFlagLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Tahoma", Font.PLAIN, 23));
        label.setForeground(new Color(0, 0, 160));
        label.setBounds(x, y, width, height);
        contentPane.add(label);
        return label;
    }

    
    public void updateFromCPU(CPU cpu) {
        lblPC.setText(CPU.decimalToHex(cpu.getPC(), 4));
        lblA.setText(CPU.decimalToHex(cpu.getA(), 2));
        lblB.setText(CPU.decimalToHex(cpu.getB(), 2));
        lblX.setText(CPU.decimalToHex(cpu.getX(), 4));
        lblY.setText(CPU.decimalToHex(cpu.getY(), 4));
        lblS.setText(CPU.decimalToHex(cpu.getS(), 4));
        lblU.setText(CPU.decimalToHex(cpu.getU(), 4));
        lblDP.setText(CPU.decimalToHex(cpu.getDP(), 2));

        lblE.setText(cpu.getFlagE() ? "1" : "0");
        lblF.setText(cpu.getFlagF() ? "1" : "0");
        lblH.setText(cpu.getFlagH() ? "1" : "0");
        lblI.setText(cpu.getFlagI() ? "1" : "0");
        lblN.setText(cpu.getFlagN() ? "1" : "0");
        lblZ.setText(cpu.getFlagZ() ? "1" : "0");
        lblV.setText(cpu.getFlagV() ? "1" : "0");
        lblC.setText(cpu.getFlagC() ? "1" : "0");
    }

    public void setInstruction(String instruction) {
        lblInstruction.setText(instruction);
    }

    public void resetDisplay() {
        lblPC.setText("FC00");
        lblInstruction.setText("");
        lblS.setText("0000");
        lblU.setText("0000");
        lblA.setText("00");
        lblB.setText("00");
        lblDP.setText("00");
        lblX.setText("0000");
        lblY.setText("0000");
        lblE.setText("0");
        lblF.setText("0");
        lblH.setText("0");
        lblI.setText("0");
        lblN.setText("0");
        lblZ.setText("0");
        lblV.setText("0");
        lblC.setText("0");
    }
}
