package com.testradar.service;

import com.testradar.model.TestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestNgExporter {

    public String buildXml(List<TestCase> selected, String suiteName) {
        Map<String, java.util.List<String>> byClass = new LinkedHashMap<>();
        for (TestCase tc : selected) {
            byClass.computeIfAbsent(tc.getFullyQualifiedClassName(), k -> new java.util.ArrayList<>())
                   .add(tc.getMethodName());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">\n");
        sb.append("<suite name=\"").append(escape(suiteName)).append("\" verbose=\"1\">\n");
        sb.append("  <test name=\"Relevant Tests\">\n");
        sb.append("    <classes>\n");
        for (Map.Entry<String, java.util.List<String>> e : byClass.entrySet()) {
            sb.append("      <class name=\"").append(escape(e.getKey())).append("\">\n");
            sb.append("        <methods>\n");
            for (String method : e.getValue()) {
                sb.append("          <include name=\"").append(escape(method)).append("\"/>\n");
            }
            sb.append("        </methods>\n");
            sb.append("      </class>\n");
        }
        sb.append("    </classes>\n");
        sb.append("  </test>\n");
        sb.append("</suite>\n");
        return sb.toString();
    }

    public void writeTo(Path file, List<TestCase> selected, String suiteName) throws IOException {
        Files.writeString(file, buildXml(selected, suiteName));
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
