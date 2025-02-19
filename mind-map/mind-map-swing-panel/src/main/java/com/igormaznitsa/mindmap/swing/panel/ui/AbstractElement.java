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

package com.igormaznitsa.mindmap.swing.panel.ui;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_BORDER_COLOR;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_FILL_COLOR;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_TEXT_COLOR;


import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.text.JTextComponent;

public abstract class AbstractElement {

  @Nonnull
  protected final Topic model;
  @Nonnull
  protected final TextBlock textBlock;
  @Nonnull
  protected final IconBlock extrasIconBlock;
  @Nonnull
  protected final VisualAttributeImageBlock visualAttributeImageBlock;

  protected final Rectangle2D bounds = new Rectangle2D.Double();
  protected final Dimension2D blockSize = new Dimension();

  protected Color fillColor;
  protected Color textColor;
  protected Color borderColor;

  protected AbstractElement(@Nonnull final AbstractElement orig) {
    this.model = orig.model;
    this.textBlock = new TextBlock(orig.textBlock);
    this.extrasIconBlock = new IconBlock(orig.extrasIconBlock);
    this.visualAttributeImageBlock = new VisualAttributeImageBlock(orig.visualAttributeImageBlock);
    this.bounds.setRect(orig.bounds);
    this.blockSize.setSize(orig.blockSize);
    this.fillColor = orig.fillColor;
    this.textColor = orig.textColor;
    this.borderColor = orig.borderColor;
  }

  public AbstractElement(@Nonnull final Topic model) {
    this.model = model;
    this.textBlock = new TextBlock(this.model.getText(), TextAlign.findForName(model.getAttribute("align")));
    this.textBlock.setTextAlign(TextAlign.findForName(model.getAttribute("align"))); //NOI18N
    this.extrasIconBlock = new IconBlock(model);
    this.visualAttributeImageBlock = new VisualAttributeImageBlock(model);
    updateColorAttributeFromModel();
  }

  @Nonnull
  public String getText() {
    return this.model.getText();
  }

  public void setText(@Nonnull final String text) {
    this.model.setText(text);
    this.textBlock.updateText(text);
  }

  public final void updateColorAttributeFromModel() {
    this.borderColor = Utils.html2color(this.model.getAttribute(ATTR_BORDER_COLOR.getText()), false);
    this.textColor = Utils.html2color(this.model.getAttribute(ATTR_TEXT_COLOR.getText()), false);
    this.fillColor = Utils.html2color(this.model.getAttribute(ATTR_FILL_COLOR.getText()), false);
  }

  @Nullable
  public AbstractElement getParent() {
    final Topic parent = this.model.getParent();
    return parent == null ? null : (AbstractElement) parent.getPayload();
  }

  @Nonnull
  public Topic getModel() {
    return this.model;
  }

  @Nonnull
  public TextAlign getTextAlign() {
    return this.textBlock.getTextAlign();
  }

  public void setTextAlign(@Nonnull final TextAlign textAlign) {
    this.textBlock.setTextAlign(textAlign);
    this.model.setAttribute("align", this.textBlock.getTextAlign().name()); //NOI18N
  }

  public void updateElementBounds(@Nonnull final MMGraphics gfx, @Nonnull final MindMapPanelConfig cfg) {
    this.visualAttributeImageBlock.updateSize(gfx, cfg);
    this.textBlock.updateSize(gfx, cfg);
    this.extrasIconBlock.updateSize(gfx, cfg);

    final double scaledHorzBlockGap = cfg.getScale() * cfg.getHorizontalBlockGap();

    double width = 0.0d;
    if (this.visualAttributeImageBlock.mayHaveContent()) {
      width += this.visualAttributeImageBlock.getBounds().getWidth() + scaledHorzBlockGap;
    }

    width += this.textBlock.getBounds().getWidth();

    if (this.extrasIconBlock.hasContent()) {
      width += this.extrasIconBlock.getBounds().getWidth() + scaledHorzBlockGap;
    }

    this.bounds.setRect(
        0d,
        0d,
        width,
        Math.max(
            this.visualAttributeImageBlock.getBounds().getHeight(),
            Math.max(
                this.textBlock.getBounds().getHeight(), this.extrasIconBlock.getBounds().getHeight()
            )
        )
    );
  }

  public void updateBlockSize(@Nonnull final MindMapPanelConfig cfg) {
    this.calcBlockSize(cfg, this.blockSize, false);
  }

  @Nonnull
  public Dimension2D getBlockSize() {
    return this.blockSize;
  }

