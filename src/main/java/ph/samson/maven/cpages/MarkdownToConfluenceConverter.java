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
import java.util.List;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MarkdownToConfluenceConverter extends SimpleFileVisitor<Path> {

    private static final Logger log = LoggerFactory.getLogger(
            MarkdownToConfluenceConverter.class);

    private final PegDownProcessor pdp;
    private final List<ConfluencePage> pages;

    public MarkdownToConfluenceConverter() {
        this.pdp = new PegDownProcessor();
        this.pages = new ArrayList<>();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        String sourceName = file.getFileName().toString();
        int extIdx = sourceName.lastIndexOf("md");
        if (extIdx == -1) {
            return FileVisitResult.CONTINUE;
        }

        log.info("converting {}", file);

        pages.add(convert(file));
        return FileVisitResult.CONTINUE;
    }

    public List<ConfluencePage> getPages() {
        return pages;
    }

    private ConfluencePage convert(Path markdownFile) throws IOException {
        String markdownSource = new String(Files.readAllBytes(markdownFile),
                StandardCharsets.UTF_8);
        RootNode root = pdp.parseMarkdown(markdownSource.toCharArray());
        ConfluenceStorageSerializer css = new ConfluenceStorageSerializer(
                markdownFile.getParent().toFile());

        String contents = css.toHtml(root);
        List<File> attachments = css.getAttachments();
        String fileName = markdownFile.getFileName().toString();
        String title = fileName.substring(0, fileName.lastIndexOf('.'));
        return new ConfluencePage(title, contents, attachments);
    }
}
