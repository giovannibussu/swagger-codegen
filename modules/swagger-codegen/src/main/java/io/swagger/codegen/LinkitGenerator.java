/**
 * 
 */
package io.swagger.codegen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import io.swagger.codegen.ignore.CodegenIgnoreProcessor;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 19 apr 2018 $
 * 
 */
public class LinkitGenerator extends DefaultGenerator {

    protected final Logger LOGGER = LoggerFactory.getLogger(LinkitGenerator.class);

	@Override
	public Generator opts(ClientOptInput opts) {
		this.opts = opts;
		this.openAPI = opts.getOpenAPI();
		this.config = opts.getConfig();
		this.config.additionalProperties().putAll(opts.getOpts().getProperties());

		String ignoreFileLocation = this.config.getIgnoreFilePathOverride();
		if(ignoreFileLocation != null) {
			final File ignoreFile = new File(ignoreFileLocation);
			if(ignoreFile.exists() && ignoreFile.canRead()) {
				this.ignoreProcessor = new CodegenIgnoreProcessor(ignoreFile);
			} else {
				LOGGER.warn("Ignore file specified at {} is not valid. This will fall back to an existing ignore file if present in the output directory.", ignoreFileLocation);
			}
		}

		if(this.ignoreProcessor == null) {
			this.ignoreProcessor = new CodegenIgnoreProcessor(this.config.getOutputDir());
		}

		return this;
	}

	@Override
	public List<File> generate() {
		List<File> generate = super.generate();
		this.generateService(generate, new ArrayList<Object>(), new ArrayList<Object>());
		return generate;
	}


	private void generateService(List<File> files, List<Object> allOperations, List<Object> allModels) {

		String servicePackage = config.additionalProperties().containsKey("servicePackage") ? config.additionalProperties().get("servicePackage").toString() : config.modelPackage();

		Map<String, List<CodegenOperation>> paths = processPaths(this.openAPI.getPaths());

		for (String tag : paths.keySet()) {
			try {

				List<CodegenOperation> ops = paths.get(tag);
				List<CodegenOperation> collect = ops;

				Collections.sort(ops, new Comparator<CodegenOperation>() {
					@Override
					public int compare(CodegenOperation one, CodegenOperation another) {
						return ObjectUtils.compare(one.operationId, another.operationId);
					}
				});

				if(this.openAPI.getTags() != null && !this.openAPI.getTags().isEmpty()) {
					for(Tag tagg: this.openAPI.getTags()) {
						collect = ops.stream().filter(o -> o.getTags().contains(tagg)).collect(Collectors.toList());

						if(!collect.isEmpty()) {
							Map<String, Object> operationByTag = processOperations(config, tag, collect, allModels);

							//		                operationByTag.put("basePath", basePath);
							//		                operationByTag.put("basePathWithoutHost", basePathWithoutHost);
							//		                operationByTag.put("contextPath", contextPath);
							operationByTag.put("baseName", tag);
							operationByTag.put("modelPackage", config.modelPackage());
							operationByTag.put("package", config.apiPackage());
							operationByTag.putAll(config.additionalProperties());
							operationByTag.put("classname", config.toApiName(tag));
							operationByTag.put("classVarName", config.toApiVarName(tag));
							operationByTag.put("importPath", config.toApiImport(tag));
							operationByTag.put("classFilename", config.toApiFilename(tag));

							allOperations.add(new HashMap<String, Object>(operationByTag));
							for (int i = 0; i < allOperations.size(); i++) {
								Map<String, Object> oo = (Map<String, Object>) allOperations.get(i);
								if (i < (allOperations.size() - 1)) {
									oo.put("hasMore", "true");
								}
							}


							Map<String, Object> operationsOrig = (Map<String, Object>) operationByTag.get("operations");
							List<CodegenOperation> operationOrig = (List<CodegenOperation>) operationsOrig.get("operation");

							Map<String, CodegenOperation> opsMap = new HashMap<>();

							for(CodegenOperation op: operationOrig) {
								if(!opsMap.containsKey(op.getOperationId())) {
									opsMap.put(op.getOperationId(), op);
								}
							}

							List<CodegenOperation> operationFiltered = new ArrayList<>();

							for(CodegenOperation op: opsMap.values()) {
								operationFiltered.add(op);
							}

							operationsOrig.put("operation", operationFiltered);

							if(!config.apiTemplateFiles().containsKey("service.mustache")) {
								config.apiTemplateFiles().put("service.mustache", ".java");
							}

							operationByTag.put("classname", config.toApiName(tag).replaceAll("Controller", ""));

							String filename = config.apiFilename("service.mustache", tag).replaceAll("Controller", "");
							filename = filename.replace(config.apiPackage().replace('.', '/'), servicePackage.replace('.', '/'));
							if (!config.shouldOverwrite(filename) && new File(filename).exists()) {
								LOGGER.info("Skipped overwriting " + filename);
								continue;
							}

							File written = processTemplateToFile(operationByTag, "service.mustache", filename);
							if(written != null) {
								files.add(written);
							}
						}
					}
				} else {
					if(!collect.isEmpty()) {
						Map<String, Object> operationByTag = processOperations(config, tag, collect, allModels);

						//	                operationByTag.put("basePath", basePath);
						//	                operationByTag.put("basePathWithoutHost", basePathWithoutHost);
						//	                operationByTag.put("contextPath", contextPath);
						operationByTag.put("baseName", tag);
						operationByTag.put("modelPackage", config.modelPackage());
						operationByTag.put("package", config.apiPackage());
						operationByTag.putAll(config.additionalProperties());
						operationByTag.put("classname", config.toApiName(tag));
						operationByTag.put("classVarName", config.toApiVarName(tag));
						operationByTag.put("importPath", config.toApiImport(tag));
						operationByTag.put("classFilename", config.toApiFilename(tag));

						allOperations.add(new HashMap<String, Object>(operationByTag));
						for (int i = 0; i < allOperations.size(); i++) {
							Map<String, Object> oo = (Map<String, Object>) allOperations.get(i);
							if (i < (allOperations.size() - 1)) {
								oo.put("hasMore", "true");
							}
						}


						Map<String, Object> operationsOrig = (Map<String, Object>) operationByTag.get("operations");
						List<CodegenOperation> operationOrig = (List<CodegenOperation>) operationsOrig.get("operation");

						Map<String, CodegenOperation> opsMap = new HashMap<>();

						for(CodegenOperation op: operationOrig) {
							if(!opsMap.containsKey(op.getOperationId())) {
								opsMap.put(op.getOperationId(), op);
							}
						}

						List<CodegenOperation> operationFiltered = new ArrayList<>();

						for(CodegenOperation op: opsMap.values()) {
							operationFiltered.add(op);
						}

						operationsOrig.put("operation", operationFiltered);

						if(!config.apiTemplateFiles().containsKey("service.mustache")) {
							config.apiTemplateFiles().put("service.mustache", ".java");
						}

						operationByTag.put("classname", config.toApiName(tag).replaceAll("Controller", ""));

						String filename = config.apiFilename("service.mustache", tag).replaceAll("Controller", "");
						filename = filename.replace(config.apiPackage().replace('.', '/'), servicePackage.replace('.', '/'));
						if (!config.shouldOverwrite(filename) && new File(filename).exists()) {
							LOGGER.info("Skipped overwriting " + filename);
							continue;
						}

						File written = processTemplateToFile(operationByTag, "service.mustache", filename);
						if(written != null) {
							files.add(written);
						}
					}
				}
			} catch(IOException e) {
				throw new RuntimeException("Could not generate api file for '" + tag + "'", e);
			}

		}
	}

}
