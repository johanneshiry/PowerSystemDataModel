/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.factory.input;

import edu.ie3.datamodel.io.factory.EntityData;
import edu.ie3.datamodel.models.UniqueEntity;
import edu.ie3.datamodel.models.input.OperatorInput;
import java.util.Map;
import java.util.Optional;

/**
 * Data used for the construction of {@link edu.ie3.datamodel.models.input.AssetInput} entities.
 * This data object can include additional information about the {@link OperatorInput}, which cannot
 * be provided through the attribute map.
 */
public class AssetInputEntityData extends EntityData {
  private final OperatorInput operatorInput;

  /**
   * Creates a new AssetInputEntityData object without operator.
   *
   * @param fieldsToAttributes attribute map: field name -> value
   * @param entityClass class of the entity to be created with this data
   */
  public AssetInputEntityData(
      Map<String, String> fieldsToAttributes, Class<? extends UniqueEntity> entityClass) {
    this(fieldsToAttributes, entityClass, null);
  }

  /**
   * Creates a new AssetInputEntityData object with operator.
   *
   * @param fieldsToAttributes attribute map: field name -> value
   * @param entityClass class of the entity to be created with this data
   * @param operatorInput operator input
   */
  public AssetInputEntityData(
      Map<String, String> fieldsToAttributes,
      Class<? extends UniqueEntity> entityClass,
      OperatorInput operatorInput) {
    super(fieldsToAttributes, entityClass);
    this.operatorInput = operatorInput;
  }

  public Optional<OperatorInput> getOperatorInput() {
    return Optional.ofNullable(operatorInput);
  }
}