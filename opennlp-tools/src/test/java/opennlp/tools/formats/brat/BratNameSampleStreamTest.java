/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.formats.brat;

import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import opennlp.tools.namefind.NameSample;
import opennlp.tools.sentdetect.NewlineSentenceDetector;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;

public class BratNameSampleStreamTest extends AbstractBratTest {

  @BeforeEach
  public void setup() throws IOException {
    super.setup();
  }

  @Test
  void readNoOverlap() throws IOException {
    BratNameSampleStream stream = createNameSampleWith("-entities.",
        null);
    int count = 0;
    NameSample sample = stream.read();
    while (sample != null) {
      count++;
      sample = stream.read();
    }

    Assertions.assertEquals(8, count);
  }

  @Test
  void readOverlapFail() {
    Assertions.assertThrows(RuntimeException.class, () -> {
      BratNameSampleStream stream = createNameSampleWith("overlapping",
          null);

      NameSample sample = stream.read();
      while (sample != null) {
        sample = stream.read();
        Assertions.assertNotNull(sample);
      }
    });

  }

  @Test
  void emptySample() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> createNameSampleWith("overlapping",
        Collections.emptySet()));
  }

  @Test
  void readOverlapFilter() throws IOException {
    BratNameSampleStream stream = createNameSampleWith("overlapping",
        Collections.singleton("Person"));
    int count = 0;
    NameSample sample = stream.read();
    while (sample != null) {
      count++;
      sample = stream.read();
    }

    Assertions.assertEquals(8, count);
  }

  private BratNameSampleStream createNameSampleWith(String nameContainsFilter,
                                                    Set<String> nameTypes) throws IOException {
    AnnotationConfiguration config = new AnnotationConfiguration(typeToClassMap);
    FileFilter fileFilter = pathname -> pathname.getName().contains(nameContainsFilter);

    ObjectStream<BratDocument> bratDocumentStream =
            new BratDocumentStream(config, directory, false, fileFilter);

    return new BratNameSampleStream(new NewlineSentenceDetector(),
            WhitespaceTokenizer.INSTANCE, bratDocumentStream, nameTypes);
  }
}
