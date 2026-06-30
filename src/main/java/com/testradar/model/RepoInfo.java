package com.testradar.model;

import java.io.File;

public class RepoInfo {

    private String source = "";
    private File localPath;
    private boolean cloned;
    private int javaFileCount;
    private int testFileCount;
    private int sourceFileCount;

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public File getLocalPath() { return localPath; }
    public void setLocalPath(File localPath) { this.localPath = localPath; }

    public boolean isCloned() { return cloned; }
    public void setCloned(boolean cloned) { this.cloned = cloned; }

    public int getJavaFileCount() { return javaFileCount; }
    public void setJavaFileCount(int javaFileCount) { this.javaFileCount = javaFileCount; }

    public int getTestFileCount() { return testFileCount; }
    public void setTestFileCount(int testFileCount) { this.testFileCount = testFileCount; }

    public int getSourceFileCount() { return sourceFileCount; }
    public void setSourceFileCount(int sourceFileCount) { this.sourceFileCount = sourceFileCount; }
}
