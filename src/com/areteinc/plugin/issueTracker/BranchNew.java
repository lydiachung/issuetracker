package com.areteinc.plugin.issueTracker;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.actions.OpenTaskAction;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import java.util.List;

import git4idea.repo.GitRepositoryManager;
import git4idea.branch.GitBranchOperationsProcessor;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/19/12
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 * -------------------------------------------------------------------
 * task.getPresentableName() => myissues-9: ninth issue
 * task.getDescription() => some description
 * task.getId() => myissues-9
 * task.getNumber() => 9
 * task.getSummary() => ninth issue
 * -------------------------------------------------------------------
 */


public class BranchNew extends OpenTaskAction {

    public BranchNew(){
        super();
    }

    // NOTE: assume only one repo for each project!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // TODO: make sure the active task deson't have a branch created for it already
    // TODO: need more protection if the open task step fail
    // TODO: should return error if more than one repo or less than one repo
    // TODO: if the task name didn't change, assume user click cancel
    public void actionPerformed(AnActionEvent event) {
        super.actionPerformed(event); // makes selected task active

        Project project = event.getData(PlatformDataKeys.PROJECT);

        TaskManager taskManager = TaskManager.getManager(project);
        Task task = taskManager.getActiveTask();

//        List<GitRepository> gitRepositories = IssueTrackerUtil.getGitRepositories(event);
//        GitRepository selectedGitRepository = IssueTrackerUtil.getSelectedGitRepository();
        String branchName = task.getNumber()+"-"+task.getSummary().replace(' ', '_');

        IssueTrackerUtil.getGitBranchOperationsProcessor(event).checkoutNewBranch(branchName);
//        new GitBranchOperationsProcessor(project, gitRepositories, selectedGitRepository).checkoutNewBranch(branchName); // makes new branch active
//        new GitBranchOperationsProcessor(myProject, myRepositories, mySelectedRepository).checkoutNewBranchStartingFrom(name, myBranchName);
//        new GitBranchOperationsProcessor(myProject, myRepositories, mySelectedRepository).deleteBranch(myBranchName);
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
