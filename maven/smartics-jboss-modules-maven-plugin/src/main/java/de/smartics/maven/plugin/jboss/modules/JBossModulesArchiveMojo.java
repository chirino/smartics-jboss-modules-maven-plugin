/*
 * Copyright 2013 smartics, Kronseder & Reiner GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.smartics.maven.plugin.jboss.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.graph.selector.AndDependencySelector;
import org.sonatype.aether.util.graph.selector.ExclusionDependencySelector;
import org.sonatype.aether.util.graph.selector.OptionalDependencySelector;
import org.sonatype.aether.util.graph.selector.ScopeDependencySelector;

import de.smartics.maven.plugin.jboss.modules.aether.Mapper;
import de.smartics.maven.plugin.jboss.modules.aether.MavenRepository;
import de.smartics.maven.plugin.jboss.modules.aether.MavenResponse;
import de.smartics.maven.plugin.jboss.modules.aether.MojoRepositoryBuilder;
import de.smartics.maven.plugin.jboss.modules.domain.ExecutionContext;
import de.smartics.maven.plugin.jboss.modules.domain.ModuleBuilder;
import de.smartics.maven.plugin.jboss.modules.domain.ModuleMap;
import de.smartics.maven.plugin.jboss.modules.domain.SlotStrategy;
import de.smartics.maven.plugin.jboss.modules.domain.TransitiveDependencyResolver;

/**
 * Generates a archive containing modules from a BOM project.
 *
 * @since 1.0
 * @description Generates a archive containing modules from a BOM project.
 */
@Mojo(name = "create-modules-archive", threadSafe = true,
    requiresProject = true,
    requiresDependencyResolution = ResolutionScope.TEST,
    defaultPhase = LifecyclePhase.PACKAGE)
public final class JBossModulesArchiveMojo extends AbstractMojo
{
  // ********************************* Fields *********************************

  // --- constants ------------------------------------------------------------

  // --- members --------------------------------------------------------------

  // ... Mojo infrastructure ..................................................

  /**
   * The Maven project.
   *
   * @since 1.0
   */
  @Component
  private MavenProject project;

  /**
   * The Maven session.
   */
  @Component
  private MavenSession session;

  /**
   * Resolver for artifact repositories.
   *
   * @since 1.0
   */
  @Component
  private RepositorySystem repositorySystem;

  /**
   * The current repository/network configuration of Maven.
   */
  @Parameter(defaultValue = "${repositorySystemSession}")
  private RepositorySystemSession repositorySession;

  /**
   * The project's remote repositories to use for the resolution of
   * dependencies.
   */
  @Parameter(defaultValue = "${project.remoteProjectRepositories}")
  private List<RemoteRepository> remoteRepos;

  /**
   * Helper to add attachments to the build.
   *
   * @since 1.0
   */
  @Component
  private MavenProjectHelper projectHelper;

  /**
   * Helper to create an archive.
   *
   * @since 1.0
   */
  @Component(role = Archiver.class, hint = "jar")
  private JarArchiver jarArchiver;

  /**
   * The archive configuration to use. See <a
   * href="http://maven.apache.org/shared/maven-archiver/index.html">Maven
   * Archiver Reference</a>.
   *
   * @since 1.0
   */
  @Parameter
  private final MavenArchiveConfiguration archive =
      new MavenArchiveConfiguration();

  /**
   * A simple flag to skip the execution of this MOJO. If set on the command
   * line use <code>-Dsmartics-jboss-modules.skip</code>.
   *
   * @since 1.0
   */
  @Parameter(property = "smartics-jboss-modules.skip", defaultValue = "false")
  private boolean skip;

  /**
   * The verbose level. If set on the command line use
   * <code>-Dsmartics-jboss-modules.verbose</code>.
   *
   * @since 1.0
   */
  @Parameter(property = "smartics-jboss-modules.verbose",
      defaultValue = "false")
  private boolean verbose;

  /**
   * Allows to attach the generated modules as a ZIP archive to the build.
   *
   * @since 1.0
   */
  @Parameter(defaultValue = "true")
  private boolean attach;

  /**
   * Controls the system to act as being offline (<code>true</code>) or not (
   * <code>false</code>).
   *
   * @since 1.0
   */
  @Parameter(defaultValue = "${offline}")
  private boolean offline;

  /**
   * The name of the default slot to write to. If not specified, the major
   * version of the dependency will be used as slot value.
   *
   * @since 1.0
   */
  @Parameter(defaultValue = "main")
  private String defaultSlot;

  /**
   * The module descriptors. The descriptors match dependencies in the project's
   * POM and creates a module for each.
   *
   * @since 1.0
   */
  @Parameter
  private List<Module> modules;

  /**
   * The folder to write the module structure to.
   *
   * @since 1.0
   */
  @Parameter(defaultValue = "${project.build.directory}/jboss-modules")
  private File targetFolder;

  /**
   * The file to attach, containing the JBoss modules.
   *
   * @since 1.0
   */
  @Parameter(
      defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}-jboss-modules.jar")
  private File modulesArchive;

  // ****************************** Initializer *******************************

  // ****************************** Constructors ******************************

  // ****************************** Inner Classes *****************************

  // ********************************* Methods ********************************

  // --- init -----------------------------------------------------------------

