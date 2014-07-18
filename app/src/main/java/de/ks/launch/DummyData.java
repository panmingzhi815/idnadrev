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
package de.ks.launch;

import de.ks.idnadrev.entity.*;
import de.ks.persistence.PersistentWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static de.ks.persistence.PersistentWork.persist;

public class DummyData extends Service {
  private static final Logger log = LoggerFactory.getLogger(DummyData.class);
  public static final String CREATE_DUMMYDATA = "create.dummydata";

  @Override
  public int getPriority() {
    return 2;
  }

  @Override
  protected void doStart() {
    if (Boolean.getBoolean(CREATE_DUMMYDATA)) {
      PersistentWork.deleteAllOf(Tag.class, WorkUnit.class, FileReference.class, Thought.class, Task.class, Context.class);
      ArrayList<Task> tasks = new ArrayList<>();

      log.info("Creating dummy data.");
      persist(new Thought("Go fishing").setDescription("on a nice lake"));
      persist(new Thought("Go hiking").setDescription("maybe the CDT"));

      Context hiking = new Context("Hiking");
      Task backpack = new Task("Build a new backpack", "DIY").setProject(true);
      backpack.setContext(hiking);
      tasks.add(backpack);

      Task sketch = new Task("Create a sketch").setDescription("sketchy\n\tsketchy");
      sketch.setEstimatedTime(Duration.ofMinutes(42));
      tasks.add(sketch);
      Task sew = new Task("Sew the backpack", "no hussle please");
      sew.setEstimatedTime(Duration.ofMinutes(60 * 3 + 32));
      tasks.add(sew);
      backpack.addChild(sketch);
      backpack.addChild(sew);

      Task task = new Task("Do some stuff").setContext(hiking).setEstimatedTime(Duration.ofMinutes(12));
      WorkUnit workUnit = new WorkUnit(task);
      workUnit.setStart(LocalDateTime.now().minus(5, ChronoUnit.MINUTES));
      workUnit.stop();
      tasks.add(task);
      Task asciiDocSample = new Task("AsciiDocSample", asciiDocString).setEstimatedTime(Duration.ofMinutes(1));
      asciiDocSample.getOutcome().setExpectedOutcome("= title\n\n== other\n");
      tasks.add(asciiDocSample);

      tasks.forEach((t) -> t.getPhysicalEffort().setAmount(ThreadLocalRandom.current().nextInt(0, 10)));
      tasks.forEach((t) -> t.getMentalEffort().setAmount(ThreadLocalRandom.current().nextInt(0, 10)));
      tasks.forEach((t) -> t.getFunFactor().setAmount(ThreadLocalRandom.current().nextInt(-5, 5)));

      persist(hiking, backpack, sketch, sew, task, workUnit, asciiDocSample);
      persist(new Context("Work"), new Context("Studying"), new Context("Music"));

    }
  }

  @Override
  protected void doStop() {

  }

  private static final String asciiDocString = "The Article Title\n" +
          "=================\n" +
          "Author's Name <authors@email.address>\n" +
          "v $version, 2003-12\n" +
          "\n" +
          ":toc:\n" +
          "\n" +
          "This is the optional preamble (an untitled section body). Useful for\n" +
          "writing simple sectionless documents consisting only of a preamble.\n" +
          "\n" +
          "NOTE: The abstract, preface, appendix, bibliography, glossary and\n" +
          "index section titles are significant ('specialsections').\n" +
          "\n" +
          "\n" +
          ":numbered!:\n" +
          "[abstract]\n" +
          "Example Abstract\n" +
          "----------------\n" +
          "The optional abstract (one or more paragraphs) goes here.\n" +
          "\n" +
          "This document is an AsciiDoc article skeleton containing briefly\n" +
          "annotated element placeholders plus a couple of example index entries\n" +
          "and footnotes.\n" +
          "\n" +
          ":numbered:\n" +
          "\n" +
          "The First Section\n" +
          "-----------------\n" +
          "Article sections start at level 1 and can be nested up to four levels\n" +
          "deep.\n" +
          "footnote:[An example footnote.]\n" +
          "indexterm:[Example index entry]\n" +
          "\n" +
          "And now for something completely different: ((monkeys)), lions and\n" +
          "tigers (Bengal and Siberian) using the alternative syntax index\n" +
          "entries.\n" +
          "(((Big cats,Lions)))\n" +
          "(((Big cats,Tigers,Bengal Tiger)))\n" +
          "(((Big cats,Tigers,Siberian Tiger)))\n" +
          "Note that multi-entry terms generate separate index entries.\n" +
          "\n" +
          "Here are a couple of image examples: an image:images/smallnew.png[]\n" +
          "example inline image followed by an example block image:\n" +
          "\n" +
          ".Tiger block image\n" +
          "image::images/tiger.png[Tiger image]\n" +
          "\n" +
          "Followed by an example table:\n" +
          "\n" +
          ".An example table\n" +
          "[width=\"60%\",options=\"header\"]\n" +
          "|==============================================\n" +
          "| Option          | Description\n" +
          "| -a 'USER GROUP' | Add 'USER' to 'GROUP'.\n" +
          "| -R 'GROUP'      | Disables access to 'GROUP'.\n" +
          "|==============================================\n" +
          "\n" +
          ".An example example\n" +
          "===============================================\n" +
          "Lorum ipum...\n" +
          "===============================================\n" +
          "\n" +
          "[[X1]]\n" +
          "Sub-section with Anchor\n" +
          "~~~~~~~~~~~~~~~~~~~~~~~\n" +
          "Sub-section at level 2.\n" +
          "\n" +
          "[source,java]\n" +
          "----\n" +
          "@MenuItem(\"/main/help\")\n" +
          "public class About implements NodeProvider<StackPane> {\n" +
          "  private static final Logger log = LoggerFactory.getLogger(About.class);\n" +
          "\n" +
          "  @Override\n" +
          "  public StackPane getNode() {\n" +
          "    return new DefaultLoader<StackPane, Object>(getClass().getResource(\"about.fxml\")).getView();\n" +
          "  }\n" +
          "}\n" +
          "\n" +
          "----" +
          "\n" +
          "+++\n" +
          "$$x = (-b +- sqrt(b^2-4ac))/(2a)$$\n" +
          "+++";
}
