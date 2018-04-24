/**
 * 
 */
package io.swagger.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public List<Map<String, String>> getConsumes() {
		return unify(consumes);
	}

	public List<Map<String, String>> getProduces() {
		return unify(produces);
	}

	/**
	 * @param produces
	 * @return
	 */
	private List<Map<String, String>> unify(List<Map<String, String>> mediaType) {
		
		if(mediaType == null)
			return null;
		
		List<Map<String, String>> lst = new ArrayList<Map<String, String>>();
		
		int count = 0;
		for(Map<String, String> type: mediaType) {
			Map<String, String> map = new HashMap<String, String>();
			for(String k: type.keySet()) {
				map.put(k, type.get(k));
			}
            count += 1;
            if (count < mediaType.size()) {
            	map.put("hasMore", "true");
            } else {
            	map.put("hasMore", null);
            }

			
			lst.add(map);
		}
		
		return lst;
	}


}
