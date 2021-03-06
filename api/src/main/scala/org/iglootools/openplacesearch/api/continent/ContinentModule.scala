package org.iglootools.openplacesearch.api.continent

import com.google.inject.{AbstractModule,Singleton}

/**
 * @author Sami Dalouche (sami.dalouche@gmail.com)
 */
final class ContinentModule extends AbstractModule {
  def configure: Unit = {
    bind(classOf[ContinentRepository]).to(classOf[InMemoryContinentRepository]).in(classOf[Singleton])
  }
}