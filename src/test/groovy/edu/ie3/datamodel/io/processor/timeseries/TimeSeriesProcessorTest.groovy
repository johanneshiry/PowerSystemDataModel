/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.processor.timeseries

import static edu.ie3.util.quantities.PowerSystemUnits.EURO_PER_MEGAWATTHOUR

import java.lang.reflect.Method
import edu.ie3.datamodel.exceptions.EntityProcessorException
import edu.ie3.datamodel.models.timeseries.IntValue
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue
import edu.ie3.datamodel.models.value.EnergyPriceValue
import spock.lang.Shared
import spock.lang.Specification
import tec.uom.se.quantity.Quantities

import java.time.ZoneId
import java.time.ZonedDateTime

import static tec.uom.se.unit.Units.METRE

class TimeSeriesProcessorTest extends Specification {
	@Shared
	IndividualTimeSeries<EnergyPriceValue> individualTimeSeries

	@Shared
	TimeBasedValue<EnergyPriceValue> firstEntry

	def setupSpec() {
		firstEntry = new TimeBasedValue<>(
				UUID.fromString("9e4dba1b-f3bb-4e40-bd7e-2de7e81b7704"),
				ZonedDateTime.of(2020, 4, 2, 10, 0, 0, 0, ZoneId.of("UTC")),
				new EnergyPriceValue(Quantities.getQuantity(5d, EURO_PER_MEGAWATTHOUR)))


		individualTimeSeries = new IndividualTimeSeries<>(
				UUID.fromString("a4bbcb77-b9d0-4b88-92be-b9a14a3e332b"),
				[
					firstEntry,
					new TimeBasedValue<>(
					UUID.fromString("520d8e37-b842-40fd-86fb-32007e88493e"),
					ZonedDateTime.of(2020, 4, 2, 10, 15, 0, 0, ZoneId.of("UTC")),
					new EnergyPriceValue(Quantities.getQuantity(15d, EURO_PER_MEGAWATTHOUR))),
					new TimeBasedValue<>(
					UUID.fromString("593d006c-ef76-46a9-b8db-f8666f69c5db"),
					ZonedDateTime.of(2020, 4, 2, 10, 30, 0, 0, ZoneId.of("UTC")),
					new EnergyPriceValue(Quantities.getQuantity(10d, EURO_PER_MEGAWATTHOUR))),
				] as Set
				)
	}

	def "A TimeSeriesProcessor is instantiated correctly"() {
		when:
		TimeSeriesProcessor<IndividualTimeSeries, TimeBasedValue, EnergyPriceValue> processor = new TimeSeriesProcessor<>(IndividualTimeSeries, TimeBasedValue, EnergyPriceValue)
		Map expectedSourceMapping = [
			"uuid": FieldSourceToMethod.FieldSource.ENTRY,
			"price": FieldSourceToMethod.FieldSource.VALUE,
			"time": FieldSourceToMethod.FieldSource.ENTRY]

		then:
		processor.with {
			/* Check for attributes in higher classes also they are ignored by the class itself. */
			assert processor.registeredClass == IndividualTimeSeries

			assert processor.registeredKey == new TimeSeriesProcessorKey(IndividualTimeSeries, TimeBasedValue, EnergyPriceValue)
			assert processor.fieldToSource.size() == expectedSourceMapping.size()
			processor.fieldToSource.each{ key, value ->
				assert expectedSourceMapping.containsKey(key)
				assert expectedSourceMapping.get(key) == value.source
			}
			/* Also test the logic of TimeSeriesProcessor#buildFieldToSource, because it is invoked during instantiation */
			assert processor.headerElements == [
				"uuid",
				"price",
				"time"] as String[]
		}
	}

