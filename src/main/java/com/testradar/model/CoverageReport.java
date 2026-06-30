package com.testradar.model;

import java.util.ArrayList;
import java.util.List;

public class CoverageReport {

    public static class ClassCoverage {
        private final String className;
        private final boolean covered;
        private final int referencingTests;

        public ClassCoverage(String className, boolean covered, int referencingTests) {
            this.className = className;
            this.covered = covered;
            this.referencingTests = referencingTests;
        }

        public String getClassName() { return className; }
        public boolean isCovered() { return covered; }
        public int getReferencingTests() { return referencingTests; }
    }

    private int totalSourceClasses;
    private int coveredSourceClasses;
    private int totalTests;
    private int totalTestClasses;
    private final List<ClassCoverage> details = new ArrayList<>();

    public double getCoveragePercent() {
        if (totalSourceClasses == 0) return 0.0;
        return (coveredSourceClasses * 100.0) / totalSourceClasses;
    }

    public int getTotalSourceClasses() { return totalSourceClasses; }
    public void setTotalSourceClasses(int v) { this.totalSourceClasses = v; }

    public int getCoveredSourceClasses() { return coveredSourceClasses; }
    public void setCoveredSourceClasses(int v) { this.coveredSourceClasses = v; }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int v) { this.totalTests = v; }

    public int getTotalTestClasses() { return totalTestClasses; }
    public void setTotalTestClasses(int v) { this.totalTestClasses = v; }

    public List<ClassCoverage> getDetails() { return details; }
}
