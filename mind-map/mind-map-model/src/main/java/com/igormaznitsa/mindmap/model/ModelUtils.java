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

package com.igormaznitsa.mindmap.model;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.model.nio.Path;
import com.igormaznitsa.mindmap.model.nio.Paths;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ModelUtils {

  public static final Comparator<String> STRING_COMPARATOR = new StringComparator();
  private static final Logger LOGGER = LoggerFactory.getLogger(ModelUtils.class);

  private static final Pattern UNESCAPE_BR = Pattern.compile("(?i)\\<\\s*?br\\s*?\\/?\\>"); //NOI18N
  private static final Pattern MD_ESCAPED_PATTERN =
      Pattern.compile("(\\\\[\\\\`*_{}\\[\\]()#<>+-.!])"); //NOI18N
  private static final String MD_ESCAPED_CHARS = "\\`*_{}[]()#<>+-.!"; //NOI18N
  private static final Pattern URI_QUERY_PARAMETERS = Pattern.compile("\\&?([^=]+)=([^&]*)");
  //NOI18N

  private ModelUtils() {
  }

  public static int calcCharsOnStart(final char chr, @Nonnull final String text) {
    int result = 0;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == chr) {
        result++;
      } else {
        break;
      }
    }
    return result;
  }

  public static boolean onlyFromChar(@Nonnull final String line, final char chr) {
    if (line.isEmpty()) {
      return false;
    }
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) != chr) {
        return false;
      }
    }
    return true;
  }

  @Nonnull
  @MustNotContainNull
  public static Object[] joinArrays(@Nonnull @MustNotContainNull final Object[]... arrs) {
    int totalLen = 0;
    for (final Object[] a : arrs) {
      totalLen += a.length;
    }

    final Object[] result = new Object[totalLen];

    int pos = 0;
    for (final Object[] a : arrs) {
      System.arraycopy(a, 0, result, pos, a.length);
      pos += a.length;
    }
    return result;
  }

  @Nonnull
  public static String makePreBlock(@Nonnull final String text) {
    return "<pre>" + escapeTextForPreBlock(text) + "</pre>"; //NOI18N
  }

  @Nonnull
  public static String escapeTextForPreBlock(@Nonnull final String text) {
    final int length = text.length();
    final StringBuilder result = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      final char chr = text.charAt(i);

      switch (chr) {
        case '\"':
          result.append("&quot;");
          break;
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        default: {
          result.append(chr);
        }
        break;
      }
    }

    return result.toString();
  }

  @Nonnull
  public static String makeMDCodeBlock(@Nonnull final String text) throws IOException {
    final int maxQuotes = calcMaxLengthOfBacktickQuotesSubstr(text) + 1;
    final StringBuilder result = new StringBuilder(text.length() + 16);
    writeChar(result, '`', maxQuotes);
    result.append(text);
    writeChar(result, '`', maxQuotes);
    return result.toString();
  }

  @Nonnull
  public static String escapeMarkdownStr(@Nonnull final String text) {
    final StringBuilder buffer = new StringBuilder(text.length() * 2);
    for (final char c : text.toCharArray()) {
      if (c == '\n') {
        buffer.append("<br/>"); //NOI18N
        continue;
      } else if (Character.isISOControl(c)) {
        continue;
      } else if (MD_ESCAPED_CHARS.indexOf(c) >= 0) {
        buffer.append('\\');
      }

      buffer.append(c);
    }
    return buffer.toString();
  }

  public static int calcMaxLengthOfBacktickQuotesSubstr(@Nullable final String text) {
    int result = 0;
    if (text != null) {
      int pos = 0;
      while (pos >= 0) {
        pos = text.indexOf('`', pos);
        if (pos >= 0) {
          int found = 0;
          while (pos < text.length() && text.charAt(pos) == '`') {
            found++;
            pos++;
          }
          result = Math.max(result, found);
        }
      }
    }
    return result;
  }

  public static void writeChar(@Nonnull final Appendable out, final char chr, final int times)
      throws IOException {
    for (int i = 0; i < times; i++) {
      out.append(chr);
    }
  }

  @Nonnull
  public static String unescapeMarkdownStr(@Nonnull final String text) {
    String unescaped = UNESCAPE_BR.matcher(text).replaceAll("\n"); //NOI18N
    final StringBuffer result = new StringBuffer(text.length());
    final Matcher escaped = MD_ESCAPED_PATTERN.matcher(unescaped);
    while (escaped.find()) {
      final String group = escaped.group(1);
      escaped.appendReplacement(result, Matcher.quoteReplacement(group.substring(1)));
    }
    escaped.appendTail(result);
    return result.toString();
  }

  @Nonnull
  public static String makeShortTextVersion(@Nonnull String text, final int maxLength) {
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength) + "..."; //NOI18N
    }
    return text;
  }

  public static int countLines(@Nonnull final String text) {
    int result = 1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        result++;
      }
    }
    return result;
  }

  @Nonnull
  @MustNotContainNull
  public static String[] breakToLines(@Nonnull final String text) {
    final int lineNum = countLines(text);
    final String[] result = new String[lineNum];
    final StringBuilder line = new StringBuilder();

    int index = 0;

    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        result[index++] = line.toString();
        line.setLength(0);
      } else {
        line.append(text.charAt(i));
      }
    }
    result[index] = line.toString();
    return result;
  }

  @Nonnull
  public static String makeQueryStringForURI(@Nullable final Properties properties) {
    if (properties == null || properties.isEmpty()) {
      return ""; //NOI18N
    }
    final StringBuilder buffer = new StringBuilder();

    final List<String> orderedkeys = new ArrayList<>(properties.stringPropertyNames());
    Collections.sort(orderedkeys);

    for (final String k : orderedkeys) {
      try {
        final String encodedKey = URLEncoder.encode(k, "UTF-8"); //NOI18N
        final String encodedValue = URLEncoder.encode(properties.getProperty(k), "UTF-8"); //NOI18N

        if (buffer.length() > 0) {
          buffer.append('&');
        }
        buffer.append(encodedKey).append('=').append(encodedValue);
      } catch (UnsupportedEncodingException ex) {
        LOGGER.error("Can't encode URI query", ex); //NOI18N
        throw new Error("Unexpected exception, can't find UTF-8 charset!"); //NOI18N
      }
    }
    return buffer.toString();
  }

  @Nonnull
  public static Properties extractQueryPropertiesFromURI(@Nonnull final URI uri) {
    final Properties result = new Properties();

    final String rawQuery = uri.getRawQuery();
    if (rawQuery != null) {
      final Matcher matcher = URI_QUERY_PARAMETERS.matcher(rawQuery);

      try {
        while (matcher.find()) {
          final String key = URLDecoder.decode(matcher.group(1), "UTF-8"); //NOI18N
          final String value = URLDecoder.decode(matcher.group(2), "UTF-8"); //NOI18N
          result.put(key, value);
        }
      } catch (UnsupportedEncodingException ex) {
        LOGGER.error("Can't decode URI query", ex); //NOI18N
        throw new Error("Unexpected exception, can't find UTF-8 charset!"); //NOI18N
      }
    }

    return result;
  }

  @Nonnull
  private static String char2UriHexByte(final char ch) {
    final String s = Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    return '%' + (s.length() < 2 ? "0" : "") + s; //NOI18N //NOI18N
  }

  @Nonnull
  public static String encodeForURI(@Nonnull final String s) {
    final StringBuilder result = new StringBuilder();

    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || "-_.~".indexOf(c) >= 0) { //NOI18N
        result.append(c);
      } else {
        if (":/?#[]@!$^'()*+,;= ".indexOf(c) >= 0) { //NOI18N
          result.append(char2UriHexByte(c));
        } else {
          result.append(c);
        }
      }
    }

    return result.toString();
  }

  @Nullable
  public static File makeFileForPath(@Nullable final String path) {
    if (path == null || path.isEmpty()) {
      return null;
    }
    if (path.startsWith("file:")) { //NOI18N
      try {
        return new File(new URI(normalizeFileURI(path)));
      } catch (URISyntaxException ex) {
        LOGGER.error("URISyntaxException for " + path, ex); //NOI18N
        return null;
      }
    } else {
      return new File(path);
    }
  }

  @Nonnull
  public static String escapeURIPath(@Nonnull final String text) {
    final String chars = "% :<>?"; //NOI18N
    String result = text;
    for (final char ch : chars.toCharArray()) {
      result = result.replace(Character.toString(ch),
          "%" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH)); //NOI18N
    }

    return result;
  }

  @Nonnull
  public static String removeISOControls(@Nonnull final String text) {
    StringBuilder result = null;
    boolean detected = false;
    for (int i = 0; i < text.length(); i++) {
      final char ch = text.charAt(i);
      if (detected) {
        if (!Character.isISOControl(ch)) {
          result.append(ch);
        }
      } else {
        if (Character.isISOControl(ch)) {
          detected = true;
          result = new StringBuilder(text.length());
          result.append(text, 0, i);
        }
      }
    }
    return detected ? result.toString() : text;
  }

  @Nonnull
  private static String normalizeFileURI(@Nonnull final String fileUri) {
    final int schemePosition = fileUri.indexOf(':');
    final String scheme =
        schemePosition < 0 ? "" : fileUri.substring(0, schemePosition + 1); //NOI18N
    final String chars = " :<>?"; //NOI18N
    String result = fileUri.substring(scheme.length());
    for (final char ch : chars.toCharArray()) {
      result = result.replace(Character.toString(ch),
          "%" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH)); //NOI18N
    }
    return scheme + result;
  }

  @Nullable
  public static URI toURI(@Nullable final Path path) {
    if (path == null) {
      return null;
    }
    try {
      final StringBuilder buffer = new StringBuilder();

      final Path root = path.getRoot();
      if (root != null) {
        buffer.append(root.toString().replace('\\', '/'));
      }

      for (final Path p : path) {
        if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) != '/') {
          buffer.append('/');
        }
        buffer.append(encodeForURI(p.toFile().getName()));
      }

      if (path.isAbsolute()) {
        buffer.insert(0, "file://" + (root == null ? "/" : "")); //NOI18N
      }

      return new URI(buffer.toString());
    } catch (Exception ex) {
      throw new IllegalArgumentException("Can't convert path to URI: " + path, ex); //NOI18N
    }
  }

  @Nonnull
  public static File toFile(@Nonnull final URI uri) {
    final List<String> pathItems = new ArrayList<>();

    final String authority = uri.getAuthority();
    if (authority != null && !authority.isEmpty()) {
      pathItems.add(authority);
    }

    final String[] splittedPath = uri.getPath().split("\\/");
    boolean separator = false;
    if (splittedPath.length == 0) {
      separator = true;
    } else {
      for (final String s : splittedPath) {
        if (!s.isEmpty()) {
          pathItems.add(separator ? File.separatorChar + s : s);
          separator = false;
        } else {
          separator = true;
        }
      }
    }

    if (separator) {
      pathItems.add(File.separator);
    }

    final String[] fullArray = pathItems.toArray(new String[0]);
    final String[] next = Arrays.copyOfRange(fullArray, 1, fullArray.length);
    return Paths.get(fullArray[0], next).toFile();
  }

  private static final class StringComparator implements Comparator<String>, Serializable {

    private static final long serialVersionUID = -68309989264175879L;

    @Override
    public int compare(@Nonnull final String o1, @Nonnull final String o2) {
      return o1.compareTo(o2);
    }
  }

}
