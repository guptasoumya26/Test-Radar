package com.testradar.model;

import java.util.ArrayList;
import java.util.List;

public class TestCase {

    private String packageName = "";
    private String simpleClassName = "";
    private String methodName = "";
    private String framework = "";
    private String filePath = "";
    private int lineNumber = 0;
    private String description = "";
    private final List<String> groups = new ArrayList<>();

    private boolean selected = false;
    private double relevanceScore = 0.0;
    private String relevanceReason = "";

    public String getFullyQualifiedClassName() {
        if (packageName == null || packageName.isBlank()) {
            return simpleClassName;
        }
        return packageName + "." + simpleClassName;
    }

    public String getDisplayName() {
        return simpleClassName + "#" + methodName + "()";
    }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName == null ? "" : packageName; }

    public String getSimpleClassName() { return simpleClassName; }
    public void setSimpleClassName(String simpleClassName) { this.simpleClassName = simpleClassName; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? "" : description; }

    public List<String> getGroups() { return groups; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(double relevanceScore) { this.relevanceScore = relevanceScore; }

    public String getRelevanceReason() { return relevanceReason; }
    public void setRelevanceReason(String relevanceReason) { this.relevanceReason = relevanceReason == null ? "" : relevanceReason; }
}
