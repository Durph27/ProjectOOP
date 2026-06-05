package com.humanitarian;

/**
 * Legacy launcher kept for existing run configurations.
 *
 * Main is now also safe to run from VS Code, IntelliJ IDEA, or java directly.
 *
 * This class delegates to Main so older launch settings continue to work.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
