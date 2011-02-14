package com.sirika.openplacesearch.api.gisgraphy.resultparser

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import com.sirika.openplacesearch.api
import api.Implicits._
import com.sirika.commons.scala.io.{InputSupliers, Urls}

@RunWith(classOf[JUnitRunner])
class GeolocalizationResultSpec extends Spec with ShouldMatchers {

  describe("toPlaces") {
    val GeolocalizationResultNearParis = Urls.toInputStreamSupplier("com/sirika/openplacesearch/api/gisgraphy/geolocalizationNearParis.xml")

    it("should return 10 places (GeolocalizationResultNearParis)") {
      placesNearParis.size should be === 10
    }

    it("should correctly initialize all fields") {
      PlaceAssertions.shouldBeParis(placesNearParis(0), checkAlternateNames=false)
    }

    def placesNearParis= {
      InputSupliers.doWithInputStream(GeolocalizationResultNearParis) { is =>
        new GeolocalizationResultParser().toPlaces(is)
      }
    }
  }

}