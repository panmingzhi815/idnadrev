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
package de.ks.idnadrev.thought.collect.file;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.ks.activity.ActivityLoadFinishedEvent;
import de.ks.activity.context.ActivityStore;
import de.ks.activity.initialization.DataStoreCallback;
import de.ks.file.FileStore;
import de.ks.idnadrev.entity.FileReference;
import de.ks.idnadrev.entity.Thought;
import de.ks.persistence.PersistentWork;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileThoughtViewController implements Initializable, DataStoreCallback<Thought> {
  private static final Logger log = LoggerFactory.getLogger(FileThoughtViewController.class);
  protected final ObservableList<File> files = FXCollections.observableArrayList();
  protected final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
  protected final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("awt.Desktop-%d").build());
  @FXML
  private Button edit;
  @FXML
  private Label fileNameLabel;
  @FXML
  private Label folderName;
  @FXML
  private ListView<File> fileList;

  @Inject
  ActivityStore store;
  @Inject
  FileStore fileStore;
  protected final Map<File, CompletableFuture<FileReference>> fileReferences = new HashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    fileList.setItems(files);

    MultipleSelectionModel<File> selectionModel = fileList.getSelectionModel();
    selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

    ReadOnlyObjectProperty<File> selection = selectionModel.selectedItemProperty();
    selection.addListener((p, o, n) -> {
      folderName.setText(n == null ? "" : n.getParentFile().getAbsolutePath());
      fileNameLabel.setText(n == null ? "" : n.getName());
    });

    BooleanBinding isDirectory = Bindings.createBooleanBinding(() -> selection.get() != null && selection.get().isDirectory(), selection);
    edit.disableProperty().bind(isDirectory);

    files.addListener((ListChangeListener<File>) change -> {
      Thought thought = store.getModel();
      while (change.next()) {
        change.getAddedSubList().forEach(file -> {
          fileReferences.put(file, fileStore.getReference(thought, file));
        });
        change.getRemoved().forEach((file) -> {
          fileReferences.remove(file);
        });
      }
    });
  }

  public void addFiles(List<File> additionalFiles) {
    additionalFiles.removeAll(files);
    files.addAll(additionalFiles);

    if (!additionalFiles.isEmpty()) {
      Collections.sort(files);
      Collections.sort(additionalFiles);
      File lastFile = additionalFiles.get(additionalFiles.size() - 1);
      fileList.scrollTo(lastFile);
      fileList.getSelectionModel().clearSelection();
      fileList.getSelectionModel().select(lastFile);
    }
  }

  public ObservableList<File> getFiles() {
    return files;
  }

  public ListView<File> getFileList() {
    return fileList;
  }

  @FXML
  void open(ActionEvent event) {
    ObservableList<File> items = fileList.getSelectionModel().getSelectedItems();
    for (File item : items) {
      executor.submit(() -> {
        try {
          log.info("Opening {}", item);
          desktop.open(item);
        } catch (IOException e) {
          log.error("Could not open {}", item, e);
        }
      });
    }
  }

  @FXML
  void edit(ActionEvent event) {
    ObservableList<File> items = fileList.getSelectionModel().getSelectedItems();
    for (File item : items) {
      executor.submit(() -> {
        try {
          log.info("Editing {}", item);
          desktop.edit(item);
        } catch (IOException e) {
          log.error("Could not open {}", item, e);
        }
      });
    }
  }

  @FXML
  void openFolder(ActionEvent event) {
    TreeSet<File> files = new TreeSet<>();

    ObservableList<File> items = fileList.getSelectionModel().getSelectedItems();
    for (File item : items) {
      if (item.isDirectory()) {
        files.add(item);
      } else {
        files.add(item.getParentFile());
      }
    }
    for (File file : files) {
      executor.submit(() -> {
        try {
          log.info("Opening {}", file);
          desktop.open(file);
        } catch (IOException e) {
          log.error("Could not open {}", file, e);
        }
      });
    }
  }

  @FXML
  public void removeFile(ActionEvent event) {
    ObservableList<File> selectedItems = fileList.getSelectionModel().getSelectedItems();
    files.removeAll(selectedItems);
  }

  @FXML
  void addNewFile(ActionEvent event) {
    FileChooser fileChooser = new FileChooser();
    File file = fileChooser.showOpenDialog(edit.getScene().getWindow());
    if (file != null) {
      addFiles(Arrays.asList(file));
    }
  }

  @Subscribe
  public void onRefresh(ActivityLoadFinishedEvent event) {
    files.clear();
  }

  @Override
  public void duringLoad(Thought model) {

  }

  @Override
  public void duringSave(Thought model) {
    this.fileReferences.entrySet().forEach(entry -> {
      try {
        File file = entry.getKey();
        CompletableFuture<FileReference> cf = entry.getValue();
        FileReference fileReference = cf.get();
        fileReference.setThought(model);
        fileStore.scheduleCopy(fileReference, file);
        PersistentWork.persist(fileReference);
      } catch (InterruptedException | ExecutionException e) {
        log.error("Could not get fileReference for file {}", entry.getKey());
        throw new RuntimeException(e);
      }
    });
  }
}
