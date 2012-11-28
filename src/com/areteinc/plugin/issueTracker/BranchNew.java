package com.areteinc.plugin.issueTracker;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.actions.OpenTaskAction;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import java.util.List;

import git4idea.repo.GitRepositoryManager;
import git4idea.branch.GitBranchOperationsProcessor;

/**
 * -------------------------------------------------------------------
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/19/12
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 * -------------------------------------------------------------------
 * [remote task]
 * task.getPresentableName() => myissues-9: ninth issue
 * task.getDescription() => some description
 * task.getId() => myissues-9
 * task.getNumber() => 9
 * task.getSummary() => ninth issue
 * task.isIssue() => true
 * task.getType() => BUG
 * task.getIssueUrl() => https://github.com/lydiachung/myissues/issues/issue/3
 * -------------------------------------------------------------------
 * [local task]
 * task.getId() => LOCAL-00020
 * task.getNumber() => 00020
 * task.isIssue() => false
 * task.getType() => OTHER
 * task.getIssueUrl() => null
 * -------------------------------------------------------------------
 */


public class BranchNew extends OpenTaskAction {

    public BranchNew(){
        super();
    }

    public void actionPerformed(AnActionEvent event) {

        Project project = IssueTrackerUtil.getProject(event);
        TaskManager taskManager = TaskManager.getManager(project);

        try{
            Task prevTask = taskManager.getActiveTask();

            // ------------------------------------------------------------------------------------------------
            // create new task
            // activate new task
            super.actionPerformed(event);

            Task task = taskManager.getActiveTask();

            // ------------------------------------------------------------------------------------------------
            // user cancelled task dialog
            if(prevTask.getId() == task.getId()){
                IssueTrackerUtil.notify("Cancelled branch new action.", MessageType.INFO, project);
                return;
            }

            // ------------------------------------------------------------------------------------------------
            // no remote task selected
            if(task.getIssueUrl() == null){
                taskManager.removeTask((LocalTask) task);
                throw new Exception("Please select a valid issue. ");
            }

            String branchName = task.getNumber()+"-"+task.getSummary().replace(' ', '_');

            IssueTrackerUtil.getGitBranchOperationsProcessor(event).checkoutNewBranch(branchName);

        }catch(Exception e){
            IssueTrackerUtil.notify("Failed to create branch - error occurred: " + e.getMessage(), MessageType.ERROR, project);
        }
    }

    public void update(AnActionEvent event) {
        if (anyRepositoryIsFresh(event)) {
            event.getPresentation().setEnabled(false);
            event.getPresentation().setDescription("Checkout of a new branch is not possible before the first commit.");
        }
    }

    private boolean anyRepositoryIsFresh(AnActionEvent event) {
        for (GitRepository repository : IssueTrackerUtil.getGitRepositories(event)) {
            if (repository.isFresh()) {
                return true;
            }
        }
        return false;
    }

}
