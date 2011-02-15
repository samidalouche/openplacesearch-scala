package com.sirika.openplacesearch.api.formatting.internal

import com.sirika.openplacesearch.api.feature.NamePart

/**
 * @author Sami Dalouche (sami.dalouche@gmail.com)
 */

protected[formatting] final class FullyQualifiedNamesExtractor extends NamesExtractor {
  def extractRelevantNames(nameParts: List[NamePart]): List[String] = nameParts map{_.name} toList
}