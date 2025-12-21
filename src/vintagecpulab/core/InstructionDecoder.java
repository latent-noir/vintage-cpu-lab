package vintagecpulab.core;

/* InstructionDecoder - Décodeur d'instructions pour Motorola 6809 */
public class InstructionDecoder {

    /* Modes d'adressage du Motorola 6809 */
    public enum AddressingMode {
        IMMEDIATE,          // #$XX ou #$XXXX
        DIRECT,             // <$XX ou $XX (Page Directe)
        EXTENDED,           // >$XXXX ou $XXXX (Adresse complète)
        EXTENDED_INDIRECT,  // [$XXXX] (Indirect étendu)
        INDEXED,            // Tous les modes indexés
        INHERENT,           // Pas d'opérande (CLRA, INCA, etc.)
        RELATIVE            // Pour les branchements (BEQ, BNE, BRA, etc.)
    }

    /* Modes d'incrémentation/décrémentation pour adressage indexé */
    public enum IncrementMode {
        NONE,           // Pas d'incrémentation
        POST_INC_1,     // ,R+
        POST_INC_2,     // ,R++
        PRE_DEC_1,      // ,-R
        PRE_DEC_2       // ,--R
    }

    /* Types d'offset pour adressage indexé */
    public enum OffsetType {
        NONE,           // ,R (pas d'offset)
        CONSTANT_5BIT,  // n,R (n = -16..+15)
        CONSTANT_8BIT,  // n,R (n = -128..+127)
        CONSTANT_16BIT, // n,R (n = -32768..+32767)
        ACCUMULATOR_A,  // A,R
        ACCUMULATOR_B,  // B,R
        ACCUMULATOR_D   // D,R
    }

    //  CLASSES INTERNES

    /* Instructions décodées */
    public static class DecodedInstruction {
        public final String operation;           // Mnémonique (LDA, STA, etc.)
        public final AddressingMode mode;        // Mode d'adressage
        public final String operand;             // Opérande nettoyée
        public final char targetRegister;        // Registre cible
        public final IndexedDetails indexedInfo; // Détails mode indexé
        public final DirectDetails directInfo;   // Détails mode direct

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

    /* Mode d'adressage indexé */
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

    /* Mode d'adressage direct */
    public static class DirectDetails {
        public final String offsetByte;         // Octet d'offset (00-FF)
        public final boolean forceDirect;       // Force le mode direct (<$XX)

        public DirectDetails(String offsetByte, boolean forceDirect) {
            this.offsetByte = offsetByte.toUpperCase();
            this.forceDirect = forceDirect;
        }

        /* Calcule l'adresse complète avec le registre DP */
        public String calculateFullAddress(String currentDP) {
            return currentDP + offsetByte;
        }

        @Override
        public String toString() {
            return String.format("Direct[offset=%s, forced=%b]", offsetByte, forceDirect);
        }
    }

    //  MÉTHODE PRINCIPALE DE DÉCODAGE
    /* Décode une ligne d'assembleur en instruction structurée */
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

        // Séparer mnémonique et opérande
        String[] parts = line.split("\\s+", 2);
        String mnemonic = parts[0].toUpperCase();
        String operandRaw = (parts.length > 1) ? parts[1].trim() : "";

        // Branchements (mode RELATIVE)
        if (isBranchInstruction(mnemonic)) {
            return new DecodedInstruction(mnemonic, AddressingMode.RELATIVE, operandRaw);
        }

        // Instructions inhérentes
        if (operandRaw.isEmpty()) {
            return new DecodedInstruction(mnemonic, AddressingMode.INHERENT, "");
        }

        // Détection du mode d'adressage

        // IMMEDIATE: #$XX ou #$XXXX
        if (operandRaw.startsWith("#")) {
            String cleanOperand = extractHexValue(operandRaw.substring(1));
            return new DecodedInstruction(mnemonic, AddressingMode.IMMEDIATE, cleanOperand);
        }

        // DIRECT: <$XX (force direct)
        if (operandRaw.startsWith("<")) {
            String cleanOperand = extractHexValue(operandRaw.substring(1));
            DirectDetails details = new DirectDetails(cleanOperand, true);
            return new DecodedInstruction(mnemonic, AddressingMode.DIRECT, cleanOperand, null, details);
        }

