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

import com.github.alexvictoor.livereload.WebSocketServer;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.glassfish.grizzly.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.glassfish.grizzly.http.server.HttpServer.createSimpleServer;

/**
 * Serves local previews.
 */
class PreviewLauncher {

    private static final Logger log = LoggerFactory.getLogger(
            PreviewLauncher.class);
    private static final int PORT = 27249;

    private final Path outputDir;
    private final Set<Path> launchedFiles = new HashSet<>();
    private boolean liveReloadStarted;
    private boolean previewServerStarted;

    PreviewLauncher(Path outputDir) {
        this.outputDir = outputDir;
    }

    synchronized void launch(Path htmlFile) {
        if (launchedFiles.contains(htmlFile)) {
            return;
        }

        liveReloadServer();
        previewServer();
        String url = "http://localhost:" + PORT + "/"
                + outputDir.relativize(htmlFile).toString();
        log.info("preview at {}", url);
        ProcessBuilder xdgOpen = new ProcessBuilder("xdg-open", url);
        // TODO: make opening a browser cross-platform
        try {
            xdgOpen.start().waitFor();
            launchedFiles.add(htmlFile);
        } catch (IOException | InterruptedException ex) {
            log.warn("Failed opening preview of {}", htmlFile);
        }
    }

    private void liveReloadServer() {
        if (!liveReloadStarted) {
            new Thread(() -> {
                try {
                    InternalLoggerFactory.setDefaultFactory(
                            new Slf4JLoggerFactory());
                    new WebSocketServer(outputDir.toString()).start();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();
            liveReloadStarted = true;
        }
    }

    private void previewServer() {
        if (!previewServerStarted) {
            try {
                HttpServer server = createSimpleServer(
                        outputDir.toString(), PORT);
                server.getListeners().stream()
                        .forEach(l -> l.getFileCache().setEnabled(false));
                server.start();
                previewServerStarted = true;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
