package com.testradar.model;

import java.util.ArrayList;
import java.util.List;

public class SuiteAnalysis {

    private final List<TestCase> testCases = new ArrayList<>();
    private final List<String> sourceClasses = new ArrayList<>();
    private final StringBuilder testSourceText = new StringBuilder();

    private int parsedFiles;
    private int parseFailures;

    public List<TestCase> getTestCases() { return testCases; }
    public List<String> getSourceClasses() { return sourceClasses; }
    public String getTestSourceText() { return testSourceText.toString(); }
    public void appendTestSource(String s) { testSourceText.append(s).append('\n'); }

    public int getParsedFiles() { return parsedFiles; }
    public void setParsedFiles(int parsedFiles) { this.parsedFiles = parsedFiles; }

    public int getParseFailures() { return parseFailures; }
    public void setParseFailures(int parseFailures) { this.parseFailures = parseFailures; }
}
