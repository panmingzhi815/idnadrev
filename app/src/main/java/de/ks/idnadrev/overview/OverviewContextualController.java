/**
 * Copyright [2014] [Christian Loehnert]
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
package de.ks.idnadrev.overview;

import de.ks.BaseController;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.task.choosenext.NextTaskChooser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class OverviewContextualController extends BaseController<OverviewModel> {
  private static final Logger log = LoggerFactory.getLogger(OverviewContextualController.class);

  @FXML
  protected TableView<Task> contextTasks;
  @FXML
  protected TableColumn<Task, String> estimatedTime;
  @FXML
  protected ComboBox<String> context;
  @FXML
  protected TableColumn<Task, String> name;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    name.prefWidthProperty().bind(contextTasks.widthProperty().subtract(estimatedTime.widthProperty()));
    context.getItems().add("");

    context.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
      if (n != null) {
        CompletableFuture.supplyAsync(() -> loadTasksForContext(n), controller.getExecutorService())//
          .thenAcceptAsync(this::fillWithTasks, controller.getJavaFXExecutor());
      }
    });

    name.setCellValueFactory(this::getTaskName);
    estimatedTime.setCellValueFactory(this::convertRemainingMinutes);
  }

  private ObservableValue<String> getTaskName(TableColumn.CellDataFeatures<Task, String> param) {
    return new SimpleStringProperty(param.getValue().getName());
  }

  protected ObservableValue<String> convertRemainingMinutes(TableColumn.CellDataFeatures<Task, String> param) {
    long remainingMinutes = param.getValue().getRemainingTime().toMinutes();
    return new SimpleStringProperty(String.valueOf(remainingMinutes));
  }

  protected void fillWithTasks(List<Task> tasks) {
    contextTasks.getItems().clear();
    contextTasks.getItems().addAll(tasks);
  }

  private List<Task> loadTasksForContext(String context) {
    if (context.trim().isEmpty()) {
      context = null;
    }
    return new NextTaskChooser().getTasksSorted(60 * 24, context);
  }

  @Override
  protected void onRefresh(OverviewModel model) {
    super.onRefresh(model);
    List<String> contextNames = model.getContexts().stream().map(c -> c.getName()).collect(Collectors.toList());
    contextNames = new ArrayList<>(contextNames);

    ObservableList<String> items = context.getItems();
    contextNames.removeAll(items);
    items.addAll(contextNames);

    if (!items.isEmpty() && context.getSelectionModel().getSelectedIndex() < 0) {
      context.getSelectionModel().select(0);
    }
  }
}
