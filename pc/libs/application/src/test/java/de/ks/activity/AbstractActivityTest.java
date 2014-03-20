/*
 * Copyright [${YEAR}] [Christian Loehnert]
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

package de.ks.activity;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.JFXCDIRunner;
import de.ks.application.Navigator;
import de.ks.datasource.NewInstanceDataSource;
import org.junit.Before;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 *
 */
@RunWith(JFXCDIRunner.class)
public abstract class AbstractActivityTest {
  protected Navigator navigator;
  @Inject
  protected ActivityController activityController;
  protected Activity activity;

  @Before
  public void setUp() throws Exception {
    navigator = Navigator.registerWithBorderPane(JFXCDIRunner.getStage());

    NewInstanceDataSource<ActivityModel> dataSource = new NewInstanceDataSource<>(ActivityModel.class, this::save);
    activity = new Activity(dataSource, ActivityHome.class, activityController, navigator);
    activity.withLink(ActivityHome.class, "showDetails", Navigator.RIGHT_AREA, DetailController.class);
    activity.withLink(ActivityHome.class, "switchView", OtherController.class);
    activity.withLink(OtherController.class, "back", ActivityHome.class);
    activity.withTask(DetailController.class, "pressMe", ActivityAction.class);
  }

  public void save(ActivityModel model) {
    //
  }
}