        // EXTENDED: >$XXXX (force étendu)
        if (operandRaw.startsWith(">")) {
            String cleanOperand = extractHexValue(operandRaw.substring(1));
            return new DecodedInstruction(mnemonic, AddressingMode.EXTENDED, cleanOperand);
        }

        // EXTENDED_INDIRECT: [$XXXX]
        if (operandRaw.startsWith("[") && operandRaw.endsWith("]") && !operandRaw.contains(",")) {
            String inside = operandRaw.substring(1, operandRaw.length() - 1).trim();
            String cleanOperand = extractHexValue(inside);
            return new DecodedInstruction(mnemonic, AddressingMode.EXTENDED_INDIRECT, cleanOperand);
        }

        // INDEXED: contient une virgule
        if (operandRaw.contains(",")) {
            return decodeIndexed(mnemonic, operandRaw);
        }

        // Par défaut: DIRECT ou EXTENDED selon longueur
        String cleanOperand = extractHexValue(operandRaw);
        if (cleanOperand.length() <= 2) {
            DirectDetails details = new DirectDetails(cleanOperand, false);
            return new DecodedInstruction(mnemonic, AddressingMode.DIRECT, cleanOperand, null, details);
        } else {
            return new DecodedInstruction(mnemonic, AddressingMode.EXTENDED, cleanOperand);
        }
    }

    //  DÉCODAGE MODE INDEXED
    private static DecodedInstruction decodeIndexed(String mnemonic, String operandRaw) {
        String operand = operandRaw.replaceAll("\\s+", "").toUpperCase();

        // Détecter si indirect [...]
        boolean isIndirect = false;
        if (operand.startsWith("[") && operand.endsWith("]")) {
            isIndirect = true;
            operand = operand.substring(1, operand.length() - 1);
        }

        // Séparer offset et registre
        String[] parts = operand.split(",", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format indexé invalide: " + operandRaw);
        }

        String offsetPart = parts[0];
        String registerPart = parts[1];

        // Identifier le registre d'index
        String indexRegister = extractIndexRegister(registerPart);

        // Analyser offset et incrémentation
        IncrementMode incMode = IncrementMode.NONE;
        OffsetType offsetType;
        String offsetValue = "";

        // Cas 1: Pas d'offset (avec ou sans incrémentation)
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
        // Cas 2: Offset accumulateur
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
        // Cas 3: Offset numérique
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

    //  CALCUL DU POSTBYTE
    public static int calculatePostByte(IndexedDetails details) {
        if (details == null) return 0x00;

        // Code du registre (bits 6-5)
        int registerCode;
        switch (details.indexRegister) {
            case "X": registerCode = 0x00; break;
            case "Y": registerCode = 0x20; break;
            case "U": registerCode = 0x40; break;
            case "S": registerCode = 0x60; break;
            default: throw new IllegalArgumentException("Registre invalide: " + details.indexRegister);
        }

        int postByte = registerCode;

        // Bit indirect (bit 4)
        if (details.isIndirect) {
            postByte |= 0x10;
        }

        // Bits selon offset et incrémentation
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

    /* Retourne le nombre d'octets supplémentaires pour l'offset */
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

    //  GESTION MODE DIRECT vs EXTENDED

    /* Vérifie si une adresse peut utiliser le mode DIRECT */
    public static boolean canUseDirect(String targetAddress, String currentDP) {
        if (targetAddress == null || currentDP == null) return false;

        // Normaliser
        while (targetAddress.length() < 4) targetAddress = "0" + targetAddress;
        while (currentDP.length() < 2) currentDP = "0" + currentDP;

        // Extraire l'octet haut
        String targetDP = targetAddress.substring(0, 2);

        // Peut utiliser DIRECT si l'octet haut correspond à DP
        return targetDP.equalsIgnoreCase(currentDP);
    }


    //  MÉTHODES UTILITAIRES

    /* Extrait le registre d'index */
    private static String extractIndexRegister(String registerPart) {
        String reg = registerPart.replaceAll("[+\\-]", "").trim();
        if (reg.equals("X") || reg.equals("Y") || reg.equals("U") || reg.equals("S")) {
            return reg;
        }
        throw new IllegalArgumentException("Registre d'index invalide: " + registerPart);
    }

    /* Extrait la valeur hexadécimale */
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

    /* Vérifie si un caractère est hexadécimal */
    private static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'A' && c <= 'F') ||
                (c >= 'a' && c <= 'f');
    }

