package com.areteinc.plugin.issueTracker;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import git4idea.actions.GitPull;

import java.util.Collection;

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
        String localBranchName = IssueTrackerUtil.getCurrentBranch(event); // => 9-ninth_issue
        String taskNumber = localBranchName.split("-")[0];
        String taskId = project.getName()+"-"+taskNumber; // myissue-9 (myissues-9), project.getName => myissues

        // ------------------------------------------------------------------------------------------------
        // activate task
        boolean clearContext = true;
        boolean createChangelist = false;

        TaskManager taskManager = TaskManager.getManager(project);
        Task task = taskManager.findTask(taskId);
        taskManager.activateTask(task, clearContext, createChangelist); // void activateTask(@NotNull Task task, boolean clearContext, boolean createChangelist);

        // ------------------------------------------------------------------------------------------------
        // check out master
        IssueTrackerUtil.getGitBranchOperationsProcessor(event).checkout(masterBranchName);

        // ------------------------------------------------------------------------------------------------
        // git pull
        super.actionPerformed(event);

        // ------------------------------------------------------------------------------------------------
        // create changelist (update changelist comment)
        String taskSummary = task.getSummary();
        String commitComment = "#"+taskNumber+" "+taskSummary+": ";

        // TODO: might not need to add changelistlistener every time
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        changeListManager.addChangeListListener(new LocalChangeListListener(changeListManager));
        LocalChangeList changeList = changeListManager.addChangeList(localBranchName, commitComment);
        changeListManager.setDefaultChangeList(changeList);

        // ------------------------------------------------------------------------------------------------
        // merge
        IssueTrackerUtil.getGitBranchOperationsProcessor(event).merge(localBranchName, true); // localBranch => true

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
