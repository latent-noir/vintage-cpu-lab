package vintagecpulab.core;

public class CPU {

    private int PC = 0xFC00;
    private int X = 0;
    private int Y = 0;
    private int S = 0;
    private int U = 0;
    private int DP = 0;
    private int A = 0;
    private int B = 0;
    private int CC = 0x04;


    public static final int C_FLAG = 0x01;
    public static final int V_FLAG = 0x02;
    public static final int Z_FLAG = 0x04;
    public static final int N_FLAG = 0x08;
    public static final int I_FLAG = 0x10;  
    public static final int H_FLAG = 0x20;  
    public static final int F_FLAG = 0x40;  
    public static final int E_FLAG = 0x80;  

    
    public int getPC() {
        return PC;
    }

    public void setPC(int pc) {
        PC = pc & 0xFFFF;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x & 0xFFFF;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y & 0xFFFF;
    }

    public int getS() {
        return S;
    }

    public void setS(int s) {
        S = s & 0xFFFF;
    }

    public int getU() {
        return U;
    }

    public void setU(int u) {
        U = u & 0xFFFF;
    }

    public int getDP() {
        return DP;
    }

    public void setDP(int dp) {
        DP = dp & 0xFF;
    }

    public int getA() {
        return A;
    }

    public void setA(int a) {
        A = a & 0xFF;
    }

    public int getB() {
        return B;
    }

    public void setB(int b) {
        B = b & 0xFF;
    }

    public int getD() {
        return (A << 8) | (B & 0xFF);
    }

    public void setD(int d) {
        A = (d >> 8) & 0xFF;
        B = d & 0xFF;
    }

    public int getCC() {
        return CC;
    }

    public void setCC(int cc) {
        CC = cc & 0xFF;
    }

    //    GESTION FLAGS
    public void setFlag(int flag, boolean value) {
        if (value) {
            CC |= flag;
        } else {
            CC &= ~flag;
        }
    }

    public boolean getFlag(int flag) {
        return (CC & flag) != 0;
    }


    public boolean getFlagC() { return getFlag(C_FLAG); }
    public boolean getFlagV() { return getFlag(V_FLAG); }
    public boolean getFlagZ() { return getFlag(Z_FLAG); }
    public boolean getFlagN() { return getFlag(N_FLAG); }
    public boolean getFlagI() { return getFlag(I_FLAG); }
    public boolean getFlagH() { return getFlag(H_FLAG); }
    public boolean getFlagF() { return getFlag(F_FLAG); }
    public boolean getFlagE() { return getFlag(E_FLAG); }

    public void setFlagC(boolean value) { setFlag(C_FLAG, value); }
    public void setFlagV(boolean value) { setFlag(V_FLAG, value); }
    public void setFlagZ(boolean value) { setFlag(Z_FLAG, value); }
    public void setFlagN(boolean value) { setFlag(N_FLAG, value); }
    public void setFlagI(boolean value) { setFlag(I_FLAG, value); }
    public void setFlagH(boolean value) { setFlag(H_FLAG, value); }
    public void setFlagF(boolean value) { setFlag(F_FLAG, value); }
    public void setFlagE(boolean value) { setFlag(E_FLAG, value); }

    
    public static int hexToDecimal(String hex) {
        try {
            return Integer.parseInt(hex, 16);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String decimalToHex(int value, int digits) {
        String hex = Integer.toHexString(value).toUpperCase();
        while (hex.length() < digits) {
            hex = "0" + hex;
        }
        return hex.substring(hex.length() - digits);
    }

    public void reset() {
        PC = 0xFC00;    
        X = 0;
        Y = 0;
        S = 0;
        U = 0;
        DP = 0;
        A = 0;
        B = 0;
        CC = 0x04;      
    }


    @Override
    public String toString() {
        return String.format(
                "CPU State:\n" +
                        "  PC=%04X  A=%02X  B=%02X  D=%04X\n" +
                        "  X=%04X  Y=%04X  S=%04X  U=%04X\n" +
                        "  DP=%02X  CC=%02X [E=%d F=%d H=%d I=%d N=%d Z=%d V=%d C=%d]",
                PC, A, B, getD(), X, Y, S, U, DP, CC,
                getFlagE()?1:0, getFlagF()?1:0, getFlagH()?1:0, getFlagI()?1:0,
                getFlagN()?1:0, getFlagZ()?1:0, getFlagV()?1:0, getFlagC()?1:0
        );
    }
}
