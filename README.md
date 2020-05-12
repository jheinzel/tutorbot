[![Build Status](https://img.shields.io/github/workflow/status/simonschiller/tutorbot/CI)](https://github.com/simonschiller/tutorbot/actions) 
[![GitHub Release](https://img.shields.io/github/v/release/simonschiller/tutorbot)](https://github.com/simonschiller/tutorbot/releases)
[![License](https://img.shields.io/github/license/simonschiller/tutorbot)](https://github.com/simonschiller/tutorbot/blob/master/LICENSE)

# Tutorbot

Tutorbot is a simple command line tool that helps programming tutors at the University of Applied Sciences in Hagenberg by automating repetitive tasks. 

### Features

Tutorbot comes with a range of different features, it can support you by:

* downloading (and extracting) all submissions for a certain exercise
* checking submissions for plagiarism
* downloading all reviews for a certain exercise
* sending feedback emails to students 

### Configuration

Tutorbot requires different user inputs, some of them are likely repetitive. To avoid repeating them every time, these inputs can be stored in a configuration file. Currently the configuration file supports the following values:

```properties
# Moodle username
username=Sxxxxxxxxxx

# Download location for submissions
location.submissions=/foo/bar/submissions

# Download location for reviews
location.reviews=/foo/bar/reviews

# Java language version used by JPlag for plagiarism detection (default is Java 1.9)
plagiarism.language.java.version=java19
```

For Tutorbot to detect this file, it should be located in the same directory as the `tutorbot.jar` and should be called `tutorbot.properties`. It is also possible to configure those parameters using environment variables:

| Key | Description |
| --- | --- |
| `TUTORBOT_USERNAME` | Moodle username |
| `TUTORBOT_LOCATION_SUBMISSIONS` | Download location for submissions |
| `TUTORBOT_LOCATION_REVIEWS` | Download location for reviews |
| `TUTORBOT_PLAGIARISM_LANGUAGE_JAVA_VERSION` | Java language version used by JPlag for plagiarism detection (default is Java 1.9) |

Values from the properties file will take precedence over values from environment variables if both are specified. 

### Buildling this project

Tutorbot is built using Gradle. You don't need to install anything, as the Gradle wrapper is included in the repository. To build the project, simply execute `./gradlew jar`, the resulting JAR file will be located under `/build/libs/tutorbot.jar`. Please note that a JDK with version 11 or higher is required to build and run this tool, this limitation comes from JPlag. 
