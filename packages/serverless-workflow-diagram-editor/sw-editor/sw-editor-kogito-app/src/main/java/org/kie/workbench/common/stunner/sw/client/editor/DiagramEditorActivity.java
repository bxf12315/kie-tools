/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.sw.client.editor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.user.client.ui.IsWidget;
import elemental2.promise.Promise;
import org.uberfire.client.mvp.AbstractActivity;
import org.uberfire.client.mvp.EditorActivity;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.security.ResourceType;
import org.uberfire.workbench.model.ActivityResourceType;

@ApplicationScoped
@Named(DiagramEditor.EDITOR_ID)
public class DiagramEditorActivity extends AbstractActivity implements EditorActivity {

    @Inject
    private DiagramEditor realPresenter;

    @Override
    public void onStartup(final PlaceRequest place) {
        super.onStartup(place);
        realPresenter.onStartup(place);
    }

    @Override
    public void onClose() {
        super.onClose();
        realPresenter.onClose();
    }

    @Override
    public void onOpen() {
        super.onOpen();
        realPresenter.onOpen();
    }

    @Override
    public IsWidget getWidget() {
        return realPresenter.asWidget();
    }

    @Override
    public Promise<Void> setContent(String path, String value) {
        return realPresenter.setContent(path, value);
    }

    @Override
    public Promise<Void> selectStateByName(String name){
        return realPresenter.selectStateByName(name);
    }

    @Override
    public Promise<String> getContent() {
        return realPresenter.getContent();
    }

    @Override
    public Promise<String> getPreview() {
        return realPresenter.getPreview();
    }

    @Override
    public Promise validate() {
        return realPresenter.validate();
    }

    @Override
    public String getIdentifier() {
        return DiagramEditor.EDITOR_ID;
    }

    @Override
    public ResourceType getResourceType() {
        return ActivityResourceType.EDITOR;
    }
}
