package com.areteinc.plugin.issueTracker.archive;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.tasks.actions.OpenTaskAction;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/16/12
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class TaskOpen extends OpenTaskAction{

    public TaskOpen(){
        super();
    }

    public void actionPerformed(AnActionEvent event) {
        super.actionPerformed(event);

//        TODO: create branch


        Project project = event.getData(PlatformDataKeys.PROJECT);
        Messages.showMessageDialog(project, "after task OpenTaskAction", "Information", Messages.getInformationIcon());
    }
}
