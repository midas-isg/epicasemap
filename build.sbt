import com.typesafe.config._

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

name := conf.getString("app.name")

version := conf.getString("app.version")

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  // javaJdbc,
  // javaEbean,
  // cache,
  // javaWs

  "com.wordnik" %% "swagger-play2" % "1.3.12",
  "com.typesafe.play" %% "play-java-ws" % "2.3.9",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.7.Final",
  javaJpa,
  "org.apache.commons" % "commons-csv" % "1.1"
)
