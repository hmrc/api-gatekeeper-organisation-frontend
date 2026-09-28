/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apigatekeeperorganisationfrontend

import java.time.temporal.ChronoUnit

import uk.gov.hmrc.apiplatform.modules.applications.core.domain.models.{ApplicationWithCollaborators, ApplicationWithCollaboratorsFixtures}
import uk.gov.hmrc.apiplatform.modules.common.domain.models.{OrganisationId, UserId}
import uk.gov.hmrc.apiplatform.modules.common.utils.FixedClock
import uk.gov.hmrc.apiplatform.modules.organisations.domain.models.{Collaborator, Collaborators, Organisation, OrganisationAddress, OrganisationName}
import uk.gov.hmrc.apiplatform.modules.tpd.core.domain.models.User
import uk.gov.hmrc.apiplatform.modules.tpd.core.dto.RegisteredOrUnregisteredUser
import uk.gov.hmrc.apiplatform.modules.tpd.test.data.UserTestData
import uk.gov.hmrc.apiplatform.modules.tpd.test.utils.LocalUserIdTracker
import uk.gov.hmrc.apigatekeeperorganisationfrontend.models.OrganisationWithAllMembersDetailsAndApplications

object OrganisationIdData {
  val one: OrganisationId = OrganisationId.random
}

object OrganisationNameData {
  val one: OrganisationName = OrganisationName("Example")
}

object OrganisationTypeData {
  val one: Organisation.OrganisationType = Organisation.OrganisationType.UkLimitedCompany
}

object UserIdData {
  val one: UserId = UserId.random
}

object MemberData {
  val one: Collaborator = Collaborators.Member(UserIdData.one)
}

object OrganisationData extends FixedClock {
  val one: Organisation = Organisation(OrganisationIdData.one, OrganisationNameData.one, OrganisationTypeData.one, instant, Set(MemberData.one))
}

trait OrganisationFixtures extends UserTestData with LocalUserIdTracker with ApplicationWithCollaboratorsFixtures {

  val memberUserDetails: User                = adminDeveloper
  val responsibleIndividualUserDetails: User = JoeBloggs

  val memberUser: RegisteredOrUnregisteredUser =
    RegisteredOrUnregisteredUser(memberUserDetails.userId, memberUserDetails.email, isRegistered = true, isVerified = true)

  val responsibleIndividualUser: RegisteredOrUnregisteredUser =
    RegisteredOrUnregisteredUser(responsibleIndividualUserDetails.userId, responsibleIndividualUserDetails.email, isRegistered = true, isVerified = true)

  val orgMembers: List[RegisteredOrUnregisteredUser] = List(memberUser, responsibleIndividualUser)
  val orgUserDetails: List[User]                     = List(memberUserDetails, responsibleIndividualUserDetails)

  val standardOrg: Organisation = Organisation(
    OrganisationIdData.one,
    OrganisationNameData.one,
    OrganisationTypeData.one,
    instant,
    Set(Collaborators.Member(memberUserDetails.userId), Collaborators.ResponsibleIndividual(responsibleIndividualUserDetails.userId))
  )

  val organisationAddress: OrganisationAddress = OrganisationAddress(
    addressLineOne = Some("Stoxley Industrial Park"),
    addressLineTwo = Some("Stoxley Park Avenue"),
    addressLineThree = Some("Stoxley"),
    careOf = Some("Easy Soft Limited"),
    country = Some("United Kingdom"),
    locality = Some("Canningley"),
    poBox = Some("PO Box 42"),
    postalCode = Some("CI8 2JS"),
    premises = Some("Unit 12"),
    region = Some("West Yorkshire")
  )

  val orgWithExtraData: Organisation = standardOrg.copy(
    companyNumber = Some("08947216"),
    corporationTaxUtr = Some("1234567890"),
    websiteUrl = Some("http://easysoft.co.uk"),
    address = Some(organisationAddress)
  )

  val orgWithMinimalData: Organisation = standardOrg.copy(
    organisationType = Organisation.OrganisationType.SoleTrader,
    collaborators = Set(Collaborators.ResponsibleIndividual(responsibleIndividualUserDetails.userId))
  )

  val extendedOrg: OrganisationWithAllMembersDetailsAndApplications = OrganisationWithAllMembersDetailsAndApplications(
    standardOrg,
    orgMembers,
    orgUserDetails,
    List(standardApp)
  )

  val extendedOrgWithExtraData: OrganisationWithAllMembersDetailsAndApplications = OrganisationWithAllMembersDetailsAndApplications(
    orgWithExtraData,
    orgMembers,
    orgUserDetails,
    List(standardApp)
  )

  val extendedOrgWithMinimalData: OrganisationWithAllMembersDetailsAndApplications = OrganisationWithAllMembersDetailsAndApplications(
    orgWithMinimalData,
    List(responsibleIndividualUser),
    List(responsibleIndividualUserDetails),
    List.empty
  )

  val prodAppWithLastAccess: ApplicationWithCollaborators =
    standardApp.copy(details = standardApp.details.copy(lastAccess = Some(standardApp.details.createdOn.plus(1, ChronoUnit.DAYS))))

  val sandboxAppWithNoLastAccess: ApplicationWithCollaborators = standardApp2.inSandbox()

  val extendedOrgWithMultipleApplications: OrganisationWithAllMembersDetailsAndApplications = OrganisationWithAllMembersDetailsAndApplications(
    standardOrg,
    orgMembers,
    orgUserDetails,
    List(prodAppWithLastAccess, sandboxAppWithNoLastAccess)
  )
}
