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

    @Parameter(property = "srcDir",
            defaultValue = "${basedir}/src")
    protected File srcDir;

    @Parameter(property = "previewDir",
            defaultValue = "${project.build.directory}/preview")
    protected File previewDir;

    private final List<Thread> viewers = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException {
        if (!srcDir.isDirectory()) {
            throw new MojoExecutionException("No src dir " + srcDir);
        }

        try {
            Files.walkFileTree(srcDir.toPath(), new PreviewBuilder());
        } catch (IOException ex) {
            throw new MojoExecutionException("Markdown conversion failed", ex);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Bad wiki dir", ex);
        }

        for (Thread preview : viewers) {
            try {
                preview.join();
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

        File outputDir = previewDir.toPath()
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

    private class PreviewBuilder extends SimpleFileVisitor<Path> {

        private final PegDownProcessor pdp = new PegDownProcessor();

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) throws IOException {
            if (dir.toFile().equals(srcDir)) {
                // skip top directory
                return FileVisitResult.CONTINUE;
            }

            log.info("preview: {}", dir);
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

            return preview(contentFile.toPath(), outputDir);
        }

        private FileVisitResult preview(Path file, File outputDir)
                throws IOException {
            String sourceName = file.getFileName().toString();
            int extIdx = sourceName.lastIndexOf("md");

            log.info("converting {}", file);

            String html = pdp.markdownToHtml(
                    new String(Files.readAllBytes(file), UTF_8));
            Path htmlFile = new File(outputDir,
                    sourceName.substring(0, extIdx) + "html").toPath();
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
