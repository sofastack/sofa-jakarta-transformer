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
package org.eclipse.transformer.maven.configuration;

import java.util.List;

/**
 *
 *
 * @author yuanxuan
 * @version : JakartaTransformerRules.java, v 0.1 2023年06月29日 14:32 yuanxuan Exp $
 */
public class JakartaTransformerRules {
	private List<String> selections;
	private List<String> renames;
	private List<String> versions;
	private List<String> bundles;
	private List<String> directs;
	private List<String> texts;
	private List<String> perClassConstants;
	private String pom;
	private List<String> immediates;
	private boolean      invert;
	private boolean      overwrite = true;
	private boolean      widen;

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

	public String getPom() {
		return pom;
	}

	public void setPom(String pom) {
		this.pom = pom;
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

	public boolean isWiden() {
		return widen;
	}

	public void setWiden(boolean widen) {
		this.widen = widen;
	}
}
