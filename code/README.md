# A Guide to the Source Code

Supplemental source code for "Revisiting the Formation of Harmonic Progressions from the Perspective of Voice-Leading with Evolutionary Computation."

`Version: 1.0.0`

## Prerequisite

* JDK 17+
* [Apache Maven](https://maven.apache.org/) 3.6.3+

## Build

```bash
mvn clean install
```

## Run with Default Setting

You can run with default parameters, six voices (SAATTB) in 17 bars:

```bash
mvn exec:java
```

The generated music file in `.musicxml` format can be found in `/data` folder and can be viewed with music notation softwares like [MuseScore](https://musescore.org/) or online viewers like [Open Sheet Music Display](https://opensheetmusicdisplay.github.io/demo/).

## Run with Specified Parameters

The following command will generate a 17-bar harmonic progression via six-voice (SAATTB) voice-leading:

```bash
mvn exec:java -Dexec.args="SAATTB 17"
```

