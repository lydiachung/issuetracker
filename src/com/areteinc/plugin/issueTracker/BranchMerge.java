package com.areteinc.plugin.issueTracker;

import com.intellij.history.*;
import com.intellij.history.Label;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager;
import com.intellij.openapi.vcs.update.ActionInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import git4idea.GitRevisionNumber;
import git4idea.GitUtil;
import git4idea.actions.GitMerge;
import git4idea.actions.GitPull;
import git4idea.commands.GitHandlerUtil;
import git4idea.commands.GitLineHandler;
import git4idea.i18n.GitBundle;
import git4idea.merge.GitMergeDialog;
import git4idea.merge.GitMergeUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/19/12
 * Time: 5:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class BranchMerge extends GitPull {

    public void actionPerformed(AnActionEvent event) {

        // ------------------------------------------------------------------------------------------------
        Project project = IssueTrackerUtil.getProject(event);

        try{

            // ------------------------------------------------------------------------------------------------
            String masterBranchName = "master";
            String localBranchName = IssueTrackerUtil.getCurrentBranch(event); // => 9-ninth_issue
            String taskNumber = localBranchName.split("-")[0]; // => 9

            if(localBranchName.equals(masterBranchName)){
                throw new Exception("Please checkout local branch for branch merge operation.");
            }

            // ------------------------------------------------------------------------------------------------
            // check out master
            IssueTrackerUtil.getGitBranchOperationsProcessor(event).checkout(masterBranchName);

            // ------------------------------------------------------------------------------------------------
            // git pull
            super.actionPerformed(event);

            IssueTrackerUtil.notify("Pulled master branch", MessageType.INFO, project);

            // ------------------------------------------------------------------------------------------------
            // create changelist (update changelist comment)
            String commitComment = "#" + localBranchName; // 3-task_name

            // TODO: might not need to add changelistlistener every time
            ChangeListManager changeListManager = ChangeListManager.getInstance(project);
            changeListManager.addChangeListListener(new LocalChangeListListener(changeListManager));
            LocalChangeList changeList = changeListManager.addChangeList(localBranchName, commitComment);
            changeListManager.setDefaultChangeList(changeList);

            IssueTrackerUtil.notify("Created change list " + localBranchName + ".", MessageType.INFO, project);

            // ------------------------------------------------------------------------------------------------
            // merge
            new GitMergeOperation(localBranchName).actionPerformed(event);

        }catch(Exception e){
            IssueTrackerUtil.notify("Failed to merge branch - error occurred: '" + e.getMessage() + "'", MessageType.ERROR, project);
        }
    }
}

class GitMergeOperation extends GitMerge {

    String mergeBranchName = null;

    public GitMergeOperation(String branchName){
        mergeBranchName = branchName;
    }

    protected void perform(final Project project, final List<VirtualFile> gitRoots, final VirtualFile defaultRoot, final Set<VirtualFile> affectedRoots, final List<VcsException> exceptions) throws VcsException {
        GitMergeDialog gitMergeDialog = new GitMergeDialog(project, gitRoots, defaultRoot);
        gitMergeDialog.close(GitMergeDialog.OK_EXIT_CODE);
        Label beforeLabel = LocalHistory.getInstance().putSystemLabel(project, "Before update");
        GitLineHandler gitLineHandler = gitMergeDialog.handler();
        gitLineHandler.addParameters("--no-commit");
        gitLineHandler.addParameters("--squash");
        gitLineHandler.addParameters(mergeBranchName);

        final VirtualFile root = project.getBaseDir();
        affectedRoots.add(root);
        GitRevisionNumber currentRev = GitRevisionNumber.resolve(project, root, "HEAD");

        try {
            GitHandlerUtil.doSynchronously(gitLineHandler, GitBundle.message("merging.title", root.getPath()), gitLineHandler.printableCommandLine());
        }finally {
            exceptions.addAll(gitLineHandler.errors());
            GitRepositoryManager manager = GitUtil.getRepositoryManager(project);
            if (manager != null) {
                manager.updateRepository(root, GitRepository.TrackedTopic.ALL_CURRENT);
            }
        }

        if (exceptions.size() != 0) {
            return;
        }

        GitMergeUtil.showUpdates(this, project, exceptions, root, currentRev, beforeLabel, getActionName(), ActionInfo.INTEGRATE);
    }
}

class LocalChangeListListener implements ChangeListListener {

    ChangeListManager changeListManager;

    public LocalChangeListListener(ChangeListManager changeListManager){
        this.changeListManager = changeListManager;
    }

    // TODO: make sure the first item is default change list
    public void changeListChanged(ChangeList list){
        if(list.getChanges().isEmpty() && list.getName() != "Default"){
            ChangeList changeList = changeListManager.getChangeLists().get(0);
            changeListManager.setDefaultChangeList((LocalChangeList) changeList);
            changeListManager.removeChangeList(list.getName());
        }
    }

    public void changeListAdded(ChangeList list){}
    public void changesRemoved(Collection<Change> changes, ChangeList fromList){}
    public void changesAdded(Collection<Change> changes, ChangeList toList){}
    public void changeListRemoved(ChangeList list){}
    public void changeListRenamed(ChangeList list, String oldName){}
    public void changeListCommentChanged(ChangeList list, String oldComment){}
    public void changesMoved(Collection<Change> changes, ChangeList fromList, ChangeList toList){}
    public void defaultListChanged(final ChangeList oldDefaultList, ChangeList newDefaultList){}
    public void unchangedFileStatusChanged(){}
    public void changeListUpdateDone(){}

}
