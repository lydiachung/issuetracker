package com.areteinc.plugin.issueTracker;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import git4idea.GitUtil;
import git4idea.actions.GitPull;
import git4idea.repo.GitRepositoryManager;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/19/12
 * Time: 5:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class BranchMerge extends GitPull {
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);

        String masterBranchName = "master";
        String currentBranchName = IssueTrackerUtil.getCurrentBranch(event); // => 9-ninth_issue
        String taskNumber = currentBranchName.split("-")[0];
        String taskId = project.getName()+"-"+taskNumber; // myissue-9 (myissues-9), project.getName => myissues

        // activate task
        boolean clearContext = true;
        boolean createChangelist = false;

        TaskManager taskManager = TaskManager.getManager(project);
        Task task = taskManager.findTask(taskId);
        taskManager.activateTask(task, clearContext, createChangelist); // void activateTask(@NotNull Task task, boolean clearContext, boolean createChangelist);

        // check out master
        IssueTrackerUtil.getGitBranchOperationsProcessor(event).checkout(masterBranchName);

        // git pull
        super.actionPerformed(event);

        // merge
        IssueTrackerUtil.getGitBranchOperationsProcessor(event).merge(currentBranchName, true); // localBranch => true

        // update changelist comment
        String taskSummary = task.getSummary();

        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        LocalChangeList changeList = changeListManager.getDefaultChangeList();
        changeList.setComment("#"+taskNumber+" "+taskSummary+": "); // => #9 ninth issue:
    }
}
