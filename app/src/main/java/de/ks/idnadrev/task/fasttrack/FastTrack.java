/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
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
package de.ks.idnadrev.task.fasttrack;

import de.ks.BaseController;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.selection.NamedPersistentObjectSelection;
import de.ks.text.AsciiDocEditor;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class FastTrack extends BaseController<Task> {
  private static final Logger log = LoggerFactory.getLogger(FastTrack.class);
  @FXML
  protected StackPane descriptionView;
  @FXML
  protected NamedPersistentObjectSelection<Task> nameController;
  @FXML
  protected Label spentTime;
  protected AsciiDocEditor description;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AsciiDocEditor.load(descriptionView.getChildren()::add, ade -> this.description = ade);

    nameController.from(Task.class);

    nameController.selectedValueProperty().addListener((p, o, n) -> {
      if (n != null) {
        store.setModel(n);
      } else {
        Task model = new Task(nameController.getInput().getText());
        store.setModel(model);
      }
    });
    StringProperty nameBinding = store.getBinding().getStringProperty(Task.class, t -> t.getName());
    nameBinding.bindBidirectional(nameController.getInput().textProperty());

    description.hideActionBar();
    StringProperty descriptionBinding = store.getBinding().getStringProperty(Task.class, t -> t.getDescription());
    descriptionBinding.bindBidirectional(description.textProperty());
  }

  @FXML
  void finishTask() {
    controller.save();
    controller.stopCurrent();
  }

  @Override
  public void onStop() {
    controller.save();
  }

  @Override
  public void onSuspend() {
    controller.save();
  }
}