  public void moveTo(final double x, final double y) {
    this.bounds.setFrame(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }

  public void moveWholeTreeBranchCoordinates(final double deltaX, final double deltaY) {
    moveTo(this.bounds.getX() + deltaX, this.bounds.getY() + deltaY);
    for (final Topic t : this.model.getChildren()) {
      final AbstractElement el = (AbstractElement) t.getPayload();
      if (el != null) {
        el.moveWholeTreeBranchCoordinates(deltaX, deltaY);
      }
    }
  }

  @Nonnull
  public Rectangle2D getBounds() {
    return this.bounds;
  }

  public final void doPaint(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig cfg, final boolean drawCollapsator) {
    final MMGraphics gfx = g.copy();
    try {
      if (this.hasChildren() && !isCollapsed()) {
        doPaintConnectors(g, isLeftDirection(), cfg);
      }

      final Rectangle clip = g.getClipBounds();

      if (clip == null) {
        gfx.translate(this.bounds.getX(), this.bounds.getY());
        drawComponent(gfx, cfg, drawCollapsator);
      } else if (clip.intersects(this.bounds)) {
        gfx.translate(this.bounds.getX(), this.bounds.getY());
        drawComponent(gfx, cfg, drawCollapsator);
      }
    } finally {
      gfx.dispose();
    }
  }

  public void doPaintConnectors(@Nonnull final MMGraphics g, final boolean leftDirection, @Nonnull final MindMapPanelConfig cfg) {
    final Rectangle2D source = this.bounds;
    for (final Topic t : this.model.getChildren()) {
      drawConnector(g, source, (assertNotNull((AbstractElement) t.getPayload())).getBounds(), leftDirection, cfg);
    }
  }

  public boolean hasChildren() {
    return this.model.hasChildren();
  }

  @Nonnull
  public JTextComponent fillByTextAndFont(@Nonnull final JTextComponent compo) {
    this.textBlock.fillByTextAndFont(compo);
    return compo;
  }

  public abstract void drawComponent(@Nonnull MMGraphics g, @Nonnull MindMapPanelConfig cfg, boolean drawCollapsator);

  public abstract void drawConnector(@Nonnull MMGraphics g, @Nonnull Rectangle2D source, @Nonnull Rectangle2D destination, boolean leftDirection, @Nonnull MindMapPanelConfig cfg);

  public abstract boolean isMoveable();

  public abstract boolean isCollapsed();

  public void alignElementAndChildren(@Nonnull MindMapPanelConfig cfg, boolean leftSide, double centerX, double centerY) {
    final double textMargin = cfg.getScale() * cfg.getTextMargins();
    final double centralBlockLineY = textMargin + Math.max(this.visualAttributeImageBlock.getBounds().getHeight(), Math.max(this.textBlock.getBounds().getHeight(), this.extrasIconBlock.getBounds().getHeight())) / 2;

    final double scaledHorzBlockGap = cfg.getScale() * cfg.getHorizontalBlockGap();

    double offset = textMargin;

    if (this.visualAttributeImageBlock.mayHaveContent()) {
      this.visualAttributeImageBlock.setCoordOffset(offset, centralBlockLineY - this.visualAttributeImageBlock.getBounds().getHeight() / 2);
      offset += this.visualAttributeImageBlock.getBounds().getWidth() + scaledHorzBlockGap;
    }

    this.textBlock.setCoordOffset(offset, centralBlockLineY - this.textBlock.getBounds().getHeight() / 2);
    offset += this.textBlock.getBounds().getWidth() + scaledHorzBlockGap;

    if (this.extrasIconBlock.hasContent()) {
      this.extrasIconBlock.setCoordOffset(offset, centralBlockLineY - this.extrasIconBlock.getBounds().getHeight() / 2);
    }
  }

  @Nonnull
  public abstract Dimension2D calcBlockSize(@Nonnull MindMapPanelConfig cfg, @Nonnull Dimension2D size, boolean childrenOnly);

  public abstract boolean hasDirection();

  @Nonnull
  public ElementPart findPartForPoint(@Nonnull final Point point) {
    ElementPart result = ElementPart.NONE;
    if (this.bounds.contains(point)) {
      final double offX = point.getX() - this.bounds.getX();
      final double offY = point.getY() - this.bounds.getY();

      result = ElementPart.AREA;
      if (this.visualAttributeImageBlock.getBounds().contains(offX, offY)) {
        result = ElementPart.VISUAL_ATTRIBUTES;
      } else {
        if (this.textBlock.getBounds().contains(offX, offY)) {
          result = ElementPart.TEXT;
        } else if (this.extrasIconBlock.getBounds().contains(offX, offY)) {
          result = ElementPart.ICONS;
        }
      }
    }
    return result;
  }

  @Nullable
  public Topic findTopicBeforePoint(@Nonnull final MindMapPanelConfig cfg, @Nonnull final Point point) {

    Topic result = null;
    if (this.hasChildren()) {
      if (this.isCollapsed()) {
        return this.getModel().getLast();
      } else {
        double py = point.getY();
        final double vertInset = cfg.getOtherLevelVerticalInset() * cfg.getScale();

        Topic prev = null;

        for (final Topic t : this.model.getChildren()) {
          final AbstractElement el = assertNotNull((AbstractElement) t.getPayload());

          final double childStartBlockY = el.calcBlockY();
          final double childEndBlockY = childStartBlockY + el.getBlockSize().getHeight() + vertInset;

          if (py < childEndBlockY) {
            result = py < el.getBounds().getCenterY() ? prev : t;
            break;
          } else if (this.model.isLastChild(t)) {
            result = t;
            break;
          }

          prev = t;
        }
      }
    }
    return result;
  }

  protected double calcBlockY() {
    return this.bounds.getY() - (this.blockSize.getHeight() - this.bounds.getHeight()) / 2;
  }

  protected double calcBlockX() {
    return this.bounds.getX() - (this.isLeftDirection() ? this.blockSize.getWidth() - this.bounds.getWidth() : 0.0d);
  }

  @Nonnull
  public Point getCenter() {
    return new Point((int)this.bounds.getCenterX(), (int)this.bounds.getCenterY());
  }

  @Nullable
  public AbstractElement findNearestOpenedTopicToPoint(@Nullable final AbstractElement elementToIgnore, @Nonnull final Point point) {
    return findNearestTopic(elementToIgnore, Double.MAX_VALUE, point);
  }

  @Nullable
  private AbstractElement findNearestTopic(@Nullable final AbstractElement elementToIgnore, double maxDistance, @Nonnull final Point point) {
    AbstractElement result = null;
    if (elementToIgnore != this) {
      final double dist = calcAverageDistanceToPoint(point);
      if (dist < maxDistance) {
        maxDistance = dist;
        result = this;
      }
    }

    if (!this.isCollapsed()) {
      for (final Topic t : this.model.getChildren()) {
        final AbstractElement element = t.getPayload() == null ? null : (AbstractElement) t.getPayload();
        if (element != null) {
          final AbstractElement nearestChild = element.findNearestTopic(elementToIgnore, maxDistance, point);
          if (nearestChild != null) {
            maxDistance = nearestChild.calcAverageDistanceToPoint(point);
            result = nearestChild;
          }
        }
      }
    }
    return result;
  }

  public double calcAverageDistanceToPoint(@Nonnull final Point point) {
    final double d1 = point.distance(this.bounds.getX(), this.bounds.getY());
    final double d2 = point.distance(this.bounds.getMaxX(), this.bounds.getY());
    final double d3 = point.distance(this.bounds.getX(), this.bounds.getMaxY());
    final double d4 = point.distance(this.bounds.getMaxX(), this.bounds.getMaxY());
    return (d1 + d2 + d3 + d4) / (this.bounds.contains(point) ? 8.0d : 4.0d);
  }

  @Nullable
  public AbstractElement findTopicBlockForPoint(@Nullable final Point point) {
    AbstractElement result = null;
    if (point != null) {
      final double px = point.getX();
      final double py = point.getY();

      if (px >= calcBlockX() && py >= calcBlockY() && px < this.bounds.getX() + this.blockSize.getWidth() && py < this.bounds.getY() + this.blockSize.getHeight()) {
        if (this.isCollapsed()) {
          result = this;
        } else {
          AbstractElement foundChild = null;
          for (final Topic t : this.model.getChildren()) {
            final AbstractElement theElement = (AbstractElement) t.getPayload();
            if (theElement != null) {
              foundChild = theElement.findTopicBlockForPoint(point);
              if (foundChild != null) {
                break;
              }
            }
          }
          result = foundChild == null ? this : foundChild;
        }
      }
    }
    return result;
  }

  @Nullable
  public AbstractElement findForPoint(@Nullable final Point point) {
    AbstractElement result = null;
    if (point != null) {
      if (this.bounds.contains(point)) {
        result = this;
      } else {
        for (final Topic t : this.model.getChildren()) {
          final AbstractElement w = (AbstractElement) t.getPayload();
          result = w == null ? null : w.findForPoint(point);
          if (result != null) {
            break;
          }
        }
      }
    }
    return result;
  }

  public boolean isLeftDirection() {
    return false;
  }

  @Nonnull
  public TextBlock getTextBlock() {
    return this.textBlock;
  }

  @Nonnull
  public IconBlock getIconBlock() {
    return this.extrasIconBlock;
  }

  @Nonnull
  public VisualAttributeImageBlock getVisualAttributeImageBlock() {
    return this.visualAttributeImageBlock;
  }

  public boolean collapseOrExpandAllChildren(final boolean collapse) {
    boolean result = false;

    if (this instanceof AbstractCollapsableElement) {
      final AbstractCollapsableElement el = (AbstractCollapsableElement) this;

      if (collapse) {
        if (!el.isCollapsed()) {
          el.setCollapse(true);
          result = true;
        }
      } else if (el.isCollapsed()) {
        el.setCollapse(false);
        result = true;
      }
    }

    if (this.hasChildren()) {
      for (final Topic t : this.model.getChildren()) {
        final AbstractElement e = (AbstractElement) t.getPayload();
        if (e != null) {
          result |= e.collapseOrExpandAllChildren(collapse);
        }
      }
    }

    return result;
  }

  @Nonnull
  public abstract Color getBackgroundColor(@Nonnull MindMapPanelConfig config);

  @Nonnull
  public abstract Color getTextColor(@Nonnull MindMapPanelConfig config);

  @Nonnull
  public Color getBorderColor(@Nonnull final MindMapPanelConfig config) {
    return this.borderColor == null ? config.getElementBorderColor() : this.borderColor;
  }

  @Nonnull
  public abstract AbstractElement makeCopy();

}
