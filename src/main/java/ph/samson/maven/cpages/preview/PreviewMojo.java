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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Renders Markdown to HTML and opens in your browser.
 */
@Mojo(name = "preview", defaultPhase = LifecyclePhase.SITE)
public class PreviewMojo extends AbstractMojo {

    @Parameter(property = "srcDir",
            defaultValue = "${basedir}/src")
    protected File srcDir;

    @Parameter(property = "previewDir",
            defaultValue = "${project.build.directory}/preview")
    protected File previewDir;

    @Override
    public void execute() throws MojoExecutionException {
        if (!srcDir.isDirectory()) {
            throw new MojoExecutionException("No src dir " + srcDir);
        }

        PreviewLauncher launcher = new PreviewLauncher(previewDir.toPath());
        PreviewBuilder previewBuilder = new PreviewBuilder(
                srcDir.toPath(), previewDir.toPath(), launcher);

        try {
            previewBuilder.buildAll().stream()
                    .forEach(htmlFile -> launcher.launch(htmlFile));
        } catch (IOException ex) {
            throw new MojoExecutionException("Markdown conversion failed", ex);
        } catch (IllegalArgumentException ex) {
            throw new MojoExecutionException("Bad wiki dir", ex);
        }

        new PreviewWatcher(srcDir.toPath(), previewBuilder).start();

        try {
            System.in.read();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
