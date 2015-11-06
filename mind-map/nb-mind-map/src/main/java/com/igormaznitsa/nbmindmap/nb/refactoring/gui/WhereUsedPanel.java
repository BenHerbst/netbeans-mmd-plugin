/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb.refactoring.gui;

import java.awt.Component;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

public class WhereUsedPanel extends javax.swing.JPanel implements CustomRefactoringPanel {

  private static final long serialVersionUID = -8079025593983130439L;
  
  
  private final AtomicBoolean initialized = new AtomicBoolean();

  private final String mindMapName;
  private final boolean findInCommentsDefaultFlag;
  private final ChangeListener parent;
  private final Lookup lookup;
  
  public WhereUsedPanel(final Lookup lookup, final String mindMapName, final boolean findInComments, final ChangeListener parent) {
    initComponents();
    this.lookup = lookup;
    this.mindMapName = mindMapName;
    this.findInCommentsDefaultFlag = findInComments;
    this.parent = parent;
  }
  
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    labelTextFieldName = new javax.swing.JLabel();
    checkBoxFindInComments = new javax.swing.JCheckBox();
    labelMindMapName = new javax.swing.JLabel();
    panelScope = new org.netbeans.modules.refactoring.spi.ui.ScopePanel(WhereUsedPanel.class.getCanonicalName().replace('.', '-'),NbPreferences.forModule(WhereUsedPanel.class),"whereUsed.scope");
    labelScopeName = new javax.swing.JLabel();

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(labelTextFieldName, bundle.getString("WhereUsedPanel.labelTextFieldName.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(checkBoxFindInComments, bundle.getString("WhereUsedPanel.checkBoxFindInComments.text")); // NOI18N
    checkBoxFindInComments.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        checkBoxFindInCommentsActionPerformed(evt);
      }
    });

    labelMindMapName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/logo/logo16.png"))); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(labelMindMapName, bundle.getString("WhereUsedPanel.labelMindMapName.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(labelScopeName, bundle.getString("WhereUsedPanel.labelScopeName.text")); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(labelTextFieldName)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(checkBoxFindInComments)
                .addGap(0, 0, Short.MAX_VALUE))
              .addComponent(labelMindMapName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(labelScopeName)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(panelScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(labelTextFieldName)
          .addComponent(labelMindMapName))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(checkBoxFindInComments)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(labelScopeName)
          .addComponent(panelScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void checkBoxFindInCommentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxFindInCommentsActionPerformed
    this.parent.stateChanged(null);
  }//GEN-LAST:event_checkBoxFindInCommentsActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox checkBoxFindInComments;
  private javax.swing.JLabel labelMindMapName;
  private javax.swing.JLabel labelScopeName;
  private javax.swing.JLabel labelTextFieldName;
  private org.netbeans.modules.refactoring.spi.ui.ScopePanel panelScope;
  // End of variables declaration//GEN-END:variables

  @Override
  public void initialize() {
    if (this.initialized.compareAndSet(false, true)){
      SwingUtilities.invokeLater(new Runnable() {

        @Override
        public void run() {
          labelMindMapName.setText(mindMapName);
          checkBoxFindInComments.setSelected(findInCommentsDefaultFlag);
          if (!panelScope.initialize(lookup, new AtomicBoolean())){
            labelScopeName.setVisible(false);
            panelScope.setVisible(false);
          }else{
            labelScopeName.setVisible(true);
            panelScope.setVisible(true);
          }
        }
      });
    }
  }

  public boolean isSearchInComments(){
    return this.checkBoxFindInComments.isSelected();
  }
  
  @Override
  public Component getComponent() {
    return this;
  }
}
