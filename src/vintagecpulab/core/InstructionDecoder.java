package vintagecpulab.core;

/**
 * InstructionDecoder - Décodeur d'instructions pour Motorola 6809
 */

public class InstructionDecoder {
    
    public enum AddressingMode {
        IMMEDIATE,
        DIRECT,
        EXTENDED,
        EXTENDED_INDIRECT,
        INDEXED,
        INHERENT,
        RELATIVE
    }


    public enum IncrementMode {
        NONE,
        POST_INC_1,
        POST_INC_2,
        PRE_DEC_1,
        PRE_DEC_2
    }

    public enum OffsetType {
        NONE,
        CONSTANT_5BIT,
        CONSTANT_8BIT,
        CONSTANT_16BIT,
        ACCUMULATOR_A,
        ACCUMULATOR_B,
        ACCUMULATOR_D
    }


    public static class DecodedInstruction {
        public final String operation;
        public final AddressingMode mode;
        public final String operand;
        public final char targetRegister;
        public final IndexedDetails indexedInfo;
        public final DirectDetails directInfo;
        public DecodedInstruction(String operation, AddressingMode mode, String operand) {
            this(operation, mode, operand, null, null);
        }

        public DecodedInstruction(String operation, AddressingMode mode, String operand,
                                  IndexedDetails indexedInfo) {
            this(operation, mode, operand, indexedInfo, null);
        }

        public DecodedInstruction(String operation, AddressingMode mode, String operand,
                                  IndexedDetails indexedInfo, DirectDetails directInfo) {
            this.operation = operation.toUpperCase();
            this.mode = mode;
            this.operand = (operand == null) ? "" : operand.toUpperCase();
            this.targetRegister = extractTargetRegister(operation);
            this.indexedInfo = indexedInfo;
            this.directInfo = directInfo;
        }

        private char extractTargetRegister(String mnem) {
            if (mnem.length() < 3) return ' ';
            char last = Character.toUpperCase(mnem.charAt(mnem.length() - 1));
            if ("ABDXYSUD".indexOf(last) >= 0) return last;
            return ' ';
        }

        @Override
        public String toString() {
            String result = operation + " [" + mode + "] operand='" + operand +
                    "' register=" + targetRegister;
            if (indexedInfo != null) result += " " + indexedInfo;
            if (directInfo != null) result += " " + directInfo;
            return result;
        }
    }


    public static class IndexedDetails {
        public final String indexRegister;      // X, Y, U, S
        public final String offset;             // Offset numérique ou registre
        public final boolean isIndirect;        // [...] forme indirecte
        public final IncrementMode incrementMode;
        public final OffsetType offsetType;

        public IndexedDetails(String indexRegister, String offset, boolean isIndirect,
                              IncrementMode incrementMode, OffsetType offsetType) {
            this.indexRegister = indexRegister;
            this.offset = offset;
            this.isIndirect = isIndirect;
            this.incrementMode = incrementMode;
            this.offsetType = offsetType;
        }

        @Override
        public String toString() {
            return String.format("Indexed[reg=%s, offset=%s, indirect=%b, inc=%s, type=%s]",
                    indexRegister, offset, isIndirect, incrementMode, offsetType);
        }
    }


    public static class DirectDetails {
        public final String offsetByte;         // Octet d'offset (00-FF)
        public final boolean forceDirect;       // Force le mode direct (<$XX)

        public DirectDetails(String offsetByte, boolean forceDirect) {
            this.offsetByte = offsetByte.toUpperCase();
            this.forceDirect = forceDirect;
        }


        public String calculateFullAddress(String currentDP) {
            return currentDP + offsetByte;
        }

        @Override
        public String toString() {
            return String.format("Direct[offset=%s, forced=%b]", offsetByte, forceDirect);
        }
    }

