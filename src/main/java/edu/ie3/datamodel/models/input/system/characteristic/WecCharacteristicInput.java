/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.models.input.system.characteristic;

import edu.ie3.datamodel.models.StandardUnits;
import java.util.SortedSet;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Speed;

/** Characteristic mapping the wind velocity to its corresponding Betz coefficient */
public class WecCharacteristicInput extends CharacteristicInput<Speed, Dimensionless> {
  private static final Pattern MATCHING_PATTERN = CharacteristicInput.buildMatchingPattern("cP");

  public WecCharacteristicInput(
      UUID uuid,
      SortedSet<CharacteristicCoordinate<Speed, Dimensionless>> characteristicCoordinates) {
    super(uuid, characteristicCoordinates, "cP", 2);
  }

  public WecCharacteristicInput(UUID uuid, String input) {
    super(
        uuid,
        input,
        MATCHING_PATTERN,
        StandardUnits.WIND_VELOCITY,
        StandardUnits.CP_CHARACTERISTIC,
        "cP",
        2);
  }

  @Override
  public String toString() {
    return "WecCharacteristicInput{" + "uuid=" + uuid + ", coordinates=" + coordinates + '}';
  }
}
