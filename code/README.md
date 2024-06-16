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

You can run without any parameters by

```bash
mvn exec:java
```

The generated music files in `.musicxml` format can be found in `/data` folder and can be viewed with music notation softwares like [MuseScore](https://musescore.org/) or online viewers like [Open Sheet Music Display](https://opensheetmusicdisplay.github.io/demo/).

## Run with Specified Parameters

The following command will produce 17 bars harmonic progressions with six voice (SAATTB):

```bash
mvn exec:java -Dexec.args="SAATTB 17"
```

