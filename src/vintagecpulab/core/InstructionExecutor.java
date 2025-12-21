package vintagecpulab.core;

import javax.swing.table.DefaultTableModel;

/**
 * EXÉCUTEUR D'INSTRUCTIONS MOTOROLA 6809 - VERSION OPTIMALE FUSIONNÉE
 */
public class InstructionExecutor {

    // ATTRIBUTS
    private final CPU cpu;
    private  final LabelManager labelManager;
    private final DefaultTableModel ramModel;
    private final DefaultTableModel romModel;
    private int romAddress = 0; // Position d'écriture en ROM pour l'assembleur

    // CONSTRUCTEUR
    public InstructionExecutor(CPU cpu, DefaultTableModel ramModel, DefaultTableModel romModel) {
        this.cpu = cpu;
        this.ramModel = ramModel;
        this.romModel = romModel;
        this.labelManager = new LabelManager();
    }

    // POINT D'ENTRÉE PRINCIPAL - EXÉCUTION

    /**
     * Exécute une instruction décodée
     */
    public void execute(InstructionDecoder.DecodedInstruction instr) {
        if (instr == null) {
            throw new IllegalArgumentException("Instruction nulle");
        }

        String mnemonic = instr.operation.toUpperCase();
        InstructionDecoder.AddressingMode mode = instr.mode;
        String operand = instr.operand;

        // DISPATCH PAR INSTRUCTION

        switch (mnemonic) {
            // LOAD INSTRUCTIONS (8 registres)
            case "LDA" -> execLDA(mode, operand);
            case "LDB" -> execLDB(mode, operand);
            case "LDD" -> execLDD(mode, operand);
            case "LDX" -> execLDX(mode, operand);
            case "LDY" -> execLDY(mode, operand);
            case "LDU" -> execLDU(mode, operand);
            case "LDS" -> execLDS(mode, operand);
            // STORE INSTRUCTIONS
            case "STA" -> execSTA(mode, operand);
            case "STB" -> execSTB(mode, operand);
            case "STD" -> execSTD(mode, operand);
            case "STX" -> execSTX(mode, operand);
            case "STY" -> execSTY(mode, operand);
            case "STU" -> execSTU(mode, operand);
            case "STS" -> execSTS(mode, operand);
            // LOAD EFFECTIVE ADDRESS (LEA)
            case "LEAX" -> execLEAX(operand);
            case "LEAY" -> execLEAY(operand);
            case "LEAS" -> execLEAS(operand);
            case "LEAU" -> execLEAU(operand);
            // COMPARE INSTRUCTIONS
            case "CMPA" -> execCMPA(mode, operand);
            case "CMPB" -> execCMPB(mode, operand);
            case "CMPD" -> execCMPD(mode, operand);
            case "CMPX" -> execCMPX(mode, operand);
            case "CMPY" -> execCMPY(mode, operand);
            case "CMPU" -> execCMPU(mode, operand);
            case "CMPS" -> execCMPS(mode, operand);
            // ADDITION INSTRUCTIONS
            case "ADDA" -> execADDA(mode, operand);
            case "ADDB" -> execADDB(mode, operand);
            case "ADDD" -> execADDD(mode, operand);
            case "ADCA" -> execADCA(mode, operand);
            case "ADCB" -> execADCB(mode, operand);
            // SUBTRACTION INSTRUCTIONS
            case "SUBA" -> execSUBA(mode, operand);
            case "SUBB" -> execSUBB(mode, operand);
            case "SUBD" -> execSUBD(mode, operand);
            case "SBCA" -> execSBCA(mode, operand);
            case "SBCB" -> execSBCB(mode, operand);
            // LOGICAL INSTRUCTIONS
            case "ANDA" -> execANDA(mode, operand);
            case "ANDB" -> execANDB(mode, operand);
            case "ANDCC" -> execANDCC(operand);
            case "ORA" -> execORA(mode, operand);
            case "ORB" -> execORB(mode, operand);
            case "ORCC" -> execORCC(operand);
            case "EORA" -> execEORA(mode, operand);
            case "EORB" -> execEORB(mode, operand);
            // BIT TEST INSTRUCTIONS
            case "BITA" -> execBITA(mode, operand);
            case "BITB" -> execBITB(mode, operand);
            // INCREMENT/DECREMENT
            case "INCA" -> execINCA();
            case "INCB" -> execINCB();
            case "INC" -> execINC(mode, operand);
            case "DECA" -> execDECA();
            case "DECB" -> execDECB();
            case "DEC" -> execDEC(mode, operand);
            // CLEAR INSTRUCTIONS
            case "CLRA" -> execCLRA();
            case "CLRB" -> execCLRB();
            case "CLR" -> execCLR(mode, operand);
            // COMPLEMENT INSTRUCTIONS
            case "COMA" -> execCOMA();
            case "COMB" -> execCOMB();
            case "COM" -> execCOM(mode, operand);
            // NEGATE INSTRUCTIONS
            case "NEGA" -> execNEGA();
            case "NEGB" -> execNEGB();
            case "NEG" -> execNEG(mode, operand);
            // TEST INSTRUCTIONS
            case "TSTA" -> execTSTA();
            case "TSTB" -> execTSTB();
            case "TST" -> execTST(mode, operand);
            // SHIFT INSTRUCTIONS (ASL/ASR/LSL/LSR)
            case "ASLA" -> execASLA();
            case "ASLB" -> execASLB();
            case "ASL" -> execASL(mode, operand);
            case "ASRA" -> execASRA();
            case "ASRB" -> execASRB();
            case "ASR" -> execASR(mode, operand);
            case "LSLA" -> execASLA(); // LSL = ASL
            case "LSLB" -> execASLB();
            case "LSL" -> execASL(mode, operand);
            case "LSRA" -> execLSRA();
            case "LSRB" -> execLSRB();
            case "LSR" -> execLSR(mode, operand);
            // ROTATE INSTRUCTIONS (ROL/ROR)
            case "ROLA" -> execROLA();
            case "ROLB" -> execROLB();
            case "ROL" -> execROL(mode, operand);
            case "RORA" -> execRORA();
            case "RORB" -> execRORB();
            case "ROR" -> execROR(mode, operand);
            // BRANCH INSTRUCTIONS (Court)
            case "BRA" -> execBRA(operand);
            //case "BRN" -> execBRN(operand);
            case "BEQ" -> execBEQ(operand);
            case "BNE" -> execBNE(operand);
            case "BCC", "BHS" -> execBCC(operand);
            case "BCS", "BLO" -> execBCS(operand);
            case "BPL" -> execBPL(operand);
            case "BMI" -> execBMI(operand);
            case "BVC" -> execBVC(operand);
            case "BVS" -> execBVS(operand);
            case "BGT" -> execBGT(operand);
            case "BLE" -> execBLE(operand);
            case "BGE" -> execBGE(operand);
            case "BLT" -> execBLT(operand);
            case "BHI" -> execBHI(operand);
            case "BLS" -> execBLS(operand);
            // LONG BRANCH INSTRUCTIONS
            case "LBRA" -> execLBRA(operand);
            case "LBSR" -> execLBSR(operand);
            // JUMP AND SUBROUTINE
            case "JMP" -> execJMP(mode, operand);
            case "JSR" -> execJSR(mode, operand);
            case "BSR" -> execBSR(operand);
            case "RTS" -> execRTS();
            case "RTI" -> execRTI();
            // STACK INSTRUCTIONS
            case "PSHS" -> execPSHS(operand);
            case "PSHU" -> execPSHU(operand);
            case "PULS" -> execPULS(operand);
            case "PULU" -> execPULU(operand);
            // TRANSFER AND EXCHANGE
            case "TFR" -> execTFR(operand);
            case "EXG" -> execEXG(operand);
            // SPECIAL INSTRUCTIONS
            case "ABX" -> execABX();
            case "MUL" -> execMUL();
            case "SEX" -> execSEX();
            case "DAA" -> execDAA();
            case "NOP" -> execNOP();
            case "SWI" -> execSWI();
            case "SWI2" -> execSWI2();
            case "SWI3" -> execSWI3();
            case "CWAI" -> execCWAI(operand);
            case "SYNC" -> execSYNC();
            // ASSEMBLER DIRECTIVES
            case "ORG" -> execORG(operand);

            default -> throw new UnsupportedOperationException(
                    "Instruction non supportée: " + mnemonic
            );
        }
    }

    // SECTION: LOAD INSTRUCTIONS

