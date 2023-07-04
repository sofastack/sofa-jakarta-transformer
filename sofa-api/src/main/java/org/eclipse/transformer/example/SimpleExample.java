package org.eclipse.transformer.example;

import org.eclipse.transformer.ApiTransformOptions;
import org.eclipse.transformer.ContainerType;
import org.eclipse.transformer.CustomRules;
import org.eclipse.transformer.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleExample {

	public static final Logger logger = LoggerFactory.getLogger(SimpleExample.class);

	/**
	 * Just use default Jakarta rules to transform. It will convert the input from
	 * Java EE to Jakarta EE.
	 * */
	public static CustomRules pureJakartaTransform() {
		return new CustomRules.CustomRulesBuilder().setContainerType(ContainerType.Jakarta).build();
	}

	/**
	 * Just use custom rules.
	 * */
	public static CustomRules pureCustomTransform() {

		List<String> renameList = new ArrayList<>(Arrays.asList(
			"alipay-api/src/main/resources/jakarta-renames-test1.properties",
			"alipay-api/src/main/resources/jakarta-renames-test2.properties"));
		List<String> textList = new ArrayList<>(Collections.singleton(
			"org.eclipse.transformer.jakarta/src/main/resources/org/" +
				"eclipse/transformer/jakarta/jakarta-text-master.properties"));

		return new CustomRules.CustomRulesBuilder()
			.setContainerType(ContainerType.None)
			.setRenames(renameList)
			.setTexts(textList)
			.build();
	}

	/**
	 * Use both default Jakarta rules and custom rules. There are several
	 * conflicts in this example:
	 * <p>
	 * 1. "javax.servlet=xxx.servlet" in the custom immediate rule and
	 *    "javax.servlet=jakarta.servlet" in the default jakarta rule
	 * <br/>
	 * 2. "javax.servlet.http=xxx.servlet.http" in the custom immediate rule and
	 *    "javax.servlet.http=yyy.servlet.http" in the custom rename rule
	 * <br/>
	 * 3. "javax.batch.api=xxx.batch.api" in the custom rename rule and
	 *    "javax.batch.api=jakarta.batch.api" in the default jakarta rule
	 * <br/>
	 * 4. "javax.batch.api.chunk=jakarta.batch.api.chunk" in the first custom
	 *    rename rule and "javax.batch.api.chunk=xxx.batch.api.chunk" in the
	 *    second custom rename rule
	 * <p>
	 * The priority of rules:
	 * <br/>
	 * immediate custom rule > custom rule (the later the rule is defined, the
	 * higher the priority) > default rule
	 * <p>
	 * So for this situation the final rules will be:<br/>
	 * "javax.servlet=xxx.servlet"<br/>
	 * "javax.servlet.http=xxx.servlet.http"<br/>
	 * "javax.batch.api=xxx.batch.api"<br/>
	 * "javax.batch.api.chunk=xxx.batch.api.chunk"
	 * */
	public static CustomRules customJakartaTransform() {
		//List<String> immediatesList = new ArrayList<>(Arrays.asList(
		//	"tr", "javax.servlet", "xxx.servlet", "tr", "javax.servlet.http", "xxx.servlet.http"));
		//
		//List<String> renameList = new ArrayList<>(Arrays.asList(
		//	"alipay-api/src/main/resources/jakarta-renames-test1.properties",
		//	"alipay-api/src/main/resources/jakarta-renames-test2.properties"));
		//
		//List<String> textList = new ArrayList<>(Collections.singleton(
		//	"org.eclipse.transformer.jakarta/src/main/resources/org/" +
		//		"eclipse/transformer/jakarta/jakarta-text-master.properties"));
		//
		//return new CustomRules.CustomRulesBuilder()
		//	.setImmediates(immediatesList)
		//	.setRenames(renameList)
		//	.setTexts(textList)
		//	.setContainerType(ContainerType.Jakarta)
		//	.build();

		List<String> pomList = new ArrayList<>(Arrays.asList(
			"/Users/zzh/Documents/jars/test.json"));

		return new CustomRules.CustomRulesBuilder()
			.setPoms(pomList)
			.setContainerType(ContainerType.Jakarta)
			.build();
	}




	/**
	 * Three steps to use the api:<br/>
	 * 1. define custom rules and default rule to build {@link CustomRules}<br/>
	 * 2. define input file path and output file path to {@link ApiTransformOptions}<br/>
	 * 3. build {@link Transformer} and run it
	 **/
	public static void main(String[] args) {

		ApiTransformOptions options = new ApiTransformOptions(customJakartaTransform(),
			"/Users/zzh/Documents/jars/buservice-sdk-1.0.0.20230608.jar",
			"/Users/zzh/Documents/buservice-sdk-1.0.0.20230608-jakarta.jar");

		Transformer transformer = new Transformer(options);
		Transformer.ResultCode rc = transformer.run();

		if (rc != Transformer.ResultCode.SUCCESS_RC) {
			logger.error("fail to transform");
		}
		else {
			logger.info("success to transform");
		}

	}
}
