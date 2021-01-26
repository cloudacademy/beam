/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.examples;

import org.apache.beam.examples.common.ExampleUtils;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;


/**
 * An example that counts words in Shakespeare.
 *
 * <p>This class, {@link MinimalWordCount}, is the first in a series of four successively more
 * detailed 'word count' examples. Here, for simplicity, we don't show any error-checking, 
 * and focus on construction of the pipeline, which chains together the application of core
 * transforms.
 *
 * <p>Next, see the {@link WordCount} pipeline, then the {@link DebuggingWordCount}, and finally the
 * {@link WindowedWordCount} pipeline, for more detailed examples that introduce additional
 * concepts.
 *
 * <p>Concepts:
 *
 * <pre>
 *   1. Reading data from text files
 *   2. Specifying 'inline' transforms
 *   3. Counting items in a PCollection
 *   4. Writing data to text files
 * </pre>
 *
 */
public class MinimalWordCount {

  public interface WordCountOptions extends PipelineOptions {

    /**
     * By default, this example reads from a public dataset containing the text of
     * King Lear. Set this option to choose a different input file or glob.
     */
    @Description("Path of the file to read from")
    @Default.String("gs://apache-beam-samples/shakespeare/kinglear.txt")
    String getInputFile();
    void setInputFile(String value);

    /**
     * Set this required option to specify where to write the output.
     */
    @Description("Path of the file to write to")
    @Required
    String getOutput();
    void setOutput(String value);
  }

  public static void main(String[] args) {
    // Create a PipelineOptions object. This object lets us set various execution
    // options for our pipeline, such as the runner you wish to use. This example
    // will run with the DirectRunner by default, based on the class path configured
    // in its dependencies.
    WordCountOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
			  .as(WordCountOptions.class);

    // Create the Pipeline object with the options we defined above.
    Pipeline p = Pipeline.create(options);

    // Apply the pipeline's transforms.

    // Concept #1: Apply a root transform to the pipeline; in this case, TextIO.Read to read a set
    // of input text files. TextIO.Read returns a PCollection where each element is one line from
    // the input text.

    p.apply(TextIO.read().from(options.getInputFile()))

     // Concept #2: Apply a ParDo transform to our PCollection of text lines. This ParDo invokes a
     // DoFn (defined in-line) on each element that tokenizes the text line into individual words.
     // The ParDo returns a PCollection<String>, where each element is an individual word in
     // the input text.
     .apply("ExtractWords", ParDo.of(new DoFn<String, String>() {
                       @ProcessElement
                       public void processElement(ProcessContext c) {
                         for (String word : c.element().split(ExampleUtils.TOKENIZER_PATTERN)) {
                           if (!word.isEmpty()) {
                             c.output(word);
                           }
                         }
                       }
                     }))

     // Concept #3: Apply the Count transform to our PCollection of individual words. The Count
     // transform returns a new PCollection of key/value pairs, where each key represents a unique
     // word in the text. The associated value is the occurrence count for that word.
     .apply(Count.<String>perElement())

     // Apply a MapElements transform that formats our PCollection of word counts into a printable
     // string, suitable for writing to an output file.
     .apply("FormatResults", MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
                       @Override
                       public String apply(KV<String, Long> input) {
                         return input.getKey() + ": " + input.getValue();
                       }
                     }))

     // Concept #4: Apply a write transform, TextIO.Write, at the end of the pipeline.
     // TextIO.Write writes the contents of a PCollection (in this case, our PCollection of
     // formatted strings) to a series of text files.
     //
     // By default, it will write to a set of files with names like wordcount-00001-of-00005
     .apply(TextIO.write().to(options.getOutput()));

    // Run the pipeline.
    p.run().waitUntilFinish();
  }
}
