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
# Moodle username and base url
moodle.username=ha20210005
# set the password for ease of use
moodle.password=XXXXXX
moodle.url=https://elearning.fh-ooe.at/

# Email address and username of person who sends feedback emails
email.address=sXXXXXXXXXX@fhooe.at
email.username=sXXXXXXXXXX

# <students-id>@<email.students.suffix> => for receivers of emails
email.students.suffix=fhooe.at

# Base directory where downloaded files will be stored
location.basedir = ../

# Subdirectory for current exercise
# it's better to not set this property, you do not want to edit the config whenever you download something
# just type it when the command asks you
# location.exercise.subdir = e1

# Subdirectory where submissions and reviews will be stored
location.submissions.subdir = submissions
location.reviews.subdir = reviews

# this current setup directory would be like this would look like this
# ./basedir/exercise.subdir/submissions
# ./basedir/exercise.subdir/reviews
# the exercise.subdir would be replaced by whatever target dir you but into the command when it asks you

# Java language version used by JPlag for plagiarism detection (default is Java 1.9)
# remove / comment line below for java
plagiarism.language.java.version=c/c++

# Templates for Email use %s for template parameters, Umlaute i.e ü, ö, ä are not supported by the encoding
email.template.subject=Feedback zur Uebung %s
email.template.body=Hallo,\n\nanbei euer Feedback zur Uebung %s.\nBei Fragen koennt ihr mir gerne auf diese E-Mail antworten.\n\nLG\n XXX.
```

For Tutorbot to detect this file, it should be located in the same directory as the `tutorbot.jar` and should be called `tutorbot.properties`. It is also possible to configure those parameters using environment variables:

| Environment variable | Description |
| --- | --- |
| `TUTORBOT_USERNAME` | Moodle username |
| `TUTORBOT_LOCATION_SUBMISSIONS` | Download location for submissions |
| `TUTORBOT_LOCATION_REVIEWS` | Download location for reviews |
| `TUTORBOT_PLAGIARISM_LANGUAGE_JAVA_VERSION` | Java language version used by JPlag for plagiarism detection (default is Java 1.9) |

Values from the properties file will take precedence over values from environment variables if both are specified. 

### Using the tutorbot

The folder "tutorbot" contains everything needed to run the cli tool.

On Windows use the powershell script `tutorbot.ps1` followed by the command you want to execute. The following image show how to execute the `reviews` command. This uses the default `tutotrbot.properties` and creates the folder `../vz/exercise-01/reviews` containing the downloaded reviews and `../vz/exercise-01/submissions` containing the submission + the result of the plagiarism check.

![img.png](images/review-example.png)

If you are getting weird characters in your console, your terminal probably does not have support for ANSI Color Codes enabled. If you are on Windows, this can be fixed by using the `Windows Terminal` app or by using the `tutorbotWithoutWT.ps1` script (instead of `tutorbot.ps1`) which opens a powershell session with ANSI color codes enabled.
 

### Buildling this project

Tutorbot is built using Gradle. You don't need to install anything, as the Gradle wrapper is included in the repository. To build the project, simply execute `./gradlew jar`, the resulting JAR file will be located under `/build/libs/tutorbot.jar`. Please note that a JDK with version 11 or higher is required to build and run this tool, this limitation comes from JPlag. 
