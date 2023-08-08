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

import java.util.ArrayList;
import java.util.List;

/**
 * Contain all rules defined by user, mainly used in the api and maven plugin.
 * */
public class CustomRules {

	private List<String> selections;
	private List<String>	renames;
	private List<String>	versions;
	private List<String>	bundles;
	private List<String>	directs;
	private List<String>	texts;
	private List<String>	perClassConstants;
	private List<String>	poms;
	private List<String>	immediates;
	private boolean			invert;
	private boolean			overwrite;
	private boolean			widen;
	private ContainerType containerType;

	public CustomRules(List<String> selections, List<String> renames, List<String> versions, List<String> bundles,
					   List<String> directs, List<String> texts, List<String> perClassConstants, List<String> poms,
					   List<String> immediates, boolean invert, boolean overwrite, boolean widen,
					   ContainerType containerType) {
		this.selections = selections;
		this.renames = renames;
		this.versions = versions;
		this.bundles = bundles;
		this.directs = directs;
		this.texts = texts;
		this.perClassConstants = perClassConstants;
		this.poms = poms;
		this.immediates = immediates;
		this.invert = invert;
		this.overwrite = overwrite;
		this.widen = widen;
		this.containerType = containerType;
	}

	public List<String> getSelections() {
		return selections;
	}

	public void setSelections(List<String> selections) {
		this.selections = selections;
	}

	public List<String> getRenames() {
		return renames;
	}

	public void setRenames(List<String> renames) {
		this.renames = renames;
	}

	public List<String> getVersions() {
		return versions;
	}

	public void setVersions(List<String> versions) {
		this.versions = versions;
	}

	public List<String> getBundles() {
		return bundles;
	}

	public void setBundles(List<String> bundles) {
		this.bundles = bundles;
	}

	public List<String> getDirects() {
		return directs;
	}

	public void setDirects(List<String> directs) {
		this.directs = directs;
	}

	public List<String> getTexts() {
		return texts;
	}

	public void setTexts(List<String> texts) {
		this.texts = texts;
	}

	public List<String> getPerClassConstants() {
		return perClassConstants;
	}

	public void setPerClassConstants(List<String> perClassConstants) {
		this.perClassConstants = perClassConstants;
	}

	public List<String> getPoms() {
		return poms;
	}

	public void setPoms(List<String> poms) {
		this.poms = poms;
	}

	public List<String> getImmediates() {
		return immediates;
	}

	public void setImmediates(List<String> immediates) {
		this.immediates = immediates;
	}

	public boolean isInvert() {
		return invert;
	}

	public void setInvert(boolean invert) {
		this.invert = invert;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * @return the widen
	 */
	public boolean isWiden() {
		return widen;
	}

	/**
	 * @param widen the widen to set
	 */
	public void setWiden(boolean widen) {
		this.widen = widen;
	}

	public ContainerType getContainerType() {
		return containerType;
	}

	public void setContainerType(ContainerType containerType) {
		this.containerType = containerType;
	}

	@Override
	public String toString() {
		return String.format(
			"selections=%s, renames=%s, versions=%s, bundles=%s, directs=%s, texts=%s, perClassConstants=%s, " +
				"immediates=%s, invert=%s, overwrite=%s, widen=%s, defaultOptions=%s",
			getSelections(), getRenames(), getVersions(), getBundles(), getDirects(), getTexts(),
			getPerClassConstants(), getImmediates(), isInvert(), isOverwrite(), isWiden(),
			getContainerType().getOptionsContainer().getClass().getSimpleName());
	}

	public static class CustomRulesBuilder {

		private List<String> selections;
		private List<String>	renames;
		private List<String>	versions;
		private List<String>	bundles;
		private List<String>	directs;
		private List<String>	texts;
		private List<String>	perClassConstants;
		private List<String>	poms;
		private List<String>	immediates;
		private boolean			invert;
		private boolean			overwrite;
		private boolean			widen;
		private ContainerType containerType = ContainerType.None;

		public CustomRulesBuilder() {
		}

		public CustomRules build() {
			return new CustomRules(selections, renames, versions, bundles, directs, texts, perClassConstants, poms,
				immediates, invert, overwrite, widen, containerType);
		}

		public CustomRulesBuilder setSelections(List<String> selections) {
			this.selections = selections;
			return this;
		}

		public CustomRulesBuilder setRenames(List<String> renames) {
			this.renames = renames;
			return this;
		}

		public CustomRulesBuilder setVersions(List<String> versions) {
			this.versions = versions;
			return this;
		}

		public CustomRulesBuilder setBundles(List<String> bundles) {
			this.bundles = bundles;
			return this;
		}

		public CustomRulesBuilder setDirects(List<String> directs) {
			this.directs = directs;
			return this;
		}

		public CustomRulesBuilder setTexts(List<String> texts) {
			this.texts = texts;
			return this;
		}

		public CustomRulesBuilder setPerClassConstants(List<String> perClassConstants) {
			this.perClassConstants = perClassConstants;
			return this;
		}

		public CustomRulesBuilder setPoms(List<String> poms) {
			this.poms = poms;
			return this;
		}

		public CustomRulesBuilder setImmediates(List<String> immediates) {
			this.immediates = immediates;
			return this;
		}

		public CustomRulesBuilder setInvert(boolean invert) {
			this.invert = invert;
			return this;
		}

		public CustomRulesBuilder setOverwrite(boolean overwrite) {
			this.overwrite = overwrite;
			return this;
		}

		public CustomRulesBuilder setWiden(boolean widen) {
			this.widen = widen;
			return this;
		}

		public CustomRulesBuilder setContainerType(ContainerType containerType) {
			this.containerType = containerType;
			return this;
		}
	}
}
