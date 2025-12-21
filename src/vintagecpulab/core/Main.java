package vintagecpulab.core;

import javax.swing.SwingUtilities;

class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new DASHBOARD();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Erreur lors du lancement du Dashboard : " + e.getMessage());
            }
        });
    }
}