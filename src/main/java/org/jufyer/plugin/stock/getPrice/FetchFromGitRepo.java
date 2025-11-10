package org.jufyer.plugin.stock.getPrice;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jufyer.plugin.stock.Main;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class FetchFromGitRepo {
    public static void update() {
        String repoDir = String.valueOf(Main.getInstance().getDataPath()) +  "\\data";

        if (!(Files.exists(Path.of(repoDir)))) {
            String repoUrl = "https://github.com/Jufyer/stocksPluginDatabase";
            String cloneDirectoryPath = repoDir;
            
            try {
                Main.getInstance().getLogger().info("Cloning repository from " + repoUrl + " to " + cloneDirectoryPath);
                Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(new File(cloneDirectoryPath))
                        .call();
                Main.getInstance().getLogger().info("Repository cloned successfully.");
            } catch (GitAPIException e) {
                e.printStackTrace();
            }
        }else {
            File repoDirGitFolder = new File(repoDir + "/.git");
            try (Git git = Git.open(repoDirGitFolder)) {
                Main.getInstance().getLogger().info("Fetching updates from the remote repository...");
                git.fetch()
                        .setRemote("origin")
                        .call();

                Main.getInstance().getLogger().info("Fetch complete. Resetting to latest commit...");

                git.reset()
                        .setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                        .setRef("origin/main")
                        .call();

                Main.getInstance().getLogger().info("Repository updated to latest remote state.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
