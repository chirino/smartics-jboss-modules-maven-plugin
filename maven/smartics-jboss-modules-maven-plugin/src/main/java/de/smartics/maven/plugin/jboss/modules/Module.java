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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sonatype.aether.artifact.Artifact;

import de.smartics.maven.plugin.jboss.modules.domain.MatchContext;

/**
 * A module descriptor to generate a <code>module.xml</code> file.
 *
 * @see <a
 *      href="https://docs.jboss.org/author/display/MODULES/Module+descriptors">Module
 *      descriptors</a>
 */
public class Module
{
  // ********************************* Fields *********************************

  // --- constants ------------------------------------------------------------

  // --- members --------------------------------------------------------------

  /**
   * The name of the module. Is used for the <code>name</code> attribute in the
   * <code>module.xml</code> base element.
   */
  private String name;

  /**
   * The slot to write to. If empty, the default slot is provided in the
   * {@link de.smartics.maven.plugin.jboss.modules.JBossModulesArchiveMojo#defaultSlot
   * defaultSlot} configuration of the Mojo.
   */
  private String slot;

  /**
   * The path to store the <code>module.xml</code> and all its resources. If not
   * given, the path defaults to the groupId and artifactId in case the groupId
   * does not end with the artifactId. If it does, it defaults to the groupId
   * alone.
   */
  private String basePath;

  /**
   * The list of inclusions.
   */
  private List<Clusion> includes;

  /**
   * The list of exclusions.
   */
  private List<Clusion> excludes;

  /**
   * The map of properties to add to the <code>module.xml</code>.
   */
  private Map<String, String> properties;

  /**
   * The list of module dependencies to be added to those calculated from the
   * BOM.
   */
  private List<Dependency> dependencies;

  // ****************************** Initializer *******************************

  // ****************************** Constructors ******************************

  /**
   * Default constructor.
   */
  public Module()
  {
  }

  // ****************************** Inner Classes *****************************

  // ********************************* Methods ********************************

  // --- init -----------------------------------------------------------------

  // --- get&set --------------------------------------------------------------

  /**
   * Returns the name of the module. Is used for the <code>name</code> attribute
   * in the <code>module.xml</code> base element.
   *
   * @return the name of the module.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the name of the module. Is used for the <code>name</code> attribute in
   * the <code>module.xml</code> base element.
   *
   * @param name the name of the module.
   */
  public void setName(final String name)
  {
    this.name = name;
  }

  /**
   * Returns the slot to write to. If empty, the default slot is provided in the
   * {@link de.smartics.maven.plugin.jboss.modules.JBossModulesArchiveMojo#defaultSlot
   * defaultSlot} configuration of the Mojo.
   *
   * @return the slot to write to.
   */
  public String getSlot()
  {
    return slot;
  }

  /**
   * Sets the slot to write to. If empty, the default slot is provided in the
   * {@link de.smartics.maven.plugin.jboss.modules.JBossModulesArchiveMojo#defaultSlot
   * defaultSlot} configuration of the Mojo.
   *
   * @param slot the slot to write to.
   */
  public void setSlot(final String slot)
  {
    this.slot = slot;
  }

  /**
   * Returns the path to store the <code>module.xml</code> and all its
   * resources. If not given, the path defaults to the groupId and artifactId in
   * case the groupId does not end with the artifactId. If it does, it defaults
   * to the groupId alone.
   *
   * @return the path to store the module.
   */
  public String getBasePath()
  {
    return basePath;
  }

  /**
   * Sets the path to store the <code>module. xml</code> and all its resources.
   * If not given, the path defaults to the groupId and artifactId in case the
   * groupId does not end with the artifactId. If it does, it defaults to the
   * groupId alone.
   *
   * @param basePath the path to store the module.
   */
  public void setBasePath(final String basePath)
  {
    this.basePath = basePath;
  }

  /**
   * Returns the list of inclusions.
   *
   * @return the list of inclusions.
   */
  public List<Clusion> getIncludes()
  {
    return includes;
  }

  /**
   * Sets the list of inclusions.
   *
   * @param includes the list of inclusions.
   */
  public void setIncludes(final List<Clusion> includes)
  {
    this.includes = includes;
  }

  /**
   * Returns the list of exclusions.
   *
   * @return the list of exclusions.
   */
  public List<Clusion> getExcludes()
  {
    return excludes;
  }

  /**
   * Sets the list of exclusions.
   *
   * @param excludes the list of exclusions.
   */
  public void setExcludes(final List<Clusion> excludes)
  {
    this.excludes = excludes;
  }

  /**
   * Returns the map of properties to add to the <code>module. xml</code>.
   *
   * @return the map of properties to add to the <code>module.xml</code>.
   */
  public Map<String, String> getProperties()
  {
    return properties;
  }

  /**
   * Sets the map of properties to add to the <code>module. xml</code>.
   *
   * @param properties the map of properties to add to the
   *          <code>module.xml</code>.
   */
  public void setProperties(final Map<String, String> properties)
  {
    this.properties = properties;
  }

  /**
   * Returns the list of module dependencies to be added to those calculated
   * from the BOM.
   *
   * @return the list of module dependencies to be added to those calculated
   *         from the BOM.
   */
  public List<Dependency> getDependencies()
  {
    return dependencies;
  }

  /**
   * Checks if there are dependencies registered with this module.
   *
   * @return <code>true</code> if there is at least one dependency,
   *         <code>false</code> otherwise.
   */
  public boolean hasDependencies()
  {
    return (dependencies != null && !dependencies.isEmpty());
  }

  /**
   * Sets the list of module dependencies to be added to those calculated from
   * the BOM.
   *
   * @param dependencies the list of module dependencies to be added to those
   *          calculated from the BOM.
   */
  public void setDependencies(final List<Dependency> dependencies)
  {
    this.dependencies = dependencies;
  }

  // --- business -------------------------------------------------------------

  /**
   * Checks if the given artifact matches the module descriptor.
   *
   * @param artifact the artifact to match.
   * @return <code>true</code> if the module descriptor matches the given
   *         artifact, <code>false</code> otherwise.
   */
  public MatchContext match(final Artifact artifact)
  {
    final MatchContext includesContext = cludes(includes, artifact);
    final MatchContext excludesContext = cludes(excludes, artifact);

    final boolean result =
        (includesContext.isMatched() && !excludesContext.isMatched());
    if (includesContext.isMatched())
    {
      return new MatchContext(result, includesContext);
    }
    else
    {
      return new MatchContext(result);
    }
  }

  private MatchContext cludes(final List<Clusion> clusions,
      final Artifact artifact)
  {
    if (clusions != null)
    {
      for (final Clusion clusion : clusions)
      {
        final MatchContext matchContext = clusion.matches(artifact);
        if (matchContext.isMatched())
        {
          return matchContext;
        }
      }
    }
    return new MatchContext(false);
  }

  // --- object basics --------------------------------------------------------

  @Override
  public int hashCode()
  {
    int result = 17;
    result = 37 * result + ObjectUtils.hashCode(name);
    result = 37 * result + ObjectUtils.hashCode(slot);

    return result;
  }

  @Override
  public boolean equals(final Object object)
  {
    if (this == object)
    {
      return true;
    }
    else if (object == null || getClass() != object.getClass())
    {
      return false;
    }

    final Module other = (Module) object;

    return (ObjectUtils.equals(name, other.name) && ObjectUtils.equals(slot,
        other.slot));
  }

  @Override
  public String toString()
  {
    return ToStringBuilder.reflectionToString(this);
  }
}
