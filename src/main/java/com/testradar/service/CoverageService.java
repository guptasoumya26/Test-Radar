package com.testradar.service;

import com.testradar.model.CoverageReport;
import com.testradar.model.SuiteAnalysis;
import com.testradar.model.TestCase;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class CoverageService {

    public CoverageReport compute(SuiteAnalysis analysis) {
        CoverageReport report = new CoverageReport();

        Set<String> sourceClasses = new LinkedHashSet<>(analysis.getSourceClasses());
        Set<String> testClasses = new LinkedHashSet<>();
        for (TestCase tc : analysis.getTestCases()) {
            testClasses.add(tc.getSimpleClassName());
        }

        String testText = analysis.getTestSourceText();

        int covered = 0;
        for (String src : sourceClasses) {
            int referencing = countReferencingTests(src, analysis);
            boolean isCovered = referencing > 0 || mentionedInText(src, testText);
            if (isCovered) covered++;
            report.getDetails().add(new CoverageReport.ClassCoverage(src, isCovered, referencing));
        }

        report.setTotalSourceClasses(sourceClasses.size());
        report.setCoveredSourceClasses(covered);
        report.setTotalTests(analysis.getTestCases().size());
        report.setTotalTestClasses(testClasses.size());
        return report;
    }

    private int countReferencingTests(String sourceClass, SuiteAnalysis analysis) {
        int count = 0;
        for (TestCase tc : analysis.getTestCases()) {
            if (normalize(tc.getSimpleClassName()).equalsIgnoreCase(sourceClass)) {
                count++;
            }
        }
        return count;
    }

    private String normalize(String testClassName) {
        String n = testClassName;
        for (String suffix : new String[]{"Tests", "Test", "IT", "ITCase", "TestCase"}) {
            if (n.endsWith(suffix) && n.length() > suffix.length()) {
                return n.substring(0, n.length() - suffix.length());
            }
        }
        if (n.startsWith("Test") && n.length() > 4) {
            return n.substring(4);
        }
        return n;
    }

    private boolean mentionedInText(String className, String testText) {
        if (className == null || className.isBlank() || testText.isEmpty()) return false;
        return Pattern.compile("\\b" + Pattern.quote(className) + "\\b").matcher(testText).find();
    }
}
