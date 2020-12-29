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

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * Abstract auxiliary class automates way to implement an abstract importer.
 *
 * @since 1.2
 */
public abstract class AbstractImporter extends AbstractPopupMenuItem implements HasMnemonic {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImporter.class);

  @Nonnull
  private static String normalizeExtension(@Nonnull final String extension) {
    String result = extension.toUpperCase(Locale.ENGLISH);
    if (!result.startsWith(".")) {
      result = '.' + result;
    }
    return result;
  }

  @Override
  @Nullable
  public JMenuItem makeMenuItem(
      @Nonnull final PluginContext context,
      @Nullable final Topic activeTopic
  ) {
    final JMenuItem result = UI_COMPO_FACTORY.makeMenuItem(getName(context), getIcon(context));
    result.setToolTipText(getReference(context));

    result.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        try {
          if (AbstractImporter.this instanceof ExternallyExecutedPlugin) {
            context.processPluginActivation((ExternallyExecutedPlugin) AbstractImporter.this, activeTopic);
          } else {
            context.getPanel().removeAllSelection();
            final MindMap map = doImport(context);
            if (map != null) {
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  context.getPanel().setModel(map, true);
                }
              });
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  final Topic root = map.getRoot();
                  if (root != null) {
                    final MindMapPanel panel = context.getPanel();
                    panel.doLayout();
                    panel.revalidate();
                    panel.focusTo(root);
                    panel.repaint();
                  }
                }
              });
            }
          }
        } catch (Exception ex) {
          LOGGER.error("Error during map import", ex); //NOI18N
          context.getDialogProvider().msgError(null, Texts.getString("MMDGraphEditor.makePopUp.errMsgCantImport"));
        }
      }
    });
    return result;
  }

  @Override
  @Nonnull
  public PopUpSection getSection() {
    return PopUpSection.IMPORT;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return false;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  @Nullable
  protected File selectFileForExtension(@Nonnull final PluginContext context,
                                        @Nonnull final String dialogTitle,
                                        @Nullable final File defaultFolder,
                                        @Nonnull final String fileExtension,
                                        @Nonnull final String fileFilterDescription,
                                        @Nonnull final String approveButtonText) {
    return MindMapUtils.selectFileToOpenForFileFilter(
        context.getPanel(),
        context,
        this.getClass().getName(),
        dialogTitle,
        defaultFolder,
        normalizeExtension(fileExtension), fileFilterDescription, approveButtonText);
  }

  @Nullable
  @Override
  public String getMnemonic() {
    return null;
  }

  @Nullable
  public abstract MindMap doImport(@Nonnull final PluginContext context) throws Exception;

  @Nonnull
  public abstract String getName(@Nonnull final PluginContext context);

  @Nonnull
  public abstract String getReference(@Nonnull final PluginContext context);

  @Nonnull
  public abstract Icon getIcon(@Nonnull final PluginContext context);


}
