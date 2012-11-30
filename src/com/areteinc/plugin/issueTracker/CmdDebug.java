package com.areteinc.plugin.issueTracker;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/29/12
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class CmdDebug extends AnAction{

    Project project;

    public void actionPerformed(AnActionEvent event) {
        project = IssueTrackerUtil.getProject(event);

        new GitCheckoutProcess(project, event).processTerminated(null);
    }

}

class GitProcessListener implements ProcessListener{

    String errorText = null;
    String gitCommand = null;
    ProcessListener gitListener = null;
    Project gitProject = null;
    AnActionEvent gitEvent = null;

    public GitProcessListener(Project project, AnActionEvent event){
        gitProject = project;
        gitEvent = event;
    }

    public void startNotified(ProcessEvent processEvent) {}

    public void processTerminated(ProcessEvent processEvent) {
        try {
            if(errorText == null){
                IssueTrackerUtil.execute(gitCommand, gitProject, gitListener);
            }else{
                throw new Exception(errorText);
            }
        } catch (Exception e) {
            IssueTrackerUtil.notify("Failed to debug branch - error occurred: '" + e.getMessage() + "'", MessageType.ERROR, gitProject);
        }
    }

    public void processWillTerminate(ProcessEvent processEvent, boolean b) {}

    public void onTextAvailable(ProcessEvent processEvent, Key key) {
        if(key == ProcessOutputTypes.STDERR){
            errorText = processEvent.getText();
        }
    }
}

class GitCheckoutProcess extends GitProcessListener{
    public GitCheckoutProcess(Project project, AnActionEvent event){
        super(project, event);
        gitCommand = "git checkout --quiet master";
        gitListener = new GitPullProcess(project, event);
    }
}

class GitPullProcess extends GitProcessListener{
    public GitPullProcess(Project project, AnActionEvent event){
        super(project, event);
        gitCommand = "git pull";
        gitListener = new GitMergeProcess(project, event);
    }
}

class GitMergeProcess extends GitProcessListener{
    public GitMergeProcess(Project project, AnActionEvent event){
        super(project, event);
        String localBranchName = IssueTrackerUtil.getCurrentBranch(gitEvent); // => 9-ninth_issue
        gitCommand = "git merge --no-commit --squash " + localBranchName;
    }
}