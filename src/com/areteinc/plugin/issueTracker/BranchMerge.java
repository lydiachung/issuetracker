package com.areteinc.plugin.issueTracker;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager;
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
            // activate task
            boolean clearContext = true;
            boolean createChangelist = false;

            TaskManager taskManager = TaskManager.getManager(project);
            Task task = IssueTrackerUtil.getTaskByNumber(taskManager, taskNumber);

            if(task == null){
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Task ").append(taskNumber).append(" not found. ");
                stringBuilder.append("Available tasks are: [");
                for(Task localTask : taskManager.getLocalTasks()){
                    stringBuilder.append(localTask.getNumber()).append(",");
                }
                stringBuilder.append("]");
                throw new Exception(stringBuilder.toString());
            }

            taskManager.activateTask(task, clearContext, createChangelist); // void activateTask(@NotNull Task task, boolean clearContext, boolean createChangelist);

            IssueTrackerUtil.notify("Activated task " + task.getPresentableName() + ".", MessageType.INFO, project);

            // ------------------------------------------------------------------------------------------------
            // check out master
            IssueTrackerUtil.getGitBranchOperationsProcessor(event).checkout(masterBranchName);

            // ------------------------------------------------------------------------------------------------
            // git pull
            super.actionPerformed(event);

            IssueTrackerUtil.notify("Pulled master branch", MessageType.INFO, project);

            // ------------------------------------------------------------------------------------------------
            // create changelist (update changelist comment)
            String taskSummary = task.getSummary();
            String commitComment = "#"+taskNumber+" "+taskSummary+": ";

            // TODO: might not need to add changelistlistener every time
            ChangeListManager changeListManager = ChangeListManager.getInstance(project);
            changeListManager.addChangeListListener(new LocalChangeListListener(changeListManager));
            LocalChangeList changeList = changeListManager.addChangeList(localBranchName, commitComment);
            changeListManager.setDefaultChangeList(changeList);

            IssueTrackerUtil.notify("Created change list " + localBranchName + ".", MessageType.INFO, project);

            // ------------------------------------------------------------------------------------------------
            // merge
            IssueTrackerUtil.getGitBranchOperationsProcessor(event).merge(localBranchName, true); // localBranch => true

        }catch(Exception e){
            IssueTrackerUtil.notify("Failed to merge branch - error occurred: '" + e.getMessage() + "'", MessageType.ERROR, project);
        }
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
