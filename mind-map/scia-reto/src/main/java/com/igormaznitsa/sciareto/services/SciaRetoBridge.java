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
package com.igormaznitsa.sciareto.services;

import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.ide.IDEBridge;
import com.igormaznitsa.mindmap.swing.ide.NotificationType;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.notifications.NotificationManager;
import com.igormaznitsa.sciareto.ui.platform.PlatformProvider;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SciaRetoBridge implements IDEBridge {

  private static final Logger LOGGER = LoggerFactory.getLogger(SciaRetoBridge.class);

  private final Map<String, Image> IMAGE_CACHE = new HashMap<>();

  @Override
  @Nonnull
  public Version getIDEVersion() {
    return SciaRetoStarter.IDE_VERSION;
  }

  @Override
  public void showIDENotification(@Nonnull final String title, @Nonnull final String message, @Nonnull final NotificationType type) {
    final NotificationManager.Type msgtype;
    switch (type) {
      case INFO:
        LOGGER.info("IDENotification : (" + title + ") " + message); //NOI18N
        msgtype = NotificationManager.Type.INFO;
        break;
      case WARNING:
        LOGGER.warn("IDENotification : (" + title + ") " + message); //NOI18N
        msgtype = NotificationManager.Type.WARN;
        break;
      case ERROR:
        LOGGER.error("IDENotification : (" + title + ") " + message); //NOI18N
        msgtype = NotificationManager.Type.ERROR;
        break;
      default: {
        LOGGER.warn("*IDENotification : (" + title + ") " + message); //NOI18N
        msgtype = NotificationManager.Type.WARN;
      }
    }

    NotificationManager.getInstance().showNotification(null, title, msgtype, message);
  }

  @Override
  public void notifyRestart() {
    JOptionPane.showMessageDialog(null, "Work of application will be completed for request! You have to restart it!", "Restart application", JOptionPane.WARNING_MESSAGE);
    try{
      PlatformProvider.getPlatform().dispose();
    }finally{
      System.exit(0);
    }
  }

  @Nonnull
  private static String removeStartSlash(@Nonnull final String path) {
    String result = path;
    if (path.startsWith("/") || path.startsWith("\\")) { //NOI18N
      result = result.substring(1);
    }
    return result;
  }

  @Override
  @Nonnull
  public Icon loadIcon(@Nonnull final String path, @Nonnull final Class<?> klazz) {
    Image image;
    synchronized (IMAGE_CACHE) {
      image = IMAGE_CACHE.get(path);
      if (image == null) {
        final InputStream in = klazz.getClassLoader().getResourceAsStream(Assertions.assertNotNull("Icon path must not be null", removeStartSlash(path))); //NOI18N
        if (in == null) {
          throw new IllegalArgumentException("Can't find icon resource : " + path); //NOI18N
        }
        try {
          image = ImageIO.read(in);
        } catch (IOException ex) {
          throw new IllegalArgumentException("Can't load icon resource : " + path, ex); //NOI18N
        }
        IMAGE_CACHE.put(path, image);
      }
    }
    return new ImageIcon(image);
  }
}
