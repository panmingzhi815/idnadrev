package de.ks.activity;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.application.Navigator;
import de.ks.application.fxml.DefaultLoader;
import de.ks.activity.callback.InitializeActivityLinks;
import de.ks.activity.callback.InitializeViewLinks;
import de.ks.activity.link.ActivityLink;
import de.ks.activity.link.TaskLink;
import de.ks.activity.link.ViewLink;
import javafx.concurrent.Task;
import javafx.scene.Node;

import java.util.*;

/**
 *
 */
public class Activity {
  private final Class<?> initialController;
  private final ActivityController activityController;
  private final Navigator navigator;
  protected final List<ViewLink> viewLinks = new ArrayList<>();
  protected final List<TaskLink> taskLinks = new ArrayList<>();
  protected final List<ActivityLink> activityLinks = new ArrayList<>();

  protected final Map<Class<?>, DefaultLoader<Node, Object>> preloads = new HashMap<>();

  public Activity(Class<?> initialController, ActivityController activityController, Navigator navigator) {
    this.initialController = initialController;
    this.activityController = activityController;
    this.navigator = navigator;
  }

  /**
   * Select next controller
   *
   * @param sourceController
   * @param id
   * @param targetController
   * @return
   */
  public Activity withLink(Class<?> sourceController, String id, Class<?> targetController) {
    return withLink(sourceController, id, Navigator.MAIN_AREA, targetController);
  }

  public Activity withLink(Class<?> sourceController, String id, String presentationArea, Class<?> targetController) {
    ViewLink viewLink = ViewLink.from(sourceController).with(id).to(targetController).in(presentationArea).build();
    viewLinks.add(viewLink);
    return this;
  }

  /**
   * Execute given task
   *
   * @param sourceController
   * @param id
   * @param task
   * @param <V>
   * @return
   */
  public <V> Activity withTask(Class<?> sourceController, String id, Class<? extends Task<V>> task) {
    TaskLink taskLink = TaskLink.from(sourceController).with(id).execute(task).build();
    taskLinks.add(taskLink);
    return this;
  }

  /**
   * Switch to next activity
   *
   * @param sourceController
   * @param id
   * @param next
   * @return
   */
  public Activity withActivity(Class<?> sourceController, String id, Activity next) {
    ActivityLink activityLink = ActivityLink.from(sourceController).with(id).start(next).build();
    activityLinks.add(activityLink);
    return this;
  }

  public Class<?> getInitialController() {
    return initialController;
  }

  public void start() {
    DefaultLoader<Node, Object> loader = new DefaultLoader<Node, Object>(initialController);
    addCallbacks(loader);
    preloads.put(initialController, loader);
    select(initialController, Navigator.MAIN_AREA);
    loadNextControllers();
  }

  protected void loadNextControllers() {
    for (Iterator<ViewLink> iterator = viewLinks.iterator(); iterator.hasNext(); ) {
      ViewLink next = iterator.next();
      loadController(next.getSourceController());
      loadController(next.getTargetController());
    }
  }

  private void loadController(Class<?> controller) {
    if (!preloads.containsKey(controller)) {
      DefaultLoader<Node, Object> loader = new DefaultLoader<>(controller);
      preloads.put(controller, loader);
      addCallbacks(loader);
    }
  }

  public void select(ViewLink link) {
    select(link.getTargetController(), link.getPresentationArea());
  }

  public void select(Class<?> targetController, String presentationArea) {
    DefaultLoader<Node, Object> loader = preloads.get(targetController);
    navigator.present(presentationArea, loader.getView());
  }

  private void addCallbacks(DefaultLoader<Node, Object> loader) {
    loader.addCallback(new InitializeViewLinks(viewLinks, activityController));
    loader.addCallback(new InitializeActivityLinks(activityLinks, activityController));
  }

  public List<ViewLink> getViewLinks() {
    return viewLinks;
  }
}
