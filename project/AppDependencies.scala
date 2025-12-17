import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.4.0"
  private val orgDomainVersion = "0.11.0"
  private val hmrcMongoVersion = "2.11.0"

  val compile = Seq(
    "uk.gov.hmrc"      %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"      %% "api-platform-organisation-domain" % orgDomainVersion,
    "uk.gov.hmrc"      %% "play-frontend-hmrc-play-30"       % "12.22.0",
    "uk.gov.hmrc"      %% "internal-auth-client-play-30"     % "3.1.0",
    "commons-validator" % "commons-validator"                % "1.7",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"               % hmrcMongoVersion

  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30"                    % bootstrapVersion,
    "org.jsoup"    % "jsoup"                                     % "1.18.3",
    "uk.gov.hmrc" %% "api-platform-organisation-domain-fixtures" % orgDomainVersion,
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"                   % hmrcMongoVersion
  ).map(_ % "test")

  val it = Seq.empty
}