	def "A TimeSeriesProcessor throws an Exception on instantiation, if the time series combination is not supported"() {
		when:
		new TimeSeriesProcessor<>(IndividualTimeSeries, TimeBasedValue, IntValue)

		then:
		EntityProcessorException thrown = thrown(EntityProcessorException)
		thrown.message.startsWith("Cannot register time series combination 'TimeSeriesProcessorKey{timeSeriesClass=class edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries, entryClass=class edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue, valueClass=class edu.ie3.datamodel.models.timeseries.IntValue}' with entity processor 'TimeSeriesProcessor'. Eligible combinations:")
	}

	def "A TimeSeriesProcessor throws an Exception, when the simple handle method is called"() {
		given:
		TimeSeriesProcessor<IndividualTimeSeries, TimeBasedValue, EnergyPriceValue> processor = new TimeSeriesProcessor<>(IndividualTimeSeries, TimeBasedValue, EnergyPriceValue)

		when:
		processor.handleEntity(individualTimeSeries)

		then:
		UnsupportedOperationException thrown = thrown(UnsupportedOperationException)
		thrown.message ==  "Don't invoke this simple method, but TimeSeriesProcessor#handleTimeSeries(TimeSeries)."
	}

	def "A TimeSeriesProcessor correctly extracts the field name to getter map"() {
		given:
		TimeSeriesProcessor<IndividualTimeSeries, TimeBasedValue, EnergyPriceValue> processor = new TimeSeriesProcessor<>(IndividualTimeSeries, TimeBasedValue, EnergyPriceValue)

		when:
		Map<String, Method> actual = processor.extractFieldToMethod(source)

		then:
		actual.size() == expectedFieldNames.size()
		actual.each{ entry -> expectedFieldNames.contains(entry.key)}

		where:
		source || expectedFieldNames
		FieldSourceToMethod.FieldSource.ENTRY || ["uuid", "time"]
		FieldSourceToMethod.FieldSource.VALUE || ["price"]
	}

	def "A TimeSeriesProcessor handles an entry correctly"() {
		given:
		TimeSeriesProcessor<IndividualTimeSeries, TimeBasedValue, EnergyPriceValue> processor = new TimeSeriesProcessor<>(IndividualTimeSeries, TimeBasedValue, EnergyPriceValue)
		Map<String, String> expected = [
			"uuid": "9e4dba1b-f3bb-4e40-bd7e-2de7e81b7704",
			"time": "2020-04-02 10:00:00",
			"price": "5.0"
		]

		when:
		Map<String, String> actual = processor.handleEntry(individualTimeSeries, firstEntry)

		then:
		actual == expected
	}

	def "A TimeSeriesProcessors handles a complete time series correctly"() {
		given:
		TimeSeriesProcessor<IndividualTimeSeries, TimeBasedValue, EnergyPriceValue> processor = new TimeSeriesProcessor<>(IndividualTimeSeries, TimeBasedValue, EnergyPriceValue)
		Set<Map<String, String>> expected = [
			[
				"uuid": "9e4dba1b-f3bb-4e40-bd7e-2de7e81b7704",
				"time": "2020-04-02 10:00:00",
				"price": "5.0"
			],
			[
				"uuid": "520d8e37-b842-40fd-86fb-32007e88493e",
				"time": "2020-04-02 10:15:00",
				"price": "15.0"
			],
			[
				"uuid": "593d006c-ef76-46a9-b8db-f8666f69c5db",
				"time": "2020-04-02 10:30:00",
				"price": "10.0"
			]] as Set

		when:
		Set<Map<String, String>> actual = processor.handleTimeSeries(individualTimeSeries)

		then:
		actual == expected
	}

	def "A TimeSeriesProcessor throws an Exception, when specific quantity handling is requested"() {
		given:
		TimeSeriesProcessor<IndividualTimeSeries, TimeBasedValue, EnergyPriceValue> processor = new TimeSeriesProcessor<>(IndividualTimeSeries, TimeBasedValue, EnergyPriceValue)

		when:
		processor.handleProcessorSpecificQuantity(Quantities.getQuantity(1d, METRE), "blubb")

		then:
		UnsupportedOperationException thrown = thrown(UnsupportedOperationException)
		thrown.message == "No specific handling of quantities needed here."
	}
}
