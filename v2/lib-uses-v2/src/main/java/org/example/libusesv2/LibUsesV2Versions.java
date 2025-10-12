package org.example.libusesv2;

import org.example.CoreVersion;
import org.example.tar.TarCompressor;

public class LibUsesV2Versions {
    public static void showVersions() {
        System.out.println("lib-uses-v2:");
        System.out.println("  CoreVersion = " + CoreVersion.VERSION);
        System.out.println("  TarCompressor = " + TarCompressor.VERSION);
    }
}