    /* Parse hexadécimal ou décimal */
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

    /* Vérifie si c'est un branchement */
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

        // Format: "LABEL:"
        if (cleaned.contains(":")) {
            return true;
        }

        // Format: "LABEL INSTRUCTION"
        String[] parts = cleaned.split("\\s+", 2);
        if (parts.length < 2) return false;

        String firstWord = parts[0].toUpperCase();

        // Liste des directives et mnémoniques connus
        String[] keywords = {
                // Directives
                "ORG", "END", "FCB", "FDB", "FCC", "RMB",

                // Chargement/Stockage
                "LDA", "LDB", "LDD", "LDX", "LDY", "LDS", "LDU",
                "STA", "STB", "STD", "STX", "STY", "STS", "STU",

                // Arithmétique
                "ADDA", "ADDB", "ADDD", "ADCA", "ADCB",
                "SUBA", "SUBB", "SUBD", "SBCA", "SBCB",
                "INCA", "INCB", "DECA", "DECB", "INC", "DEC",
                "NEGA", "NEGB", "NEG", "COMA", "COMB", "COM",

                // Logique
                "ANDA", "ANDB", "ORA", "ORB", "EORA", "EORB",
                "BITA", "BITB", "CLRA", "CLRB", "CLR",

                // Comparaison
                "CMPA", "CMPB", "CMPD", "CMPX", "CMPY", "CMPS", "CMPU",

                // Shifts/Rotations
                "ASLA", "ASLB", "ASL", "ASRA", "ASRB", "ASR",
                "LSLA", "LSLB", "LSL", "LSRA", "LSRB", "LSR",
                "ROLA", "ROLB", "ROL", "RORA", "RORB", "ROR",

                // Branches
                "BRA", "BRN", "BEQ", "BNE", "BCC", "BCS", "BPL", "BMI",
                "BVC", "BVS", "BGT", "BLE", "BGE", "BLT", "BHI", "BLS", "BSR",
                "LBRA", "LBRN", "LBEQ", "LBNE", "LBCC", "LBCS", "LBPL", "LBMI",
                "LBVC", "LBVS", "LBGT", "LBLE", "LBGE", "LBLT", "LBHI", "LBLS", "LBSR",

                // Sauts/Appels
                "JMP", "JSR", "RTS", "RTI",

                // Stack
                "PSHS", "PULS", "PSHU", "PULU",

                // Transferts
                "TFR", "EXG", "LEA", "LEAX", "LEAY", "LEAS", "LEAU",

                // Divers
                "MUL", "DAA", "NOP", "SWI", "SWI2", "SWI3", "CWAI", "SYNC", "SEX", "ABX"
        };

        // Si le premier mot est un mnémonique, ce n'est pas une étiquette
        for (String keyword : keywords) {
            if (firstWord.equals(keyword)) {
                return false;
            }
        }

        // Vérifier que c'est un nom valide (lettres, chiffres, underscore)
        return firstWord.matches("^[A-Z_][A-Z0-9_]*$");
    }

    /* Extrait le nom de l'étiquette d'une ligne */
    public static String extractLabel(String line) {
        if (line == null || !hasLabel(line)) return null;

        String cleaned = line.trim();

        // Format: "LABEL:"
        if (cleaned.contains(":")) {
            int colonIndex = cleaned.indexOf(':');
            return cleaned.substring(0, colonIndex).trim().toUpperCase();
        }

        // Format: "LABEL INSTRUCTION"
        String[] parts = cleaned.split("\\s+", 2);
        return parts[0].toUpperCase();
    }

    /* Retire l'étiquette d'une ligne et retourne l'instruction */
    public static String removeLabel(String line) {
        if (line == null || !hasLabel(line)) return line;

        String cleaned = line.trim();

        // Format: "LABEL:"
        if (cleaned.contains(":")) {
            int colonIndex = cleaned.indexOf(':');
            if (colonIndex < cleaned.length() - 1) {
                return cleaned.substring(colonIndex + 1).trim();
            }
            return "";
        }

        // Format: "LABEL INSTRUCTION"
        String[] parts = cleaned.split("\\s+", 2);
        if (parts.length > 1) {
            return parts[1].trim();
        }

        return "";
    }
}