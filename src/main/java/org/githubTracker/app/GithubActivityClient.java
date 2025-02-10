package org.githubTracker.app;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.System.out;

public class GithubActivityClient {
    private final String USER_GITHUB_URL;

    public GithubActivityClient(String username) {
        this.USER_GITHUB_URL = String.format("https://api.github.com/users/%s/events", username);
    }

    public void getUserActivity() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(USER_GITHUB_URL))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
            displayActivity(jsonArray);

        } catch (MalformedURLException e) {
            throw new RuntimeException("malformed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted");
        }
    }

    private void displayActivity(JsonArray events) {
        int totalCommitAmount = 0;
        int totalNewBranches = 0;
        int totalNewRepos = 0;
        int totalCreatedForks = 0;
        int totalDeletions = 0;
        int totalWatchEvents = 0;
        int totalLogs = events.size();

        final String REPO = "repository";
        final String BRANCH = "branch";

        // launch debugger on the line below to see how nicely JsonElement packages the array of events!
        for (JsonElement element : events) {
            JsonObject event = element.getAsJsonObject();
            String type = event.get("type").getAsString();
            String action;
            switch (type) {
                case "PushEvent":
                    int commitCount = event.get("payload").getAsJsonObject().get("commits").getAsJsonArray().size();
                    totalCommitAmount += commitCount;
                    action = "Pushed " + commitCount + " commit(s) to " + event.get("repo").getAsJsonObject().get("name");
                    break;
                case "IssuesEvent":
                    action = event.get("payload").getAsJsonObject().get("action").getAsString().toUpperCase().charAt(0)
                            + event.get("payload").getAsJsonObject().get("action").getAsString()
                            + " an issue in ${event.repo.name}";
                    totalWatchEvents++;
                    break;
                case "WatchEvent":
                    action = "Starred " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    break;
                case "ForkEvent":
                    action = "Forked " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    totalCreatedForks++;
                    break;
                case "CreateEvent":
                    String branchOrRepo = event.get("payload").getAsJsonObject().get("ref_type").getAsString();
                    action = "Created " + branchOrRepo + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    if (BRANCH.equals(branchOrRepo)) {
                        totalNewBranches++;
                    } else if (REPO.equals(branchOrRepo)) {
                        totalNewRepos++;
                    }
                    break;
                default:
                    action = event.get("type").getAsString().replace("Event", "")
                            + " in " + event.get("repo").getAsJsonObject().get("name").getAsString();
                    totalDeletions++;
                    break;
            }
            out.println(action);
            totalLogs++;
        }
        out.println("Total commit amount: " + totalCommitAmount);
        out.println("Total creation of branches: " + totalNewBranches);
        out.println("Total creation of repos: " + totalNewRepos);
        out.println("Total deletion amount: " + totalDeletions);
        out.println("Total fork amount: " + totalCreatedForks);
        out.println("Total new watched repos amount: " + totalWatchEvents);
        out.println("Total activity log amount: " + totalLogs);
    }
}
