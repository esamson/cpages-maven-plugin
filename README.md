## Confluence Pages Maven Plugin

Compose [Confluence][1] wiki pages as a Maven project.

* Compose page content using [Markdown syntax][2].
* Create diagrams using [PlantUML][3].

### Project Structure

Each wiki page is represented by a directory under the `src` directory. A wiki
dir must contain exactly one Markdown file which will be the contents of the
wiki page. Actual name of the file does not matter as long it ends in `.md`.
The name of the directory will be taken as the title of the wiki page.

Diagrams written in PlantUML must have file names ending in `.puml`. In the
Markdown file, refer to diagrams as images by replacing the file name extension
with `.png`.

A wiki dir may have subdirectories. These are wiki dirs themselves and are
rendered as child pages in Confluence.

For example:

    wiki-project/
    ├── pom.xml
    └── src/
        └── Main/
            ├── Main.md
            ├── diagram1.puml
            └── More Details/
                └── details.md

Will upload a page titled **Main** with a child page with title
**More Details**. Inside `Main.md`, the diagram is referenced as:

`![Diagram][diagram1.png]`

### Confluence Credentials

The plugin requires your Confluence credentials to access the Confluence REST
API. Add a `server` section to your Maven settings file (`~/.m2/settings.xml`):

    <server>
        <id>my-confluence</id>
        <username>me</username>
        <password>{PCtnibAGfiQHDyPPaKbgPaxqb/FCHDMJrnUrdgCYfKw=}</password>
    </server>

You may use Maven [Password Encryption][4].

### Project Configuration

In `pom.xml` you should have, at least.

    <groupId>com.example</groupId>
    <artifactId>wiki-project</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>cpages</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>ph.samson.maven</groupId>
                <artifactId>cpages-maven-plugin</artifactId>
                <version>0.3.0</version>
                <extensions>true</extensions>
            </plugin>
        </plugins>
    </build>

Setting `<extensions>true</extensions>` for the plugin gives us the `cpages`
packaging type which we use for the project. Configuration may be provided as
properties in the POM.

    <properties>
        <confluence.serverId>my-confluence</confluence.serverId>
        <confluence.endpoint>https://example.atlassian.net/wiki/rest/api/</confluence.endpoint>
        <confluence.spaceKey>My Space</confluence.spaceKey>
        <confluence.parentTitle>The Parent</confluence.parentTitle>
        <confluence.scmUrl>https://foohub.com/me/wiki-project</confluence.scmUrl>
    </properties>

The properties are as follows.

* `confluence.serverId` - Server ID from your maven settings.
* `confluence.endpoint` - Confluence REST API endpoint.
* `confluence.spaceKey` - Wiki space to place pages under.
* `confluence.parentTitle` - Title of existing page to serve as parent. If not
    provided, pages will be placed at the top level of the wiki space.
* `confluence.scmUrl` - If provided, a link will be added to the foot of each
    page to point the reader to the source project.

### Using

`mvn site` will render to HTML locally so you can preview the result. This
starts a [LiveReload][5] server so you can continue editing your text and
diagrams and get an automatically updated view on your browser.

`mvn deploy` will render and upload the pages to your Confluence wiki. New
pages and attachments will be created, existing ones get updated.

### Why?

I prefer writing in plain text formats and being able to use Git for version
control.

[1]: https://www.atlassian.com/software/confluence
[2]: http://daringfireball.net/projects/markdown/syntax
[3]: http://plantuml.com/
[4]: https://maven.apache.org/guides/mini/guide-encryption.html
[5]: https://github.com/livereload/livereload-js

