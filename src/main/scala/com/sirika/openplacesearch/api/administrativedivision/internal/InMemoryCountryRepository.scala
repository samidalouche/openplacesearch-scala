package com.sirika.openplacesearch.api.administrativedivision.internal

import com.sirika.openplacesearch.api.administrativedivision.{Country, IsoCountryCode, FipsCountryCode, CountryAdministrativeInformation,CountryGeographicInformation, CountryRepository, SimpleFeatureNameProvider}
import com.google.common.io.{Resources, LineProcessor, CharStreams, InputSupplier}
import java.io.InputStreamReader
import com.google.common.base.Charsets
import grizzled.slf4j.Logging
import com.ibm.icu.util.{ULocale, Currency}
import com.sirika.openplacesearch.api.continent.internal.InMemoryContinentRepository
import com.sirika.commons.scala.{InputStreamReaderTransformer, Urls}

/**
 * @author Sami Dalouche (sami.dalouche@gmail.com)
 */

class InMemoryCountryRepository extends CountryRepository with Logging {
  private[this] val continentRepository = new InMemoryContinentRepository()

  private lazy val countries = parseCountries(Urls.toInputReaderSupplier("com/sirika/openplacesearch/api/administrativedivision/countries"))
  private val fipsLookupTable : Map[String, Country] = Map(countries.filter{_.fipsCountryCode.fipsCode.isDefined}.map{c : Country => (c.fipsCountryCode.fipsCode.get, c)} : _*)
  private val alpha2LookupTable : Map[String, Country] = Map(countries.map{c : Country => (c.isoCountryCode.alpha2Code, c)} : _*)
  private val alpha3LookupTable : Map[String, Country] = Map(countries.map{c : Country => (c.isoCountryCode.alpha3Code, c)} : _*)

  def findAll: List[Country] = countries
  def getByFipsCode(code: String): Country = fipsLookupTable.get(code).get
  def getByIsoAlpha3Code(code: String): Country = alpha3LookupTable.get(code).get
  def getByIsoAlpha2Code(code: String): Country = alpha2LookupTable.get(code).get

  private def parseCountries(readerSupplier: InputSupplier[InputStreamReader]) : List[Country] = {
    // ISO,ISO3,ISO-Numeric,fips,Country,Capital,Area(in sq km),Population,Continent,tld,CurrencyCode,CurrencyName,Phone,Postal Code Format,Postal Code Regex,Languages,geonameid,neighbours,EquivalentFipsCode

    new InputStreamReaderTransformer(readerSupplier).map { (line, lineNumber) =>
      sanitizeLineSplit(line.split('\t')) match {
        case List(isoAlpha2CountryCode,isoAlpha3CountryCode,isoNumericCountryCode,fipsCountryCode,countryName,
        capitalName,areaInSquareMeters,population,continentCode,topLevelDomain,currencyCode,currencyName,
        phonePrefix,postalCodeMask,postalCodeRegex,preferredLocales,geonamesId,neighbours, equivalentFipsCode)
        =>
          Some(Country(
            isoCountryCode=
              IsoCountryCode(
                alpha3Code=isoAlpha3CountryCode,
                alpha2Code=isoAlpha2CountryCode,
                numeric=isoNumericCountryCode.toInt),
            continent=continentRepository.getByGeonamesCode(continentCode),
            featureNameProvider= SimpleFeatureNameProvider(defaultName = countryName, parentAdministrativeEntity=None),
            currency=someIfNonEmpty(currencyCode, {c => Currency.getInstance(c)}),
            fipsCountryCode=
              FipsCountryCode(
                fipsCode=someIfNonEmpty(fipsCountryCode),
                equivalentFipsCode=someIfNonEmpty(equivalentFipsCode)),
            countryAdministrativeInformation=
              CountryAdministrativeInformation(
                preferredLocales=toLocales(preferredLocales),
                topLevelDomain=someIfNonEmpty(topLevelDomain),
                phonePrefix=someIfNonEmpty(phonePrefix),
                postalCodeRegex=someIfNonEmpty(postalCodeRegex),
                postalCodeMask=someIfNonEmpty(postalCodeMask)),
            countryGeographicInformation=
              CountryGeographicInformation(
                population = someIfNonEmpty(population, p=> p.toLong),
                areaInSquareKilometers = someIfNonEmpty(areaInSquareMeters, a => a.toDouble))))
        case _ => throw new IllegalArgumentException("Error processing line: %s".format(line))
      }
    }
  }

  private def sanitizeLineSplit(line: Array[String]):List[String] = {
    val ExpectedNumberOfFields = 19
    def actualNumberOfFields = line.size
    def numberOfMissingFields = ExpectedNumberOfFields-actualNumberOfFields

    line.toList ++ List.fill(numberOfMissingFields)("")
  }

  /**
   * trims the value, and returns an Option :
   * <ul>
   *  <li>None if the string is empty (after trimming)</li>
   *  <li>Some() on what the result callback returns (by default, the string itself)</li>
   * </ul>
   */
  private def someIfNonEmpty[T](value: String, result: (String)=> T = {s:String => s}):Option[T] = value.trim match {
    case s if s.isEmpty => None
    case s:String => Some(result(s))
  }

  private def toLocales(preferredLocalesAsString: String): List[ULocale] = {
    preferredLocalesAsString.split(",").map { localeAsString =>
      new ULocale(localeAsString.replaceAll("-", "_"))
    }.toList
  }
}