/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.source.influxdb;

import edu.ie3.datamodel.io.connectors.InfluxDbConnector;
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueData;
import edu.ie3.datamodel.io.factory.timeseries.TimeBasedWeatherValueFactory;
import edu.ie3.datamodel.io.source.IdCoordinateSource;
import edu.ie3.datamodel.io.source.WeatherSource;
import edu.ie3.datamodel.models.timeseries.individual.IndividualTimeSeries;
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue;
import edu.ie3.datamodel.models.value.WeatherValue;
import edu.ie3.util.interval.ClosedInterval;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.locationtech.jts.geom.Point;

/** InfluxDB Source for weather data */
public class InfluxDbWeatherSource implements WeatherSource {
  private static final String BASIC_QUERY_STRING = "Select * from weather";
  private static final String COORDINATE_ID_COLUMN_NAME = "coordinate";
  private static final String MEASUREMENT_NAME_WEATHER = "weather";
  private static final int MILLI_TO_NANO_FACTOR = 1000000;
  private final InfluxDbConnector connector;
  private final IdCoordinateSource coordinateSource;
  private final TimeBasedWeatherValueFactory weatherValueFactory;

  /**
   * Initializes a new InfluxDbWeatherSource
   *
   * @param connector needed for database connection
   * @param coordinateSource needed to map coordinates to ID as InfluxDB does not support spatial
   *     types
   */
  public InfluxDbWeatherSource(InfluxDbConnector connector, IdCoordinateSource coordinateSource) {
    this.connector = connector;
    this.coordinateSource = coordinateSource;
    this.weatherValueFactory = new TimeBasedWeatherValueFactory();
  }

