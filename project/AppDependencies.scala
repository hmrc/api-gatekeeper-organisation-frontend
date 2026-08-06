import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"
  private val orgDomainVersion = "1.4.0"
  private val tpdDomainVersion = "1.0.0"
  private val tpaDomainVersion = "1.2.0"
  private val hmrcMongoVersion = "2.13.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"       %% "api-platform-organisation-domain" % orgDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-tpd-domain"          % tpdDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-application-domain"  % tpaDomainVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"       % "12.32.0",
    "uk.gov.hmrc"       %% "internal-auth-client-play-30"     % "4.4.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"               % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"                    % bootstrapVersion,
    "org.mockito"       %% "mockito-scala-scalatest"                   % "2.2.1",
    "org.jsoup"          % "jsoup"                                     % "1.22.1",
    "uk.gov.hmrc"       %% "api-platform-organisation-domain-fixtures" % orgDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-application-domain-fixtures" % tpaDomainVersion,
    "uk.gov.hmrc"       %% "api-platform-test-tpd-domain"              % tpdDomainVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"                   % hmrcMongoVersion
  ).map(_ % "test")

  val it = Seq.empty
}
