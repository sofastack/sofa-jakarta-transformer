/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.transformer;

import org.codehaus.plexus.util.StringUtils;
import org.eclipse.transformer.jakarta.JakartaOptionsContainer;

import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ApiTransformOptions implements TransformOptions {
	private final CustomRules rules;
	private final Map<String, String>	optionDefaults;
	private final Function<String, URL>	ruleLoader;

	private final String inputFileName;

	private final String outputFileName;


	public ApiTransformOptions(CustomRules customRules, String inputFileName, String outputFileName) {
		this.rules = requireNonNull(customRules);
		this.inputFileName = inputFileName;
		this.outputFileName = outputFileName;
		if (customRules.getContainerType() == ContainerType.None) {
			this.optionDefaults = Collections.emptyMap();
			this.ruleLoader = getClass()::getResource;
			return;
		}
		this.optionDefaults = customRules.getContainerType().getOptionsContainer().getOptionDefaults();
		this.ruleLoader = customRules.getContainerType().getOptionsContainer().getRuleLoader();
	}

	private static List<String> condition(List<String> values) {
		if (values == null) {
			return null;
		}
		for (ListIterator<String> iterator = values.listIterator(); iterator.hasNext();) {
			String value = StringUtils.trim(iterator.next());
			if (StringUtils.isBlank(value) || Objects.equals("-", value)) {
				iterator.remove();
			} else {
				iterator.set(value);
			}
		}
		return values;
	}

	@Override
	public List<String> getOptionValues(AppOption option) {
		List<String> values;
		switch (option) {
			case RULES_BUNDLES :
				values = rules.getBundles();
				break;
			case RULES_DIRECT :
				values = rules.getDirects();
				break;
			case RULES_IMMEDIATE_DATA :
				values = rules.getImmediates();
				break;
			case RULES_MASTER_TEXT :
				values = rules.getTexts();
				break;
			case RULES_PER_CLASS_CONSTANT :
				values = rules.getPerClassConstants();
				break;
			case RULES_RENAMES :
				values = rules.getRenames();
				break;
			case RULES_SELECTIONS :
				values = rules.getSelections();
				break;
			case RULES_VERSIONS :
				values = rules.getVersions();
				break;
			case RULES_POM:
				values = rules.getPoms();
				break;
			default :
				values = null;
				break;
		}
		return condition(values);
	}

	@Override
	public boolean hasOption(AppOption option) {
		boolean has;
		switch (option) {
			case OVERWRITE :
				has = rules.isOverwrite();
				break;
			case INVERT :
				has = rules.isInvert();
				break;
			case WIDEN_ARCHIVE_NESTING :
				has = rules.isWiden();
				break;
			default :
				has = TransformOptions.super.hasOption(option);
				break;
		}
		return has;
	}

	@Override
	public String getDefaultValue(AppOption option) {
		String longTag = option.getLongTag();
		String defaultValue = optionDefaults.get(longTag);
		if (defaultValue == null) {
			String shortTag = option.getShortTag();
			defaultValue = optionDefaults.get(shortTag);
		}
		return defaultValue;
	}

	@Override
	public Function<String, URL> getRuleLoader() {
		return ruleLoader;
	}

	@Override
	public String getInputFileName() {
		return this.inputFileName;
	}

	@Override
	public String getOutputFileName() {
		return this.outputFileName;
	}
}
