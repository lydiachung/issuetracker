package com.areteinc.plugin.issueTracker;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.github.GitHubRepository;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lchung
 * Date: 11/27/12
 * Time: 5:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class BranchDebug extends AnAction {
    public void actionPerformed(AnActionEvent event) {



        Project project = IssueTrackerUtil.getProject(event);
        TaskManager taskManager = TaskManager.getManager(project);

        try{

            List<Task> taskList = taskManager.getIssues("");
            for(Task task: taskList){
                IssueTrackerUtil.notify("task: " + task.getId(), MessageType.INFO, project);
            }

            for(TaskRepository repository : taskManager.getAllRepositories()){
                IssueTrackerUtil.notify("getPresentableName: " + repository.getPresentableName(), MessageType.INFO, project);
                IssueTrackerUtil.notify("getUrl: " + repository.getUrl(), MessageType.INFO, project);
                IssueTrackerUtil.notify("getRepositoryType: " + repository.getRepositoryType(), MessageType.INFO, project);
                IssueTrackerUtil.notify("isConfigured: " + repository.isConfigured(), MessageType.INFO, project);
                IssueTrackerUtil.notify("testConnection: " + taskManager.testConnection(repository), MessageType.INFO, project);

                GitHubRepository gitHubRepository = (GitHubRepository) repository;

                StringBuilder httpUrl = new StringBuilder();
                httpUrl.append("https://api.github.com/repos/");
                httpUrl.append(gitHubRepository.getRepoAuthor()).append("/");
                httpUrl.append(gitHubRepository.getRepoName()).append("/issues");

                HttpClient httpClient = new HttpClient();
                httpClient.getParams().setConnectionManagerTimeout(3000);
                httpClient.getParams().setSoTimeout(3000);
                httpClient.getParams().setAuthenticationPreemptive(true);
                httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(gitHubRepository.getUsername(), gitHubRepository.getPassword()));
                httpClient.getParams().setContentCharset("UTF-8");
                HttpMethod httpMethod = new GetMethod(httpUrl.toString());
                httpClient.executeMethod(httpMethod);

                InputStream inputStream = httpMethod.getResponseBodyAsStream();

                IssueTrackerUtil.notify("httpUrl: " + httpUrl.toString(), MessageType.INFO, project);
                IssueTrackerUtil.notify("response: " + outputString(inputStream), MessageType.INFO, project);

            }

        }catch(Exception e){
            IssueTrackerUtil.notify("Failed to debug branch - error occurred: '" + e.getMessage() + "'", MessageType.ERROR, project);
        }
    }


    public String outputString(InputStream inputStream) throws IOException {
        InputStreamReader is = new InputStreamReader(inputStream);
        StringBuilder sb=new StringBuilder();
        BufferedReader br = new BufferedReader(is);
        String read = br.readLine();

        while(read != null) {
            sb.append(read);
            read =br.readLine();
        }

        return sb.toString();
    }
}
