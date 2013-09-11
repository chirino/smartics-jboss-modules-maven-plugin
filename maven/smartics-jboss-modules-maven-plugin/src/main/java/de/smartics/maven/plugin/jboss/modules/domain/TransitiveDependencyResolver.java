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

import java.util.Set;

import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.resolution.DependencyResolutionException;

/**
 * Resolves the transitive dependencies for a given artifact.
 */
public interface TransitiveDependencyResolver
{
  // ********************************* Fields *********************************

  // --- constants ------------------------------------------------------------

  // ****************************** Initializer *******************************

  // ****************************** Inner Classes *****************************

  // ********************************* Methods ********************************

  // --- get&set --------------------------------------------------------------

  // --- business -------------------------------------------------------------

  // /**
  // * Resolves the transitive dependencies for the given artifact.
  // *
  // * @param artifact the artifact whose calculation of transitive dependencies
  // * is requested.
  // * @return the set of transitive dependencies.
  // */
  // Set<Artifact> resolve(final Artifact artifact)
  // throws DependencyResolutionException;

  /**
   * Resolves the transitive dependencies for the given dependency.
   *
   * @param dependency the dependency whose calculation of transitive
   *          dependencies is requested.
   * @return the set of transitive dependencies.
   * @throws DependencyResolutionException if the dependency cannot be resolved.
   */
  Set<Dependency> resolve(final Dependency dependency)
    throws DependencyResolutionException;

  // --- object basics --------------------------------------------------------

}
