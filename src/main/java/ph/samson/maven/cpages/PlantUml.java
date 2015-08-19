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
import java.nio.file.Path;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.Option;
import net.sourceforge.plantuml.SourceFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlantUml {

    private static final Logger log = LoggerFactory.getLogger(PlantUml.class);

    static void generate(Path dir, File outputDir) throws IOException {
        File[] plantumlFiles = dir.toFile().listFiles((file)
                -> file.getName().endsWith(".puml"));

        Option option = new Option();
        option.setFileFormat(FileFormat.PNG);
        for (File plantumlFile : plantumlFiles) {
            SourceFileReader reader = new SourceFileReader(
                    plantumlFile, outputDir, option.getFileFormatOption());
            for (GeneratedImage image : reader.getGeneratedImages()) {
                log.info("generated: {}", image);
            }
        }
    }
}
