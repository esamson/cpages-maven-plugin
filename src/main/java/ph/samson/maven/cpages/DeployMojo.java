/*
 * Copyright 2015 Edward Samson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ph.samson.maven.cpages;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.samson.maven.cpages.rest.Confluence;
import ph.samson.maven.cpages.rest.model.Attachment;
import ph.samson.maven.cpages.rest.model.AttachmentsResult;
import ph.samson.maven.cpages.rest.model.Page;

/**
 * Deploy pages and attachments to Confluence
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DeployMojo extends AbstractMojo {

    private static final Logger log = LoggerFactory.getLogger(DeployMojo.class);

    /**
     * The SCM URL to link to in generated pages' footer.
     *
     * We could use ${project.scm.url} but it's buggy for inherited URL values.
     * See https://jira.codehaus.org/browse/MNG-3244 and linked issues.
     */
    @Parameter(name = "scmUrl",
            property = "confluence.scmUrl")
    private String scmUrl;

    @Parameter(property = "srcDir",
            defaultValue = "${basedir}/src")
    protected File srcDir;

    @Parameter(property = "genDir",
            defaultValue = "${project.build.directory}/generated")
    protected File genDir;

    @Parameter(name = "serverId",
            property = "confluence.serverId",
            required = true)
    private String serverId;

    @Parameter(name = "endpoint",
            property = "confluence.endpoint",
            required = true)
    private String endpoint;

    @Parameter(name = "spaceKey",
            property = "confluence.spaceKey",
            required = true)
    private String spaceKey;

    @Parameter(name = "parentTitle",
            property = "confluence.parentTitle")
    private String parentTitle;

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    @Component
    private SettingsDecrypter decrypter;

    private final List<Thread> viewers = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!srcDir.isDirectory()) {
            throw new MojoExecutionException("No src dir " + srcDir);
        }

        Server server = getServerSettings(serverId);
        Confluence confluence = new Confluence(endpoint,
                server.getUsername(),
                server.getPassword());
        final Page parentPage;
        if (parentTitle != null) {
            parentPage = confluence.getPage(spaceKey, parentTitle);
            if (parentPage == null) {
                throw new MojoExecutionException("Parent page '" + parentTitle
                        + "' does not exist under space " + spaceKey);
            }
        } else {
            parentPage = null;
        }

        try {
            Files.walkFileTree(srcDir.toPath(),
                    new Deployer(confluence, parentPage));
        } catch (IOException ex) {
            throw new MojoExecutionException("Markdown conversion failed", ex);
        }

        for (Thread viewer : viewers) {
            try {
                viewer.join();
            } catch (InterruptedException ignore) {
            }
        }
    }

    /**
     * Get the equivalent output directory for the given source directory.
     *
     * The output directory is created if necessary.
     *
     * @param sourceDir source directory
     * @return output directory
     * @throws IOException if the output directory does not exist and cannot be
     * created.
     */
    private File outputDir(Path sourceDir) throws IOException {
        File outputDir = genDir.toPath()
                .resolve(srcDir.toPath().relativize(sourceDir))
                .toFile();

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                if (!outputDir.isDirectory()) {
                    throw new IOException("Cannot create " + outputDir);
                }
            }
        }

        return outputDir;
    }

    private static final String CPAGES_URL
            = "https://github.com/esamson/cpages-maven-plugin";

    private String footer() {
        StringBuilder footer = new StringBuilder();

        footer.append("<br /><br /><br /><br />")
                .append("<hr />")
                .append("<p style=\"text-align: center;\">")
                .append("<small>")
                .append("This page is automatically generated using the ")
                .append("<a href=\"").append(CPAGES_URL).append("\">")
                .append("cpages-maven-plugin")
                .append("</a>")
                .append(". Any edits made here will be lost.")
                .append("</small>")
                .append("</p>");

        if (scmUrl != null) {
            footer.append("<p style=\"text-align: center;\">")
                    .append("<small>")
                    .append("Provide your edits at the source: ")
                    .append("<a href=\"").append(scmUrl).append("\">")
                    .append(scmUrl)
                    .append("</a>")
                    .append("</small>")
                    .append("</p>");
        }

        return footer.toString();
    }

    private void updateAttachments(Confluence confluence, String pageId,
            List<File> attachments) throws IOException {
        Map<String, File> filenameMap = new HashMap<>();
        for (File attachment : attachments) {
            filenameMap.put(attachment.getName(), attachment);
        }

        AttachmentsResult existing = confluence.getAttachments(pageId);
        for (Attachment attachment : existing.getResults()) {
            File update = filenameMap.get(attachment.getTitle());
            if (update != null) {
                confluence.updateAttachment(attachment, update);
                filenameMap.remove(attachment.getTitle());
            }
        }

        if (!filenameMap.isEmpty()) {
            confluence.createAttachments(pageId, filenameMap.values().toArray(
                    new File[filenameMap.size()]));
        }

        // TODO: delete old attachments
    }

    private Server getServerSettings(String id) throws MojoExecutionException {
        SettingsDecryptionRequest sdr = new DefaultSettingsDecryptionRequest(
                settings.getServer(id));
        SettingsDecryptionResult decrypt = decrypter.decrypt(sdr);
        for (SettingsProblem problem : decrypt.getProblems()) {
            switch (problem.getSeverity()) {
                case WARNING:
                    log.warn("{} ({})", problem.getMessage(),
                            problem.getLocation());
                    break;
                case ERROR:
                    log.error("{} ({})", problem.getMessage(),
                            problem.getLocation());
                    break;
                case FATAL:
                    log.error("{} ({})", problem.getMessage(),
                            problem.getLocation());
                    throw new MojoExecutionException(problem.getMessage());
            }
        }
        return decrypt.getServer();
    }

    private class Deployer extends SimpleFileVisitor<Path> {

        private final PegDownProcessor pdp = new PegDownProcessor();
        private final Confluence confluence;
        private final HashMap<Path, Page> pathMap = new HashMap<>();

        private Deployer(Confluence confluence, Page topParent) {
            this.confluence = confluence;
            pathMap.put(srcDir.toPath(), topParent);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) throws IOException {
            if (dir.toFile().equals(srcDir)) {
                // skip top directory
                return FileVisitResult.CONTINUE;
            }

            log.info("deploy: {}", dir);
            File[] contentFiles = dir.toFile().listFiles((file)
                    -> file.getName().endsWith(".md"));
            if (contentFiles.length < 1) {
                log.error("{} has no page content", dir);
                throw new IllegalArgumentException("No page content in " + dir);
            } else if (contentFiles.length > 1) {
                log.error("{} has more than one content file: {}",
                        dir, contentFiles);
                throw new IllegalArgumentException(
                        "More than one content file in " + dir);
            }
            File contentFile = contentFiles[0];
            File outputDir = outputDir(contentFile.toPath().getParent());
            PlantUml.generate(dir, outputDir);

            ConfluencePage cPage = convert(contentFile.toPath());
            Page deployedPage = deploy(cPage, pathMap.get(dir.getParent()));
            pathMap.put(dir, deployedPage);

            return FileVisitResult.CONTINUE;
        }

        private ConfluencePage convert(Path markdownFile) throws IOException {
            String markdownSource = new String(Files.readAllBytes(markdownFile),
                    StandardCharsets.UTF_8);
            RootNode root = pdp.parseMarkdown(markdownSource.toCharArray());
            ConfluenceStorageSerializer css = new ConfluenceStorageSerializer(
                    outputDir(markdownFile.getParent()));

            String contents = css.toHtml(root);
            List<File> attachments = css.getAttachments();
            String title = markdownFile.getParent().getFileName().toString();
            return new ConfluencePage(title, contents, attachments);
        }

        private Page deploy(ConfluencePage page, Page parentPage) throws
                IOException {
            List<File> attachments = page.getAttachments();
            Page cPage = confluence.getPage(spaceKey, page.getTitle());
            String contents = page.getContents() + footer();

            if (cPage == null) {
                log.info("Creating {}", page.getTitle());
                if (parentPage == null) {
                    cPage = confluence.createPage(spaceKey, page.getTitle(),
                            contents);
                } else {
                    cPage = confluence.createChildPage(spaceKey,
                            parentPage.getId(),
                            page.getTitle(),
                            contents);
                }

                if (!attachments.isEmpty()) {
                    confluence.createAttachments(cPage.getId(),
                            attachments.toArray(new File[attachments.size()]));
                }
            } else {
                log.info("Updating {}", page.getTitle());
                confluence.updatePage(cPage, contents);

                if (!attachments.isEmpty()) {
                    updateAttachments(confluence, cPage.getId(), attachments);
                }
            }

            Thread viewer = new Thread(new ShowDeployed(cPage));
            viewer.start();
            viewers.add(viewer);
            return cPage;
        }
    }

    private static class ShowDeployed implements Runnable {

        private final String pageUrl;

        public ShowDeployed(Page page) {
            pageUrl = page.get_links().getBase() + page.get_links().getWebui();
        }

        @Override
        public void run() {
            // TODO: make this cross-platform
            ProcessBuilder xdgOpen = new ProcessBuilder("xdg-open", pageUrl);
            try {
                xdgOpen.start().waitFor();
            } catch (IOException | InterruptedException ex) {
                log.warn("Failed opening {}", pageUrl);
            }
        }
    }
}
