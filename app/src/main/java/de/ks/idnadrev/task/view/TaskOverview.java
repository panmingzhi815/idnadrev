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
package de.ks.idnadrev.task.view;

import com.google.common.eventbus.Subscribe;
import de.ks.activity.ActivityController;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.context.ActivityStore;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Task;
import de.ks.text.AsciiDocParser;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TaskOverview implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(TaskOverview.class);
  public static final String NEGATIVE_FUN_FACTOR = "negativeFunFactor";
  @FXML
  protected TreeTableView<Task> tasksView;
  @FXML
  protected TreeTableColumn<Task, String> taskViewNameColumn;
  @FXML
  protected TreeTableColumn<Task, String> taskViewCreationTimeColumn;
  @FXML
  protected Label name;
  @FXML
  protected Label context;
  @FXML
  protected Label estimatedTime;
  @FXML
  protected Label spentTime;
  @FXML
  protected Hyperlink parentProject;
  @FXML
  protected ProgressBar physicalEffort;
  @FXML
  protected ProgressBar mentalEffort;
  @FXML
  protected ProgressBar funFactor;
  @FXML
  protected FlowPane tagPane;
  @FXML
  protected WebView description;
  @FXML
  protected Button start;

  @Inject
  ActivityStore store;
  @Inject
  ActivityController controller;
  @Inject
  AsciiDocParser parser;

  protected ObservableList<Task> tasks = FXCollections.observableArrayList();
  protected final ConcurrentHashMap<Long, String> renderedDescription = new ConcurrentHashMap<>();
  private Map<Task, TreeItem<Task>> task2TreeItem = new HashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tasksView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> applyTask(n));
    taskViewNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getName()));
    taskViewCreationTimeColumn.setCellValueFactory(param -> {
      TreeItem<Task> treeItem = param.getValue();
      Task task = treeItem.getValue();
      LocalDateTime creationTime = task.getCreationTime();
      return new SimpleStringProperty(creationTime.toString());
    });
  }

  protected void applyTask(TreeItem<Task> taskTreeItem) {
    clear();
    if (taskTreeItem != null) {
      Task task = taskTreeItem.getValue();
      name.setText(task.getName());
      context.setText(task.getContext() != null ? task.getContext().getName() : "");
      estimatedTime.setText(parseDuration(task.isProject() ? task.getTotalEstimatedTime() : task.getEstimatedTime()));
      parentProject.setText(task.getParent() != null ? task.getParent().getName() : null);
      physicalEffort.setProgress(task.getPhysicalEffort().getAmount() / 10D);
      mentalEffort.setProgress(task.getMentalEffort().getAmount() / 10D);

      if (task.getFunFactor().getAmount() < 0) {
        funFactor.getStyleClass().add(NEGATIVE_FUN_FACTOR);
      } else {
        funFactor.getStyleClass().remove(NEGATIVE_FUN_FACTOR);
      }
      funFactor.setProgress(Math.abs(task.getFunFactor().getAmount()) / 5D);

      task.getTags().forEach((tag) -> tagPane.getChildren().add(new Label(tag.getName())));

      String asciiDoc = renderedDescription.get(task.getId());
      if (asciiDoc != null) {
        description.getEngine().loadContent(asciiDoc);
      } else {
        String desc = task.getDescription();
        description.getEngine().loadContent(desc == null ? "" : desc);
      }
    }
  }

  private String parseDuration(Duration duration) {
    if (duration == null) {
      return null;
    } else {
      long hours = duration.toHours();
      if (hours == 0) {
        return duration.toMinutes() + Localized.get("duration.minutes");
      } else {
        long remainingMinutes = duration.minus(Duration.ofHours(hours)).toMinutes();
        return hours + ":" + remainingMinutes + Localized.get("duration.hours.short");
      }
    }
  }

  private void clear() {
    name.setText(null);
    spentTime.setText(null);
    description.getEngine().loadContent("");
    parentProject.setText(null);
    context.setText(null);
    physicalEffort.setProgress(0);
    mentalEffort.setProgress(0);
    funFactor.setProgress(0);
    tagPane.getChildren().clear();
  }

  @FXML
  void selectParentProject() {
    Task parent = tasksView.getSelectionModel().getSelectedItem().getValue().getParent();
    TreeItem<Task> nextSelection = task2TreeItem.get(parent);
    tasksView.getSelectionModel().select(nextSelection);
  }

  @FXML
  void showTimeUnits() {

  }

  @FXML
  void startWork() {

  }

  @FXML
  void finishTask() {

  }

  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public void afterLoad(ActivityLoadFinishedEvent event) {
    List<Task> loaded = event.getModel();
    tasks.clear();
    tasks.addAll(loaded);
    TreeItem<Task> root = buildTreeStructure(loaded);
    tasksView.setRoot(root);
    Platform.runLater(() -> {
      if (!root.getChildren().isEmpty()) {
        root.setExpanded(true);
        tasksView.getSelectionModel().select(root.getChildren().get(0));
      }
    });

    tasks.forEach(task -> {
      CompletableFuture.supplyAsync(() -> Pair.of(task, parser.parse(task.getDescription())), controller.getCurrentExecutorService())//
              .thenApply(pair -> {
                Task currentTask = pair.getKey();
                String asciiDocHtml = pair.getValue();
                renderedDescription.put(currentTask.getId(), asciiDocHtml);
                return pair;
              })//
              .thenAcceptAsync(pair -> {
                Task currentTask = pair.getKey();
                String asciiDocHtml = pair.getValue();
                TreeItem<Task> selectedItem = tasksView.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue().equals(currentTask)) {
                  description.getEngine().loadContent(asciiDocHtml);
                }
              }, controller.getJavaFXExecutor());
    });
  }

  protected TreeItem<Task> buildTreeStructure(List<Task> loaded) {
    TreeItem<Task> root = new TreeItem<>(new Task(Localized.get("all")) {
      {
        id = 0L;
      }
    });
    task2TreeItem = new HashMap<>(loaded.size());
    calculateTotalTime(loaded, root);
    loaded.forEach((task) -> {
      TreeItem<Task> treeItem = new TreeItem<>(task);
      task2TreeItem.put(task, treeItem);
    });
    loaded.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).forEach((task) -> {
      if (task.getParent() == null) {
        root.getChildren().add(task2TreeItem.get(task));
      } else {
        TreeItem<Task> parentItem = task2TreeItem.get(task.getParent());
        TreeItem<Task> childItem = task2TreeItem.get(task);
        parentItem.getChildren().add(childItem);
      }
    });
    return root;
  }

  private void calculateTotalTime(List<Task> loaded, TreeItem<Task> root) {
    Duration total = Duration.ofHours(0);
    for (Task task : loaded) {
      total = total.plus(task.getEstimatedTime());
    }
    root.getValue().setEstimatedTime(total);
  }
}
