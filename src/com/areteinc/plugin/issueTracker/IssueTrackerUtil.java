package com.areteinc.plugin.issueTracker;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import git4idea.branch.GitBranchOperationsProcessor;
import git4idea.ui.branch.GitMultiRootBranchConfig;

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

    public static GitBranchOperationsProcessor getGitBranchOperationsProcessor(AnActionEvent event){
        Project project = event.getData(PlatformDataKeys.PROJECT);
        GitRepositoryManager gitRepositoryManager = GitUtil.getRepositoryManager(project);
        List<GitRepository> gitRepositories = gitRepositoryManager.getRepositories();
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

}
