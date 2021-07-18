/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.plugins.exporters;

import static org.junit.Assert.assertTrue;


import com.igormaznitsa.mindmap.model.MindMap;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class MindmupExporterTest extends AbstractStandardExporterTest<MindmupExporter> {

  @Test
  public void testNoExceptionsAndResultPresented() throws Exception {
    final MindMap map = new MindMap(true);
    map.getRoot().setText("Hello World!");
    final String text = new String(export(map, null), StandardCharsets.UTF_8);
    System.out.print("JSON\n----------------\n" + text + "\n----------------\n");
    assertTrue(text.contains("Hello World!"));
  }

  @Override
  public MindmupExporter generateExporterInstance() {
    return new MindmupExporter();
  }
}
