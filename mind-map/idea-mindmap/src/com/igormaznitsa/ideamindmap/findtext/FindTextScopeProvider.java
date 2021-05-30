/*
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.igormaznitsa.ideamindmap.findtext;

import javax.annotation.Nonnull;

public interface FindTextScopeProvider {

  boolean toSearchIn(@Nonnull SearchTextScope scope);

  enum SearchTextScope {
    IN_TOPIC_TEXT,
    IN_TOPIC_NOTES,
    IN_TOPIC_FILES,
    IN_TOPIC_URI,
    CASE_INSENSETIVE
  }
}
