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

package ph.samson.maven.cpages.preview;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceFileReader;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;

/**
 * Build local previews
 */
class PreviewBuilder {

    private static final Logger log = LoggerFactory.getLogger(
            PreviewBuilder.class);
    private static final byte[] LIVE_RELOAD_SCRIPT = ("<script"
            + " src=\"http://localhost:35729/livereload.js?snipver=1\">"
            + "</script>")
            .getBytes(UTF_8);
    private static final FileFormatOption PNG
            = new FileFormatOption(FileFormat.PNG);

    private final PegDownProcessor pdp = new PegDownProcessor();
    private final Path sourceDir;
    private final Path outputDir;
    private final PreviewLauncher launcher;

    PreviewBuilder(Path sourceDir, Path outputDir,
            PreviewLauncher launcher) {
        this.sourceDir = sourceDir;
        this.outputDir = outputDir;
        this.launcher = launcher;
    }

    Path buildHtml(Path sourceMd) throws IOException {
        log.info("buildHtml {}", sourceMd);
        String sourceName = sourceMd.getFileName().toString();
        int extIdx = sourceName.lastIndexOf("md");
        String html = pdp.markdownToHtml(
                new String(Files.readAllBytes(sourceMd), UTF_8));
        Path htmlFile = targetDir(sourceMd).resolve(
                sourceName.substring(0, extIdx) + "html");
        Files.write(htmlFile, html.getBytes(UTF_8));
        Files.write(htmlFile, LIVE_RELOAD_SCRIPT, APPEND);
        log.info("built {}", htmlFile);
        return htmlFile;
    }

    List<Path> buildPngs(Path sourcePuml) throws IOException {
        log.info("buildPng {}", sourcePuml);
        SourceFileReader reader = new SourceFileReader(
                sourcePuml.toFile(), targetDir(sourcePuml).toFile(), PNG);
        List<Path> pngs = reader.getGeneratedImages().stream()
                .map(image -> image.getPngFile().toPath())
                .collect(Collectors.toList());
        log.info("built {}", pngs);
        return pngs;
    }

    void build(Path filename) throws IOException {
        if (filename.toString().endsWith(".md")) {
            launcher.launch(buildHtml(filename));
        } else if (filename.toString().endsWith(".puml")) {
            buildPngs(filename);
        }
    }

    List<Path> buildAll() throws IOException {
        List<Path> htmlFiles = new ArrayList<>();
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs) throws IOException {
                if (dir.equals(sourceDir)) {
                    // skip top directory
                    return FileVisitResult.CONTINUE;
                }

                log.info("preview: {}", dir);
                File[] contentFiles = dir.toFile()
                        .listFiles(file -> file.getName().endsWith(".md"));
                if (contentFiles.length < 1) {
                    log.error("{} has no page content", dir);
                    throw new IllegalArgumentException("No page content in "
                            + dir);
                } else if (contentFiles.length > 1) {
                    log.error("{} has more than one content file: {}",
                            dir, contentFiles);
                    throw new IllegalArgumentException(
                            "More than one content file in " + dir);
                }
                htmlFiles.add(buildHtml(contentFiles[0].toPath()));

                Arrays.stream(dir.toFile()
                        .listFiles(file -> file.getName().endsWith(".puml")))
                        .forEach(puml -> {
                            try {
                                buildPngs(puml.toPath());
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });

                return FileVisitResult.CONTINUE;
            }
        });
        return htmlFiles;
    }

    private Path targetDir(Path sourceFile) throws IOException {
        return Files.createDirectories(outputDir.resolve(
                sourceDir.relativize(sourceFile.getParent())
                .toString().replaceAll("\\s+", "")
        ));
    }
}
