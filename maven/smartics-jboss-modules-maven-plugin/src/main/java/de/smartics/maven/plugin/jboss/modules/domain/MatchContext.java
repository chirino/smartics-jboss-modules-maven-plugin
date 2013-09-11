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

import java.util.regex.Matcher;

/**
 * Contains the result of a regular expression match.
 */
public final class MatchContext
{
  // ********************************* Fields *********************************

  // --- constants ------------------------------------------------------------

  // --- members --------------------------------------------------------------

  /**
   * The result of the match.
   */
  private final boolean result;

  /**
   * The matcher to access group information.
   */
  private final Matcher matcher;

  // ****************************** Initializer *******************************

  // ****************************** Constructors ******************************

  /**
   * Default constructor with a non-regexp match result.
   *
   * @param result the result of the match.
   */
  public MatchContext(final boolean result)
  {
    this.result = result;
    this.matcher = null;
  }

  /**
   * Default constructor with a matcher.
   *
   * @param matcher the matcher to access group information.
   */
  public MatchContext(final Matcher matcher)
  {
    this.result = matcher.matches();
    this.matcher = matcher;
  }

  /**
   * Constructor with a context.
   *
   * @param result the result of the match.
   * @param context the context of a match to derive from.
   */
  public MatchContext(final boolean result, final MatchContext context)
  {
    this.result = result;
    this.matcher =
        context != null && context.isMatched() ? context.matcher : null;
  }

  // ****************************** Inner Classes *****************************

  // ********************************* Methods ********************************

  // --- init -----------------------------------------------------------------

  // --- get&set --------------------------------------------------------------

  /**
   * Checks if the match was successful.
   *
   * @return <code>true</code> if the match was successful, <code>false</code>
   *         otherwise.
   */
  public boolean isMatched()
  {
    return result;
  }

  /**
   * Translates the name if it contains placeholders with the matching groups.
   *
   * @param input the input name that may contain placeholders.
   * @return the translated string. It is the input string, if {@code input}
   *         does not contain any placeholders.
   */
  public String translateName(final String input)
  {
    if (matcher != null && isMatched())
    {
      final int groupCount = matcher.groupCount();
      if (groupCount > 0)
      {
        String translation = input;
        for (int group = 1; group <= groupCount; group++)
        {
          final String replacement = matcher.group(group);
          translation = translation.replace("$" + group, replacement);
        }
        return translation;
      }
    }
    return input;
  }

  /**
   * Checks if the match produced at least one group match.
   *
   * @return <code>true</code> if at least one group is matched,
   *         <code>false</code> otherwise.
   */
  public boolean hasGroupMatch()
  {
    if (matcher != null && isMatched())
    {
      final int groupCount = matcher.groupCount();
      return (groupCount > 0);
    }
    return false;
  }

  // --- business -------------------------------------------------------------

  // --- object basics --------------------------------------------------------

}
