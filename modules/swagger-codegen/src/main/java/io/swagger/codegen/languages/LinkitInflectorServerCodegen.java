/**
 * 
 */
package io.swagger.codegen.languages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.codegen.CodegenModelFactory;
import io.swagger.codegen.CodegenModelType;
import io.swagger.codegen.LinkitCodegenOperation;
import io.swagger.codegen.SupportingFile;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 19 apr 2018 $
 * 
 */
public class LinkitInflectorServerCodegen extends JavaInflectorServerCodegen {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkitInflectorServerCodegen.class);

	/**
	 * 
	 */
	public LinkitInflectorServerCodegen() {
		super();
		sourceFolder = "src/main/java";
	}
	@Override
	public void processOpts() {
		super.processOpts();

		CodegenModelFactory.setTypeMapping(CodegenModelType.OPERATION, LinkitCodegenOperation.class);
		try {

			if(additionalProperties.containsKey("artifactId")) {
				artifactId = additionalProperties.get("artifactId").toString();
			}

			String base = groupId + ".servlets." + artifactId;

			if(!additionalProperties.containsKey("modelPackage")) {
				modelPackage = base + ".beans.base";
			} else {
				modelPackage = additionalProperties.get("modelPackage").toString();
			}

			if(!additionalProperties.containsKey("servicePackage")) {
				additionalProperties.put("servicePackage", base + ".service");
			}
			String servicePackage = additionalProperties.get("servicePackage").toString();

			if(!additionalProperties.containsKey("apiPackage")) {
				apiPackage = base + ".controllers";
			} else {
				apiPackage = additionalProperties.get("apiPackage").toString();
			}

			for(int i =0; i < supportingFiles.size(); i++) {
				SupportingFile file = supportingFiles.get(i);
				if(file.templateFile.contains("StringUtil")) {
					supportingFiles.remove(file);
				}
			}


			importMapping.put("JsonProperty", "org.codehaus.jackson.annotate.JsonProperty");
			importMapping.put("JsonSubTypes", "org.codehaus.jackson.annotate.JsonSubTypes");
			importMapping.put("JsonTypeInfo", "org.codehaus.jackson.annotate.JsonTypeInfo");
			importMapping.put("JsonCreator", "org.codehaus.jackson.annotate.JsonCreator");
			importMapping.put("JsonValue", "org.codehaus.jackson.annotate.JsonValue");

			supportingFiles.add(new SupportingFile("JSONSerializable.mustache",
					(sourceFolder + '/' + modelPackage).replace(".", "/"), "JSONSerializable.java"));

			supportingFiles.add(new SupportingFile("BaseController.mustache",
					(sourceFolder + '/' + apiPackage).replace(".", "/"), "BaseController.java"));

			supportingFiles.add(new SupportingFile("BaseRsService.mustache",
					(sourceFolder + '/' + servicePackage).replace(".", "/"), "BaseRsService.java"));
			supportingFiles.add(new SupportingFile("SimpleDateFormatUtils.mustache",
					(sourceFolder + '/' + servicePackage + '/' + "utils").replace(".", "/"), "SimpleDateFormatUtils.java"));
			supportingFiles.add(new SupportingFile("IAutorizzato.mustache",
					(sourceFolder + '/' + servicePackage + '/' + "utils").replace(".", "/"), "IAutorizzato.java"));

		} catch(Exception e) {
			LOGGER.error("Errore init: "+ e.getMessage(), e);
		}

	}

}
