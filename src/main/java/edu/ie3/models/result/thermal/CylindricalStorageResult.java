/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.models.result.thermal;

import edu.ie3.models.StandardUnits;
import edu.ie3.models.input.thermal.CylindricalStorageInput;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

/** Respresents the results of {@link CylindricalStorageInput} */
public class CylindricalStorageResult extends ThermalStorageResult {
  /** Fill level of the storage */
  private Quantity<Dimensionless> fillLevel;

  /**
   * Constructs the result with
   *
   * @param timestamp date and time when the result is produced
   * @param inputModel uuid of the input model that produces the result
   * @param energy Currently stored energy
   * @param qDot Heat power flowing into (> 0) or coming from (< 0) the storage
   * @param fillLevel Fill level of the storage
   */
  public CylindricalStorageResult(
      ZonedDateTime timestamp,
      UUID inputModel,
      Quantity<Energy> energy,
      Quantity<Power> qDot,
      Quantity<Dimensionless> fillLevel) {
    super(timestamp, inputModel, energy, qDot);
    this.fillLevel = fillLevel.to(StandardUnits.FILL_LEVEL);
  }

  /**
   * Constructs the result with
   *
   * @param uuid uuid of this result entity, for automatic uuid generation use primary constructor
   *     above
   * @param timestamp date and time when the result is produced
   * @param inputModel uuid of the input model that produces the result
   * @param energy Currently stored energy
   * @param qDot Heat power flowing into (> 0) or coming from (< 0) the storage
   * @param fillLevel Fill level of the storage
   */
  public CylindricalStorageResult(
      UUID uuid,
      ZonedDateTime timestamp,
      UUID inputModel,
      Quantity<Energy> energy,
      Quantity<Power> qDot,
      Quantity<Dimensionless> fillLevel) {
    super(uuid, timestamp, inputModel, energy, qDot);
    this.fillLevel = fillLevel.to(StandardUnits.FILL_LEVEL);
  }

  public Quantity<Dimensionless> getFillLevel() {
    return fillLevel;
  }

  public void setFillLevel(Quantity<Dimensionless> fillLevel) {
    this.fillLevel = fillLevel.to(StandardUnits.FILL_LEVEL);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    CylindricalStorageResult that = (CylindricalStorageResult) o;
    return fillLevel.equals(that.fillLevel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), fillLevel);
  }
}