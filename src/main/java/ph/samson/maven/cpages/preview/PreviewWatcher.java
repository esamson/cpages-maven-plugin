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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watch source files for previews that need to be rebuilt.
 */
class PreviewWatcher extends Thread {

    private static final Logger log = LoggerFactory.getLogger(
            PreviewWatcher.class);

    private final Path srcDir;
    private final PreviewBuilder previewBuilder;

    PreviewWatcher(Path srcDir, PreviewBuilder previewBuilder) {
        this.srcDir = srcDir;
        this.previewBuilder = previewBuilder;
    }

    @Override
    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault()
                    .newWatchService();
            Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) throws
                        IOException {

                    dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });

            for (;;) {
                WatchKey key;
                Path dir;
                try {
                    key = watcher.take();
                    dir = (Path) key.watchable();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                log.debug("Changes in {}", dir);
                key.pollEvents().stream()
                        .filter(event -> {
                            WatchEvent.Kind<?> kind = event.kind();
                            return kind == ENTRY_CREATE || kind == ENTRY_MODIFY;
                        })
                        .map(event -> (Path) event.context())
                        .map(file -> dir.resolve(file))
                        .distinct()
                        .sorted()
                        .forEach(path -> {
                            try {
                                if (path.toFile().isDirectory()) {
                                    log.info("Watching {}", path);
                                    path.register(watcher, ENTRY_CREATE,
                                            ENTRY_MODIFY);
                                } else {
                                    previewBuilder.build(path);
                                }
                            } catch (IOException ex) {
                                log.warn("Error rebuilding {}", path, ex);
                            }
                        });

                if (!key.reset()) {
                    break;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
