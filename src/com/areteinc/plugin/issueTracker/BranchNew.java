package com.areteinc.plugin.issueTracker;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import git4idea.GitUtil;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import com.intellij.util.IconUtil;
import java.util.List;

import git4idea.repo.GitRepositoryManager;
import git4idea.ui.branch.GitMultiRootBranchConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import git4idea.branch.GitBranchOperationsProcessor;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/19/12
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class BranchNew extends DumbAwareAction {

    private Project myProject;
    private List<GitRepository> myRepositories;
//    @NotNull private GitRepository mySelectedRepository;

    public BranchNew(){
        super();


    }
//
//    public BranchNew(@NotNull Project project, @NotNull List<GitRepository> repositories, @NotNull GitRepository selectedRepository) {
//        super("New Branch", "Create and checkout new branch", IconUtil.getAddRowIcon());
//
//
//
//        myProject = project;
//        myRepositories = repositories;
//        mySelectedRepository = selectedRepository;
//        System.out.println("project: " + project);
//        System.out.println("repositories: " + repositories);
//        System.out.println("selectedRepository: " + selectedRepository);
//    }

//    @Override
    public void actionPerformed(AnActionEvent event) {

//        in the default constructor of branchnew
//        project.getName(): myissues
//        myRepositories: [D:\idea\myissues-plugin]
//        repo getPresentableUrl: D:\idea\myissues-plugin
//        repo getCurrentBranch: refs/heads/master


        System.out.println("in the default constructor of branchnew");
        Project project = event.getData(PlatformDataKeys.PROJECT);
        myProject = project;
//        System.out.println("project.getName(): " + project.getName()); // myissues

        GitRepositoryManager myRepositoryManager = GitUtil.getRepositoryManager(project);
//        myRepositoryManager.
        myRepositories = myRepositoryManager.getRepositories();
        GitMultiRootBranchConfig myMultiRootBranchConfig = new GitMultiRootBranchConfig(myRepositories);
//        GitRepository mySelectedRepository = myMultiRootBranchConfig.getCurrentBranch();
//        System.out.println("getCurrentBranch: "+myMultiRootBranchConfig.getCurrentBranch()); // getCurrentBranch: master
//        myMultiRootBranchConfig.

//        System.out.println("myRepositories: " + myRepositories); // [D:\idea\myissues-plugin]

//        GitBranchUtil.
//        GitUtil.
        for(GitRepository repo : myRepositories){
//            System.out.println("repo getPresentableUrl: " + repo.getPresentableUrl()); // D:\idea\myissues-plugin
//            System.out.println("repo getCurrentBranch: " + repo.getCurrentBranch()); // refs/heads/master
//            System.out.println("repo getCurrentBranch: " + repo.get()); // refs/heads/master
        }
        // NOTE: assume only one repo for each project!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        GitRepository mySelectedRepository = myRepositories.get(0);
        String name = "hi";
        new GitBranchOperationsProcessor(myProject, myRepositories, mySelectedRepository).checkoutNewBranch(name);
    }

//    @Override
    public void update(AnActionEvent e) {
        if (anyRepositoryIsFresh()) {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setDescription("Checkout of a new branch is not possible before the first commit.");
        }
    }

    private boolean anyRepositoryIsFresh() {
//        for (GitRepository repository : myRepositories) {
//            if (repository.isFresh()) {
//                return true;
//            }
//        }
        return false;
    }

}
