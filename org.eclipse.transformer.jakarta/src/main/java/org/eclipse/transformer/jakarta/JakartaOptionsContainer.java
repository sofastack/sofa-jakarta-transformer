/********************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: (EPL-2.0 OR Apache-2.0)
 ********************************************************************************/

package org.eclipse.transformer.jakarta;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This container contain all rules which can transform all code related to Javax to Jakarta, the scenario is
 * <a href="https://jakarta.ee/blogs/javax-jakartaee-namespace-ecosystem-progress/">this</a>
 * */
public class JakartaOptionsContainer implements OptionsContainer {

	public static final String	DEFAULT_SELECTION_REFERENCE		= "jakarta-selection.properties";
	public static final String	DEFAULT_RENAMES_REFERENCE		= "jakarta-renames.properties";
	public static final String	DEFAULT_VERSIONS_REFERENCE		= "jakarta-versions.properties";
	public static final String	DEFAULT_BUNDLES_REFERENCE		= "jakarta-bundles.properties";
	public static final String	DEFAULT_DIRECT_REFERENCE		= "jakarta-direct.properties";
	public static final String	DEFAULT_MASTER_TEXT_REFERENCE	= "jakarta-text-master.properties";

	private JakartaOptionsContainer() {
	}

	public static final JakartaOptionsContainer JAKARTA_OPTIONS_CONTAINER = new JakartaOptionsContainer();

	public Function<String, URL> getRuleLoader() {
		return JakartaOptionsContainer.doGetRuleLoader();
	}

	public Map<String, String> getOptionDefaults() {
		return doGetOptionDefaults();
	}

	public static Function<String, URL> doGetRuleLoader() {
		return JakartaOptionsContainer.class::getResource;
	}

	public static Map<String, String> doGetOptionDefaults() {
		Map<String, String> optionDefaults = new HashMap<>();

		optionDefaults.put("selection", DEFAULT_SELECTION_REFERENCE);
		optionDefaults.put("renames", DEFAULT_RENAMES_REFERENCE);
		optionDefaults.put("versions", DEFAULT_VERSIONS_REFERENCE);
		optionDefaults.put("bundles", DEFAULT_BUNDLES_REFERENCE);
		optionDefaults.put("direct", DEFAULT_DIRECT_REFERENCE);
		optionDefaults.put("text", DEFAULT_MASTER_TEXT_REFERENCE);
		return optionDefaults;
	}
}
