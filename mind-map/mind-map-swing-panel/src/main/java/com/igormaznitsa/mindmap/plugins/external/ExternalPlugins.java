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

package com.igormaznitsa.mindmap.plugins.external;

import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.commons.version.VersionValidator;
import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;

public class ExternalPlugins {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalPlugins.class);
  private static final String PLUGIN_EXTENSION = "mmdp";
  private final File pluginsFolder;
  private final VersionValidator pluginApiValidator;

  public ExternalPlugins(@Nonnull final File pluginsFolder) {
    this.pluginApiValidator = new VersionValidator(">=" + MindMapPlugin.API);
    this.pluginsFolder = pluginsFolder;
    LOGGER.info("External plugins folder is " + pluginsFolder);
  }

  public void init() {
    final Collection<File> plugins = FileUtils.listFiles(this.pluginsFolder, new String[] {PLUGIN_EXTENSION, PLUGIN_EXTENSION.toUpperCase(Locale.ENGLISH)}, false);
    LOGGER.info("Detected " + plugins.size() + " plugin(s)");
    for (final File plugin : plugins) {
      try {
        final PluginClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<PluginClassLoader>() {
          @Override
          @Nullable
          public PluginClassLoader run() {
            PluginClassLoader result;
            try {
              result = new PluginClassLoader(plugin);
            } catch (IOException ex) {
              LOGGER.error("Can't create plugin class loader", ex);
              result = null;
            }
            return result;
          }
        });

        if (loader == null) {
          throw new IOException("Can't create plugin class loader, see log for stacktrace");
        }

        final String pluginTitle = GetUtils.ensureNonNull(loader.getAttributes(Attribute.TITLE), "<unknown>");
        final Version pluginVersion = new Version(loader.getAttributes(Attribute.VERSION));

        LOGGER.info(String.format("Detected plugin %s [%s]", pluginTitle, pluginVersion));
        final Version pluginApiVersion = loader.getApiVersion();
        if (this.pluginApiValidator.isValid(pluginApiVersion)) {
          LOGGER.info(String.format("Plugin %s [%s] is valid for API", pluginTitle, pluginVersion));
          final String[] classes = loader.extractPluginClassNames();
          for (final String klazzName : classes) {
            LOGGER.info(String.format("Loading plugin class %s from %s", klazzName, pluginTitle));
            final MindMapPlugin pluginInstance = (MindMapPlugin) loader.loadClass(klazzName).newInstance();
            MindMapPluginRegistry.getInstance().registerPlugin(pluginInstance);
          }
        } else {
          LOGGER.warn(String.format("Plugin %s [%s] is not valid for API : %s", pluginTitle, pluginVersion, pluginApiVersion));
        }
      } catch (Exception ex) {
        LOGGER.error("Can't load plugin from : " + plugin.getAbsolutePath(), ex);
      }
    }
  }
}
