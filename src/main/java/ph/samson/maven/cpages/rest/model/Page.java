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

package ph.samson.maven.cpages.rest.model;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class Page {

    private String id;
    private String type = "page";
    private String status;
    private String title;
    private Space space;
    private Body body;
    private Version version;
    private List<Page> ancestors;
    private Links _links;

    public Page() {
    }

    public Page(String id) {
        this.id = id;
    }

    public Page(String spaceKey, String parentId, String title,
            String bodyContents, String bodyRepresentation) {
        this.space = new Space(spaceKey);
        this.title = title;
        this.body = new Body(bodyContents, bodyRepresentation);

        if (parentId != null) {
            ancestors = ImmutableList.of(new Page(parentId));
        }
    }

    public Body getBody() {
        if (body == null) {
            body = new Body();
        }
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public List<Page> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<Page> ancestors) {
        this.ancestors = ancestors;
    }

    public Version getVersion() {
        if (version == null) {
            version = new Version();
        }
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Links get_links() {
        return _links;
    }

    public void set_links(Links _links) {
        this._links = _links;
    }
}
