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
package org.eclipse.transformer.maven;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.eclipse.transformer.ApiTransformOptions;
import org.eclipse.transformer.ContainerType;
import org.eclipse.transformer.CustomRules;
import org.eclipse.transformer.CustomRules.CustomRulesBuilder;
import org.eclipse.transformer.Transformer;
import org.eclipse.transformer.Transformer.ResultCode;
import org.eclipse.transformer.maven.configuration.JakartaTransformerRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.transformer.PomConstants.*;

/**
 * Transforms a specified artifact into a new artifact for install and deploy phase.
 *
 * @author yuanxuan
 * @version : JakartaJarTransformerMojo.java, v 0.1 2023年06月29日 12:04 yuanxuan Exp $
 */
@Mojo(name = "jakarta", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution =
	ResolutionScope.COMPILE, threadSafe = true)
public class JakartaJarTransformerMojo extends AbstractMojo {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
	private List<MavenProject> reactorProjects;

	@Parameter(defaultValue = "${mojoExecution}", required = true, readonly = true)
	private MojoExecution mojoExecution;

	/**
	 * The build directory into which the new transformed artifact is written.
	 */
	@Parameter(defaultValue = "${project.build.directory}")
	private File buildDirectory;

	/**
	 * The base name of the transformed artifact.
	 */
	@Parameter(defaultValue = "${project.build.finalName}")
	private String baseName;

	/**
	 * Time stamp for reproducible output archive entries, either formatted as
	 * ISO 8601 yyyy-MM-dd'T'HH:mm:ssXXX or as an int representing seconds since
	 * the epoch (like SOURCE_DATE_EPOCH).
	 */
	@Parameter(defaultValue = "${project.build.outputTimestamp}")
	private String outputTimestamp;

	/**
	 * Attach the transformed artifact to the project.
	 */
	@Parameter(defaultValue = "true")
	private boolean attach;

	@Parameter(defaultValue = "false")
	private boolean skip;

	@Parameter
	private JakartaTransformerRules rules;

	@Component
	private ArtifactHandlerManager artifactHandlerManager;

	private List<Artifact> transformedArtifacts = new ArrayList<>();

	private JsonNode moduleConfig;

	private static final String JAVADOC_EXTENSION = "javadoc";

	private static final String JAVA_SOURCE_EXTENSION = "java-source";

	private static final String JAVADOC_SUFFIX = "-javadoc";

	private static final String JAVA_SOURCE_SUFFIX = "-sources";

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (isSkip()) {
			logger.info("jakarta transform skipped");
			return;
		}

