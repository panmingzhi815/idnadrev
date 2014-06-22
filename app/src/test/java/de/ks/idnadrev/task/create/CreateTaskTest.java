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
package de.ks.idnadrev.task.create;

import de.ks.FXPlatform;
import de.ks.LauncherRunner;
import de.ks.activity.ActivityController;
import de.ks.idnadrev.entity.Context;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.Task;
import de.ks.idnadrev.entity.WorkUnit;
import de.ks.persistence.PersistentWork;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class CreateTaskTest {
  @Inject
  ActivityController activityController;
  private MainTaskInfo controller;
  private CreateTask createTask;

  @Before
  public void setUp() throws Exception {
    PersistentWork.deleteAllOf(WorkUnit.class, Context.class, Task.class, Tag.class);
    PersistentWork.persist(new Context("context"));

    activityController.start(CreateTaskActivity.class);
    activityController.waitForDataSource();
    createTask = activityController.<CreateTask>getCurrentController();
    controller = createTask.mainInfoController;
  }

  @After
  public void tearDown() throws Exception {
    activityController.stop(CreateTaskActivity.class);
  }

  @Test
  public void testPersist() throws InterruptedException {
    FXPlatform.invokeLater(() -> {
      controller.name.setText("name");
      controller.contextController.getInput().setText("context");
      controller.estimatedTimeDuration.setText("15min");
      controller.funFactor.valueProperty().set(3);
      controller.mentalEffort.valueProperty().set(10);
      controller.physicalEffort.valueProperty().set(7);
    });
    FXPlatform.invokeLater(() -> {
      controller.save();
    });
    activityController.waitForDataSource();

    List<Task> tasks = PersistentWork.from(Task.class, (t) -> {
      t.getContext().getName();
    });
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    assertEquals("name", task.getName());
    assertNotNull(task.getContext());
    assertEquals("context", task.getContext().getName());
    assertEquals(7, task.getPhysicalEffort().getAmount());
    assertEquals(3, task.getFunFactor().getAmount());
    assertEquals(10, task.getMentalEffort().getAmount());

    Duration estimatedTime = task.getEstimatedTime();
    assertNotNull(estimatedTime);
    assertEquals(Duration.ofMinutes(15), estimatedTime);
  }

  @Test
  public void testTags() throws Exception {
    PersistentWork.persist(new Tag("tag1"));

    FXPlatform.invokeLater(() -> {
      controller.name.setText("name");

      controller.tagAddController.getInput().setText("tag1");
      controller.tagAddController.getOnAction().handle(null);
    });
    activityController.getCurrentExecutorService().waitForAllTasksDone();
    FXPlatform.invokeLater(() -> {
      controller.tagAddController.getInput().setText("tag2");
      controller.tagAddController.getOnAction().handle(null);
    });
    activityController.getCurrentExecutorService().waitForAllTasksDone();

    FXPlatform.invokeLater(() -> {
      controller.save();
    });
    activityController.waitForDataSource();

    List<Task> tasks = PersistentWork.from(Task.class, (t) -> {
      t.getTags().toString();
    });
    Task task = tasks.get(0);
    assertEquals(2, task.getTags().size());
  }
}
