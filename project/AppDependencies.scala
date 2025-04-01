import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.11.0"
  private val orgDomainVersion = "0.6.0"

  val compile = Seq(
    "uk.gov.hmrc"      %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"      %% "api-platform-organisation-domain" % orgDomainVersion,
    "uk.gov.hmrc"      %% "play-frontend-hmrc-play-30"       % "11.13.0",
    "uk.gov.hmrc"      %% "internal-auth-client-play-30"     % "3.1.0",
    "commons-validator" % "commons-validator"                % "1.7"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30"                    % bootstrapVersion,
    "org.jsoup"    % "jsoup"                                     % "1.18.3",
    "uk.gov.hmrc" %% "api-platform-organisation-domain-fixtures" % orgDomainVersion
  ).map(_ % "test")

  val it = Seq.empty
}
