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
package de.smartics.maven.plugin.jboss.modules.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.smartics.maven.plugin.jboss.modules.Module;
import de.smartics.util.lang.Arg;

/**
 * Maps artifacts to their modules.
 */
@NotThreadSafe
public final class ModuleMap
{
  // ********************************* Fields *********************************

  // --- constants ------------------------------------------------------------

  // --- members --------------------------------------------------------------

  /**
   * The modules encountered or declared.
   */
  private final List<Module> modules;

  /**
   * Maps modules to their dependencies.
   */
  private final Multimap<Module, Dependency> module2Dependency = HashMultimap
      .create();

  /**
   * Maps a dependency to its module.
   */
  private final Map<Dependency, Module> dependency2Module =
      new HashMap<Dependency, Module>();

  // ****************************** Initializer *******************************

  // ****************************** Constructors ******************************

  /**
   * Constructor to create an empty map.
   */
  public ModuleMap()
  {
    this(new ArrayList<Module>());
  }

  /**
   * Default constructor.
   *
   * @param modules the configured modules.
   */
  public ModuleMap(final List<Module> modules)
  {
    this.modules = modules;
  }

  /**
   * Convenience constructor to initialize with values.
   *
   * @param modules the configured modules.
   * @param dependencies the dependencies to add.
   */
  public ModuleMap(final List<Module> modules,
      final List<Dependency> dependencies)
  {
    this.modules =
        new ArrayList<Module>(modules != null ? modules
            : new ArrayList<Module>());

    initDependencies(dependencies);
  }

  // ****************************** Inner Classes *****************************

  // ********************************* Methods ********************************

  // --- init -----------------------------------------------------------------

  private void initDependencies(final List<Dependency> dependencies)
  {
    for (final Dependency dependency : dependencies)
    {
      add(dependency);
    }
  }

  // --- get&set --------------------------------------------------------------

  /**
   * Returns the map of modules.
   *
   * @return the map of modules.
   */
  public Map<Module, List<Dependency>> toMap()
  {
    final Map<Module, List<Dependency>> map =
        new LinkedHashMap<Module, List<Dependency>>();
    for (final Entry<Module, Collection<Dependency>> entry : module2Dependency
        .asMap().entrySet())
    {
      final List<Dependency> list = new ArrayList<Dependency>(entry.getValue());
      map.put(entry.getKey(), list);
    }
    return map;
  }

  // --- business -------------------------------------------------------------

  /**
   * Adds the given dependency to its module. If the dependency has already been
   * added, the already associated module is returned without adding the
   * dependency a second time.
   *
   * @param dependency the dependency to add.
   * @return the module the dependency is associated with.
   */
  public Module add(final Dependency dependency)
  {
    final Module alreadyStoredModule = dependency2Module.get(dependency);
    if (alreadyStoredModule != null)
    {
      return alreadyStoredModule;
    }

    for (final Module module : modules)
    {
      final MatchContext matchContext = module.match(dependency.getArtifact());
      if (matchContext.isMatched())
      {
        if (matchContext.hasGroupMatch())
        {
          final Module newModule = createModule(matchContext, module);
          storeArtifact(newModule, dependency);
          return newModule;
        }
        else
        {
          storeArtifact(module, dependency);
          return module;
        }
      }
    }

    final Module module = createModule(dependency);
    storeArtifact(module, dependency);
    return module;
  }

  private void storeArtifact(final Module module, final Dependency dependency)
  {
    module2Dependency.put(module, dependency);
    dependency2Module.put(dependency, module);
  }

  private Module createModule(final MatchContext matchContext,
      final Module originalModule)
  {
    final Module module = new Module();

    final String name = matchContext.translateName(originalModule.getName());
    module.setName(name);

    return module;
  }

  private Module createModule(final Dependency dependency)
  {
    final Module module = new Module();

    final Artifact artifact = dependency.getArtifact();
    final String groupId = artifact.getGroupId();
    final String artifactId = artifact.getArtifactId();

    final String name = createName(groupId, artifactId);

    module.setName(name);
    return module;
  }

  /**
   * Constructs a default module name for a given group ID and artifact ID.
   *
   * @param groupId the group ID of the artifact to create a module name for.
   * @param artifactId the artifact ID of the artifact to create a module name
   *          for.
   * @return a default module name.
   * @throws NullPointerException if any of {@code groupId} or
   *           {@code artifactId} is <code>null</code>.
   * @throws IllegalArgumentException if any of {@code groupId} or
   *           {@code artifactId} is blank.
   */
  static String createName(final String groupId, final String artifactId)
    throws NullPointerException, IllegalArgumentException
  {
    Arg.checkNotBlank("groupId", groupId);
    Arg.checkNotBlank("artifactId", artifactId);

    final String name;
    if (groupId.equals(artifactId) || groupId.endsWith('.' + artifactId))
    {
      name = groupId;
    }
    else
    {
      name = groupId + '.' + artifactId;
    }
    return name;
  }

  /**
   * Returns the module for the given dependency.
   *
   * @param dependency the artifact whose module is requested.
   * @return the module of the dependency.
   */
  @CheckForNull
  public Module getModule(final Dependency dependency)
  {
    Module module = dependency2Module.get(dependency);

    if(module == null)
    {
      module = add(dependency);
    }
    return module;
  }

  // --- object basics --------------------------------------------------------

  @Override
  public String toString()
  {
    final StringBuilder buffer = new StringBuilder(2048);
    for (final Entry<Module, Collection<Dependency>> entry : module2Dependency
        .asMap().entrySet())
    {
      final Module module = entry.getKey();
      buffer.append('\n').append(module.getName()).append(':');
      for (final Dependency dependency : entry.getValue())
      {
        buffer.append("\n  ").append(dependency.getArtifact().getArtifactId());
      }
    }

    return buffer.toString();
  }
}
