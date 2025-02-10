package org.githubTracker;

import org.githubTracker.app.GithubActivityClient;

public class Main {
    public static void main(String[] args) {
        new GithubActivityClient("ThisRoger").getUserActivity();
    }
}