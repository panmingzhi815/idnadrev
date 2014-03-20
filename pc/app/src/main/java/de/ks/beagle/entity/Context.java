/*
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

package de.ks.beagle.entity;

import de.ks.persistence.entity.NamedPersistentObject;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Context extends NamedPersistentObject<Context> {
  private static final long serialVersionUID = 1L;

  @OneToMany(mappedBy = "context", fetch = FetchType.LAZY)
  protected Set<Task> tasks = new HashSet<>();

  public Context() {
  }

  public Context(String name) {
    super(name);
  }

  public Set<Task> getTasks() {
    return tasks;
  }

}
