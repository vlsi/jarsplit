package org.example.svg;

import org.example.CoreVersion;
import org.example.tar.TarCompressor;
import org.example.xz.XzCompressor;

public class SvgFactory {
    public static void showVersions() {
        System.out.println("svg:");
        System.out.println("  CoreVersion = " + CoreVersion.VERSION);
        System.out.println("  TarCompressor = " + TarCompressor.VERSION);
        System.out.println("  XzCompressor = " + XzCompressor.VERSION);
    }
}