    public static DecodedInstruction decode(String line) {
        if (line == null) return null;

        // Normalisation
        line = line.trim();
        if (line.isEmpty()) return null;

        // Retirer les commentaires
        int commentIndex = line.indexOf(';');
        if (commentIndex >= 0) {
            line = line.substring(0, commentIndex).trim();
        }
        if (line.isEmpty()) return null;

        // Ignorer les étiquettes
        if (line.endsWith(":")) return null;

        String[] parts = line.split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();
        String operandRaw = (parts.length > 1) ? parts[1].trim() : "";


        if (isStackInstruction(mnemonic)) {
            return new DecodedInstruction(mnemonic, AddressingMode.INHERENT, operandRaw);
        }

        if (isBranchInstruction(mnemonic)) {
            return new DecodedInstruction(mnemonic, AddressingMode.RELATIVE, operandRaw);
        }

        if (operandRaw.isEmpty()) {
            return new DecodedInstruction(mnemonic, AddressingMode.INHERENT, "");
        }


        if (operandRaw.startsWith("#")) {
            String cleanOperand = extractHexValue(operandRaw.substring(1));
            return new DecodedInstruction(mnemonic, AddressingMode.IMMEDIATE, cleanOperand);
        }

        if (operandRaw.startsWith("<")) {
            String cleanOperand = extractHexValue(operandRaw.substring(1));
            DirectDetails details = new DirectDetails(cleanOperand, true);
            return new DecodedInstruction(mnemonic, AddressingMode.DIRECT, cleanOperand, null, details);
        }

        if (operandRaw.startsWith(">")) {
            String cleanOperand = extractHexValue(operandRaw.substring(1));
            return new DecodedInstruction(mnemonic, AddressingMode.EXTENDED, cleanOperand);
        }

        if (operandRaw.startsWith("[") && operandRaw.endsWith("]") && !operandRaw.contains(",")) {
            String inside = operandRaw.substring(1, operandRaw.length() - 1).trim();
            String cleanOperand = extractHexValue(inside);
            return new DecodedInstruction(mnemonic, AddressingMode.EXTENDED_INDIRECT, cleanOperand);
        }

        if (operandRaw.contains(",")) {
            return decodeIndexed(mnemonic, operandRaw);
        }

        String cleanOperand = extractHexValue(operandRaw);
        if (cleanOperand.length() <= 2) {
            DirectDetails details = new DirectDetails(cleanOperand, false);
            return new DecodedInstruction(mnemonic, AddressingMode.DIRECT, cleanOperand, null, details);
        } else {
            return new DecodedInstruction(mnemonic, AddressingMode.EXTENDED, cleanOperand);
        }
    }

    private static DecodedInstruction decodeIndexed(String mnemonic, String operandRaw) {
        String operand = operandRaw.replaceAll("\\s+", "").toUpperCase();

        // Détecter si indirect [...]
        boolean isIndirect = false;
        if (operand.startsWith("[") && operand.endsWith("]")) {
            isIndirect = true;
            operand = operand.substring(1, operand.length() - 1);
        }

        String[] parts = operand.split(",", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format indexé invalide: " + operandRaw);
        }

        String offsetPart = parts[0];
        String registerPart = parts[1];

        String indexRegister = extractIndexRegister(registerPart);

        IncrementMode incMode = IncrementMode.NONE;
        OffsetType offsetType;
        String offsetValue = "";

        if (offsetPart.isEmpty()) {
            if (registerPart.endsWith("++")) {
                incMode = IncrementMode.POST_INC_2;
                indexRegister = registerPart.substring(0, registerPart.length() - 2);
                offsetType = OffsetType.NONE;
            } else if (registerPart.endsWith("+")) {
                incMode = IncrementMode.POST_INC_1;
                indexRegister = registerPart.substring(0, registerPart.length() - 1);
                offsetType = OffsetType.NONE;
            } else if (registerPart.startsWith("--")) {
                incMode = IncrementMode.PRE_DEC_2;
                indexRegister = registerPart.substring(2);
                offsetType = OffsetType.NONE;
            } else if (registerPart.startsWith("-")) {
                incMode = IncrementMode.PRE_DEC_1;
                indexRegister = registerPart.substring(1);
                offsetType = OffsetType.NONE;
            } else {
                offsetType = OffsetType.NONE;
            }
        }
        else if (offsetPart.equals("A")) {
            offsetType = OffsetType.ACCUMULATOR_A;
            offsetValue = "A";
        } else if (offsetPart.equals("B")) {
            offsetType = OffsetType.ACCUMULATOR_B;
            offsetValue = "B";
        } else if (offsetPart.equals("D")) {
            offsetType = OffsetType.ACCUMULATOR_D;
            offsetValue = "D";
        }
        else {
            offsetValue = extractHexValue(offsetPart);
            int value = parseHexOrDecimal(offsetValue, offsetPart);

            if (value >= -16 && value <= 15) {
                offsetType = OffsetType.CONSTANT_5BIT;
            } else if (value >= -128 && value <= 127) {
                offsetType = OffsetType.CONSTANT_8BIT;
            } else {
                offsetType = OffsetType.CONSTANT_16BIT;
            }
        }

        IndexedDetails details = new IndexedDetails(indexRegister, offsetValue, isIndirect,
                incMode, offsetType);
        return new DecodedInstruction(mnemonic, AddressingMode.INDEXED, operandRaw, details);
    }


