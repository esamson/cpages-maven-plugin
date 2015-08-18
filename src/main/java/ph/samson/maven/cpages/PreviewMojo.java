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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Renders Markdown to HTML and opens in your browser.
 */
@Mojo(name = "preview", defaultPhase = LifecyclePhase.PACKAGE)
public class PreviewMojo extends AbstractMojo {

    private static final Logger log
            = LoggerFactory.getLogger(PreviewMojo.class);

    @Parameter(property = "wikiDir",
            defaultValue = "${project.build.directory}/wiki")
    protected File wikiDir;

    private final List<Thread> viewers = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException {
        if (!wikiDir.isDirectory()) {
            log.info("skip non existing wikiDir {}", wikiDir);
            return;
        }
        try {
            Files.walkFileTree(wikiDir.toPath(), new Converter());
        } catch (IOException ex) {
            throw new MojoExecutionException("Markdown conversion failed", ex);
        }

        for (Thread preview : viewers) {
            try {
                preview.join();
            } catch (InterruptedException ignore) {
            }
        }
    }

    private class Converter extends SimpleFileVisitor<Path> {

        private final PegDownProcessor pdp = new PegDownProcessor();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            String sourceName = file.getFileName().toString();
            int extIdx = sourceName.lastIndexOf("md");
            if (extIdx == -1) {
                return FileVisitResult.CONTINUE;
            }

            log.info("converting {}", file);

            String html = pdp.markdownToHtml(
                    new String(Files.readAllBytes(file), UTF_8));
            Path htmlFile = file.resolveSibling(
                    sourceName.substring(0, extIdx) + "html");
            Files.write(htmlFile, html.getBytes(UTF_8));

            Thread viewer = new Thread(new ShowPreview(htmlFile));
            viewer.start();
            viewers.add(viewer);

            return FileVisitResult.CONTINUE;
        }

    }

    private static class ShowPreview implements Runnable {

        private final Path htmlFile;

        public ShowPreview(Path htmlFile) {
            this.htmlFile = htmlFile;
        }

        @Override
        public void run() {
            // TODO: make this cross-platform
            ProcessBuilder xdgOpen = new ProcessBuilder(
                    "xdg-open", htmlFile.toString());
            try {
                xdgOpen.start().waitFor();
            } catch (IOException | InterruptedException ex) {
                log.warn("Failed opening preview of {}", htmlFile);
            }
        }
    }
}
