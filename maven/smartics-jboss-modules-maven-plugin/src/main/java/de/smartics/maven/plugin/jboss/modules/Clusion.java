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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.sonatype.aether.artifact.Artifact;

import de.smartics.maven.plugin.jboss.modules.domain.MatchContext;

/**
 * Models an inclusion or exclusion. An include/exclude matches if all given
 * information matches (that is: <code>and</code>ed).
 */
public class Clusion
{
  // ********************************* Fields *********************************

  // --- constants ------------------------------------------------------------

  // --- members --------------------------------------------------------------

  /**
   * The groupId to match. May contain wildcards (<code>*</code>).
   */
  private String groupId;

  /**
   * The groupId pattern to match. May be <code>null</code>, if groupId does not
   * specify a pattern.
   */
  private Pattern groupIdPattern;

  /**
   * The artifactId to match. May contain wildcards (<code>*</code>).
   */
  private String artifactId;

  /**
   * The artifactId pattern to match. May be <code>null</code>, if artifactId
   * does not specify a pattern.
   */
  private Pattern artifactIdPattern;

  // ****************************** Initializer *******************************

  // ****************************** Constructors ******************************

  /**
   * Default constructor.
   */
  public Clusion()
  {
  }

  // ****************************** Inner Classes *****************************

  // ********************************* Methods ********************************

  // --- init -----------------------------------------------------------------

  // --- get&set --------------------------------------------------------------

  /**
   * Returns the groupId to match. May contain a regexp.
   *
   * @return the groupId to match.
   */
  public String getGroupId()
  {
    return groupId;
  }

  /**
   * Sets the groupId to match. May contain a regexp.
   *
   * @param groupId the groupId to match.
   */
  public void setGroupId(final String groupId)
  {
    this.groupId = groupId;
    groupIdPattern = compilePattern(groupId);
  }

  private static Pattern compilePattern(final String pattern)
  {
    if (StringUtils.isNotBlank(pattern))
    {
      try
      {
        return Pattern.compile(pattern);
      }
      catch (final PatternSyntaxException e)
      {
        // ignore
      }
    }
    return null;
  }

  /**
   * Returns the artifactId to match. May contain a regexp.
   *
   * @return the artifactId to match.
   */
  public String getArtifactId()
  {
    return artifactId;
  }

  /**
   * Sets the artifactId to match. May contain a regexp.
   *
   * @param artifactId the artifactId to match.
   */
  public void setArtifactId(final String artifactId)
  {
    this.artifactId = artifactId;
    artifactIdPattern = compilePattern(artifactId);
  }

  // --- business -------------------------------------------------------------

  // --- object basics --------------------------------------------------------

  @Override
  public String toString()
  {
    return ObjectUtils.toString(groupId) + ':'
           + ObjectUtils.toString(artifactId);
  }

  /**
   * Checks id tge clusion matches the artifact.
   *
   * @param artifact the artifact to match.
   * @return a context to access the match result, with <code>true</code> if the
   *         artifact matches groupId and artifactId, <code>false</code>
   *         otherwise.
   */
  public MatchContext matches(final Artifact artifact)
  {
    final MatchContext matchesGroupId =
        matches(groupIdPattern, groupId, artifact);
    if (matchesGroupId != null && !matchesGroupId.isMatched())
    {
      return new MatchContext(false);
    }
    final MatchContext matchesArtifactId =
        matches(artifactIdPattern, artifactId, artifact);

    final boolean result =
        (matchesGroupId != null && matchesGroupId.isMatched() && (matchesArtifactId == null || matchesArtifactId
            .isMatched()))
            || (matchesArtifactId != null && matchesArtifactId.isMatched());

    final MatchContext context = new MatchContext(result, matchesArtifactId);
    return context;
  }

  private static MatchContext matches(final Pattern pattern, final String id,
      final Artifact artifact)
  {
    final String input = artifact.getArtifactId();
    if (pattern != null)
    {
      final Matcher matcher = pattern.matcher(input);
      return new MatchContext(matcher);
    }

    if (StringUtils.isNotBlank(id))
    {
      return new MatchContext(id.equals(input));
    }

    return null;
  }
}
