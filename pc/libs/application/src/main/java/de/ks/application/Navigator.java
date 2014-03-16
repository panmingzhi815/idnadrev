package de.ks.application;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.collect.MapMaker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class Navigator {
  private static final Logger log = LoggerFactory.getLogger(Navigator.class);
  private static ConcurrentMap<Window, Navigator> navigators = new MapMaker().weakKeys().makeMap();

  public static Navigator getCurrentNavigator() {
    for (Map.Entry<Window, Navigator> entry : navigators.entrySet()) {
      if (entry.getKey().isFocused() && entry.getKey().isShowing()) {
        return entry.getValue();
      }
    }
    return null;
  }

  /**
   * @param node
   * @return the navigator associated with the window of this node.
   */
  public static Navigator getNavigator(Node node) {
    return navigators.get(node.getScene().getWindow());
  }

  public static Navigator getNavigator(Window stage) {
    return navigators.get(stage);
  }

  public static Navigator register(Window stage, Pane root) {
    Navigator navigator = new Navigator(root);
    Navigator old = navigators.remove(stage);
    if (old != null) {
      log.warn("Replacing root pane of navigator, old={} new={}", old.getMainArea().getContent(), root);
    }
    navigators.put(stage, navigator);
    return navigator;
  }

  public static Navigator registerWithBorderPane(Stage stage) {
    BorderPane borderPane = new BorderPane();
    borderPane.setCenter(new StackPane());
    borderPane.setTop(new StackPane());
    borderPane.setBottom(new StackPane());
    borderPane.setLeft(new StackPane());
    borderPane.setRight(new StackPane());
    return registerWithExistingPane(stage, borderPane);
  }

  public static Navigator registerWithExistingPane(Stage stage, BorderPane borderPane) {
    Navigator navigator = new Navigator((Pane) borderPane.getCenter());
    if (borderPane.getTop() instanceof Pane) {
      navigator.addPresentationArea(TOP_AREA, (Pane) borderPane.getTop());
    }
    if (borderPane.getBottom() instanceof Pane) {
      navigator.addPresentationArea(BOTTOM_AREA, (Pane) borderPane.getBottom());
    }
    if (borderPane.getLeft() instanceof Pane) {
      navigator.addPresentationArea(LEFT_AREA, (Pane) borderPane.getLeft());
    }
    if (borderPane.getRight() instanceof Pane) {
      navigator.addPresentationArea(RIGHT_AREA, (Pane) borderPane.getRight());
    }
    navigators.put(stage, navigator);
    return navigator;
  }


  public static final String MAIN_AREA = "main";
  public static final String LEFT_AREA = "left";
  public static final String RIGHT_AREA = "right";
  public static final String TOP_AREA = "content";
  public static final String BOTTOM_AREA = "bottom";

  protected final ObservableMap<String, PresentationArea> presentationAreas = FXCollections.observableHashMap();
  protected final Map<String, Deque<Class<?>>> histories = new HashMap<>();


  private Navigator(Pane mainArea) {
    addPresentationArea(MAIN_AREA, mainArea);
  }

  public void addPresentationArea(String id, Pane pane) {
    PresentationArea presentationArea = new PresentationArea(id, pane);
    presentationArea.currentNodeProperty().addListener((observable, previous, current) -> {
      if (previous != null) {
//  histories.get(presentationArea.getId()).add()
      }
    });
    presentationAreas.put(id, presentationArea);
  }

  public void presentInMain(Node node) {
    present(MAIN_AREA, node);
  }

  public void present(String area, Node node) {
    presentationAreas.get(area).setCurrentNode(node);
  }

  public void back(String area) {

  }

  public void next(String area) {

  }

  public PresentationArea getPresentationArea(String id) {
    return presentationAreas.get(id);
  }

  public PresentationArea getMainArea() {
    return getPresentationArea(MAIN_AREA);
  }
}
