package org.example.http;

import org.example.CoreVersion;
import org.example.tar.TarCompressor;

public class HttpFactory {
    public static void showVersions() {
        System.out.println("http:");
        System.out.println("  CoreVersion = " + CoreVersion.VERSION);
        System.out.println("  TarCompressor = " + TarCompressor.VERSION);
        System.out.println("Using a new method from tar v1.1");
        TarCompressor.methodAddedIn11();
    }
}