    public static int calculatePostByte(IndexedDetails details) {
        if (details == null) return 0x00;

        int registerCode;
        switch (details.indexRegister) {
            case "X": registerCode = 0x00; break;
            case "Y": registerCode = 0x20; break;
            case "U": registerCode = 0x40; break;
            case "S": registerCode = 0x60; break;
            default: throw new IllegalArgumentException("Registre invalide: " + details.indexRegister);
        }

        int postByte = registerCode;

        if (details.isIndirect) {
            postByte |= 0x10;
        }

        switch (details.offsetType) {
            case NONE:
                switch (details.incrementMode) {
                    case POST_INC_1:  postByte |= 0x80; break; // ,R+
                    case POST_INC_2:  postByte |= 0x81; break; // ,R++
                    case PRE_DEC_1:   postByte |= 0x82; break; // ,-R
                    case PRE_DEC_2:   postByte |= 0x83; break; // ,--R
                    case NONE:        postByte |= 0x84; break; // ,R
                }
                break;

            case CONSTANT_5BIT:
                int offset5 = parseHexOrDecimal(details.offset, details.offset);
                postByte |= (offset5 & 0x1F);
                break;

            case CONSTANT_8BIT:
                postByte |= 0x88;
                break;

            case CONSTANT_16BIT:
                postByte |= 0x89;
                break;

            case ACCUMULATOR_A:
                postByte |= 0x86;
                break;

            case ACCUMULATOR_B:
                postByte |= 0x85;
                break;

            case ACCUMULATOR_D:
                postByte |= 0x8B;
                break;
        }

        return postByte & 0xFF;
    }


    public static int getOffsetByteCount(IndexedDetails details) {
        if (details == null) return 0;

        switch (details.offsetType) {
            case CONSTANT_5BIT:
            case NONE:
            case ACCUMULATOR_A:
            case ACCUMULATOR_B:
            case ACCUMULATOR_D:
                return 0;

            case CONSTANT_8BIT:
                return 1;

            case CONSTANT_16BIT:
                return 2;

            default:
                return 0;
        }
    }

    public static boolean canUseDirect(String targetAddress, String currentDP) {
        if (targetAddress == null || currentDP == null) return false;

        while (targetAddress.length() < 4) targetAddress = "0" + targetAddress;
        while (currentDP.length() < 2) currentDP = "0" + currentDP;

        String targetDP = targetAddress.substring(0, 2);

        return targetDP.equalsIgnoreCase(currentDP);
    }

    private static boolean isStackInstruction(String mnemonic) {
        return mnemonic.equals("PSHS") ||
                mnemonic.equals("PULS") ||
                mnemonic.equals("PSHU") ||
                mnemonic.equals("PULU");
    }

    private static String extractIndexRegister(String registerPart) {
        String reg = registerPart.replaceAll("[+\\-]", "").trim();
        if (reg.equals("X") || reg.equals("Y") || reg.equals("U") || reg.equals("S")) {
            return reg;
        }
        throw new IllegalArgumentException("Registre d'index invalide: " + registerPart);
    }

    private static String extractHexValue(String text) {
        if (text == null || text.isEmpty()) return "";

        text = text.replace("$", "").replace("0x", "").replace("0X", "")
                .replace("<", "").replace(">", "");

        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (isHexChar(c)) {
                result.append(c);
            }
        }

