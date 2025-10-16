package org.example.app;

import org.example.svg.SvgFactory;
import org.example.http.HttpFactory;

public class Main {
    public static void main(String[] args) {
        SvgFactory.showVersions();
        HttpFactory.showVersions();
    }
}
