package com.areteinc.plugin.issueTracker;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/20/12
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConflictResolve extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        ChangeList changeList = changeListManager.getDefaultChangeList();
        for(Change change : changeList.getChanges()){
            System.out.println("change.getFileStatus" + change.getFileStatus());
            System.out.println("change.getVirtualFile.getPath" + change.getVirtualFile().getPath());

            Runtime runTime = Runtime.getRuntime();
            try {
                String gitMergeCmd = "git mergetool "+change.getVirtualFile().getPath()+" | echo";
                String projectPath = project.getBasePath();
                System.out.println("gitMergeCmd: " + gitMergeCmd);
                System.out.println("projectPath: " + projectPath);
                Process process = runTime.exec(gitMergeCmd, new String[]{}, new File(projectPath));


//                Process process = runTime.exec("'C:\\Program Files\\SourceGear\\Vault Client\\sgdm.exe' --merge --result=");
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        }
    }
}
