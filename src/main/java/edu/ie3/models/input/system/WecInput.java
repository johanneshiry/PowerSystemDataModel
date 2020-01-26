/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.models.input.system;

import edu.ie3.models.input.NodeInput;
import edu.ie3.models.input.OperatorInput;
import edu.ie3.models.input.system.type.WecTypeInput;
import edu.ie3.util.interval.ClosedInterval;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Describes a Wind Energy Converter */
public class WecInput extends SystemParticipantInput {

  /** Type of this WEC, containing default values for WEC assets of this kind */
  private WecTypeInput type;
  /** Is this asset market oriented? */
  private boolean marketReaction;
  /**
   * @param uuid of the input entity
   * @param operationInterval Empty for a non-operated asset, Interval of operation period else
   * @param operator of the asset
   * @param id of the asset
   * @param node the asset is connected to
   * @param qCharacteristics
   * @param cosphi Power factor
   * @param type of this WEC
   * @param marketReaction Is this asset market oriented?
   */
  public WecInput(
      UUID uuid,
      Optional<ClosedInterval<ZonedDateTime>> operationInterval,
      OperatorInput operator,
      String id,
      NodeInput node,
      String qCharacteristics,
      double cosphi,
      WecTypeInput type,
      boolean marketReaction) {
    super(uuid, operationInterval, operator, id, node, qCharacteristics, cosphi);
    this.type = type;
    this.marketReaction = marketReaction;
  }

  /**
   * If both operatesFrom and operatesUntil are Empty, it is assumed that the asset is non-operated.
   *
   * @param uuid of the input entity
   * @param operatesFrom start of operation period, will be replaced by LocalDateTime.MIN if Empty
   * @param operatesUntil end of operation period, will be replaced by LocalDateTime.MAX if Empty
   * @param operator of the asset
   * @param id of the asset
   * @param node the asset is connected to
   * @param qCharacteristics
   * @param cosphi Power factor
   * @param type of this WEC
   * @param marketReaction Is this asset market oriented?
   */
  public WecInput(
      UUID uuid,
      Optional<ZonedDateTime> operatesFrom,
      Optional<ZonedDateTime> operatesUntil,
      OperatorInput operator,
      String id,
      NodeInput node,
      String qCharacteristics,
      double cosphi,
      WecTypeInput type,
      boolean marketReaction) {
    super(uuid, operatesFrom, operatesUntil, operator, id, node, qCharacteristics, cosphi);
    this.type = type;
    this.marketReaction = marketReaction;
  }
  /**
   * Constructor for a non-operated asset
   *
   * @param uuid of the input entity
   * @param id of the asset
   * @param node the asset is connected to
   * @param qCharacteristics
   * @param cosphi Power factor
   * @param type of this WEC
   * @param marketReaction Is this asset market oriented?
   */
  public WecInput(
      UUID uuid,
      String id,
      NodeInput node,
      String qCharacteristics,
      double cosphi,
      WecTypeInput type,
      boolean marketReaction) {
    super(uuid, id, node, qCharacteristics, cosphi);
    this.type = type;
    this.marketReaction = marketReaction;
  }

  public WecTypeInput getType() {
    return type;
  }

  public void setType(WecTypeInput type) {
    this.type = type;
  }

  public boolean getMarketReaction() {
    return marketReaction;
  }

  public void setMarketReaction(boolean marketReaction) {
    this.marketReaction = marketReaction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    WecInput wecInput = (WecInput) o;
    return Objects.equals(type, wecInput.type)
        && Objects.equals(marketReaction, wecInput.marketReaction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type, marketReaction);
  }
}