  @Override
  public Map<Point, IndividualTimeSeries<WeatherValue>> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval) {
    try (InfluxDB session = connector.getSession()) {
      String query = createQueryStringForInterval(timeInterval);
      QueryResult queryResult = session.query(new Query(query));
      Stream<Optional<TimeBasedValue>> optValues = optTimeBasedValueStream(queryResult);
      Set<TimeBasedValue<WeatherValue>> timeBasedValues =
          filterEmptyOptionals(optValues).collect(Collectors.toSet());
      Map<Point, Set<TimeBasedValue<WeatherValue>>> coordinateToValues =
          timeBasedValues.stream()
              .collect(
                  Collectors.groupingBy(
                      timeBasedWeatherValue -> timeBasedWeatherValue.getValue().getCoordinate(),
                      Collectors.toSet()));
      return coordinateToValues.entrySet().stream()
          .collect(
              Collectors.toMap(
                  Map.Entry::getKey, e -> new IndividualTimeSeries<>(null, e.getValue())));
    }
  }

  @Override
  public Map<Point, IndividualTimeSeries<WeatherValue>> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval, Collection<Point> coordinates) {
    if (coordinates == null) return getWeather(timeInterval);
    Map<Point, Optional<Integer>> coordinatesToId =
        coordinates.stream().collect(Collectors.toMap(point -> point, coordinateSource::getId));
    HashMap<Point, IndividualTimeSeries<WeatherValue>> coordinateToTimeSeries = new HashMap<>();
    try (InfluxDB session = connector.getSession()) {
      for (Map.Entry<Point, Optional<Integer>> entry : coordinatesToId.entrySet()) {
        Optional<Integer> coordinateId = entry.getValue();
        if (coordinateId.isPresent()) {
          String query =
              createQueryStringForIntervalAndCoordinate(timeInterval, coordinateId.get());
          QueryResult queryResult = session.query(new Query(query));
          Stream<Optional<TimeBasedValue>> optValues = optTimeBasedValueStream(queryResult);
          Set<TimeBasedValue<WeatherValue>> timeBasedValues =
              filterEmptyOptionals(optValues).collect(Collectors.toSet());
          IndividualTimeSeries<WeatherValue> timeSeries =
              new IndividualTimeSeries<>(null, timeBasedValues);
          coordinateToTimeSeries.put(entry.getKey(), timeSeries);
        }
      }
    }
    return coordinateToTimeSeries;
  }

  /**
   * Return the weather for the given time interval AND coordinate
   *
   * @param timeInterval Queried time interval
   * @param coordinate Queried coordinate
   * @return weather data for the specified time and coordinate
   */
  public IndividualTimeSeries<WeatherValue> getWeather(
      ClosedInterval<ZonedDateTime> timeInterval, Point coordinate) {
    Optional<Integer> coordinateId = coordinateSource.getId(coordinate);
    if (!coordinateId.isPresent()) {
      return new IndividualTimeSeries<>(UUID.randomUUID(), Collections.emptySet());
    }
    try (InfluxDB session = connector.getSession()) {
      String query = createQueryStringForIntervalAndCoordinate(timeInterval, coordinateId.get());
      QueryResult queryResult = session.query(new Query(query));
      Stream<Optional<TimeBasedValue>> optValues = optTimeBasedValueStream(queryResult);
      return new IndividualTimeSeries<>(
          null, filterEmptyOptionals(optValues).collect(Collectors.toSet()));
    }
  }

  @Override
  public Optional<TimeBasedValue<WeatherValue>> getWeather(ZonedDateTime date, Point coordinate) {
    Optional<Integer> coordinateId = coordinateSource.getId(coordinate);
    if (!coordinateId.isPresent()) {
      return Optional.empty();
    }
    try (InfluxDB session = connector.getSession()) {
      String query = createQueryStringForDateAndCoordinate(date, coordinateId.get());
      QueryResult queryResult = session.query(new Query(query));
      return filterEmptyOptionals(optTimeBasedValueStream(queryResult)).findFirst();
    }
  }

  /**
   * Parses an influxQL QueryResult and then transforms them into a Stream of optional
   * TimeBasedValue&lt;WeatherValue&gt;, with a present Optional value, if the transformation was
   * successful and an empty optional otherwise.
   */
  private Stream<Optional<TimeBasedValue>> optTimeBasedValueStream(QueryResult queryResult) {
    Map<String, Set<Map<String, String>>> measurementsMap =
        InfluxDbConnector.parseQueryResult(queryResult, MEASUREMENT_NAME_WEATHER);
    return measurementsMap.get(MEASUREMENT_NAME_WEATHER).stream()
        .map(
            fields -> {
              Optional<Point> coordinate =
                  coordinateSource.getCoordinate(
                      Integer.parseInt(fields.remove(COORDINATE_ID_COLUMN_NAME)));
              if (!coordinate.isPresent()) return null;
              fields.putIfAbsent("uuid", UUID.randomUUID().toString());
              return new TimeBasedWeatherValueData(fields, coordinate.get());
            })
        .filter(Objects::nonNull)
        .map(weatherValueFactory::getEntity);
  }

  private String createQueryStringForIntervalAndCoordinate(
      ClosedInterval<ZonedDateTime> timeInterval, int coordinateId) {
    return createQueryStringForInterval(timeInterval)
        + " and "
        + createCoordinateConstraintString(coordinateId);
  }

  private String createQueryStringForDateAndCoordinate(ZonedDateTime date, int coordinateId) {
    return createQueryStringForDate(date)
        + " and "
        + createCoordinateConstraintString(coordinateId);
  }

  private String createQueryStringForInterval(ClosedInterval<ZonedDateTime> timeInterval) {
    String timeConstraint =
        "time >= "
            + timeInterval.getLower().toInstant().toEpochMilli() * MILLI_TO_NANO_FACTOR
            + " and time <= "
            + timeInterval.getUpper().toInstant().toEpochMilli() * MILLI_TO_NANO_FACTOR;
    return BASIC_QUERY_STRING + " where " + timeConstraint;
  }

  private String createQueryStringForDate(ZonedDateTime date) {
    String timeConstraint = "time=" + date.toInstant().toEpochMilli() * MILLI_TO_NANO_FACTOR;
    return BASIC_QUERY_STRING + " where " + timeConstraint;
  }

  private String createCoordinateConstraintString(int coordinateId) {
    return "coordinate='" + coordinateId + "'";
  }

  /**
   * Removes empty Optionals
   *
   * @param elements stream to filter
   * @return filtered elements Stream
   */
  protected Stream<TimeBasedValue<WeatherValue>> filterEmptyOptionals(
      Stream<Optional<TimeBasedValue>> elements) {
    return elements.filter(Optional::isPresent).map(Optional::get).map(TimeBasedValue.class::cast);
  }
}
