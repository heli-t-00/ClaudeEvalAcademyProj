ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "ClaudeEvalAcademyProj",
    libraryDependencies ++= Seq(
      "io.cucumber"             % "cucumber-java"          % "7.20.1"  % Test,
      "io.cucumber"             % "cucumber-junit"         % "7.20.1"  % Test,
      "io.cucumber"             % "cucumber-picocontainer" % "7.20.1"  % Test,
      "junit"                   % "junit"                  % "4.13.2"  % Test,
      "com.github.sbt"          % "junit-interface"        % "0.13.3"  % Test,
      "org.seleniumhq.selenium" % "selenium-java"          % "4.27.0"  % Test,
      "io.github.bonigarcia"    % "webdrivermanager"       % "5.9.2"   % Test,
    ),
    Test / fork              := true,
    Test / parallelExecution := false,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v"),
  )