    /**
     * LDA Load Accumulator A
     * Flags: N, Z, V=0
     */
    private void execLDA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        cpu.setA(value & 0xFF);
        cpu.setFlagZ((value & 0xFF) == 0);
        cpu.setFlagN((value & 0x80) != 0);
        cpu.setFlagV(false);
    }

    /**
     * LDB - Load Accumulator B
     * Flags: N, Z, V=0
     */
    private void execLDB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        cpu.setB(value & 0xFF);
        cpu.setFlagZ((value & 0xFF) == 0);
        cpu.setFlagN((value & 0x80) != 0);
        cpu.setFlagV(false);
    }

    /**
     * LDD - Load Accumulator D (A:B)
     * Flags: N, Z, V=0
     */
    private void execLDD(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        cpu.setD(value & 0xFFFF);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    /**
     * LDX - Load Index Register X
     * Flags: N, Z, V=0
     */
    private void execLDX(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        cpu.setX(value & 0xFFFF);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    /**
     * LDY - Load Index Register Y
     * Flags: N, Z, V=0
     */
    private void execLDY(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        cpu.setY(value & 0xFFFF);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    /**
     * LDU - Load User Stack Pointer
     * Flags: N, Z, V=0
     */
    private void execLDU(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        cpu.setU(value & 0xFFFF);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    /**
     * LDS - Load System Stack Pointer
     * Flags: N, Z, V=0
     */
    private void execLDS(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        cpu.setS(value & 0xFFFF);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    // SECTION: STORE INSTRUCTIONS
    /**
     * STA - Store Accumulator A
     * Flags: N, Z, V=0
     */
    private void execSTA(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = cpu.getA();
        writeMemoryByte(address, value);
        cpu.setFlagZ((value & 0xFF) == 0);
        cpu.setFlagN((value & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execSTB(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = cpu.getB();
        writeMemoryByte(address, value);
        cpu.setFlagZ((value & 0xFF) == 0);
        cpu.setFlagN((value & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execSTD(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = cpu.getD();
        writeMemoryWord(address, value);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    private void execSTX(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = cpu.getX();
        writeMemoryWord(address, value);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    private void execSTY(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = cpu.getY();
        writeMemoryWord(address, value);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    private void execSTU(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = cpu.getU();
        writeMemoryWord(address, value);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    private void execSTS(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = cpu.getS();
        writeMemoryWord(address, value);
        cpu.setFlagZ((value & 0xFFFF) == 0);
        cpu.setFlagN((value & 0x8000) != 0);
        cpu.setFlagV(false);
    }

    // SECTION: LEA INSTRUCTIONS (Load Effective Address)

    /**
     * LEAX - Load Effective Address into X
     * Flags: Z (N, V, C inchangés)
     */
    private void execLEAX(String operand) {
        int ea = resolveIndexedAddress(operand);
        cpu.setX(ea & 0xFFFF);
        cpu.setFlagZ(ea == 0);
    }

    private void execLEAY(String operand) {
        int ea = resolveIndexedAddress(operand);
        cpu.setY(ea & 0xFFFF);
        cpu.setFlagZ(ea == 0);
    }

    private void execLEAS(String operand) {
        int ea = resolveIndexedAddress(operand);
        cpu.setS(ea & 0xFFFF);
        // LEAS ne modifie AUCUN flag
    }

    private void execLEAU(String operand) {
        int ea = resolveIndexedAddress(operand);
        cpu.setU(ea & 0xFFFF);
        // LEAU ne modifie AUCUN flag
    }

    // SECTION: COMPARE INSTRUCTIONS
    /**
     * CMPA - Compare Accumulator A
     * Flags: N, Z, V, C
     * Effectue A - M sans modifier A
     */
    private void execCMPA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int a = cpu.getA();
        int result = a - value;

        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((a ^ value) & (a ^ result) & 0x80) != 0);
    }

    private void execCMPB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int b = cpu.getB();
        int result = b - value;

        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((b ^ value) & (b ^ result) & 0x80) != 0);
    }

    private void execCMPD(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        int d = cpu.getD();
        int result = d - value;

        cpu.setFlagZ((result & 0xFFFF) == 0);
        cpu.setFlagN((result & 0x8000) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((d ^ value) & (d ^ result) & 0x8000) != 0);
    }

    private void execCMPX(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        int x = cpu.getX();
        int result = x - value;

        cpu.setFlagZ((result & 0xFFFF) == 0);
        cpu.setFlagN((result & 0x8000) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((x ^ value) & (x ^ result) & 0x8000) != 0);
    }

    private void execCMPY(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        int y = cpu.getY();
        int result = y - value;

        cpu.setFlagZ((result & 0xFFFF) == 0);
        cpu.setFlagN((result & 0x8000) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((y ^ value) & (y ^ result) & 0x8000) != 0);
    }

    private void execCMPU(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        int u = cpu.getU();
        int result = u - value;

        cpu.setFlagZ((result & 0xFFFF) == 0);
        cpu.setFlagN((result & 0x8000) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((u ^ value) & (u ^ result) & 0x8000) != 0);
    }

    private void execCMPS(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        int s = cpu.getS();
        int result = s - value;

        cpu.setFlagZ((result & 0xFFFF) == 0);
        cpu.setFlagN((result & 0x8000) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((s ^ value) & (s ^ result) & 0x8000) != 0);
    }

    // SECTION: ADDITION INSTRUCTIONS
    /**
     * ADDA - Add to Accumulator A
     * Flags: H, N, Z, V, C
     */
    private void execADDA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int a = cpu.getA();
        int result = a + value;

        cpu.setA(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result > 0xFF);
        cpu.setFlagV(((a ^ result) & (value ^ result) & 0x80) != 0);
        cpu.setFlagH(((a & 0x0F) + (value & 0x0F)) > 0x0F);
    }

    private void execADDB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int b = cpu.getB();
        int result = b + value;

        cpu.setB(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result > 0xFF);
        cpu.setFlagV(((b ^ result) & (value ^ result) & 0x80) != 0);
        cpu.setFlagH(((b & 0x0F) + (value & 0x0F)) > 0x0F);
    }

    private void execADDD(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        int d = cpu.getD();
        int result = d + value;

        cpu.setD(result & 0xFFFF);
        cpu.setFlagZ((result & 0xFFFF) == 0);
        cpu.setFlagN((result & 0x8000) != 0);
        cpu.setFlagC(result > 0xFFFF);
        cpu.setFlagV(((d ^ result) & (value ^ result) & 0x8000) != 0);
    }

    /**
     * ADCA - Add with Carry to Accumulator A
     * Flags: H, N, Z, V, C
     */
    private void execADCA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int a = cpu.getA();
        int carry = cpu.getFlagC() ? 1 : 0;
        int result = a + value + carry;

        cpu.setA(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result > 0xFF);
        cpu.setFlagV(((a ^ result) & (value ^ result) & 0x80) != 0);
        cpu.setFlagH(((a & 0x0F) + (value & 0x0F) + carry) > 0x0F);
    }

    private void execADCB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int b = cpu.getB();
        int carry = cpu.getFlagC() ? 1 : 0;
        int result = b + value + carry;

        cpu.setB(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result > 0xFF);
        cpu.setFlagV(((b ^ result) & (value ^ result) & 0x80) != 0);
        cpu.setFlagH(((b & 0x0F) + (value & 0x0F) + carry) > 0x0F);
    }

    // SECTION: SUBTRACTION INSTRUCTIONS

    private void execSUBA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int a = cpu.getA();
        int result = a - value;

        cpu.setA(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((a ^ value) & (a ^ result) & 0x80) != 0);
    }

    private void execSUBB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int b = cpu.getB();
        int result = b - value;

        cpu.setB(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((b ^ value) & (b ^ result) & 0x80) != 0);
    }

    private void execSUBD(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand16(mode, operand);
        int d = cpu.getD();
        int result = d - value;

        cpu.setD(result & 0xFFFF);
        cpu.setFlagZ((result & 0xFFFF) == 0);
        cpu.setFlagN((result & 0x8000) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((d ^ value) & (d ^ result) & 0x8000) != 0);
    }

    private void execSBCA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int a = cpu.getA();
        int carry = cpu.getFlagC() ? 1 : 0;
        int result = a - value - carry;

        cpu.setA(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((a ^ value) & (a ^ result) & 0x80) != 0);
    }

    private void execSBCB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int b = cpu.getB();
        int carry = cpu.getFlagC() ? 1 : 0;
        int result = b - value - carry;

        cpu.setB(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result < 0);
        cpu.setFlagV(((b ^ value) & (b ^ result) & 0x80) != 0);
    }

    // SECTION: LOGICAL INSTRUCTIONS

    private void execANDA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int result = cpu.getA() & value;
        cpu.setA(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execANDB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int result = cpu.getB() & value;
        cpu.setB(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execANDCC(String operand) {
        int mask = CPU.hexToDecimal(operand) & 0xFF;
        cpu.setCC(cpu.getCC() & mask);
    }

    private void execORA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int result = cpu.getA() | value;
        cpu.setA(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execORB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int result = cpu.getB() | value;
        cpu.setB(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execORCC(String operand) {
        int mask = CPU.hexToDecimal(operand) & 0xFF;
        cpu.setCC(cpu.getCC() | mask);
    }

    private void execEORA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int result = cpu.getA() ^ value;
        cpu.setA(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execEORB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int result = cpu.getB() ^ value;
        cpu.setB(result & 0xFF);
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
    }

    // SECTION: BIT TEST INSTRUCTIONS
    private void execBITA(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int result = cpu.getA() & value;
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execBITB(InstructionDecoder.AddressingMode mode, String operand) {
        int value = readOperand8(mode, operand);
        int result = cpu.getB() & value;
        cpu.setFlagZ((result & 0xFF) == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
    }

    // SECTION: INCREMENT/DECREMENT

    private void execINCA() {
        int result = (cpu.getA() + 1) & 0xFF;
        cpu.setA(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(result == 0x80);
    }

    private void execINCB() {
        int result = (cpu.getB() + 1) & 0xFF;
        cpu.setB(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(result == 0x80);
    }

    private void execINC(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = (readMemoryByte(address) + 1) & 0xFF;
        writeMemoryByte(address, value);
        cpu.setFlagZ(value == 0);
        cpu.setFlagN((value & 0x80) != 0);
        cpu.setFlagV(value == 0x80);
    }

    private void execDECA() {
        int result = (cpu.getA() - 1) & 0xFF;
        cpu.setA(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(result == 0x7F);
    }

    private void execDECB() {
        int result = (cpu.getB() - 1) & 0xFF;
        cpu.setB(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(result == 0x7F);
    }

    private void execDEC(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = (readMemoryByte(address) - 1) & 0xFF;
        writeMemoryByte(address, value);
        cpu.setFlagZ(value == 0);
        cpu.setFlagN((value & 0x80) != 0);
        cpu.setFlagV(value == 0x7F);
    }

    // SECTION: CLEAR INSTRUCTIONS

    private void execCLRA() {
        cpu.setA(0);
        cpu.setFlagZ(true);
        cpu.setFlagN(false);
        cpu.setFlagV(false);
        cpu.setFlagC(false);
    }

    private void execCLRB() {
        cpu.setB(0);
        cpu.setFlagZ(true);
        cpu.setFlagN(false);
        cpu.setFlagV(false);
        cpu.setFlagC(false);
    }

    private void execCLR(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        writeMemoryByte(address, 0);
        cpu.setFlagZ(true);
        cpu.setFlagN(false);
        cpu.setFlagV(false);
        cpu.setFlagC(false);
    }

    // SECTION: COMPLEMENT INSTRUCTIONS

    private void execCOMA() {
        int result = (~cpu.getA()) & 0xFF;
        cpu.setA(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
        cpu.setFlagC(true);
    }

    private void execCOMB() {
        int result = (~cpu.getB()) & 0xFF;
        cpu.setB(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
        cpu.setFlagC(true);
    }

    private void execCOM(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = readMemoryByte(address);
        int result = (~value) & 0xFF;
        writeMemoryByte(address, result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(false);
        cpu.setFlagC(true);
    }

    // SECTION: NEGATE INSTRUCTIONS

    private void execNEGA() {
        int a = cpu.getA();
        int result = (-a) & 0xFF;
        cpu.setA(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result != 0);
        cpu.setFlagV(a == 0x80);
    }

    private void execNEGB() {
        int b = cpu.getB();
        int result = (-b) & 0xFF;
        cpu.setB(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result != 0);
        cpu.setFlagV(b == 0x80);
    }

    private void execNEG(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = readMemoryByte(address);
        int result = (-value) & 0xFF;
        writeMemoryByte(address, result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC(result != 0);
        cpu.setFlagV(value == 0x80);
    }

    // SECTION: TEST INSTRUCTIONS

    private void execTSTA() {
        int a = cpu.getA();
        cpu.setFlagZ(a == 0);
        cpu.setFlagN((a & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execTSTB() {
        int b = cpu.getB();
        cpu.setFlagZ(b == 0);
        cpu.setFlagN((b & 0x80) != 0);
        cpu.setFlagV(false);
    }

    private void execTST(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = readMemoryByte(address);
        cpu.setFlagZ(value == 0);
        cpu.setFlagN((value & 0x80) != 0);
        cpu.setFlagV(false);
    }

    // SECTION: SHIFT INSTRUCTIONS

    private void execASLA() {
        int a = cpu.getA();
        int result = (a << 1) & 0xFF;
        cpu.setA(result);
        cpu.setFlagC((a & 0x80) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(((a & 0x80) != 0) != ((result & 0x80) != 0));
    }

    private void execASLB() {
        int b = cpu.getB();
        int result = (b << 1) & 0xFF;
        cpu.setB(result);
        cpu.setFlagC((b & 0x80) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(((b & 0x80) != 0) != ((result & 0x80) != 0));
    }

    private void execASL(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = readMemoryByte(address);
        int result = (value << 1) & 0xFF;
        writeMemoryByte(address, result);
        cpu.setFlagC((value & 0x80) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(((value & 0x80) != 0) != ((result & 0x80) != 0));
    }

    private void execASRA() {
        int a = cpu.getA();
        int result = (a >> 1) | (a & 0x80);
        cpu.setA(result & 0xFF);
        cpu.setFlagC((a & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
    }

    private void execASRB() {
        int b = cpu.getB();
        int result = (b >> 1) | (b & 0x80);
        cpu.setB(result & 0xFF);
        cpu.setFlagC((b & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
    }

    private void execASR(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = readMemoryByte(address);
        int result = (value >> 1) | (value & 0x80);
        writeMemoryByte(address, result);
        cpu.setFlagC((value & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
    }

    private void execLSRA() {
        int a = cpu.getA();
        int result = (a >>> 1) & 0xFF;
        cpu.setA(result);
        cpu.setFlagC((a & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN(false);
    }

    private void execLSRB() {
        int b = cpu.getB();
        int result = (b >>> 1) & 0xFF;
        cpu.setB(result);
        cpu.setFlagC((b & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN(false);
    }

    private void execLSR(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = readMemoryByte(address);
        int result = (value >>> 1) & 0xFF;
        writeMemoryByte(address, result);
        cpu.setFlagC((value & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN(false);
    }

    // SECTION: ROTATE INSTRUCTIONS

    private void execROLA() {
        int a = cpu.getA();
        int carry = cpu.getFlagC() ? 1 : 0;
        int result = ((a << 1) | carry) & 0xFF;
        cpu.setA(result);
        cpu.setFlagC((a & 0x80) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(((a & 0x80) != 0) != ((result & 0x80) != 0));
    }

    private void execROLB() {
        int b = cpu.getB();
        int carry = cpu.getFlagC() ? 1 : 0;
        int result = ((b << 1) | carry) & 0xFF;
        cpu.setB(result);
        cpu.setFlagC((b & 0x80) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(((b & 0x80) != 0) != ((result & 0x80) != 0));
    }

    private void execROL(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = readMemoryByte(address);
        int carry = cpu.getFlagC() ? 1 : 0;
        int result = ((value << 1) | carry) & 0xFF;
        writeMemoryByte(address, result);
        cpu.setFlagC((value & 0x80) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagV(((value & 0x80) != 0) != ((result & 0x80) != 0));
    }

    private void execRORA() {
        int a = cpu.getA();
        int carry = cpu.getFlagC() ? 0x80 : 0;
        int result = ((a >>> 1) | carry) & 0xFF;
        cpu.setA(result);
        cpu.setFlagC((a & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
    }

    private void execRORB() {
        int b = cpu.getB();
        int carry = cpu.getFlagC() ? 0x80 : 0;
        int result = ((b >>> 1) | carry) & 0xFF;
        cpu.setB(result);
        cpu.setFlagC((b & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
    }

    private void execROR(InstructionDecoder.AddressingMode mode, String operand) {
        int address = getEffectiveAddress(mode, operand);
        int value = readMemoryByte(address);
        int carry = cpu.getFlagC() ? 0x80 : 0;
        int result = ((value >>> 1) | carry) & 0xFF;
        writeMemoryByte(address, result);
        cpu.setFlagC((value & 0x01) != 0);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
    }

    // SECTION: BRANCH INSTRUCTIONS
    /**
     * BRA - Branch Always (déplacement relatif signé 8 bits)
     */
    private void execBRA(String operand) {
        // L'opérande contient le déplacement (ex: "$FA" = -6)
        int displacement = parseSignedDisplacement8(operand);
        int pc = cpu.getPC();
        int target = (pc + 2 + displacement) & 0xFFFF; // BRA = 2 octets
        cpu.setPC(target);

        System.out.println("🔄 BRA: PC=$" + CPU.decimalToHex(pc, 4) +
                " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
    }

    /**
     * BEQ - Branch if Equal (Z=1)
     */
    private void execBEQ(String operand) {
        if (cpu.getFlagZ()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF; // BEQ = 2 octets
            cpu.setPC(target);

            System.out.println("🔄 BEQ: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BNE - Branch if Not Equal (Z=0)
     */
    private void execBNE(String operand) {
        if (!cpu.getFlagZ()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF; // BNE = 2 octets
            cpu.setPC(target);

            System.out.println("🔄 BNE: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BCC - Branch if Carry Clear (C=0)
     */
    private void execBCC(String operand) {
        if (!cpu.getFlagC()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BCC: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BCS - Branch if Carry Set (C=1)
     */
    private void execBCS(String operand) {
        if (cpu.getFlagC()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BCS: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BPL - Branch if Plus (N=0)
     */
    private void execBPL(String operand) {
        if (!cpu.getFlagN()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BPL: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BMI - Branch if Minus (N=1)
     */
    private void execBMI(String operand) {
        if (cpu.getFlagN()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BMI: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BVC - Branch if Overflow Clear (V=0)
     */
    private void execBVC(String operand) {
        if (!cpu.getFlagV()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BVC: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BVS - Branch if Overflow Set (V=1)
     */
    private void execBVS(String operand) {
        if (cpu.getFlagV()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BVS: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BGT - Branch if Greater Than (signed: Z=0 AND N=V)
     */
    private void execBGT(String operand) {
        boolean Z = cpu.getFlagZ();
        boolean N = cpu.getFlagN();
        boolean V = cpu.getFlagV();
        if (!Z && (N == V)) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BGT: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BLE - Branch if Less or Equal (signed: Z=1 OR N!=V)
     */
    private void execBLE(String operand) {
        boolean Z = cpu.getFlagZ();
        boolean N = cpu.getFlagN();
        boolean V = cpu.getFlagV();
        if (Z || (N != V)) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BLE: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BGE - Branch if Greater or Equal (signed: N=V)
     */
    private void execBGE(String operand) {
        boolean N = cpu.getFlagN();
        boolean V = cpu.getFlagV();
        if (N == V) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BGE: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BLT - Branch if Less Than (signed: N!=V)
     */
    private void execBLT(String operand) {
        boolean N = cpu.getFlagN();
        boolean V = cpu.getFlagV();
        if (N != V) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BLT: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BHI - Branch if Higher (unsigned: C=0 AND Z=0)
     */
    private void execBHI(String operand) {
        if (!cpu.getFlagC() && !cpu.getFlagZ()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BHI: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * BLS - Branch if Lower or Same (unsigned: C=1 OR Z=1)
     */
    private void execBLS(String operand) {
        if (cpu.getFlagC() || cpu.getFlagZ()) {
            int displacement = parseSignedDisplacement8(operand);
            int pc = cpu.getPC();
            int target = (pc + 2 + displacement) & 0xFFFF;
            cpu.setPC(target);

            System.out.println("🔄 BLS: PC=$" + CPU.decimalToHex(pc, 4) +
                    " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
        }
    }

    /**
     * LBRA - Long Branch Always (déplacement 16 bits)
     */
    private void execLBRA(String operand) {
        int displacement = parseSignedDisplacement16(operand);
        int pc = cpu.getPC();
        int target = (pc + 3 + displacement) & 0xFFFF; // LBRA = 3 octets
        cpu.setPC(target);

        System.out.println("🔄 LBRA: PC=$" + CPU.decimalToHex(pc, 4) +
                " + 3 + " + displacement + " = $" + CPU.decimalToHex(target, 4));
    }

    /**
     * LBSR - Long Branch to Subroutine
     */
    private void execLBSR(String operand) {
        int displacement = parseSignedDisplacement16(operand);
        int pc = cpu.getPC();
        int target = (pc + 3 + displacement) & 0xFFFF; // LBSR = 3 octets

        // Sauvegarder l'adresse de retour
        int s = cpu.getS();
        s = pushWord(s, pc + 3); // Adresse de retour = PC + 3
        cpu.setS(s);

        cpu.setPC(target);

        System.out.println("🔄 LBSR: PC=$" + CPU.decimalToHex(pc, 4) +
                " + 3 + " + displacement + " = $" + CPU.decimalToHex(target, 4) +
                " retour @ $" + CPU.decimalToHex(pc + 3, 4));
    }

    /**
     * BSR - Branch to Subroutine (court)
     */
    private void execBSR(String operand) {
        int displacement = parseSignedDisplacement8(operand);
        int pc = cpu.getPC();
        int target = (pc + 2 + displacement) & 0xFFFF; // BSR = 2 octets

        // Sauvegarder l'adresse de retour
        int s = cpu.getS();
        s = pushWord(s, pc + 2); // Adresse de retour = PC + 2
        cpu.setS(s);

        cpu.setPC(target);

        System.out.println("🔄 BSR: PC=$" + CPU.decimalToHex(pc, 4) +
                " + 2 + " + displacement + " = $" + CPU.decimalToHex(target, 4) +
                " retour @ $" + CPU.decimalToHex(pc + 2, 4));
    }



    // SECTION: JUMP AND SUBROUTINE

    private void execJMP(InstructionDecoder.AddressingMode mode, String operand) {
        int target = getEffectiveAddress(mode, operand);
        cpu.setPC(target);
    }

    private void execJSR(InstructionDecoder.AddressingMode mode, String operand) {
        int target = getEffectiveAddress(mode, operand);
        int s = cpu.getS();
        s = pushWord(s, cpu.getPC());
        cpu.setS(s);
        cpu.setPC(target);
    }


    private void execRTS() {
        int s = cpu.getS();
        int returnAddr = pullWord(s);
        cpu.setS((s + 2) & 0xFFFF);
        cpu.setPC(returnAddr);
    }

    private void execRTI() {
        int s = cpu.getS();
        cpu.setCC(pullByte(s));
        s = (s + 1) & 0xFFFF;
        cpu.setA(pullByte(s));
        s = (s + 1) & 0xFFFF;
        cpu.setB(pullByte(s));
        s = (s + 1) & 0xFFFF;
        cpu.setDP(pullByte(s));
        s = (s + 1) & 0xFFFF;
        cpu.setX(pullWord(s));
        s = (s + 2) & 0xFFFF;
        cpu.setY(pullWord(s));
        s = (s + 2) & 0xFFFF;
        cpu.setU(pullWord(s));
        s = (s + 2) & 0xFFFF;
        cpu.setPC(pullWord(s));
        s = (s + 2) & 0xFFFF;
        cpu.setS(s);
    }

    // SECTION: STACK INSTRUCTIONS
    /**
     * PSHS - Push sur pile système
     * Ordre: PC, U, Y, X, DP, B, A, CC (MSB→LSB)
     */
    private void execPSHS(String operand) {
        int mask = parseRegisterMask(operand);
        int s = cpu.getS();

        if ((mask & 0x80) != 0) s = pushWord(s, cpu.getPC());
        if ((mask & 0x40) != 0) s = pushWord(s, cpu.getU());
        if ((mask & 0x20) != 0) s = pushWord(s, cpu.getY());
        if ((mask & 0x10) != 0) s = pushWord(s, cpu.getX());
        if ((mask & 0x08) != 0) s = pushByte(s, cpu.getDP());
        if ((mask & 0x04) != 0) s = pushByte(s, cpu.getB());
        if ((mask & 0x02) != 0) s = pushByte(s, cpu.getA());
        if ((mask & 0x01) != 0) s = pushByte(s, cpu.getCC());

        cpu.setS(s);
    }

    private void execPSHU(String operand) {
        int mask = parseRegisterMask(operand);
        int u = cpu.getU();

        if ((mask & 0x80) != 0) u = pushWord(u, cpu.getPC());
        if ((mask & 0x40) != 0) u = pushWord(u, cpu.getS());
        if ((mask & 0x20) != 0) u = pushWord(u, cpu.getY());
        if ((mask & 0x10) != 0) u = pushWord(u, cpu.getX());
        if ((mask & 0x08) != 0) u = pushByte(u, cpu.getDP());
        if ((mask & 0x04) != 0) u = pushByte(u, cpu.getB());
        if ((mask & 0x02) != 0) u = pushByte(u, cpu.getA());
        if ((mask & 0x01) != 0) u = pushByte(u, cpu.getCC());

        cpu.setU(u);
    }

    /**
     * PULS - Pull depuis pile système
     * Ordre inverse: CC, A, B, DP, X, Y, U, PC (LSB→MSB)
     */
    private void execPULS(String operand) {
        int mask = parseRegisterMask(operand);
        int s = cpu.getS();

        // ORDRE LSB → MSB (bit 0 vers bit 7) - INVERSE de PSHS
        if ((mask & 0x01) != 0) {
            cpu.setCC(pullByte(s));
            s = (s + 1) & 0xFFFF;
        }

        if ((mask & 0x02) != 0) {
            cpu.setA(pullByte(s));
            s = (s + 1) & 0xFFFF;
        }

        if ((mask & 0x04) != 0) {
            cpu.setB(pullByte(s));
            s = (s + 1) & 0xFFFF;
        }

        if ((mask & 0x08) != 0) {
            cpu.setDP(pullByte(s));
            s = (s + 1) & 0xFFFF;
        }

        if ((mask & 0x10) != 0) {
            cpu.setX(pullWord(s));
            s = (s + 2) & 0xFFFF; // +2 car 16 bits
        }

        if ((mask & 0x20) != 0) {
            cpu.setY(pullWord(s));
            s = (s + 2) & 0xFFFF;
        }

        if ((mask & 0x40) != 0) {
            cpu.setU(pullWord(s));
            s = (s + 2) & 0xFFFF;
        }

        if ((mask & 0x80) != 0) {
            cpu.setPC(pullWord(s));
            s = (s + 2) & 0xFFFF;
        }

        cpu.setS(s);
    }

    private void execPULU(String operand) {
        int mask = parseRegisterMask(operand);
        int u = cpu.getU();

        // ORDRE LSB → MSB (inverse de PSHU)
        if ((mask & 0x01) != 0) {
            cpu.setCC(pullByte(u));
            u = (u + 1) & 0xFFFF;
        }

        if ((mask & 0x02) != 0) {
            cpu.setA(pullByte(u));
            u = (u + 1) & 0xFFFF;
        }

        if ((mask & 0x04) != 0) {
            cpu.setB(pullByte(u));
            u = (u + 1) & 0xFFFF;
        }

        if ((mask & 0x08) != 0) {
            cpu.setDP(pullByte(u));
            u = (u + 1) & 0xFFFF;
        }

        if ((mask & 0x10) != 0) {
            cpu.setX(pullWord(u));
            u = (u + 2) & 0xFFFF;
        }

        if ((mask & 0x20) != 0) {
            cpu.setY(pullWord(u));
            u = (u + 2) & 0xFFFF;
        }

        if ((mask & 0x40) != 0) {
            cpu.setS(pullWord(u)); // S au lieu de U!
            u = (u + 2) & 0xFFFF;
        }

        if ((mask & 0x80) != 0) {
            cpu.setPC(pullWord(u));
            u = (u + 2) & 0xFFFF;
        }

        cpu.setU(u);
    }


    // SECTION: TRANSFER AND EXCHANGE

    private void execTFR(String operand) {
        String[] regs = operand.split(",");
        if (regs.length != 2) return;

        char source = regs[0].trim().toUpperCase().charAt(0);
        char dest = regs[1].trim().toUpperCase().charAt(0);

        int value = getRegisterValue(source);
        setRegisterValue(dest, value);
    }

    private void execEXG(String operand) {
        String[] regs = operand.split(",");
        if (regs.length != 2) return;

        char reg1 = regs[0].trim().toUpperCase().charAt(0);
        char reg2 = regs[1].trim().toUpperCase().charAt(0);

        int val1 = getRegisterValue(reg1);
        int val2 = getRegisterValue(reg2);

        setRegisterValue(reg1, val2);
        setRegisterValue(reg2, val1);
    }

    // SECTION: SPECIAL INSTRUCTIONS
    private void execABX() {
        int result = (cpu.getX() + cpu.getB()) & 0xFFFF;
        cpu.setX(result);
        // ABX ne modifie AUCUN flag
    }

    private void execMUL() {
        int result = cpu.getA() * cpu.getB();
        cpu.setD(result & 0xFFFF);
        cpu.setFlagZ((result & 0xFFFF) == 0);
        cpu.setFlagC((result & 0x80) != 0);
    }

    private void execSEX() {
        int b = cpu.getB();
        if ((b & 0x80) != 0) {
            cpu.setA(0xFF);
        } else {
            cpu.setA(0x00);
        }
        cpu.setFlagZ(cpu.getD() == 0);
        cpu.setFlagN((cpu.getD() & 0x8000) != 0);
    }

    private void execDAA() {
        int a = cpu.getA();
        int cf = 0;
        int lsn = a & 0x0F;
        int msn = (a >> 4) & 0x0F;

        if (cpu.getFlagH() || lsn > 9) {
            cf |= 0x06;
        }
        if (cpu.getFlagC() || msn > 9 || (msn > 8 && lsn > 9)) {
            cf |= 0x60;
        }

        int result = (a + cf) & 0xFF;
        cpu.setA(result);
        cpu.setFlagZ(result == 0);
        cpu.setFlagN((result & 0x80) != 0);
        cpu.setFlagC((a + cf) > 0xFF);
    }

    private void execNOP() {
        // Ne fait rien
    }

    private void execSWI() {
        // TODO: Implémenter interruption logicielle
    }

    private void execSWI2() {
        // TODO: Implémenter SWI2
    }

    private void execSWI3() {
        // TODO: Implémenter SWI3
    }

    private void execCWAI(String operand) {
        // TODO: Implémenter CWAI
    }

    private void execSYNC() {
        // TODO: Implémenter SYNC
    }

    private void execORG(String operand) {
        romAddress = CPU.hexToDecimal(operand);
    }

    // SECTION: ROM MANAGEMENT (Pour l'assembleur)
    /**
     * Génère le code machine (opcodes + opérandes) en ROM
     * Cette méthode est utilisée par l'assembleur
     */
    public void emitToROM(InstructionDecoder.DecodedInstruction instr) {
        // 1. Émettre les opcodes
        int[] opcodes = getOpcodeSequence(instr);
        for (int opcode : opcodes) {
            writeOpcodeToROM(opcode);
        }

        // 2. Émettre les opérandes
        switch (instr.mode) {
            case IMMEDIATE -> {
                char reg = instr.targetRegister;
                if (reg == 'D' || reg == 'X' || reg == 'Y' || reg == 'S' || reg == 'U') {
                    writeOperandBytesToROM(CPU.hexToDecimal(instr.operand) & 0xFFFF, 2);
                } else {
                    writeOperandBytesToROM(CPU.hexToDecimal(instr.operand) & 0xFF, 1);
                }
            }
            case DIRECT -> writeOperandBytesToROM(CPU.hexToDecimal(instr.operand) & 0xFF, 1);
            case EXTENDED, EXTENDED_INDIRECT -> writeOperandBytesToROM(CPU.hexToDecimal(instr.operand) & 0xFFFF, 2);
            case INDEXED -> {
                if (instr.indexedInfo != null) {
                    int postByte = InstructionDecoder.calculatePostByte(instr.indexedInfo);
                    writeOpcodeToROM(postByte);

                    int offsetBytes = InstructionDecoder.getOffsetByteCount(instr.indexedInfo);
                    if (offsetBytes > 0 && instr.indexedInfo.offset != null) {
                        String offset = instr.indexedInfo.offset.replace("$", "");
                        int offsetValue = Integer.parseInt(offset, 16);
                        writeOperandBytesToROM(offsetValue, offsetBytes);
                    }
                }
            }
            case RELATIVE -> {
                int target = CPU.hexToDecimal(instr.operand) & 0xFFFF;
                int currentPC = romAddress;
                int displacement = target - currentPC;

                if (instr.operation.startsWith("L")) {
                    writeOperandBytesToROM(displacement & 0xFFFF, 2);
                } else {
                    writeOperandBytesToROM(displacement & 0xFF, 1);
                }
            }
            case INHERENT -> {
                String m = instr.operation.toUpperCase();
                if (m.equals("PSHS") || m.equals("PSHU") || m.equals("PULS") || m.equals("PULU")) {
                    int mask = parseRegisterMask(instr.operand);
                    writeOpcodeToROM(mask);
                } else if (m.equals("ORCC") || m.equals("ANDCC")) {
                    writeOperandBytesToROM(CPU.hexToDecimal(instr.operand) & 0xFF, 1);
                }
            }
        }
    }

    /**
     * Calcule la taille en octets d'une instruction complète
     */


    public void resetRomAddress() {
        romAddress = 0;
    }

    public int getRomAddress() {
        return romAddress;
    }

    private void writeOpcodeToROM(int opcode) {
        try {
            if (romAddress >= 0 && romAddress < romModel.getRowCount()) {
                romModel.setValueAt(CPU.decimalToHex(opcode & 0xFF, 2), romAddress, 1);
            }
        } catch (Exception e) {
            System.err.println("Erreur écriture ROM @" + romAddress + ": " + e.getMessage());
        } finally {
            romAddress = (romAddress + 1) & 0xFFFF;
        }
    }

    private void writeOperandBytesToROM(int value, int bytes) {
        if (bytes == 2) {
            writeOpcodeToROM((value >> 8) & 0xFF);
            writeOpcodeToROM(value & 0xFF);
        } else {
            writeOpcodeToROM(value & 0xFF);
        }
    }

    // SECTION: OPCODE TABLE

    /**
     * Retourne la séquence d'opcodes pour une instruction
     */
    public int[] getOpcodeSequence(InstructionDecoder.DecodedInstruction instr) {
        String m = instr.operation.toUpperCase();
        InstructionDecoder.AddressingMode mode = instr.mode;

        return switch (m) {
            // LOAD INSTRUCTIONS
            case "LDA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x86};
                case DIRECT -> new int[]{0x96};
                case INDEXED -> new int[]{0xA6};
                case EXTENDED -> new int[]{0xB6};
                default -> new int[]{0x12};
            };
            case "LDB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC6};
                case DIRECT -> new int[]{0xD6};
                case INDEXED -> new int[]{0xE6};
                case EXTENDED -> new int[]{0xF6};
                default -> new int[]{0x12};
            };
            case "LDD" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xCC};
                case DIRECT -> new int[]{0xDC};
                case INDEXED -> new int[]{0xEC};
                case EXTENDED -> new int[]{0xFC};
                default -> new int[]{0x12};
            };
            case "LDX" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x8E};
                case DIRECT -> new int[]{0x9E};
                case INDEXED -> new int[]{0xAE};
                case EXTENDED -> new int[]{0xBE};
                default -> new int[]{0x12};
            };
            case "LDY" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x10, 0x8E};
                case DIRECT -> new int[]{0x10, 0x9E};
                case INDEXED -> new int[]{0x10, 0xAE};
                case EXTENDED -> new int[]{0x10, 0xBE};
                default -> new int[]{0x12};
            };
            case "LDU" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xCE};
                case DIRECT -> new int[]{0xDE};
                case INDEXED -> new int[]{0xEE};
                case EXTENDED -> new int[]{0xFE};
                default -> new int[]{0x12};
            };
            case "LDS" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x10, 0xCE};
                case DIRECT -> new int[]{0x10, 0xDE};
                case INDEXED -> new int[]{0x10, 0xEE};
                case EXTENDED -> new int[]{0x10, 0xFE};
                default -> new int[]{0x12};
            };

            // STORE INSTRUCTIONS
            case "STA" -> switch (mode) {
                case DIRECT -> new int[]{0x97};
                case INDEXED -> new int[]{0xA7};
                case EXTENDED -> new int[]{0xB7};
                default -> new int[]{0x12};
            };
            case "STB" -> switch (mode) {
                case DIRECT -> new int[]{0xD7};
                case INDEXED -> new int[]{0xE7};
                case EXTENDED -> new int[]{0xF7};
                default -> new int[]{0x12};
            };
            case "STD" -> switch (mode) {
                case DIRECT -> new int[]{0xDD};
                case INDEXED -> new int[]{0xED};
                case EXTENDED -> new int[]{0xFD};
                default -> new int[]{0x12};
            };
            case "STX" -> switch (mode) {
                case DIRECT -> new int[]{0x9F};
                case INDEXED -> new int[]{0xAF};
                case EXTENDED -> new int[]{0xBF};
                default -> new int[]{0x12};
            };
            case "STY" -> switch (mode) {
                case DIRECT -> new int[]{0x10, 0x9F};
                case INDEXED -> new int[]{0x10, 0xAF};
                case EXTENDED -> new int[]{0x10, 0xBF};
                default -> new int[]{0x12};
            };
            case "STU" -> switch (mode) {
                case DIRECT -> new int[]{0xDF};
                case INDEXED -> new int[]{0xEF};
                case EXTENDED -> new int[]{0xFF};
                default -> new int[]{0x12};
            };
            case "STS" -> switch (mode) {
                case DIRECT -> new int[]{0x10, 0xDF};
                case INDEXED -> new int[]{0x10, 0xEF};
                case EXTENDED -> new int[]{0x10, 0xFF};
                default -> new int[]{0x12};
            };

            // LEA INSTRUCTIONS
            case "LEAX" -> new int[]{0x30};
            case "LEAY" -> new int[]{0x31};
            case "LEAS" -> new int[]{0x32};
            case "LEAU" -> new int[]{0x33};

            // COMPARE INSTRUCTIONS
            case "CMPA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x81};
                case DIRECT -> new int[]{0x91};
                case INDEXED -> new int[]{0xA1};
                case EXTENDED -> new int[]{0xB1};
                default -> new int[]{0x12};
            };
            case "CMPB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC1};
                case DIRECT -> new int[]{0xD1};
                case INDEXED -> new int[]{0xE1};
                case EXTENDED -> new int[]{0xF1};
                default -> new int[]{0x12};
            };
            case "CMPD" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x10, 0x83};
                case DIRECT -> new int[]{0x10, 0x93};
                case INDEXED -> new int[]{0x10, 0xA3};
                case EXTENDED -> new int[]{0x10, 0xB3};
                default -> new int[]{0x12};
            };
            case "CMPX" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x8C};
                case DIRECT -> new int[]{0x9C};
                case INDEXED -> new int[]{0xAC};
                case EXTENDED -> new int[]{0xBC};
                default -> new int[]{0x12};
            };
            case "CMPY" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x10, 0x8C};
                case DIRECT -> new int[]{0x10, 0x9C};
                case INDEXED -> new int[]{0x10, 0xAC};
                case EXTENDED -> new int[]{0x10, 0xBC};
                default -> new int[]{0x12};
            };
            case "CMPU" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x11, 0x83};
                case DIRECT -> new int[]{0x11, 0x93};
                case INDEXED -> new int[]{0x11, 0xA3};
                case EXTENDED -> new int[]{0x11, 0xB3};
                default -> new int[]{0x12};
            };
            case "CMPS" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x11, 0x8C};
                case DIRECT -> new int[]{0x11, 0x9C};
                case INDEXED -> new int[]{0x11, 0xAC};
                case EXTENDED -> new int[]{0x11, 0xBC};
                default -> new int[]{0x12};
            };

            // ARITHMETIC
            case "ADDA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x8B};
                case DIRECT -> new int[]{0x9B};
                case INDEXED -> new int[]{0xAB};
                case EXTENDED -> new int[]{0xBB};
                default -> new int[]{0x12};
            };
            case "ADDB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xCB};
                case DIRECT -> new int[]{0xDB};
                case INDEXED -> new int[]{0xEB};
                case EXTENDED -> new int[]{0xFB};
                default -> new int[]{0x12};
            };
            case "ADDD" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC3};
                case DIRECT -> new int[]{0xD3};
                case INDEXED -> new int[]{0xE3};
                case EXTENDED -> new int[]{0xF3};
                default -> new int[]{0x12};
            };
            case "ADCA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x89};
                case DIRECT -> new int[]{0x99};
                case INDEXED -> new int[]{0xA9};
                case EXTENDED -> new int[]{0xB9};
                default -> new int[]{0x12};
            };
            case "ADCB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC9};
                case DIRECT -> new int[]{0xD9};
                case INDEXED -> new int[]{0xE9};
                case EXTENDED -> new int[]{0xF9};
                default -> new int[]{0x12};
            };
            case "SUBA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x80};
                case DIRECT -> new int[]{0x90};
                case INDEXED -> new int[]{0xA0};
                case EXTENDED -> new int[]{0xB0};
                default -> new int[]{0x12};
            };
            case "SUBB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC0};
                case DIRECT -> new int[]{0xD0};
                case INDEXED -> new int[]{0xE0};
                case EXTENDED -> new int[]{0xF0};
                default -> new int[]{0x12};
            };
            case "SUBD" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x83};
                case DIRECT -> new int[]{0x93};
                case INDEXED -> new int[]{0xA3};
                case EXTENDED -> new int[]{0xB3};
                default -> new int[]{0x12};
            };
            case "SBCA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x82};
                case DIRECT -> new int[]{0x92};
                case INDEXED -> new int[]{0xA2};
                case EXTENDED -> new int[]{0xB2};
                default -> new int[]{0x12};
            };
            case "SBCB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC2};
                case DIRECT -> new int[]{0xD2};
                case INDEXED -> new int[]{0xE2};
                case EXTENDED -> new int[]{0xF2};
                default -> new int[]{0x12};
            };

            // LOGICAL
            case "ANDA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x84};
                case DIRECT -> new int[]{0x94};
                case INDEXED -> new int[]{0xA4};
                case EXTENDED -> new int[]{0xB4};
                default -> new int[]{0x12};
            };
            case "ANDB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC4};
                case DIRECT -> new int[]{0xD4};
                case INDEXED -> new int[]{0xE4};
                case EXTENDED -> new int[]{0xF4};
                default -> new int[]{0x12};
            };
            case "ANDCC" -> new int[]{0x1C};
            case "ORA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x8A};
                case DIRECT -> new int[]{0x9A};
                case INDEXED -> new int[]{0xAA};
                case EXTENDED -> new int[]{0xBA};
                default -> new int[]{0x12};
            };
            case "ORB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xCA};
                case DIRECT -> new int[]{0xDA};
                case INDEXED -> new int[]{0xEA};
                case EXTENDED -> new int[]{0xFA};
                default -> new int[]{0x12};
            };
            case "ORCC" -> new int[]{0x1A};
            case "EORA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x88};
                case DIRECT -> new int[]{0x98};
                case INDEXED -> new int[]{0xA8};
                case EXTENDED -> new int[]{0xB8};
                default -> new int[]{0x12};
            };
            case "EORB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC8};
                case DIRECT -> new int[]{0xD8};
                case INDEXED -> new int[]{0xE8};
                case EXTENDED -> new int[]{0xF8};
                default -> new int[]{0x12};
            };

            // BIT TEST
            case "BITA" -> switch (mode) {
                case IMMEDIATE -> new int[]{0x85};
                case DIRECT -> new int[]{0x95};
                case INDEXED -> new int[]{0xA5};
                case EXTENDED -> new int[]{0xB5};
                default -> new int[]{0x12};
            };
            case "BITB" -> switch (mode) {
                case IMMEDIATE -> new int[]{0xC5};
                case DIRECT -> new int[]{0xD5};
                case INDEXED -> new int[]{0xE5};
                case EXTENDED -> new int[]{0xF5};
                default -> new int[]{0x12};
            };

            // INCREMENT/DECREMENT
            case "INCA" -> new int[]{0x4C};
            case "INCB" -> new int[]{0x5C};
            case "INC" -> switch (mode) {
                case DIRECT -> new int[]{0x0C};
                case INDEXED -> new int[]{0x6C};
                case EXTENDED -> new int[]{0x7C};
                default -> new int[]{0x12};
            };
            case "DECA" -> new int[]{0x4A};
            case "DECB" -> new int[]{0x5A};
            case "DEC" -> switch (mode) {
                case DIRECT -> new int[]{0x0A};
                case INDEXED -> new int[]{0x6A};
                case EXTENDED -> new int[]{0x7A};
                default -> new int[]{0x12};
            };

            // CLEAR
            case "CLRA" -> new int[]{0x4F};
            case "CLRB" -> new int[]{0x5F};
            case "CLR" -> switch (mode) {
                case DIRECT -> new int[]{0x0F};
                case INDEXED -> new int[]{0x6F};
                case EXTENDED -> new int[]{0x7F};
                default -> new int[]{0x12};
            };

            // COMPLEMENT
            case "COMA" -> new int[]{0x43};
            case "COMB" -> new int[]{0x53};
            case "COM" -> switch (mode) {
                case DIRECT -> new int[]{0x03};
                case INDEXED -> new int[]{0x63};
                case EXTENDED -> new int[]{0x73};
                default -> new int[]{0x12};
            };

            // NEGATE
            case "NEGA" -> new int[]{0x40};
            case "NEGB" -> new int[]{0x50};
            case "NEG" -> switch (mode) {
                case DIRECT -> new int[]{0x00};
                case INDEXED -> new int[]{0x60};
                case EXTENDED -> new int[]{0x70};
                default -> new int[]{0x12};
            };

            // TEST
            case "TSTA" -> new int[]{0x4D};
            case "TSTB" -> new int[]{0x5D};
            case "TST" -> switch (mode) {
                case DIRECT -> new int[]{0x0D};
                case INDEXED -> new int[]{0x6D};
                case EXTENDED -> new int[]{0x7D};
                default -> new int[]{0x12};
            };

            // SHIFT
            case "ASLA", "LSLA" -> new int[]{0x48};
            case "ASLB", "LSLB" -> new int[]{0x58};
            case "ASL", "LSL" -> switch (mode) {
                case DIRECT -> new int[]{0x08};
                case INDEXED -> new int[]{0x68};
                case EXTENDED -> new int[]{0x78};
                default -> new int[]{0x12};
            };
            case "ASRA" -> new int[]{0x47};
            case "ASRB" -> new int[]{0x57};
            case "ASR" -> switch (mode) {
                case DIRECT -> new int[]{0x07};
                case INDEXED -> new int[]{0x67};
                case EXTENDED -> new int[]{0x77};
                default -> new int[]{0x12};
            };
            case "LSRA" -> new int[]{0x44};
            case "LSRB" -> new int[]{0x54};
            case "LSR" -> switch (mode) {
                case DIRECT -> new int[]{0x04};
                case INDEXED -> new int[]{0x64};
                case EXTENDED -> new int[]{0x74};
                default -> new int[]{0x12};
            };

            // ROTATE
            case "ROLA" -> new int[]{0x49};
            case "ROLB" -> new int[]{0x59};
            case "ROL" -> switch (mode) {
                case DIRECT -> new int[]{0x09};
                case INDEXED -> new int[]{0x69};
                case EXTENDED -> new int[]{0x79};
                default -> new int[]{0x12};
            };
            case "RORA" -> new int[]{0x46};
            case "RORB" -> new int[]{0x56};
            case "ROR" -> switch (mode) {
                case DIRECT -> new int[]{0x06};
                case INDEXED -> new int[]{0x66};
                case EXTENDED -> new int[]{0x76};
                default -> new int[]{0x12};
            };

            // BRANCH
            case "BRA" -> new int[]{0x20};
            case "BRN" -> new int[]{0x21};
            case "BHI" -> new int[]{0x22};
            case "BLS" -> new int[]{0x23};
            case "BCC", "BHS" -> new int[]{0x24};
            case "BCS", "BLO" -> new int[]{0x25};
            case "BNE" -> new int[]{0x26};
            case "BEQ" -> new int[]{0x27};
            case "BVC" -> new int[]{0x28};
            case "BVS" -> new int[]{0x29};
            case "BPL" -> new int[]{0x2A};
            case "BMI" -> new int[]{0x2B};
            case "BGE" -> new int[]{0x2C};
            case "BLT" -> new int[]{0x2D};
            case "BGT" -> new int[]{0x2E};
            case "BLE" -> new int[]{0x2F};
            case "LBRA" -> new int[]{0x16};
            case "LBSR" -> new int[]{0x17};
            case "BSR" -> new int[]{0x8D};

            // JUMP
            case "JMP" -> switch (mode) {
                case DIRECT -> new int[]{0x0E};
                case INDEXED -> new int[]{0x6E};
                case EXTENDED -> new int[]{0x7E};
                default -> new int[]{0x12};
            };
            case "JSR" -> switch (mode) {
                case DIRECT -> new int[]{0x9D};
                case INDEXED -> new int[]{0xAD};
                case EXTENDED -> new int[]{0xBD};
                default -> new int[]{0x12};
            };
            case "RTS" -> new int[]{0x39};
            case "RTI" -> new int[]{0x3B};

            // STACK
            case "PSHS" -> new int[]{0x34};
            case "PSHU" -> new int[]{0x36};
            case "PULS" -> new int[]{0x35};
            case "PULU" -> new int[]{0x37};

            // TRANSFER/EXCHANGE
            case "TFR" -> new int[]{0x1F};
            case "EXG" -> new int[]{0x1E};

            // SPECIAL
            case "ABX" -> new int[]{0x3A};
            case "MUL" -> new int[]{0x3D};
            case "SEX" -> new int[]{0x1D};
            case "DAA" -> new int[]{0x19};
            case "NOP" -> new int[]{0x12};
            case "SWI" -> new int[]{0x3F};
            case "SWI2" -> new int[]{0x10, 0x3F};
            case "SWI3" -> new int[]{0x11, 0x3F};
            case "CWAI" -> new int[]{0x3C};
            case "SYNC" -> new int[]{0x13};

            default -> new int[]{0x12}; // NOP par défaut
        };
    }

    // SECTION: MEMORY ACCESS HELPERS

    /*
     * Lit un octet depuis la mémoire (RAM ou ROM selon l'adresse)
     */
    private int readMemoryByte(int address) {
        try {
            address &= 0xFFFF;

            // Déterminer RAM ou ROM
            DefaultTableModel model;
            int offset;

            if (address >= 0xFC00) {
                // Zone ROM
                model = romModel;
                offset = address - 0xFC00;
            } else {
                // Zone RAM
                model = ramModel;
                offset = address;
            }

            if (offset < 0 || offset >= model.getRowCount()) return 0;

            Object value = model.getValueAt(offset, 1);
            if (value == null) return 0;

            String hex = value.toString().replaceAll("[^0-9A-Fa-f]", "");
            if (hex.isEmpty()) return 0;

            return Integer.parseInt(hex, 16) & 0xFF;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Lit un mot (16 bits) depuis la RAM
     * Format big-endian: byte_haut @ address, byte_bas @ address+1
     */
    private int readMemoryWord(int address) {
        int high = readMemoryByte(address) & 0xFF;
        int low = readMemoryByte((address + 1) & 0xFFFF) & 0xFF;
        return ((high << 8) | low) & 0xFFFF;
    }

    /**
     * Écrit un octet dans la RAM
     */
    private void writeMemoryByte(int address, int value) {
        try {
            address &= 0xFFFF;

            DefaultTableModel model;
            int offset;

            if (address >= 0xFC00) {
                model = romModel;
                offset = address - 0xFC00;
            } else {
                model = ramModel;
                offset = address;
            }

            if (offset < 0 || offset >= model.getRowCount()) return;

            model.setValueAt(CPU.decimalToHex(value & 0xFF, 2), offset, 1);
        } catch (Exception e) {
            System.err.println("Erreur écriture @" + Integer.toHexString(address));
        }
    }

    /**
     * Écrit un mot (16 bits) dans la RAM
     */
    private void writeMemoryWord(int address, int value) {
        writeMemoryByte(address, (value >> 8) & 0xFF);
        writeMemoryByte((address + 1) & 0xFFFF, value & 0xFF);
    }

    // SECTION: OPERAND READING (Selon mode d'adressage)

    /**
     * Lit une valeur 8-bit selon le mode d'adressage
     */
    private int readOperand8(InstructionDecoder.AddressingMode mode, String operand) {
        return switch (mode) {
            case IMMEDIATE -> CPU.hexToDecimal(operand) & 0xFF;
            case DIRECT -> {
                int addr = ((cpu.getDP() & 0xFF) << 8) | (CPU.hexToDecimal(operand) & 0xFF);
                yield readMemoryByte(addr);
            }
            case EXTENDED -> {
                int addr = CPU.hexToDecimal(operand) & 0xFFFF;
                yield readMemoryByte(addr);
            }
            case EXTENDED_INDIRECT -> {
                int ptr = CPU.hexToDecimal(operand) & 0xFFFF;
                int addr = readMemoryWord(ptr);
                yield readMemoryByte(addr);
            }
            case INDEXED -> {
                int addr = resolveIndexedAddress(operand);
                yield readMemoryByte(addr);
            }
            default -> 0;
        };
    }

    /**
     * Lit une valeur 16-bit selon le mode d'adressage
     */
    private int readOperand16(InstructionDecoder.AddressingMode mode, String operand) {
        return switch (mode) {
            case IMMEDIATE -> CPU.hexToDecimal(operand) & 0xFFFF;
            case DIRECT -> {
                int addr = ((cpu.getDP() & 0xFF) << 8) | (CPU.hexToDecimal(operand) & 0xFF);
                yield readMemoryWord(addr);
            }
            case EXTENDED -> {
                int addr = CPU.hexToDecimal(operand) & 0xFFFF;
                yield readMemoryWord(addr);
            }
            case EXTENDED_INDIRECT -> {
                int ptr = CPU.hexToDecimal(operand) & 0xFFFF;
                int addr = readMemoryWord(ptr);
                yield readMemoryWord(addr);
            }
            case INDEXED -> {
                int addr = resolveIndexedAddress(operand);
                yield readMemoryWord(addr);
            }
            default -> 0;
        };
    }

    // SECTION: EFFECTIVE ADDRESS CALCULATION
    /**
     * Calcule l'adresse effective selon le mode d'adressage
     */
    private int getEffectiveAddress(InstructionDecoder.AddressingMode mode, String operand) {
        return switch (mode) {
            case DIRECT -> ((cpu.getDP() & 0xFF) << 8) | (CPU.hexToDecimal(operand) & 0xFF);
            case EXTENDED -> CPU.hexToDecimal(operand) & 0xFFFF;
            case EXTENDED_INDIRECT -> {
                int ptr = CPU.hexToDecimal(operand) & 0xFFFF;
                yield readMemoryWord(ptr);
            }
            case INDEXED -> resolveIndexedAddress(operand);
            default -> 0;
        };
    }

    private int resolveIndexedAddress(String indexedOperand) {
        if (indexedOperand == null) return 0;

        String op = indexedOperand.replaceAll("\\s", "").toUpperCase();

        // GESTION DU MODE INDIRECT
        boolean indirect = false;
        if (op.startsWith("[") && op.endsWith("]")) {
            indirect = true;
            op = op.substring(1, op.length() - 1);
        }
        // PARSING: "offset,registre"
        String[] parts = op.split(",", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format indexé invalide: " + indexedOperand);
        }
        String offsetStr = parts[0].trim();
        String regStr = parts[1].trim();

        //  CORRECTION 1: Détecter auto-inc/dec AVANT de traiter le registre
        // Format: ",R+" → offsetStr = "", regStr = "R+"
        // Format: "5,R"  → offsetStr = "5", regStr = "R"

        String baseRegister = regStr;
        String incrementMode = "";

        // Détecter les suffixes/préfixes d'incrémentation
        if (regStr.endsWith("++")) {
            baseRegister = regStr.substring(0, regStr.length() - 2);
            incrementMode = "++";
        } else if (regStr.endsWith("+")) {
            baseRegister = regStr.substring(0, regStr.length() - 1);
            incrementMode = "+";
        } else if (regStr.startsWith("--")) {
            baseRegister = regStr.substring(2);
            incrementMode = "--";
        } else if (regStr.startsWith("-")) {
            baseRegister = regStr.substring(1);
            incrementMode = "-";
        }
        // VALIDATION DU REGISTRE
        if (!baseRegister.matches("^[XYUS]$")) {
            throw new IllegalArgumentException("Registre indexé invalide: " + baseRegister +
                    " (doit être X, Y, U ou S)");
        }

        // Obtenir la valeur du registre d'index
        int baseAddress = switch (baseRegister) {
            case "X" -> cpu.getX();
            case "Y" -> cpu.getY();
            case "U" -> cpu.getU();
            case "S" -> cpu.getS();
            default -> throw new IllegalArgumentException("Registre invalide: " + baseRegister);
        };

        int effectiveAddress = baseAddress;

        // CALCUL DE L'ADRESSE SELON LE TYPE D'OFFSET
        if (offsetStr.isEmpty()) {

            // CAS 1: PAS D'OFFSET (avec ou sans auto-inc/dec)
            switch (incrementMode) {
                case "+":
                    // ,R+ (post-incrémentation +1)
                    effectiveAddress = baseAddress;
                    updateIndexRegister(baseRegister, (baseAddress + 1) & 0xFFFF);
                    break;

                case "++":
                    // ,R++ (post-incrémentation +2)
                    effectiveAddress = baseAddress;
                    updateIndexRegister(baseRegister, (baseAddress + 2) & 0xFFFF);
                    break;

                case "-":
                    // ,-R (pré-décrémentation -1)
                    effectiveAddress = (baseAddress - 1) & 0xFFFF;
                    updateIndexRegister(baseRegister, effectiveAddress);
                    break;

                case "--":
                    // ,--R (pré-décrémentation -2)
                    effectiveAddress = (baseAddress - 2) & 0xFFFF;
                    updateIndexRegister(baseRegister, effectiveAddress);
                    break;

                default:
                    // ,R (pas d'offset ni d'incrémentation)
                    effectiveAddress = baseAddress;
                    break;
            }

        } else if (offsetStr.equals("A")) {
            int aValue = cpu.getA();
            if ((aValue & 0x80) != 0) {
                aValue |= 0xFFFFFF00; // Extension de signe
            }
            effectiveAddress = (baseAddress + aValue) & 0xFFFF;

        } else if (offsetStr.equals("B")) {
            int bValue = cpu.getB();
            if ((bValue & 0x80) != 0) {
                bValue |= 0xFFFFFF00;
            }
            effectiveAddress = (baseAddress + bValue) & 0xFFFF;

        } else if (offsetStr.equals("D")) {
            int dValue = cpu.getD();
            if ((dValue & 0x8000) != 0) {
                dValue |= 0xFFFF0000; // Extension de signe
            }
            effectiveAddress = (baseAddress + dValue) & 0xFFFF;

        } else {
            String cleanOffset = offsetStr.replace("$", "").replace("#", "").trim();
            int offset;

            try {
                // Parser l'offset (hexadécimal ou décimal)
                if (offsetStr.contains("$") || offsetStr.matches("^[0-9A-Fa-f]+$")) {
                    // Hexadécimal
                    offset = Integer.parseInt(cleanOffset, 16);
                } else {
                    // Décimal
                    offset = Integer.parseInt(cleanOffset);
                }

                if (cleanOffset.length() <= 2) {
                    // Offset 8-bit SIGNÉ
                    if (offset >= 0x80) {
                        offset = offset - 0x100;
                    }
                } else if (cleanOffset.length() <= 4) {
                    // Offset 16-bit SIGNÉ (-32768 à +32767)
                    if (offset >= 0x8000) {
                        offset = offset - 0x10000; // Extension de signe 16→32 bits
                    }
                }

                effectiveAddress = (baseAddress + offset) & 0xFFFF;

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Format d'offset invalide: " + offsetStr +
                                " (attendu: nombre hexadécimal ou décimal)"
                );
            }
        }
        // GESTION DU MODE INDIRECT
        if (indirect) {
            // L'adresse calculée est un pointeur vers l'adresse réelle
            effectiveAddress = readMemoryWord(effectiveAddress);
        }

        return effectiveAddress & 0xFFFF;
    }
    /**
     * Met à jour un registre d'index (pour auto-inc/dec)
     */
    private void updateIndexRegister(String register, int newValue) {
        int value = newValue & 0xFFFF;
        switch (register) {
            case "X" -> cpu.setX(value);
            case "Y" -> cpu.setY(value);
            case "U" -> cpu.setU(value);
            case "S" -> cpu.setS(value);
            default -> throw new IllegalArgumentException("Registre invalide: " + register);
        }
    }
    // SECTION: REGISTER ACCESS HELPERS
    private int getRegisterValue(char register) {
        return switch (register) {
            case 'A' -> cpu.getA();
            case 'B' -> cpu.getB();
            case 'D' -> cpu.getD();
            case 'X' -> cpu.getX();
            case 'Y' -> cpu.getY();
            case 'U' -> cpu.getU();
            case 'S' -> cpu.getS();
            case 'P' -> cpu.getPC();
            default -> 0;
        };
    }

    private void setRegisterValue(char register, int value) {
        switch (register) {
            case 'A' -> cpu.setA(value & 0xFF);
            case 'B' -> cpu.setB(value & 0xFF);
            case 'D' -> cpu.setD(value & 0xFFFF);
            case 'X' -> cpu.setX(value & 0xFFFF);
            case 'Y' -> cpu.setY(value & 0xFFFF);
            case 'U' -> cpu.setU(value & 0xFFFF);
            case 'S' -> cpu.setS(value & 0xFFFF);
            case 'P' -> cpu.setPC(value & 0xFFFF);
        }
    }

    // SECTION: STACK HELPERS
    /**
     * Empile un octet (décrémente SP puis écrit)
     */
    private int pushByte(int sp, int value) {
        sp = (sp - 1) & 0xFFFF;
        writeMemoryByte(sp, value & 0xFF);
        return sp;
    }

    /**
     * Empile un mot (décrémente SP deux fois puis écrit MSB puis LSB)
     */
    private int pushWord(int sp, int value) {
        // ÉTAPE 1: Empiler MSB (high byte) en PREMIER
        sp = (sp - 1) & 0xFFFF;
        writeMemoryByte(sp, (value >> 8) & 0xFF);  // MSB

        // ÉTAPE 2: Empiler LSB (low byte) ensuite
        sp = (sp - 1) & 0xFFFF;
        writeMemoryByte(sp, value & 0xFF);         // LSB

        return sp;
    }


    /**
     * Dépile un octet (lit puis incrémente SP)
     */
    private int pullByte(int sp) {
        return readMemoryByte(sp) & 0xFF;
    }

    /**
     * Dépile un mot (lit MSB puis LSB)
     */
    private int pullWord(int sp) {
        int high = readMemoryByte(sp) & 0xFF;
        int low = readMemoryByte((sp + 1) & 0xFFFF) & 0xFF;
        return ((high << 8) | low) & 0xFFFF;
    }

    /**
     * Parse le masque de registres pour PSHS/PULS/PSHU/PULU
     * Format: "A,B,X" ou "$FF"
     * <p>
     * Bits: 7=PC, 6=U/S, 5=Y, 4=X, 3=DP, 2=B, 1=A, 0=CC
     */
    private int parseRegisterMask(String operand) {
        operand = operand.replaceAll("\\s*,\\s*", ",").trim();
        // Vérification de sécurité
        if (operand == null || operand.trim().isEmpty()) {
            throw new IllegalArgumentException("Opérande vide pour PSHS/PULS/PSHU/PULU");
        }
        // Nettoyer l'opérande
        operand = operand.trim();
        if (operand.startsWith("$") || operand.startsWith("#")) {
            String hex = operand
                    .replace("#", "")
                    .replace("$", "")
                    .trim();

            try {
                int value = Integer.parseInt(hex, 16);
                return value & 0xFF;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Format hexadécimal invalide: " + operand
                );
            }
        }

        // CAS 2: Liste de registres
        int mask = 0;

        // Séparer par virgules
        String[] regs = operand.toUpperCase().split(",");

        for (String reg : regs) {
            // CRITIQUE: Enlever TOUS les espaces avant et après
            reg = reg.trim();

            // Ignorer les chaînes vides (cas: "A,,,B")
            if (reg.isEmpty()) {
                continue;
            }

            // Mapper chaque registre à son bit
            int regBit = switch (reg) {
                case "CC", "CCR" -> 0x01;  // Bit 0 - Condition Code Register
                case "A" -> 0x02;          // Bit 1 - Accumulateur A
                case "B" -> 0x04;          // Bit 2 - Accumulateur B
                case "D" -> 0x06;          // Bits 1+2 - Accumulateur D (A+B)
                case "DP" -> 0x08;         // Bit 3 - Direct Page Register
                case "X" -> 0x10;          // Bit 4 - Index Register X
                case "Y" -> 0x20;          // Bit 5 - Index Register Y
                case "U" -> 0x40;          // Bit 6 - User Stack Pointer
                case "S" -> 0x40;          // Bit 6 - System Stack Pointer
                case "PC" -> 0x80;         // Bit 7 - Program Counter
                default -> {
                    // Registre non reconnu - afficher erreur claire
                    throw new IllegalArgumentException(
                            "Registre d'index invalide: " + reg +
                                    "\nRegistres valides: CC, A, B, D, DP, X, Y, U, S, PC"
                    );
                }
            };

            mask |= regBit;
        }

        // Vérifier que le masque n'est pas vide
        if (mask == 0) {
            throw new IllegalArgumentException(
                    "Aucun registre valide trouvé dans: " + operand
            );
        }

        return mask;
    }

    public void registerLabel(String name, int address) {
        labelManager.addLabel(name, address);
    }

    /**
     * Obtient le gestionnaire d'étiquettes
     */
    public LabelManager getLabelManager() {
        return labelManager;
    }

    /**
     * Efface toutes les étiquettes
     */
    public void clearLabels() {
        labelManager.clear();
    }

    /**
     * Affiche la table des symboles (pour debug)
     */
    public void printSymbolTable() {
        labelManager.print();
    }

    /**
     * Résout une étiquette en adresse hexadécimale
     * Si c'est déjà un nombre, le retourne tel quel
     */
    private String resolveLabel(String operand) {
        if (operand == null || operand.trim().isEmpty()) {
            return operand;
        }

        String op = operand.trim();

        // Si c'est déjà un nombre hexadécimal, ne rien faire
        if (op.startsWith("$") || op.startsWith("#")) {
            return operand;
        }

        // Si c'est un nombre hex sans $
        if (op.matches("^[0-9A-Fa-f]+$") && op.length() <= 4) {
            return operand;
        }

        // Si c'est un label, le résoudre
        if (labelManager.isLabelName(op)) {
            Integer address = labelManager.getAddress(op);

            if (address == null) {
                throw new IllegalArgumentException(
                        "⚠️  Étiquette non définie: " + op
                );
            }

            return CPU.decimalToHex(address, 4);
        }

        return operand;
    }

    /**
     * Calcule la taille d'une instruction en octets
     * Nécessaire pour la collecte d'étiquettes (passe 1)
     */
    public int computeInstructionSize(InstructionDecoder.DecodedInstruction instr) {
        if (instr == null) return 0;

        String mnemonic = instr.operation;
        InstructionDecoder.AddressingMode mode = instr.mode;

        // Instructions inhérentes (1 octet)
        if (mnemonic.matches("NOP|RTS|RTI|SWI|INCA|INCB|DECA|DECB|CLRA|CLRB|COMA|COMB|NEGA|NEGB|ASLA|ASLB|ASRA|ASRB|LSLA|LSLB|LSRA|LSRB|ROLA|ROLB|RORA|RORB|DAA|SEX|ABX|MUL")) {
            return 1;
        }

        // PSHS/PULS/PSHU/PULU (2 octets: opcode + postbyte)
        if (mnemonic.matches("PSHS|PULS|PSHU|PULU")) {
            return 2;
        }

        // TFR/EXG (2 octets: opcode + postbyte)
        if (mnemonic.matches("TFR|EXG")) {
            return 2;
        }

        // Branches courtes (2 octets: opcode + offset 8 bits)
        if (mnemonic.matches("BRA|BRN|BEQ|BNE|BCC|BCS|BPL|BMI|BVC|BVS|BGT|BLE|BGE|BLT|BHI|BLS|BSR")) {
            return 2;
        }

        // Branches longues (3 octets: opcode + offset 16 bits)
        if (mnemonic.matches("LBRA|LBSR|LBEQ|LBNE|LBCC|LBCS|LBPL|LBMI|LBVC|LBVS|LBGT|LBLE|LBGE|LBLT|LBHI|LBLS")) {
            return 3;
        }

        // Modes d'adressage
        switch (mode) {
            case IMMEDIATE:
                // 8 bits pour A/B, 16 bits pour D/X/Y/S/U
                if (mnemonic.matches("LDD|LDX|LDY|LDS|LDU|ADDD|SUBD|CMPD|CMPX|CMPY|CMPS|CMPU")) {
                    return 3; // opcode + 2 octets
                }
                return 2; // opcode + 1 octet

            case DIRECT:
                return 2; // opcode + 1 octet d'adresse

            case EXTENDED:
                return 3; // opcode + 2 octets d'adresse

            case INDEXED:
                if (instr.indexedInfo != null) {
                    int postByteSize = 1; // Le postbyte lui-même
                    int offsetSize = InstructionDecoder.getOffsetByteCount(instr.indexedInfo);
                    return 1 + postByteSize + offsetSize; // opcode + postbyte + offset
                }
                return 2; // Par défaut

            case INHERENT:
                return 1;

            case RELATIVE:
                return 2; // Déjà géré dans les branches

            default:
                return 2; // Taille par défaut
        }
    }
    public void setRomAddress(int address) {
        this.romAddress = address & 0xFFFF;
    }

    /**
     * Parse un déplacement signé 8 bits
     * Exemple: "$FA" = -6, "$05" = +5
     */
    private int parseSignedDisplacement8(String operand) {
        try {
            // Retirer le $ si présent
            String hexStr = operand.replace("$", "").replace("#", "");
            int value = Integer.parseInt(hexStr, 16) & 0xFF;

            // Extension de signe pour 8 bits
            if ((value & 0x80) != 0) {
                value = value - 0x100; // Ex: 0xFA = 250 → 250 - 256 = -6
            }
            return value;
        } catch (NumberFormatException e) {
            System.err.println("Erreur parsing déplacement 8 bits: " + operand);
            return 0;
        }
    }

    /**
     * Parse un déplacement signé 16 bits
     */
    private int parseSignedDisplacement16(String operand) {
        try {
            String hexStr = operand.replace("$", "").replace("#", "");
            int value = Integer.parseInt(hexStr, 16) & 0xFFFF;

            // Extension de signe pour 16 bits
            if ((value & 0x8000) != 0) {
                value = value - 0x10000; // Ex: 0xFFFA = 65530 → 65530 - 65536 = -6
            }
            return value;
        } catch (NumberFormatException e) {
            System.err.println("Erreur parsing déplacement 16 bits: " + operand);
            return 0;
        }
    }
}

