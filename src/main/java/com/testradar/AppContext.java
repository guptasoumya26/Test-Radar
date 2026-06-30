package com.testradar;

import com.testradar.model.AppSettings;
import com.testradar.model.CoverageReport;
import com.testradar.model.RepoInfo;
import com.testradar.model.SuiteAnalysis;
import com.testradar.model.TestCase;
import com.testradar.service.CoverageService;
import com.testradar.service.GitService;
import com.testradar.service.JiraService;
import com.testradar.service.OpenAiService;
import com.testradar.service.SettingsService;
import com.testradar.service.TestNgExporter;
import com.testradar.service.TestSuiteAnalyzer;

import java.util.ArrayList;
import java.util.List;

public class AppContext {

    private final SettingsService settingsService = new SettingsService();
    private AppSettings settings;

    private final GitService gitService = new GitService();
    private final TestSuiteAnalyzer analyzer = new TestSuiteAnalyzer();
    private final CoverageService coverageService = new CoverageService();
    private final TestNgExporter exporter = new TestNgExporter();
    private OpenAiService openAiService;
    private JiraService jiraService;

    private RepoInfo repoInfo;
    private SuiteAnalysis suiteAnalysis;
    private CoverageReport coverageReport;

    private final List<Runnable> suiteLoadedListeners = new ArrayList<>();

    public AppContext() {
        this.settings = settingsService.load();
        rebuildClients();
    }

    public void rebuildClients() {
        this.openAiService = new OpenAiService(settings);
        this.jiraService = new JiraService(settings);
    }

    public void addSuiteLoadedListener(Runnable r) { suiteLoadedListeners.add(r); }

    public void fireSuiteLoaded() {
        for (Runnable r : suiteLoadedListeners) r.run();
    }

    public boolean hasSuite() {
        return suiteAnalysis != null && !suiteAnalysis.getTestCases().isEmpty();
    }

    public List<TestCase> getTestCases() {
        return suiteAnalysis == null ? List.of() : suiteAnalysis.getTestCases();
    }

    public SettingsService getSettingsService() { return settingsService; }
    public AppSettings getSettings() { return settings; }
    public void setSettings(AppSettings settings) { this.settings = settings; }

    public GitService getGitService() { return gitService; }
    public TestSuiteAnalyzer getAnalyzer() { return analyzer; }
    public CoverageService getCoverageService() { return coverageService; }
    public TestNgExporter getExporter() { return exporter; }
    public OpenAiService getOpenAiService() { return openAiService; }
    public JiraService getJiraService() { return jiraService; }

    public RepoInfo getRepoInfo() { return repoInfo; }
    public void setRepoInfo(RepoInfo repoInfo) { this.repoInfo = repoInfo; }

    public SuiteAnalysis getSuiteAnalysis() { return suiteAnalysis; }
    public void setSuiteAnalysis(SuiteAnalysis suiteAnalysis) { this.suiteAnalysis = suiteAnalysis; }

    public CoverageReport getCoverageReport() { return coverageReport; }
    public void setCoverageReport(CoverageReport coverageReport) { this.coverageReport = coverageReport; }
}
