/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.extractor;

import edu.ie3.datamodel.models.input.NodeInput;

/**
 * Interface that should be implemented by all elements holding a two {@link NodeInput} elements and
 * should be processable by the {@link Extractor}. For now, this only holds true by all {@link
 * edu.ie3.datamodel.models.input.connector.ConnectorInput} and {@link
 * edu.ie3.datamodel.models.input.connector.TransformerInput}
 *
 * @version 0.1
 * @since 31.03.20
 */
public interface Nodes extends Nested {

  NodeInput getNodeA();

  NodeInput getNodeB();
}
