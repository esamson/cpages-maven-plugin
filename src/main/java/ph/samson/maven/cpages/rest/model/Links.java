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

public class Links {

    private String self;
    private String base;
    private String context;
    private String webui;
    private String tinyui;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getWebui() {
        return webui;
    }

    public void setWebui(String webui) {
        this.webui = webui;
    }

    public String getTinyui() {
        return tinyui;
    }

    public void setTinyui(String tinyui) {
        this.tinyui = tinyui;
    }

    @Override
    public String toString() {
        return "Links{"
                + "self=" + self
                + ", base=" + base
                + ", context=" + context
                + ", webui=" + webui
                + ", tinyui=" + tinyui
                + '}';
    }
}
