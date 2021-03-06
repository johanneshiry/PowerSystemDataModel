/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.factory.timeseries;

import static edu.ie3.datamodel.io.factory.timeseries.TimeBasedSimpleValueFactory.*;

import edu.ie3.datamodel.models.StandardUnits;
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue;
import edu.ie3.datamodel.models.value.WeatherValue;
import edu.ie3.util.TimeUtil;
import edu.ie3.util.quantities.interfaces.Irradiation;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import org.locationtech.jts.geom.Point;
import tech.units.indriya.ComparableQuantity;

public class TimeBasedWeatherValueFactory
    extends TimeBasedValueFactory<TimeBasedWeatherValueData, WeatherValue> {

  private static final String UUID = "uuid";
  private static final String TIME = "time";

  private final TimeUtil timeUtil;

  public TimeBasedWeatherValueFactory() {
    this("yyyy-MM-dd'T'HH:mm:ss[.S[S][S]]'Z'");
  }

  public TimeBasedWeatherValueFactory(String timestampPattern) {
    super(WeatherValue.class);
    timeUtil = new TimeUtil(ZoneId.of("UTC"), Locale.GERMANY, timestampPattern);
  }

  @Override
  protected List<Set<String>> getFields(TimeBasedWeatherValueData data) {
    Set<String> minConstructorParams =
        newSet(
            UUID,
            TIME,
            DIFFUSE_IRRADIATION,
            DIRECT_IRRADIATION,
            TEMPERATURE,
            WIND_DIRECTION,
            WIND_VELOCITY);
    return Collections.singletonList(minConstructorParams);
  }

  @Override
  protected TimeBasedValue<WeatherValue> buildModel(TimeBasedWeatherValueData data) {
    Point coordinate = data.getCoordinate();
    UUID uuid = data.getUUID(UUID);
    ZonedDateTime time = timeUtil.toZonedDateTime(data.getField(TIME));
    ComparableQuantity<Irradiation> directIrradiation =
        data.getQuantity(DIRECT_IRRADIATION, StandardUnits.IRRADIATION);
    ComparableQuantity<Irradiation> diffuseIrradiation =
        data.getQuantity(DIFFUSE_IRRADIATION, StandardUnits.IRRADIATION);
    ComparableQuantity<Temperature> temperature =
        data.getQuantity(TEMPERATURE, StandardUnits.TEMPERATURE);
    ComparableQuantity<Angle> windDirection =
        data.getQuantity(WIND_DIRECTION, StandardUnits.WIND_DIRECTION);
    ComparableQuantity<Speed> windVelocity =
        data.getQuantity(WIND_VELOCITY, StandardUnits.WIND_VELOCITY);
    WeatherValue weatherValue =
        new WeatherValue(
            coordinate,
            directIrradiation,
            diffuseIrradiation,
            temperature,
            windDirection,
            windVelocity);
    return new TimeBasedValue<>(uuid, time, weatherValue);
  }
}
