import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.7.0"
  val commonDomainVersion      = "0.18.0"


  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30"   % bootstrapVersion,
    "uk.gov.hmrc" %% "api-platform-common-domain"   % commonDomainVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30"   % "11.11.0",
    "uk.gov.hmrc" %% "internal-auth-client-play-30" % "3.0.0",
    "commons-validator"  % "commons-validator"          % "1.7"

  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30"              % bootstrapVersion,
    "org.jsoup"    % "jsoup"                               % "1.18.3",
    "uk.gov.hmrc" %% "api-platform-common-domain-fixtures" % commonDomainVersion
  ).map(_ % "test")

  val it = Seq.empty
}
