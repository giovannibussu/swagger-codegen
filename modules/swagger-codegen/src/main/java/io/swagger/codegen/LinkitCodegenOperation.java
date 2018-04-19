/**
 * 
 */
package io.swagger.codegen;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 19 apr 2018 $
 * 
 */
public class LinkitCodegenOperation extends CodegenOperation {

	 public boolean getHasConsumes() {
	     return consumes != null && consumes.size() > 0;
	 }

	 public boolean getHasProduces() {
	     return produces != null && produces.size() > 0;
	 }

}
