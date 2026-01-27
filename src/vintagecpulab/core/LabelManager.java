package vintagecpulab.core;

import java.util.HashMap;
import java.util.Map;

/** LabelManager : Gestionnaire d'étiquettes pour l'assembleur 6809 */


public class LabelManager {

    private final Map<String, Integer> labels;

    public LabelManager() {
        this.labels = new HashMap<>();
    }

    public void addLabel(String name, int address) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nom d'étiquette invalide");
        }

        String normalizedName = name.trim().toUpperCase();

        if (labels.containsKey(normalizedName)) {
            System.err.println("Avertissement: Étiquette '" + name +
                    "' redéfinie (@" + address + ")");
        }

        labels.put(normalizedName, address & 0xFFFF);
        System.out.println("Étiquette enregistrée: " + normalizedName +
                " = $" + CPU.decimalToHex(address, 4));
    }

    public Integer getAddress(String name) {
        if (name == null) return null;
        return labels.get(name.trim().toUpperCase());
    }

    public boolean isLabelName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        return labels.containsKey(name.trim().toUpperCase());
    }

    public boolean hasLabel(String name) {
        return isLabelName(name);
    }

    public int getLabelCount() {
        return labels.size();
    }

    public void clear() {
        labels.clear();
        System.out.println("Table des symboles effacée");
    }

    public void print() {
        if (labels.isEmpty()) {
            System.out.println("   (vide)");
            return;
        }

        labels.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    System.out.println(String.format("   %-12s → $%s",
                            entry.getKey(),
                            CPU.decimalToHex(entry.getValue(), 4)));
                });
    }

    public Map<String, Integer> getAllLabels() {
        return new HashMap<>(labels);
    }
}