        return result.toString().toUpperCase();
    }

    private static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'A' && c <= 'F') ||
                (c >= 'a' && c <= 'f');
    }

    private static int parseHexOrDecimal(String cleaned, String original) {
        try {
            if (original.startsWith("-")) {
                String absValue = cleaned.isEmpty() ? original.substring(1) : cleaned;
                return -Integer.parseInt(absValue, 16);
            }
            return Integer.parseInt(cleaned, 16);
        } catch (NumberFormatException e) {
            try {
                return Integer.parseInt(original.replaceAll("[^0-9\\-]", ""));
            } catch (NumberFormatException e2) {
                return 0;
            }
        }
    }

    private static boolean isBranchInstruction(String mnemonic) {
        return mnemonic.equals("BRA") || mnemonic.equals("BRN") ||
                mnemonic.equals("BHI") || mnemonic.equals("BLS") ||
                mnemonic.equals("BCC") || mnemonic.equals("BCS") ||
                mnemonic.equals("BNE") || mnemonic.equals("BEQ") ||
                mnemonic.equals("BVC") || mnemonic.equals("BVS") ||
                mnemonic.equals("BPL") || mnemonic.equals("BMI") ||
                mnemonic.equals("BGE") || mnemonic.equals("BLT") ||
                mnemonic.equals("BGT") || mnemonic.equals("BLE") ||
                mnemonic.equals("BSR") || mnemonic.equals("LBRA") ||
                mnemonic.equals("LBSR") || mnemonic.equals("LBRN");
    }
    public static boolean hasLabel(String line) {
        if (line == null || line.trim().isEmpty()) return false;

        String cleaned = line.trim();


        if (cleaned.contains(":")) {
            return true;
        }


        String[] parts = cleaned.split("\\s+", 2);
        if (parts.length < 2) return false;

        String firstWord = parts[0].toUpperCase();


        String[] keywords = {
                "ORG", "END", "FCB", "FDB", "FCC", "RMB",
                "LDA", "LDB", "LDD", "LDX", "LDY", "LDS", "LDU",
                "STA", "STB", "STD", "STX", "STY", "STS", "STU",

                "ADDA", "ADDB", "ADDD", "ADCA", "ADCB",
                "SUBA", "SUBB", "SUBD", "SBCA", "SBCB",
                "INCA", "INCB", "DECA", "DECB", "INC", "DEC",
                "NEGA", "NEGB", "NEG", "COMA", "COMB", "COM",

                "ANDA", "ANDB", "ORA", "ORB", "EORA", "EORB",
                "BITA", "BITB", "CLRA", "CLRB", "CLR",

                "CMPA", "CMPB", "CMPD", "CMPX", "CMPY", "CMPS", "CMPU",

                "ASLA", "ASLB", "ASL", "ASRA", "ASRB", "ASR",
                "LSLA", "LSLB", "LSL", "LSRA", "LSRB", "LSR",
                "ROLA", "ROLB", "ROL", "RORA", "RORB", "ROR",

                "BRA", "BRN", "BEQ", "BNE", "BCC", "BCS", "BPL", "BMI",
                "BVC", "BVS", "BGT", "BLE", "BGE", "BLT", "BHI", "BLS", "BSR",
                "LBRA", "LBRN", "LBEQ", "LBNE", "LBCC", "LBCS", "LBPL", "LBMI",
                "LBVC", "LBVS", "LBGT", "LBLE", "LBGE", "LBLT", "LBHI", "LBLS", "LBSR",

                "JMP", "JSR", "RTS", "RTI",

                "PSHS", "PULS", "PSHU", "PULU",

                "TFR", "EXG", "LEA", "LEAX", "LEAY", "LEAS", "LEAU",

                "MUL", "DAA", "NOP", "SWI", "SWI2", "SWI3", "CWAI", "SYNC", "SEX", "ABX"
        };

        for (String keyword : keywords) {
            if (firstWord.equals(keyword)) {
                return false;
            }
        }

        return firstWord.matches("^[A-Z_][A-Z0-9_]*$");
    }

    public static String extractLabel(String line) {
        if (line == null || !hasLabel(line)) return null;

        String cleaned = line.trim();

        if (cleaned.contains(":")) {
            int colonIndex = cleaned.indexOf(':');
            return cleaned.substring(0, colonIndex).trim().toUpperCase();
        }

        String[] parts = cleaned.split("\\s+", 2);
        return parts[0].toUpperCase();
    }
    public static String removeLabel(String line) {
        if (line == null || !hasLabel(line)) return line;

        String cleaned = line.trim();

        if (cleaned.contains(":")) {
            int colonIndex = cleaned.indexOf(':');
            if (colonIndex < cleaned.length() - 1) {
                return cleaned.substring(colonIndex + 1).trim();
            }
            return "";
        }

        String[] parts = cleaned.split("\\s+", 2);
        if (parts.length > 1) {
            return parts[1].trim();
        }

        return "";
    }
}
