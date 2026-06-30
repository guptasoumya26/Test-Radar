package com.testradar.service;

import com.testradar.model.RepoInfo;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GitService {

    public RepoInfo acquire(String source, Consumer<String> progress) throws Exception {
        RepoInfo info = new RepoInfo();
        info.setSource(source);

        File local = new File(source);
        if (local.isDirectory()) {
            log(progress, "Using local folder: " + local.getAbsolutePath());
            info.setLocalPath(local);
            info.setCloned(false);
        } else {
            Path target = Files.createTempDirectory("testradar-repo-");
            log(progress, "Cloning " + source + " ...");
            try (Git git = Git.cloneRepository()
                    .setURI(source)
                    .setDirectory(target.toFile())
                    .setDepth(1)
                    .call()) {
                log(progress, "Clone complete.");
            }
            info.setLocalPath(target.toFile());
            info.setCloned(true);
        }

        countFiles(info, progress);
        return info;
    }

    private void countFiles(RepoInfo info, Consumer<String> progress) throws IOException {
        int[] java = {0};
        int[] tests = {0};
        int[] src = {0};
        try (Stream<Path> stream = Files.walk(info.getLocalPath().toPath())) {
            stream.filter(p -> p.toString().endsWith(".java"))
                  .filter(Files::isRegularFile)
                  .forEach(p -> {
                      java[0]++;
                      String s = p.toString().replace('\\', '/');
                      boolean isTest = s.contains("/src/test/")
                              || p.getFileName().toString().endsWith("Test.java")
                              || p.getFileName().toString().endsWith("Tests.java")
                              || p.getFileName().toString().endsWith("IT.java")
                              || p.getFileName().toString().startsWith("Test");
                      if (isTest) tests[0]++; else src[0]++;
                  });
        }
        info.setJavaFileCount(java[0]);
        info.setTestFileCount(tests[0]);
        info.setSourceFileCount(src[0]);
        log(progress, "Found " + java[0] + " Java files (" + tests[0] + " look like tests).");
    }

    public void cleanup(RepoInfo info) {
        if (info == null || !info.isCloned() || info.getLocalPath() == null) return;
        try (Stream<Path> stream = Files.walk(info.getLocalPath().toPath())) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException ignored) {
        }
    }

    private void log(Consumer<String> progress, String msg) {
        if (progress != null) progress.accept(msg);
    }
}
