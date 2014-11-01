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

import de.ks.activity.ActivityCfg;
import de.ks.menu.MenuItem;

@MenuItem(order = 21, value = "/main/task")
public class ViewTasksActvity extends ActivityCfg {
  public ViewTasksActvity() {
    super(ViewTasksDS.class, ViewTasks.class);
    addAdditionalController(TaskFilterView.class);
    addAdditionalController(WorkUnitController.class);
  }
}
