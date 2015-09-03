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

package ph.samson.maven.cpages.rest;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.samson.maven.cpages.rest.model.Attachment;
import ph.samson.maven.cpages.rest.model.AttachmentsResult;
import ph.samson.maven.cpages.rest.model.GetPageResults;
import ph.samson.maven.cpages.rest.model.Page;
import ph.samson.maven.cpages.rest.model.Version;

import static com.cedarsoftware.util.io.JsonWriter.formatJson;
import static com.cedarsoftware.util.io.JsonWriter.objectToJson;
import static com.google.common.hash.Hashing.sha1;
import static java.nio.file.Files.readAllBytes;

/**
 * Confluence REST API client
 *
 * @see https://developer.atlassian.com/confdev/confluence-rest-api
 */
public class Confluence {

    private static final Logger log
            = LoggerFactory.getLogger(Confluence.class);

    private final Client client;
    private final WebTarget webTarget;

    public Confluence(String baseUri, String username, String password) {
        client = ClientBuilder.newBuilder()
                .register(HttpAuthenticationFeature.basic(username, password))
                .register(MultiPartFeature.class)
                .build();
        webTarget = client.target(baseUri).path("content");
    }

    public Page getPage(String spaceKey, String title) {
        WebTarget w = webTarget.queryParam("spaceKey", spaceKey)
                .queryParam("expand", "space,ancestors,version")
                .queryParam("title", title);
        log.debug("requesting: {}", w.getUri());
        Response response = w.request(MediaType.APPLICATION_JSON_TYPE).get();
        int status = response.getStatus();

        logDebug(response, status);

        GetPageResults results = response.readEntity(GetPageResults.class);
        if (results.getSize() == 0) {
            return null;
        } else {
            if (results.getSize() > 1) {
                log.warn("Query returned more than one page.");
            }
            Page result = results.getResults().get(0);
            result.get_links().setBase(results.get_links().getBase());
            return result;
        }
    }

    public Page createChildPage(String spaceKey, String parentId, String title,
            String wikiText) {
        /*
         * Since it is not possible to set a Version message when creating a
         * new page, we have nowhere to place the content hash.
         * Instead, we create with an empty body and immediately update with
         * the actual body so we get the content hash in Version 2.
         */
        Page page = new Page(spaceKey, parentId, title, "New Page.", "storage");
        if (log.isDebugEnabled()) {
            log.debug("createPage: {}", objectToJson(page));
        }
        Response response = webTarget.request().post(Entity.json(page));
        int status = response.getStatus();
        logDebug(response, status);

        return updatePage(response.readEntity(Page.class), wikiText);
    }

    public Page createPage(String spaceKey, String title, String wikiText) {
        return createChildPage(spaceKey, null, title, wikiText);
    }

    public Page updatePage(Page page, String newWikiText) {
        String hash = sha1().hashUnencodedChars(newWikiText).toString();
        if (hash.equals(page.getVersion().getMessage())) {
            log.info("No changes to {} body", page.getTitle());
            return page;
        }

        WebTarget pageTarget = webTarget.path(page.getId());

        page.getBody().getStorage().setValue(newWikiText);
        page.getBody().getStorage().setRepresentation("storage");
        Version version = new Version(page.getVersion().getNumber() + 1);
        version.setMessage(hash);
        page.setVersion(version);
        if (page.getAncestors().size() > 1) {
            page.setAncestors(ImmutableList.of(page.getAncestors().get(
                    page.getAncestors().size() - 1)));
        }
        Response response = pageTarget.request().put(Entity.json(page));
        int status = response.getStatus();

        logDebug(response, status);

        return response.readEntity(Page.class);
    }

    public AttachmentsResult getAttachments(String pageId) {
        WebTarget attachment = webTarget.path(pageId).path("child")
                .path("attachment")
                .queryParam("expand", "container,version");
        log.debug("requesting: {}", attachment.getUri());
        Response response = attachment
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        int status = response.getStatus();

        logDebug(response, status);

        return response.readEntity(AttachmentsResult.class);
    }

    public AttachmentsResult createAttachments(String pageId, File... files)
            throws IOException {
        WebTarget attachment = webTarget.path(pageId).path("child")
                .path("attachment");

        FormDataMultiPart multiPart = new FormDataMultiPart();
        for (File fileEntity : files) {
            log.info("Creating attachment {}", fileEntity);
            multiPart.bodyPart(new FileDataBodyPart("file", fileEntity));
            multiPart.field("comment", sha1().hashBytes(
                    readAllBytes(fileEntity.toPath())).toString());
        }

        Response response = attachment.request(MediaType.APPLICATION_JSON_TYPE)
                .header("X-Atlassian-Token", "no-check")
                .post(Entity.entity(multiPart, multiPart.getMediaType()));
        int status = response.getStatus();

        logDebug(response, status);

        return response.readEntity(AttachmentsResult.class);
    }

    public AttachmentsResult updateAttachment(Attachment attachment,
            File fileEntity) throws IOException {
        String hash = sha1().hashBytes(
                readAllBytes(fileEntity.toPath())).toString();
        if (hash.equals(attachment.getMetadata().getComment())) {
            log.info("No changes to attachment {}", attachment.getTitle());
            return null;
        }

        WebTarget data = webTarget.path(attachment.getContainer().getId())
                .path("child").path("attachment")
                .path(attachment.getId())
                .path("data");
        log.info("Updating attachment {}", attachment.getTitle());

        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.bodyPart(new FileDataBodyPart("file", fileEntity));
        multiPart.field("comment", hash);

        Response response = data.request(MediaType.APPLICATION_JSON_TYPE)
                .header("X-Atlassian-Token", "no-check")
                .post(Entity.entity(multiPart, multiPart.getMediaType()));
        int status = response.getStatus();

        logDebug(response, status);

        return response.readEntity(AttachmentsResult.class);
    }

    private void logDebug(Response response, int status) throws RuntimeException {
        if (log.isDebugEnabled() && response.bufferEntity()) {
            String body = response.readEntity(String.class);
            log.debug("response: {}; {}", status, formatJson(body));
        }
    }
}
