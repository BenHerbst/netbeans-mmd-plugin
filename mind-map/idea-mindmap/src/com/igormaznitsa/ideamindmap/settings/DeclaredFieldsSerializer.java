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

package com.igormaznitsa.ideamindmap.settings;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.util.xmlb.annotations.Property;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DeclaredFieldsSerializer implements Serializable {

  private static final long serialVersionUID = -92387498231123L;

  private static final Logger LOGGER = LoggerFactory.getLogger(DeclaredFieldsSerializer.class);

  @Property
  private final Map<String, String> storage = new TreeMap<>(Comparator.naturalOrder());

  public DeclaredFieldsSerializer() {
  }

  public DeclaredFieldsSerializer(@Nonnull final Object object, @Nullable final Converter converter) {
    visitFields(object, (instance, field, fieldName, fieldType) -> {
      try {
        final Object value = field.get(instance);

        if (fieldType.isPrimitive()) {
          if (fieldType == float.class) {
            storage.put(makeName(fieldName, value, false), Integer.toString((Float.floatToIntBits((Float) value))));
          } else if (fieldType == double.class) {
            storage.put(makeName(fieldName, value, false), Long.toString((Double.doubleToLongBits((Double) value))));
          } else {
            storage.put(makeName(fieldName, value, false), value.toString());
          }
        } else if (fieldType == String.class) {
          storage.put(makeName(fieldName, value, false), value == null ? "" : (String) value);
        } else {
          if (converter == null) {
            throw new NullPointerException("Unexpected field type " + fieldType.getName() + ", provide converter!");
          } else {
            final String converted = value == null ? null : converter.asString(fieldType, value);
            storage.put(makeName(fieldName, converted, true), converted);
          }
        }
      } catch (Exception ex) {
        LOGGER.error("Can't make data for field [" + fieldName + ']');
        if (ex instanceof RuntimeException) {
          throw ((RuntimeException) ex);
        } else {
          throw new Error("Can't serialize field [" + fieldName + ']', ex);
        }
      }
    });
  }

  private static void visitFields(@Nonnull final Object object, @Nonnull final FieldVisitor visitor) {
    for (final Field f : object.getClass().getDeclaredFields()) {
      if ((f.getModifiers() & (Modifier.FINAL | Modifier.NATIVE | Modifier.STATIC | Modifier.TRANSIENT)) == 0) {
        f.setAccessible(true);
        visitor.visitField(object, f, f.getName(), f.getType());
      }
    }
  }

  private static String makeName(final String fieldName, final Object value, final boolean needConverter) {
    final StringBuilder result = new StringBuilder(fieldName);
    if (value == null) {
      result.append('.');
    }
    if (needConverter) {
      result.append('@');
    }

    return result.toString();
  }

  private static boolean isNull(final String fieldName) {
    return fieldName.indexOf('.') >= 0;
  }

  private static boolean doesNeedConverter(final String fieldName) {
    return fieldName.indexOf('@') >= 0;
  }

  @Nullable
  public String get(@Nonnull final String fieldName) {
    final String storageFieldName = findStorageFieldName(fieldName);
    return storageFieldName == null ? null : this.storage.get(storageFieldName);
  }

  @Nullable
  public String findStorageFieldName(@Nonnull final String fieldName) {
    for (final String k : this.storage.keySet()) {
      if (k.equals(fieldName)) {
        return k;
      } else if (k.startsWith(fieldName) && (k.length() - fieldName.length()) < 3) {
        final String rest = k.substring(fieldName.length());
        boolean onlySpecialChars = true;
        for (int i = 0; i < rest.length(); i++) {
          if (".@".indexOf(rest.charAt(i)) < 0) {
            onlySpecialChars = false;
            break;
          }
        }
        if (onlySpecialChars) {
          return k;
        }
      }
    }
    return null;
  }

  public void fill(@Nonnull final Object instance, @Nullable final Converter converter) {
    visitFields(instance, (instance1, field, fieldName, fieldType) -> {
      try {
        final String storageFieldName = findStorageFieldName(fieldName);
        if (storageFieldName == null) {
          if (converter == null) {
            throw new NullPointerException("Needed converter for non-saved field, to provide default value [" + fieldName + ']');
          } else {
            field.set(instance1, converter.provideDefaultValue(fieldName, fieldType));
          }
        } else {
          final String value = get(storageFieldName);
          final boolean isNull = isNull(storageFieldName);
          final boolean needsConverter = doesNeedConverter(storageFieldName);
          if (isNull) {
            field.set(instance1, null);
          } else if (needsConverter) {
            field.set(instance1, converter.fromString(fieldType, value));
          } else {
            if (fieldType == boolean.class) {
              field.set(instance1, Boolean.parseBoolean(value));
            } else if (fieldType == byte.class) {
              field.set(instance1, Byte.parseByte(value));
            } else if (fieldType == char.class) {
              field.set(instance1, (char) Integer.parseInt(value));
            } else if (fieldType == short.class) {
              field.set(instance1, Short.parseShort(value));
            } else if (fieldType == int.class) {
              field.set(instance1, Integer.parseInt(value));
            } else if (fieldType == long.class) {
              field.set(instance1, Long.parseLong(value));
            } else if (fieldType == float.class) {
              field.set(instance1, Float.intBitsToFloat(Integer.parseInt(value)));
            } else if (fieldType == double.class) {
              field.set(instance1, Double.longBitsToDouble(Long.parseLong(value)));
            } else if (fieldType == String.class) {
              field.set(instance1, value);
            } else {
              throw new Error("Unexpected primitive type [" + fieldName + " " + fieldType + ']');
            }
          }
        }
      } catch (Exception ex) {
        LOGGER.error("Can't fill field by data [" + fieldName + ']');
        if (ex instanceof RuntimeException) {
          throw (RuntimeException) ex;
        } else {
          throw new Error("Unexpected exception for field processing [" + field + ']', ex);
        }
      }
    });
  }

  public interface Converter {
    @Nullable
    Object fromString(@Nonnull Class<?> fieldType, @Nonnull String value);

    @Nonnull
    String asString(@Nonnull Class<?> fieldType, @Nonnull Object value);

    @Nullable
    Object provideDefaultValue(@Nonnull String fieldName, @Nonnull Class<?> fieldType);
  }

  private interface FieldVisitor {
    void visitField(@Nonnull Object instance, @Nonnull Field field, @Nonnull String fieldName, @Nonnull Class<?> fieldType);
  }
}
