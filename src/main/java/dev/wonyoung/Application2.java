package dev.wonyoung;

import dev.wonyoung.infrastructure.config.AppConfig2;

import javax.swing.SwingUtilities;

public class Application2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppConfig2().startApp());
    }
}