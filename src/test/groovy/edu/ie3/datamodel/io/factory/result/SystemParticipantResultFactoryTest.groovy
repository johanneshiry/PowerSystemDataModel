package edu.ie3.datamodel.io.factory.result

import edu.ie3.datamodel.exceptions.FactoryException
import edu.ie3.datamodel.io.factory.SimpleEntityData
import edu.ie3.datamodel.models.StandardUnits
import edu.ie3.datamodel.models.result.system.*
import edu.ie3.test.helper.FactoryTestHelper
import edu.ie3.util.TimeTools
import spock.lang.Specification
import tec.uom.se.unit.Units

class SystemParticipantResultFactoryTest extends Specification implements FactoryTestHelper {

    def "A SystemParticipantResultFactory should contain all expected classes for parsing"() {
        given:
        def resultFactory = new SystemParticipantResultFactory()
        def expectedClasses = [LoadResult, FixedFeedInResult, BmResult, PvResult,
                               ChpResult, WecResult, StorageResult, EvcsResult, EvResult]

        expect:
        resultFactory.classes() == Arrays.asList(expectedClasses.toArray())
    }

    def "A SystemParticipantResultFactory should parse a valid result model correctly"() {
        given: "a system participant factory and model data"
        def resultFactory = new SystemParticipantResultFactory()
        Map<String, String> parameter = [
            "timestamp":    "2020-01-30 17:26:44",
            "inputModel":   "91ec3bcf-1777-4d38-af67-0bf7c9fa73c7",
            "p":            "2",
            "q":            "2"
        ]

        if (modelClass == EvResult) { parameter["soc"] = "10" }

        when:
        Optional<? extends SystemParticipantResult> result = resultFactory.getEntity(new SimpleEntityData(parameter, modelClass))

        then:
        result.present
        result.get().getClass() == resultingModelClass
        ((SystemParticipantResult) result.get()).with {
            assert p == getQuant(parameter["p"], StandardUnits.ACTIVE_POWER_RESULT)
            assert q == getQuant(parameter["q"], StandardUnits.REACTIVE_POWER_RESULT)
            assert timestamp == TimeTools.toZonedDateTime(parameter["timestamp"])
            assert inputModel == UUID.fromString(parameter["inputModel"])
        }

        if (modelClass == EvResult) {
            assert (((EvResult) result.get()).soc == getQuant(parameter["soc"], Units.PERCENT))
        }

        where:
        modelClass        || resultingModelClass
        LoadResult        || LoadResult
        FixedFeedInResult || FixedFeedInResult
        BmResult          || BmResult
        EvResult          || EvResult
        PvResult          || PvResult
        EvcsResult        || EvcsResult
        ChpResult         || ChpResult
        WecResult         || WecResult

    }

    def "A SystemParticipantResultFactory should parse a StorageResult correctly"() {
        given: "a system participant factory and model data"
        def resultFactory = new SystemParticipantResultFactory()
        Map<String, String> parameter = [
            "timestamp":    "2020-01-30 17:26:44",
            "inputModel":   "91ec3bcf-1777-4d38-af67-0bf7c9fa73c7",
            "soc":          "20",
            "p":            "2",
            "q":            "2"
        ]
        when:
        Optional<? extends SystemParticipantResult> result = resultFactory.getEntity(new SimpleEntityData(parameter, StorageResult))

        then:
        result.present
        result.get().getClass() == StorageResult
        ((StorageResult) result.get()).with {
            assert p == getQuant(parameter["p"], StandardUnits.ACTIVE_POWER_RESULT)
            assert q == getQuant(parameter["q"], StandardUnits.REACTIVE_POWER_RESULT)
            assert soc == getQuant(parameter["soc"], Units.PERCENT)
            assert timestamp == TimeTools.toZonedDateTime(parameter["timestamp"])
            assert inputModel == UUID.fromString(parameter["inputModel"])
        }
    }

    def "A SystemParticipantResultFactory should throw an exception on invalid or incomplete data"() {
        given: "a system participant factory and model data"
        def resultFactory = new SystemParticipantResultFactory()
        Map<String, String> parameter = [
            "timestamp":    "2020-01-30 17:26:44",
            "inputModel":   "91ec3bcf-1777-4d38-af67-0bf7c9fa73c7",
            "q":            "2"
        ]
        when:
        resultFactory.getEntity(new SimpleEntityData(parameter, WecResult))

        then:
        FactoryException ex = thrown()
        ex.message == "The provided fields [inputModel, q, timestamp] with data {inputModel -> 91ec3bcf-1777-4d38-af67-0bf7c9fa73c7,q -> 2,timestamp -> 2020-01-30 17:26:44} are invalid for instance of WecResult. \n" +
                "The following fields to be passed to a constructor of WecResult are possible:\n" +
                "0: [inputModel, p, q, timestamp]\n" +
                "1: [inputModel, p, q, timestamp, uuid]\n"

    }

    def "A SystempParticipantResultFactor should be performant"() {
        given: "a factory and dummy model data"
        def resultFactory = new SystemParticipantResultFactory()
        Map<String, String> parameter = [
            "timestamp":    "2020-01-30 17:26:44",
            "inputModel":   "91ec3bcf-1777-4d38-af67-0bf7c9fa73c7",
            "soc":          "20",
            "p":            "2",
            "q":            "2",
        ]
        expect: "that the factory should not need more than 2 seconds for processing 100.000 entities"
        Long startTime = System.currentTimeMillis()
        10000.times {
            resultFactory.getEntity(new SimpleEntityData(parameter, StorageResult))
        }
        BigDecimal elapsedTime = (System
                .currentTimeMillis() - startTime) / 1000.0
        elapsedTime < 2

    }

}
