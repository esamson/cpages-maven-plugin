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

import java.io.File;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ph.samson.maven.cpages.rest.model.Attachment;
import ph.samson.maven.cpages.rest.model.AttachmentsResult;
import ph.samson.maven.cpages.rest.model.Page;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * TODO: clean up Confluence state.
 *
 * Until then, manually execute tests as needed. Something like this in
 * ~/.m2/settings.xml feeds the test parameters:
 *
 * <pre>
 *     <profile>
 *         <id>cpages-it</id>
 *         <properties>
 *             <confluence.uri>https://my-instance.atlassian.net/wiki/rest/api/</confluence.uri>
 *             <confluence.username>me</confluence.username>
 *             <confluence.password>secret</confluence.password>
 *         </properties>
 *     </profile>
 * </pre>
 */
@Ignore
public class ConfluenceIT {

    private static final Logger log
            = LoggerFactory.getLogger(ConfluenceIT.class);

    private static String baseUri;
    private static String username;
    private static String password;

    public ConfluenceIT() {
    }

    @BeforeClass
    public static void requireTestParams() {
        baseUri = System.getProperty("uri");
        assertThat("baseUri required", baseUri, not(isEmptyOrNullString()));

        username = System.getProperty("username");
        assertThat("username required", username, not(isEmptyOrNullString()));

        password = System.getProperty("password");
        assertThat("password required", password, not(isEmptyOrNullString()));
    }

    /**
     * Test of getPage method, of class Confluence.
     */
    @Test
    public void testGetPage() {
        System.out.println("getPage");
        String spaceKey = "~esamson";
        String title = "Security Services";
        Confluence instance = new Confluence(baseUri, username, password);
        Page page = instance.getPage(spaceKey, title);
        log.debug("page: {}", page);
    }

    @Test
    public void testCreatePage() {
        String spaceKey = "~esamson";
        String title = "Test Create";
        Confluence instance = new Confluence(baseUri, username, password);
        instance.createPage(spaceKey, title, "h1. One\nh2. Two\nh3. Three");
    }

    @Test
    public void testCreateChildPage() {
        String spaceKey = "~esamson";
        String title = "Test Create Child";
        Confluence instance = new Confluence(baseUri, username, password);
        instance.createChildPage(spaceKey, "30113837", title,
                "h1. Four\nh2. Five\nh3. Six");
    }

    @Test
    public void testUpdatePage() {
        String spaceKey = "~esamson";
        String title = "Test Create Child";
        Confluence instance = new Confluence(baseUri, username, password);
        Page page = instance.getPage(spaceKey, title);
        log.debug("page: {}", page);

        instance.updatePage(page, "h6. Six\nh5. Five\nh4. Four");
    }

    @Test
    public void testGetAttachments() {
        String pageId = "30113837";
        Confluence instance = new Confluence(baseUri, username, password);
        AttachmentsResult attachments = instance.getAttachments(pageId);
        log.debug("attachments: {}", attachments);
    }

    @Test
    public void testCreateAttachments() throws IOException {
        String pageId = "30113837";
        Confluence instance = new Confluence(baseUri, username, password);
        instance.createAttachments(pageId,
                new File("src/test/resources/test1.png"),
                new File("src/test/resources/test2.png"),
                new File("src/test/resources/test3.png"));
    }

    @Test
    public void testUpdateAttachment() throws IOException {
        String pageId = "30113837";
        Confluence instance = new Confluence(baseUri, username, password);
        AttachmentsResult attachments = instance.getAttachments(pageId);

        for (Attachment attachment : attachments.getResults()) {
            if ("avatar.jpg".equals(attachment.getTitle())) {
                instance.updateAttachment(attachment,
                        new File("src/test/resources/avatar.jpg"));
                break;
            }
        }
    }
}
