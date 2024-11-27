# Plasma CLI

![Maven Central Version](https://img.shields.io/maven-central/v/org.plasmalabs/plasma-cli_2.13?link=https%3A%2F%2Fs01.oss.sonatype.org%2F%23nexus-search%3Bgav~org.plasmalabs~plasma-cli_2.13~~~~kw%2Cversionexpand)

The Plasma CLI is a command line interface to the Plasma platform. 

It is a simple tool to interact with the Plasma platform.

The documentation is in progress and will be linked soon.


### For developers

How to run Integration test:

Run your desired version of the node in your local, [plasma-node/releases](https://github.com/PlasmaLaboratories/plasma-node/releases)

> docker run -it -p 9084:9084  ghcr.io/plasmalaboratories/plasma-node:x.y.x

```sbtShell
> sbt:plasma-cli-umbrella> cliIt/test
> [info] Passed: Total 25, Failed 0, Errors 0, Passed 25
> [success] Total time: 482 s (08:02), completed Nov 26, 2024, 11:44:44 AM
```