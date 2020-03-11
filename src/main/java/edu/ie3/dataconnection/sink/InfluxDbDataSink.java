/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.dataconnection.sink;

import edu.ie3.dataconnection.dataconnectors.DataConnector;
import edu.ie3.dataconnection.dataconnectors.InfluxDbConnector;
import edu.ie3.models.UniqueEntity;
import edu.ie3.models.influxdb.InfluxDbEntity;
import edu.ie3.models.influxdb.InfluxDbMapper;
import edu.ie3.models.result.ResultEntity;
import java.util.Collection;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

public class InfluxDbDataSink implements DataSink {

  InfluxDbConnector connector;

  public InfluxDbDataSink(InfluxDbConnector connector) {
    this.connector = connector;
  }

  @Override
  public DataConnector getDataConnector() {
    return connector;
  }

  @Override
  public void persist(ResultEntity entity) {
    write(InfluxDbMapper.transformToInfluxDbEntity(entity));
  }

  public void write(InfluxDbEntity entity) {
    try (InfluxDB session = connector.getSession()) {
      Point point = Point.measurementByPOJO(entity.getClass()).addFieldsFromPOJO(entity).build();
      session.write(point);
    }
  }

  @Override
  public void persistAll(Collection<? extends ResultEntity> entities) {
    BatchPoints batchPoints = BatchPoints.builder().build();
    for (UniqueEntity entity : entities) {
      InfluxDbEntity influxDbEntity = InfluxDbMapper.transformToInfluxDbEntity(entity);
      Point point =
          Point.measurementByPOJO(influxDbEntity.getClass())
              .addFieldsFromPOJO(influxDbEntity)
              .build();
      batchPoints.point(point);
    }

    try (InfluxDB session = connector.getSession()) {
      session.write(batchPoints);
    }
  }
}