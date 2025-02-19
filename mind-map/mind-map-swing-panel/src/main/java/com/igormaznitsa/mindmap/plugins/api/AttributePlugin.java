/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.mindmap.model.TopicFinder;
import javax.annotation.Nonnull;

/**
 * Interface for plug-ins processing attributes of model topics.
 *
 * @since 1.2
 */
public interface AttributePlugin extends MindMapPlugin, TopicFinder {

  /**
   * Attribute name which can be returned if plug-in doesn't process attributes.
   */
  String NULL_ATTRIBUTE = ",=,";

  /**
   * Get name of attribute key associated with the plug-in.
   *
   * @return the name of the attribute key.
   * @see #NULL_ATTRIBUTE
   */
  @Nonnull
  String getAttributeKey();
}