  // --- get&set --------------------------------------------------------------

  // --- business -------------------------------------------------------------

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException
  {
    final Log log = getLog();

    if (skip)
    {
      log.info("Skipping creating archive for JBoss modules since skip='true'.");
      return;
    }

    this.repositorySession = adjustSession();

    final Set<Dependency> rootDependencies = resolveRootDependencies();
    final List<Dependency> dependencies = resolveDependencies(rootDependencies);

    final ExecutionContext context = createContext(dependencies);
    for (final Entry<Module, List<Dependency>> entry : context.getModuleMap()
        .toMap().entrySet())
    {
      final Module module = entry.getKey();
      final List<Dependency> moduleDependencies = entry.getValue();
      final ModuleBuilder builder =
          new ModuleBuilder(context, module, moduleDependencies);
      try
      {
        builder.create();
      }
      catch (final IOException e)
      {
        throw new MojoExecutionException("Cannot write module '"
                                         + entry.getKey().getName() + "'.", e);
      }
    }

    attach();
  }

  private DefaultRepositorySystemSession adjustSession()
  {
    final DefaultRepositorySystemSession session =
        new DefaultRepositorySystemSession(repositorySession);
    final AndDependencySelector selector =
        new AndDependencySelector(new ScopeDependencySelector("test"),
            new OptionalDependencySelector(), new ExclusionDependencySelector());
    session.setDependencySelector(selector);
    return session;
  }

  private void attach() throws MojoExecutionException
  {
    if (!attach)
    {
      return;
    }
    if (!targetFolder.isDirectory())
    {
      getLog().info("Nothing to attach.");
      return;
    }

    try
    {

      jarArchiver.addDirectory(targetFolder);

      final MavenArchiver archiver = new MavenArchiver();
      archiver.setArchiver(jarArchiver);
      archiver.setOutputFile(modulesArchive);
      archiver.createArchive(session, project, archive);
      projectHelper.attachArtifact(project, "jar", "jboss-modules",
          modulesArchive);
    }
    catch (final Exception e)
    {
      final String message =
          String.format("Cannot create archive '%s'.: %s",
              modulesArchive.getAbsolutePath(), e.getMessage());
      throw new MojoExecutionException(message, e);
    }
  }

  private ExecutionContext createContext(final List<Dependency> dependencies)
  {
    final ExecutionContext.Builder builder = new ExecutionContext.Builder();
    builder.withTargetFolder(targetFolder);

    final TransitiveDependencyResolver resolver =
        createDirectDependencyResolver();
    builder.with(resolver);

    final SlotStrategy slotStrategy = SlotStrategy.fromString(this.defaultSlot);
    builder.with(slotStrategy);

    final ModuleMap moduleMap = new ModuleMap(modules, dependencies);
    builder.with(moduleMap);

    if (verbose)
    {
      getLog().info("Modules:\n" + moduleMap.toString());
    }

    return builder.build();
  }

  @SuppressWarnings("unchecked")
  private Set<Dependency> resolveRootDependencies()
    throws MojoExecutionException
  {
    final Set<Dependency> rootDependencies = new HashSet<Dependency>();

    final List<Dependency> projectDependencies = project.getDependencies();
    rootDependencies.addAll(projectDependencies);

    final DependencyManagement management = project.getDependencyManagement();
    final TransitiveDependencyResolver resolver =
        createDirectDependencyResolver();

    final Mapper mapper = new Mapper();
    for (final org.apache.maven.model.Dependency mavenDependency : management
        .getDependencies())
    {
      final Dependency dependency = mapper.map(mavenDependency);
      try
      {
        resolver.resolve(dependency);
        rootDependencies.add(dependency);
      }
      catch (final DependencyResolutionException e)
      {
        throw new MojoExecutionException("Cannot resolve root dependencies.", e);
      }
    }

    return rootDependencies;
  }

  private List<Dependency> resolveDependencies(
      final Collection<Dependency> rootDependencies)
    throws MojoExecutionException
  {
    final List<Dependency> dependencies = new ArrayList<Dependency>();
    final TransitiveDependencyResolver resolver =
        createDirectDependencyResolver();
    for (final Dependency dependency : rootDependencies)
    {
      try
      {
        dependencies.addAll(resolver.resolve(dependency));
      }
      catch (final DependencyResolutionException e)
      {
        getLog().error("Cannot resolve dependency: " + e.getMessage());
      }
    }
    return dependencies;
  }

  private TransitiveDependencyResolver createDirectDependencyResolver()
  {
    final List<DependencyFilter> dependencyFilters =
        new ArrayList<DependencyFilter>();
    final MojoRepositoryBuilder builder = new MojoRepositoryBuilder();
    builder.with(repositorySystem).with(repositorySession).with(remoteRepos)
        .withDependencyFilters(dependencyFilters).withOffline(offline).build();
    final MavenRepository repository = builder.build();

    return new TransitiveDependencyResolver()
    {
      @Override
      public Set<Dependency> resolve(final Dependency dependency)
        throws DependencyResolutionException
      {
        final MavenResponse response = repository.resolve(dependency);
        final Set<Dependency> dependencies = response.getDependencies();
        return dependencies;
      }
    };
  }
  // --- object basics --------------------------------------------------------

}
