[![Build Status](https://img.shields.io/github/workflow/status/simonschiller/tutorbot/CI)](https://github.com/simonschiller/tutorbot/actions)
[![GitHub Release](https://img.shields.io/github/v/release/simonschiller/tutorbot)](https://github.com/simonschiller/tutorbot/releases)
[![License](https://img.shields.io/github/license/simonschiller/tutorbot)](https://github.com/simonschiller/tutorbot/blob/master/LICENSE)

# Tutorbot

Tutorbot is a simple command line tool that helps programming tutors at the University of Applied Sciences in Hagenberg
by automating repetitive tasks.

### Features

Tutorbot comes with a range of different features, it can support you by:

* downloading (and extracting) all submissions for a certain exercise
* checking submissions for plagiarism
* downloading all reviews for a certain exercise
* sending feedback emails to students

### Configuration

Tutorbot requires different user inputs, some of them are likely repetitive. To avoid repeating them every time, these
inputs can be stored in a configuration file. Currently the configuration file supports the following values:

```properties
# Moodle username (only works for local accounts since mandatory 2FA)
moodle.username=ha20210005
# Set the password for ease of use
moodle.password=XXXXXX
moodle.url=https://elearning.fh-ooe.at/
# Uncomment to authorize with the MoodleSessionlmsfhooe cookie 
# Instead of local username and password
# moodle.auth.method=cookie 
# Email address and username of person who sends feedback emails
email.address=sXXXXXXXXXX@fhooe.at
email.username=sXXXXXXXXXX
email.password=XXXXXX
# <students-id>@<email.students.suffix> => for receivers of emails
email.students.suffix=fhooe.at
# Base directory where exercise folders will be stored
location.basedir=../
# Subdirectory for current exercise
# it's better to not set this property, you do not want to edit the config whenever you download something
# just type it when the command asks you
# location.exercise.subdir = e1
# Subdirectory where submissions and reviews will be stored
location.submissions.subdir=submissions
location.reviews.subdir=reviews
# This current setup directory would be like this would look like this
# ./basedir/exercise.subdir/submissions
# ./basedir/exercise.subdir/reviews
# The exercise.subdir would be replaced by whatever target dir you put into the command when it asks you
# Java language version used by JPlag for plagiarism detection (default is Java 1.9)
# remove / comment line below for java
plagiarism.language.java.version=c/c++
# Templates for Email use %s for template parameters, Umlaute i.e ü, ö, ä are not supported by the encoding
email.template.subject=Feedback zur Uebung %s
email.template.body=Hallo,\n\nanbei euer Feedback zur Uebung %s.\nBei Fragen koennt ihr mir gerne auf diese E-Mail antworten.\n\nLG\n XXX.
```

For Tutorbot to detect this file, it should be located in the same directory as the `tutorbot.jar` and should be
called `tutorbot.properties`. It is also possible to configure those parameters using environment variables:

| Environment variable                        | Description                                                                                                                            |
|---------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `TUTORBOT_MOODLE_USERNAME`                  | Moodle username (only works for local accounts since mandatory 2FA)                                                                    |
| `TUTORBOT_MOODLE_PASSWORD`                  | Moodle password                                                                                                                        |
| `TUTORBOT_MOODLE_URL`                       | Moodle base url (default https://elearning.fh-ooe.at/)                                                                                 |
| `TUTORBOT_MOODLE_AUTH_METHOD`               | Moodle authorization method used for downloading. Can optionally be set to "cookie". Default authorization uses username and password. |
| `TUTORBOT_EMAIL_ADDRESS`                    | E-Mail address the feedback will be sent from                                                                                          |
| `TUTORBOT_EMAIL_USERNAME`                   | Username for the E-Mail service                                                                                                        |
| `TUTORBOT_EMAIL_PASSWORD`                   | Password for the E-Mail service                                                                                                        |
| `TUTORBOT_EMAIL_TEMPLATE_SUBJECT`           | Template string for the subject of E-Mails sent to students. %s is used as placeholder.                                                |
| `TUTORBOT_EMAIL_TEMPLATE_BODY`              | Template string for the body of E-Mails sent to students. %s is used as placeholder.                                                   |
| `TUTORBOT_EMAIL_STUDENTS_SUFFIX`            | Suffix of E-Mail addresses of students (default fhooe.at)                                                                              |
| `TUTORBOT_LOCATION_BASEDIR`                 | Base-location where Tutorbot will create other folders                                                                                 |
| `TUTORBOT_LOCATION_EXERCISE_SUBDIR`         | Subfolder of basedir for the exercise                                                                                                  |
| `TUTORBOT_LOCATION_SUBMISSIONS_SUBDIR`      | Subfolder of exercise as download location for submissions                                                                             |
| `TUTORBOT_LOCATION_REVIEWS_SUBDIR`          | Subfolder of exercise as download location for reviews                                                                                 |
| `TUTORBOT_PLAGIARISM_LANGUAGE_JAVA_VERSION` | Java language version used by JPlag for plagiarism detection (default is Java 1.9). Also supports C/C++.                               |

Values from the properties file will take precedence over values from environment variables if both are specified.

### Using the tutorbot

The folder "tutorbot" contains everything needed to run the cli tool.

On Windows use the powershell script `tutorbot.ps1` followed by the command you want to execute. The following image
show how to execute the `reviews` command. This uses the default `tutotrbot.properties` and creates the
folder `../vz/exercise-01/reviews` containing the downloaded reviews and `../vz/exercise-01/submissions` containing the
submission + the result of the plagiarism check.

![img.png](images/review-example.png)

If you are getting weird characters in your console, your terminal probably does not have support for ANSI Color Codes
enabled. If you are on Windows, this can be fixed by using the `Windows Terminal` app or by using
the `tutorbotWithoutWT.ps1` script (instead of `tutorbot.ps1`) which opens a powershell session with ANSI color codes
enabled.

#### Authorization with cookie
Since moodle has a strict OIDC authorization for students' accounts, a simple login with username and password is not possible anymore. 
Current workarounds include:
* Using a local moodle account, which is added to the courses.
* Authorizing with a student account using a copied cookie from a browser.

To perform the latter a few steps have to be taken:
* Enabling `moodle.auth.method=cookie` in the properties.
* Logging in moodle using any browser.
* Copying the value of the `MoodleSessionlmsfhooe` Cookie after successful login.

After this, any download command of tutorbot can be performed using the cookie value. If this did not work,
check if the cookie value changed after a browser refresh.

### Buildling this project

Tutorbot is built using Gradle. You don't need to install anything, as the Gradle wrapper is included in the repository.
To build the project, simply execute `./gradlew jar`, the resulting JAR file will be located
under `/build/libs/tutorbot.jar`. Please note that a JDK with version 11 or higher is required to build and run this
tool, this limitation comes from JPlag. 