		String extension = artifactHandlerManager.getArtifactHandler(project.getPackaging()).getExtension();
		if ("pom".equals(extension)) {
			return;
		}
		try {
			moduleConfig = match(project.getArtifact());
		} catch (IOException e) {
			throw new MojoExecutionException("fail to read json config");
		}
		if (project.getArtifactId() == null || moduleConfig == null) {
			return;
		}
		// handle pom.xml
		transformPomFile();
		// handle jar
		transformJar();
		// add transform artifact into project attach artifact
		addProjectAttachArtifact();

	}

	/**
	 * Check whether current module need to be transformed.
	 * */
	private JsonNode match(Artifact artifact) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(new File(rules.getPom()));
		JsonNode moduleNode = rootNode.get(MODULES);
		for (JsonNode child: moduleNode) {
			if (!child.hasNonNull(GROUP_ID) || !child.hasNonNull(ARTIFACT_ID)) {
				continue;
			}
			if (child.get(GROUP_ID).asText().equals(artifact.getGroupId()) &&
				child.get(ARTIFACT_ID).asText().equals(artifact.getArtifactId())) {
				return child;
			}
		}
		return null;
	}

	/**
	 * Transform pom.xml of a module.
	 */
	private void transformPomFile() {
		File file = project.getFile();
		if (isFile(file)) {
			String transformedFileName = "pom.xml";
			String transformedFile = getOutput(transformedFileName);
			Transformer transformer = buildTransformer(project.getFile().getAbsolutePath(), transformedFile,
				rules.getImmediates());
			ResultCode rc = transformer.run();

			if (rc != Transformer.ResultCode.SUCCESS_RC) {
				logger.error("fail to transform:{}.rc:{}", file.getAbsolutePath(), rc);
			} else {
				ProjectArtifact projectArtifact = new ProjectArtifact(project);
				projectArtifact.setArtifactId(moduleConfig.hasNonNull(TARGET_ARTIFACT_ID) ?
					moduleConfig.get(TARGET_ARTIFACT_ID).asText() : projectArtifact.getArtifactId());
				projectArtifact.selectVersion(moduleConfig.hasNonNull(TARGET_VERSION) ?
					moduleConfig.get(TARGET_VERSION).asText() : projectArtifact.getVersion());

				projectArtifact.setFile(new File(transformedFile));
				addTransformedArtifact(projectArtifact);
				logger.info("success to transform: {}", file.getAbsolutePath());
			}
		}
	}

	private void transformJar() {
		if (project.getArtifact() == null) {
			return;
		}
		// transform artifact
		doTransformJar(project.getArtifact());
		project.getAttachedArtifacts().stream().filter(artifact ->
			artifact.getType().equalsIgnoreCase(JAVA_SOURCE_EXTENSION) || artifact.getType()
				.equalsIgnoreCase(JAVADOC_EXTENSION)).forEach(this::doTransformJar);
	}

	private void doTransformJar(Artifact artifact) {
		File file = artifact.getFile();
		String suffix = "";
		if (isFile(file)) {
			if (artifact.getType().equalsIgnoreCase(JAVA_SOURCE_EXTENSION)) {
				suffix = JAVA_SOURCE_SUFFIX;
			}
			else if (artifact.getType().equalsIgnoreCase(JAVADOC_EXTENSION)) {
				suffix = JAVADOC_SUFFIX;
			}
			String transformedFileName = generateFileName(file.getName(), suffix);
			String transformedFile = getOutput(transformedFileName);
			Transformer transformer = buildTransformer(file.getAbsolutePath(), transformedFile, rules.getImmediates());
			transformer.logRules();
			ResultCode rc = transformer.run();
			if (rc != Transformer.ResultCode.SUCCESS_RC) {
				logger.error("fail to transform: {}， rc: {}", project.getFile().getAbsolutePath(), rc);
			} else {
				Artifact newArtifact = ArtifactUtils.copyArtifact(artifact);
				newArtifact.setArtifactId(moduleConfig.hasNonNull(TARGET_ARTIFACT_ID) ?
					moduleConfig.get(TARGET_ARTIFACT_ID).asText() : artifact.getArtifactId());
				newArtifact.selectVersion(moduleConfig.hasNonNull(TARGET_VERSION) ?
					moduleConfig.get(TARGET_VERSION).asText() : artifact.getVersion());
				newArtifact.setFile(new File(transformedFile));
				addTransformedArtifact(newArtifact);
				logger.info("success to transform: {}", project.getFile().getAbsolutePath());
			}
		}

	}

	private void addProjectAttachArtifact() {
		transformedArtifacts.forEach(project.getAttachedArtifacts()::add);
	}

	private void addTransformedArtifact(Artifact artifact) {
		if (artifact != null) {
			this.transformedArtifacts.add(artifact);
		}
	}

	private CustomRules createCustomRules() {
		if (rules != null) {
			return new CustomRulesBuilder()
				.setBundles(rules.getBundles())
				.setDirects(rules.getDirects())
				.setImmediates(rules.getImmediates() == null ? new ArrayList<>() : rules.getImmediates())
				.setOverwrite(rules.isOverwrite())
				.setRenames(rules.getRenames())
				.setTexts(rules.getTexts())
				.setPoms(new ArrayList<>(Collections.singletonList(rules.getPom())))
				.setContainerType(ContainerType.Jakarta)
				.setPerClassConstants(rules.getPerClassConstants())
				.setInvert(rules.isInvert())
				.build();
		}
		return new CustomRulesBuilder().setContainerType(ContainerType.Jakarta).setOverwrite(true).build();
	}

	private Transformer buildTransformer(String inputFile, String outputFile, List<String> immediates) {
		CustomRules customRules = createCustomRules();
		if (immediates != null) {
			customRules.getImmediates().addAll(immediates);
		}
		ApiTransformOptions apiTransformOptions = new ApiTransformOptions(customRules, inputFile, outputFile);
		return new Transformer(logger, apiTransformOptions);
	}

	private String getOutput(String fileName) {
		return Paths.get(buildDirectory.toURI()).resolve(fileName).toFile().getAbsolutePath();
	}

	private String generateFileName(String name, String suffix) {
		return moduleConfig.get(TARGET_GROUP_ID).asText() + "-" + (moduleConfig.hasNonNull(TARGET_ARTIFACT_ID)
			? moduleConfig.get(TARGET_ARTIFACT_ID).asText() : project.getArtifact().getArtifactId()) + "-" +
			(moduleConfig.hasNonNull(TARGET_VERSION) ? moduleConfig.get(TARGET_VERSION).asText() :
				project.getArtifact().getVersion()) + suffix + name.substring(name.lastIndexOf("."));
	}

	private boolean isFile(File file) {
		return file != null && file.isFile();
	}

	public MavenProject getProject() {
		return project;
	}

	public List<MavenProject> getReactorProjects() {
		return reactorProjects;
	}

	public MojoExecution getMojoExecution() {
		return mojoExecution;
	}

	public File getBuildDirectory() {
		return buildDirectory;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getOutputTimestamp() {
		return outputTimestamp;
	}

	public void setOutputTimestamp(String outputTimestamp) {
		this.outputTimestamp = outputTimestamp;
	}

	public boolean isAttach() {
		return attach;
	}

	public void setAttach(boolean attach) {
		this.attach = attach;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public JakartaTransformerRules getRules() {
		return rules;
	}

	public void setRules(JakartaTransformerRules rules) {
		this.rules = rules;
	}

	public ArtifactHandlerManager getArtifactHandlerManager() {
		return artifactHandlerManager;
	}
}
