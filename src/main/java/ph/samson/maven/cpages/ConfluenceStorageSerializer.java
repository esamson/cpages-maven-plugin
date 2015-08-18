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
import java.util.ArrayList;
import java.util.List;
import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;

class ConfluenceStorageSerializer extends ToHtmlSerializer {

    private final File attachmentsDirectory;
    private final List<File> attachments;

    public ConfluenceStorageSerializer(File attachmentsDirectory) {
        super(new LinkRenderer());
        this.attachmentsDirectory = attachmentsDirectory;
        this.attachments = new ArrayList<>();
    }

    List<File> getAttachments() {
        return attachments;
    }

    @Override
    protected void printImageTag(LinkRenderer.Rendering rendering) {
        printer.print("<ac:image>");
        if (rendering.href.startsWith("http://")
                || rendering.href.startsWith("https://")) {
            printer.print("<ri:url ri:value=\"");
            printer.print(rendering.href);
            printer.print("\" />");
        } else {
            File attachment = new File(attachmentsDirectory, rendering.href);
            if (attachment.canRead()) {
                attachments.add(attachment);
            } else {
                throw new IllegalArgumentException("Cannot read attachment: "
                        + attachment);
            }

            printer.print("<ri:attachment ri:filename=\"");
            printer.print(rendering.href);
            printer.print("\" />");
        }
        printer.print("</ac:image>");
    }

}
