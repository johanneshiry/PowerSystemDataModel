/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.connectors

import edu.ie3.datamodel.io.csv.FileNamingStrategy
import edu.ie3.datamodel.io.csv.timeseries.ColumnScheme
import edu.ie3.util.io.FileIOUtils
import org.apache.commons.io.FilenameUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CsvFileConnectorTest extends Specification {
	@Shared
	Path tmpFolder

	@Shared
	CsvFileConnector cfc

	@Shared
	Set<String> timeSeriesPaths

	@Shared
	Set<String> pathsToIgnore

	def setupSpec() {
		tmpFolder = Files.createTempDirectory("psdm_csv_file_connector_")
		cfc = new CsvFileConnector(tmpFolder.toString(), new FileNamingStrategy())
		timeSeriesPaths = [
			"its_pq_53990eea-1b5d-47e8-9134-6d8de36604bf.csv",
			"its_p_fcf0b851-a836-4bde-8090-f44c382ed226.csv",
			"its_pqh_5022a70e-a58f-4bac-b8ec-1c62376c216b.csv",
			"its_c_b88dee50-5484-4136-901d-050d8c1c97d1.csv",
			"its_c_c7b0d9d6-5044-4f51-80b4-f221d8b1f14b.csv",
			"its_weather_085d98ee-09a2-4de4-b119-83949690d7b6.csv"
		]
		pathsToIgnore = [
			"file_to_be_ignored.txt"
		]
		(pathsToIgnore + timeSeriesPaths).forEach { it -> Files.createFile(Paths.get(FilenameUtils.concat(tmpFolder.toString(), it))) }
	}

	def cleanupSpec() {
		FileIOUtils.deleteRecursively(tmpFolder)
		cfc.shutdown()
	}

	def "The csv file connector is able to provide correct paths time series files"() {
		when:
		def actual = cfc.individualTimeSeriesFilePaths

		then:
		noExceptionThrown()

		actual.size() == timeSeriesPaths.size()
		actual.containsAll(timeSeriesPaths)
	}

	def "The csv file connector returns empty Optional of TimeSeriesReadingData when pointed to non-individual time series"() {
		given:
		def pathString = "lpts_h0_53990eea-1b5d-47e8-9134-6d8de36604bf"

		when:
		def actual = cfc.buildReadingData(pathString)

		then:
		!actual.present
	}

	def "The csv file connector returns empty Optional of TimeSeriesReadingData when pointed to non-existing file"() {
		given:
		def pathString = "its_pq_32f38421-f7fd-4295-8f9a-3a54b4e7dba9"

		when:
		def actual = cfc.buildReadingData(pathString)

		then:
		!actual.present
	}

	def "The csv file connector is able to build correct reading information from valid input"() {
		given:
		def pathString = "its_pq_53990eea-1b5d-47e8-9134-6d8de36604bf"
		def expected = new CsvFileConnector.TimeSeriesReadingData(
				UUID.fromString("53990eea-1b5d-47e8-9134-6d8de36604bf"),
				ColumnScheme.APPARENT_POWER,
				Mock(BufferedReader)
				)

		when:
		def actual = cfc.buildReadingData(pathString)

		then:
		actual.present
		actual.get().with {
			assert uuid == expected.uuid
			assert columnScheme == expected.columnScheme
			/* Don't check the reader explicitly */
		}
	}

	def "The csv file connector is able to init readers for all time series files"() {
		when:
		def actual = cfc.initTimeSeriesReader()

		then:
		actual.size() == 5
		def energyPriceEntries = actual.get(ColumnScheme.ENERGY_PRICE)
		Objects.nonNull(energyPriceEntries)
		energyPriceEntries.size() == 2
	}
}
