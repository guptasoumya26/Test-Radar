package com.testradar.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.testradar.model.RepoInfo;
import com.testradar.model.SuiteAnalysis;
import com.testradar.model.TestCase;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class TestSuiteAnalyzer {

    private final JavaParser parser =
            new JavaParser(new ParserConfiguration()
                    .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17));

    public SuiteAnalysis analyze(RepoInfo repo, Consumer<String> progress) throws Exception {
        SuiteAnalysis result = new SuiteAnalysis();
        int[] parsed = {0};
        int[] failed = {0};

        try (Stream<Path> stream = Files.walk(repo.getLocalPath().toPath())) {
            List<Path> files = stream
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(Files::isRegularFile)
                    .toList();

            for (Path p : files) {
                String norm = p.toString().replace('\\', '/');
                boolean isTest = looksLikeTest(p, norm);
                try {
                    String content = Files.readString(p);
                    Optional<CompilationUnit> cuOpt = parser.parse(content).getResult();
                    if (cuOpt.isEmpty()) { failed[0]++; continue; }
                    CompilationUnit cu = cuOpt.get();
                    parsed[0]++;

                    if (isTest) {
                        result.appendTestSource(content);
                        extractTests(cu, p, result);
                    } else {
                        cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                                .filter(c -> !c.isInterface())
                                .forEach(c -> result.getSourceClasses().add(c.getNameAsString()));
                    }
                } catch (Exception e) {
                    failed[0]++;
                }
            }
        }

        result.setParsedFiles(parsed[0]);
        result.setParseFailures(failed[0]);
        if (progress != null) {
            progress.accept("Discovered " + result.getTestCases().size()
                    + " test methods across the suite.");
        }
        return result;
    }

    private boolean looksLikeTest(Path p, String norm) {
        String name = p.getFileName().toString();
        return norm.contains("/src/test/")
                || name.endsWith("Test.java")
                || name.endsWith("Tests.java")
                || name.endsWith("IT.java")
                || name.startsWith("Test");
    }

    private void extractTests(CompilationUnit cu, Path file, SuiteAnalysis result) {
        String pkg = cu.getPackageDeclaration().map(d -> d.getNameAsString()).orElse("");
        String framework = detectFramework(cu);

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
            if (cls.isInterface()) return;
            boolean classLevelTestNg = "TestNG".equals(framework)
                    && cls.getAnnotationByName("Test").isPresent();

            for (MethodDeclaration m : cls.getMethods()) {
                boolean methodIsTest = isTestMethod(m);
                if (!methodIsTest && !(classLevelTestNg && m.isPublic() && m.getType().isVoidType())) {
                    continue;
                }

                TestCase tc = new TestCase();
                tc.setPackageName(pkg);
                tc.setSimpleClassName(cls.getNameAsString());
                tc.setMethodName(m.getNameAsString());
                tc.setFramework(framework);
                tc.setFilePath(file.toString());
                tc.setLineNumber(m.getBegin().map(pos -> pos.line).orElse(0));
                tc.setDescription(extractDescription(m));
                extractGroups(m, tc);
                result.getTestCases().add(tc);
            }
        });
    }

    private boolean isTestMethod(MethodDeclaration m) {
        return m.getAnnotationByName("Test").isPresent()
                || m.getAnnotationByName("ParameterizedTest").isPresent()
                || m.getAnnotationByName("RepeatedTest").isPresent()
                || m.getAnnotationByName("TestFactory").isPresent()
                || m.getAnnotationByName("TestTemplate").isPresent();
    }

    private String detectFramework(CompilationUnit cu) {
        String imports = cu.getImports().toString();
        if (imports.contains("org.testng")) return "TestNG";
        if (imports.contains("org.junit.jupiter")) return "JUnit5";
        if (imports.contains("org.junit.")) return "JUnit4";
        return "JUnit5";
    }

    private String extractDescription(MethodDeclaration m) {
        Optional<AnnotationExpr> display = m.getAnnotationByName("DisplayName");
        if (display.isPresent() && display.get() instanceof SingleMemberAnnotationExpr s) {
            return stripQuotes(s.getMemberValue().toString());
        }
        Optional<AnnotationExpr> test = m.getAnnotationByName("Test");
        if (test.isPresent() && test.get() instanceof NormalAnnotationExpr n) {
            for (MemberValuePair pair : n.getPairs()) {
                if (pair.getNameAsString().equals("description")) {
                    return stripQuotes(pair.getValue().toString());
                }
            }
        }
        return m.getJavadoc()
                .map(jd -> jd.getDescription().toText().trim().split("\\R", 2)[0])
                .orElse("");
    }

    private void extractGroups(MethodDeclaration m, TestCase tc) {
        Optional<AnnotationExpr> test = m.getAnnotationByName("Test");
        if (test.isPresent() && test.get() instanceof NormalAnnotationExpr n) {
            for (MemberValuePair pair : n.getPairs()) {
                if (pair.getNameAsString().equals("groups")) {
                    addGroupValues(pair.getValue(), tc);
                }
            }
        }
        m.getAnnotations().stream()
                .filter(a -> a.getNameAsString().equals("Tag"))
                .forEach(a -> {
                    if (a instanceof SingleMemberAnnotationExpr s) {
                        tc.getGroups().add(stripQuotes(s.getMemberValue().toString()));
                    }
                });
    }

    private void addGroupValues(Expression value, TestCase tc) {
        if (value instanceof ArrayInitializerExpr arr) {
            arr.getValues().forEach(v -> tc.getGroups().add(stripQuotes(v.toString())));
        } else {
            tc.getGroups().add(stripQuotes(value.toString()));
        }
    }

    private String stripQuotes(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
