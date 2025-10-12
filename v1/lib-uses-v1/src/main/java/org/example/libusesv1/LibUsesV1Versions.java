package org.example.libusesv1;

import org.example.CoreVersion;
import org.example.tar.TarCompressor;
import org.example.xz.XzCompressor;

public class LibUsesV1Versions {
    public static void showVersions() {
        System.out.println("lib-uses-v1:");
        System.out.println("  CoreVersion = " + CoreVersion.VERSION);
        System.out.println("  TarCompressor = " + TarCompressor.VERSION);
        System.out.println("  XzCompressor = " + XzCompressor.VERSION);
    }
}
