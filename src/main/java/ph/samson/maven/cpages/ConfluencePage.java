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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.List;

class ConfluencePage {

    private final String title;
    private final String contents;
    private final List<File> attachments;

    ConfluencePage(String title, String contents, List<File> attachments) {
        this.title = title;
        this.contents = contents;
        this.attachments = ImmutableList.copyOf(attachments);
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }

    public List<File> getAttachments() {
        return attachments;
    }
}
