package com.areteinc.plugin.issueTracker;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import git4idea.branch.GitBranchOperationsProcessor;
import git4idea.ui.branch.GitMultiRootBranchConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/19/12
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class IssueTrackerUtil {

    static NotificationGroup notificationGroup;

    static ToolWindow toolWindow = null;
    static ConsoleView consoleView = null;
    static String toolWindowName = "Git Console";

    public static void notify(String messageContent, MessageType messageType, Project project){
        if(notificationGroup == null){
            notificationGroup = NotificationGroup.toolWindowGroup("Issue Tracker Messages", ChangesViewContentManager.TOOLWINDOW_ID, true);
        }
        notificationGroup.createNotification(messageContent, messageType).notify(project);
    }

    public static Project getProject(AnActionEvent event){
        return event.getData(PlatformDataKeys.PROJECT);
    }

    public static List<GitRepository> getGitRepositories(AnActionEvent event){
        Project project = event.getData(PlatformDataKeys.PROJECT);
        if(project != null) {
            GitRepositoryManager gitRepositoryManager = GitUtil.getRepositoryManager(project);
            List<GitRepository> gitRepositories = gitRepositoryManager.getRepositories();
            return gitRepositories;
        }else{
            return new ArrayList<GitRepository>(); // return empty list to prevent blow up
        }

    }

    public static String getCurrentBranch(AnActionEvent event){
        GitMultiRootBranchConfig gitMultiRootBranchConfig = new GitMultiRootBranchConfig(getGitRepositories(event));
        String currentBranch = gitMultiRootBranchConfig.getCurrentBranch();
        return currentBranch;
    }

    public static GitBranchOperationsProcessor getGitBranchOperationsProcessor(AnActionEvent event) throws Exception {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        GitRepositoryManager gitRepositoryManager = GitUtil.getRepositoryManager(project);
        List<GitRepository> gitRepositories = gitRepositoryManager.getRepositories();

        if(gitRepositories.size() != 1){
            throw new Exception("More than one git repository detected [" + gitRepositories + "].");
        }

        GitRepository selectedGitRepository = gitRepositories.get(0);
        return new GitBranchOperationsProcessor(project, gitRepositories, selectedGitRepository);
    }

    public static Task getTaskByNumber(TaskManager taskManager, String taskNumber){
        for(Task task : taskManager.getLocalTasks()){
            if(taskNumber.equals(task.getNumber())){
                return task;
            }
        }
        return null;
    }

    public static void execute(String command, Project project, ProcessListener processListener) throws Exception {

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command, new String[0], new File(project.getBasePath()));
            ToolWindowManager manager = ToolWindowManager.getInstance(project);

            if (consoleView == null) {
                TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
                TextConsoleBuilder builder = factory.createBuilder(project);
                consoleView = builder.getConsole();
            }

            OSProcessHandler processHandler = new OSProcessHandler(process, command);

            if(processListener != null){
                processHandler.addProcessListener(processListener);
            }

            consoleView.attachToProcess(processHandler);
            processHandler.startNotify();
            toolWindow = manager.getToolWindow(toolWindowName);

            if (toolWindow == null) {
                toolWindow = manager.registerToolWindow(toolWindowName, consoleView.getComponent(), ToolWindowAnchor.BOTTOM);
                toolWindow.show(new Runnable() {
                    public void run() {}
                });
            }
            int exitValue = process.waitFor();
        } catch (Exception e) {
            throw e;
        }
    }

}

