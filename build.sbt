// See LICENSE for license details.

import microsites.{MicrositeFavicon, ExtraMdFileConfig}

import Version._

val commonSettings = Seq.empty

lazy val chisel = (project in file("chisel3"))
  .settings(commonSettings)
  .settings(scalacOptions ++= docSourceUrl("chisel3"))

lazy val testers = (project in file("chisel-testers"))
  .settings(commonSettings)
  .settings(scalacOptions ++= docSourceUrl("chisel-testers"))

fork := true

val technologies: String =
  """|
     | - first: ["Scala", "Chisel is powered by Scala and brings all the power of object-oriented and functional programming to type-safe hardware design and generation."]
     | - second: ["Chisel", "Chisel, the Chisel standard library, and Chisel testing infrastructure enable agile, expressive, and reusable hardware design methodologies."]
     | - third: ["FIRRTL", "The FIRRTL circuit compiler starts after Chisel and enables backend (FPGA, ASIC, technology) specialization, automated circuit transformation, and Verilog generation."]
     |""".stripMargin

val determineContributors = taskKey[Unit]("determine contributors for subprojects")

lazy val micrositeSettings = Seq(
  scalaVersion := "2.12.6",
  micrositeName := "Chisel/FIRRTL",
  micrositeDescription := "Chisel/FIRRTL\nHardware Compiler Framework",
  micrositeUrl := "https://www.chisel-lang.org",
  micrositeBaseUrl := "",
  micrositeAuthor := "the Chisel/FIRRTL Developers",
  micrositeTwitter := "@chisel_lang",
  micrositeGithubOwner := "freechipsproject",
  micrositeGithubRepo := "chisel3",
  micrositeGithubLinks := false,
  micrositeShareOnSocial := false,
  micrositeDocumentationUrl := "api/chisel3/latest/",
  micrositeDocumentationLabelDescription := "API Documentation",
  /* mdoc doesn't work with extraMDFiles so this is disabled for now */
  // micrositeCompilingDocsTool := WithMdoc,
  // mdocIn := file("docs/src/main/mdoc"),
  /* Copy markdown files from each of the submodules to build out the website:
   * - Chisel3 README becomes the landing page
   * - Other READMEs become the landing pages of each sub-project's documentation
   */
  micrositeExtraMdFiles := Map(
    file("chisel3/README.md") -> ExtraMdFileConfig(
      "index.md", "home",
      Map("title" -> "Home",
          "section" -> "home",
          "technologies" -> technologies)),
    file("chisel-testers/README.md") -> ExtraMdFileConfig(
      "chisel-testers/index.md", "docs",
      Map("title" -> "Testers",
          "section" -> "chisel-testers",
          "position" -> "2")),
    file("firrtl/README.md") -> ExtraMdFileConfig(
      "firrtl/index.md", "docs",
      Map("title" -> "FIRRTL",
          "section" -> "firrtl",
          "position" -> "3")),
    file("treadle/README.md") -> ExtraMdFileConfig(
      "treadle/index.md", "docs",
      Map("title" -> "Treadle",
          "section" -> "treadle",
          "position" -> "4")),
    file("diagrammer/README.md") -> ExtraMdFileConfig(
      "diagrammer/index.md", "docs",
      Map("title" -> "Diagrammer",
          "section" -> "diagrammer",
          "position" -> "5"))
  ),
  micrositeStaticDirectory := file("docs/target/site/api"),
  /* Known colors:
   *   - Chisel logo: #212560
   *   - FIRRTL logo: #136527
   */
  micrositePalette := Map(
    "brand-primary"     -> "#7B95A2",
    "brand-secondary"   -> "#1A3C79",
    "brand-tertiary"    -> "#1A1C54",
    "gray-dark"         -> "#453E46",
    "gray"              -> "#837F84",
    "gray-light"        -> "#E3E2E3",
    "gray-lighter"      -> "#F4F3F4",
    "white-color"       -> "#FFFFFF"),
  micrositeAnalyticsToken := "UA-145179088-1",
  autoAPIMappings := true,
  ghpagesNoJekyll := false,
  scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
    "-groups",
    "-sourcepath",
    baseDirectory.in(LocalRootProject).value.getAbsolutePath,
    "-diagrams"
  ),
  ghpagesRepository := file("build/gh-pages"),
  ghpagesBranch := "gh-pages",
  git.remoteRepo := "git@github.com:freechipsproject/www.chisel-lang.org.git",
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md" |
    "*.svg" | "*.woff" | "*.ttf",
  includeFilter in Jekyll := (includeFilter in makeSite).value,
  excludeFilter in ghpagesCleanSite :=
    new FileFilter{
      def accept(f: File) = (ghpagesRepository.value / "CNAME").getCanonicalPath == f.getCanonicalPath
    } || "versions.html"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val contributors =
  project
    .settings(
      determineContributors := {
        import java.io.{File, PrintWriter}
        val uniqueContributors =
          Seq( GitHubRepository("ucb-bar", "chisel2-deprecated"),
               GitHubRepository("freechipsproject", "chisel3"),
               GitHubRepository("freechipsproject", "firrtl"),
               GitHubRepository("freechipsproject", "chisel-testers"),
               GitHubRepository("freechipsproject", "treadle"),
               GitHubRepository("freechipsproject", "diagrammer"),
               GitHubRepository("freechipsproject", "www.chisel-lang.org") )
            .map(Contributors.contributors)
            .reduce(_ ++ _)
            .distinct
        val writer = new PrintWriter(new File("docs/src/main/tut/contributors.md"))
        writer.write(s"""|<!-- Automatically generated by build.sbt 'contributors' task -->
                         |${Contributors.contributorsMarkdown(uniqueContributors)}""".stripMargin)
        writer.close()
      }
    )

lazy val docs = project
  .enablePlugins(MicrositesPlugin)
  .settings(commonSettings)
  .settings(micrositeSettings)
  .settings(scalacOptions ++= (Seq("-Xsource:2.11")))
  .dependsOn(chisel, contributors)
