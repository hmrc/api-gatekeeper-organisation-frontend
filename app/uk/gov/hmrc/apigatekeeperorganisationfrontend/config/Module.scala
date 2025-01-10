package uk.gov.hmrc.apigatekeeperorganisationfrontend.config

import com.google.inject.AbstractModule

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[AppConfig]).asEagerSingleton()
  }
}
