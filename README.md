veraPDF-parser
==============
*Greenfield PDF parser developed for veraPDF*

[![Build Status](https://jenkins.openpreservation.org/job/veraPDF/job/1.25/job/parser/badge/icon)](https://jenkins.openpreservation.org/job/veraPDF/job/1.23/job/parser/ "OPF Jenkins")
[![Maven Central](https://img.shields.io/maven-central/v/org.verapdf/parser.svg)](https://repo1.maven.org/maven2/org/verapdf/parser/ "Maven central")
[![CodeCov Coverage](https://img.shields.io/codecov/c/github/veraPDF/veraPDF-parser.svg)](https://codecov.io/gh/veraPDF/veraPDF-parser/ "CodeCov coverage")
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6285400847ba461d8d5e331ffca08bff)](https://app.codacy.com/gh/veraPDF/veraPDF-parser/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade "Codacy coverage")

[![GitHub issues](https://img.shields.io/github/issues/veraPDF/veraPDF-library.svg)](https://github.com/veraPDF/veraPDF-library/issues "Open issues on GitHub")
[![GitHub issues](https://img.shields.io/github/issues-closed/veraPDF/veraPDF-library.svg)](https://github.com/veraPDF/veraPDF-library/issues?q=is%3Aissue+is%3Aclosed "Closed issues on GitHub")
[![GitHub issues](https://img.shields.io/github/issues-pr/veraPDF/veraPDF-parser.svg)](https://github.com/veraPDF/veraPDF-parser/pulls "Open pull requests on GitHub")
[![GitHub issues](https://img.shields.io/github/issues-pr-closed/veraPDF/veraPDF-parser.svg)](https://github.com/veraPDF/veraPDF-parser/pulls?q=is%3Apr+is%3Aclosed "Closed pull requests on GitHub")

Licensing
---------
The veraPDF Parser is dual-licensed, see:

 - [GPLv3+](LICENSE.GPL "GNU General Public License, version 3")
 - [MPLv2+](LICENSE.MPL "Mozilla Public License, version 2.0")

Documentation
-------------
See the [veraPDF documentation site](https://docs.verapdf.org/).

Quick Start
-----------
### Pre-requisites

In order to build the parser you'll need:

 * Java 8 - 17, which can be downloaded [from Oracle](https://www.oracle.com/technetwork/java/javase/downloads/index.html), or for Linux users [OpenJDK](http://openjdk.java.net/install/index.html).
 * [Maven v3+](https://maven.apache.org/)

### Building the veraPDF Parser

 1. Download the veraPDF-model repository, either: `git clone https://github.com/veraPDF/veraPDF-parser`
 or download the [latest tar archive](https://github.com/veraPDF/veraPDF-parser/archive/integration.tar.gz "veraPDF-parser latest GitHub tar archive") or [zip equivalent](https://github.com/veraPDF/veraPDF-parser/archive/integration.zip "veraPDF-parser latest GitHub zip archive") from GitHub.
 2. Move to the downloaded project directory, e.g. `cd veraPDF-parser`
 3. Build and install using Maven: `mvn clean install`
