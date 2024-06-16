/*
 * Copyright 2024 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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
package art.cctcc.music;

import static art.cctcc.music.Settings.*;
import static art.cctcc.music.ga.Evaluation.*;
import art.cctcc.music.ga.Explorer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Main {

  public static void main(String... args) throws IOException {

    var voice = VOICE;
    var chord_no = CHORD_NO;
    var batch = 1;

    if (args.length > 0) {
      System.out.println("args = " + Arrays.toString(args));

      if (args.length < 2 || !args[0].matches("[SATB]+|-") || !args[1].matches("[0-9]+|-"))
        printHelp();

      voice = args[0].equals("-") ? VOICE : args[0];
      chord_no = args[1].equals("-") ? CHORD_NO : Integer.parseInt(args[1]);

      if (args.length > 2 && args[2].matches("[0-9]+")) batch = Integer.parseInt(args[2]);
    }

    for (int i = 0; i < batch; i++) {
      if (batch > 1) {
        System.out.println("\n" + "*".repeat(40));
        System.out.println("*** Explorer #" + i);
        System.out.println("*".repeat(40));
      }
      var explorer =
          new Explorer(
              voice,
              chord_no,
              1200,
              0.25,
              0.10,
              0.75,
              0.10,
              3,
              voice.length(),
              List.of(
                  MelodicSmoothness,
                  VoiceIndependence,
                  ImproperOuterVoices,
                  NotTriadOrSeventhChord,
                  SuccessiveDissonantChords,
                  ImproperResolution,
                  StartWithNonTriad,
                  ImproperCadentialForm),
              0.98,
              1200);
      explorer.start();
      explorer.saveScore();
      explorer.saveData();
    }
  }

  private static void printHelp() {

    System.out.printf(
        """
        Args: Voices Chords
        Ex: "%s %d" -> %dv; %d chords (default)
        """,
        VOICE, CHORD_NO, VOICE.length(), CHORD_NO);
    System.exit(0);
  }
}
