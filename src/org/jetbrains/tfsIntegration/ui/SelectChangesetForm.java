/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.tfsIntegration.ui;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.DoubleClickListener;
import com.intellij.util.EventDispatcher;
import com.microsoft.schemas.teamfoundation._2005._06.versioncontrol.clientservices._03.Changeset;
import com.microsoft.schemas.teamfoundation._2005._06.versioncontrol.clientservices._03.VersionSpec;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.tfsIntegration.core.TFSBundle;
import org.jetbrains.tfsIntegration.core.tfs.WorkspaceInfo;
import org.jetbrains.tfsIntegration.core.tfs.version.ChangesetVersionSpec;
import org.jetbrains.tfsIntegration.core.tfs.version.DateVersionSpec;
import org.jetbrains.tfsIntegration.core.tfs.version.LatestVersionSpec;
import org.jetbrains.tfsIntegration.exceptions.TfsException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.EventListener;
import java.util.List;

public class SelectChangesetForm {

  private static final DateFormat DATE_FORMAT = DateFormat.getInstance();

  interface Listener extends EventListener {
    void selectionChanged(Integer changeset);

    void selected(Integer changeset);
  }

  private JTable myChangesetsTable;
  private JTextField myPathField;
  private JTextField myUserField;
  private JRadioButton myAllChangesRadioButton;
  private JRadioButton myChangeNumberRadioButton;
  private JTextField myFromChangesetField;
  private JTextField myToChangesetField;
  private JRadioButton myCreatedDateRadioButton;
  private JTextField myFromDateField;
  private JTextField myToDateField;
  private JButton myFindButton;
  private JPanel panel;

  private final ChangesetsTableModel myChangesetsTableModel;
  private final WorkspaceInfo myWorkspace;
  private final String myServerPath;
  private final boolean myRecursive;

  private final EventDispatcher<Listener> myEventDispatcher = EventDispatcher.create(Listener.class);

  public SelectChangesetForm(final WorkspaceInfo workspace, String serverPath, boolean recursive) {
    myWorkspace = workspace;
    myServerPath = serverPath;
    myRecursive = recursive;
    myChangesetsTableModel = new ChangesetsTableModel();
    myChangesetsTable.setModel(myChangesetsTableModel);
    myChangesetsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    for (int i = 0; i < ChangesetsTableModel.Column.values().length; i++) {
      myChangesetsTable.getColumnModel().getColumn(i).setPreferredWidth(ChangesetsTableModel.Column.values()[i].getWidth());
    }

    myPathField.setText(serverPath);

    myFindButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        search();
      }
    });

    final ActionListener radioButtonListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    };

    myAllChangesRadioButton.addActionListener(radioButtonListener);
    myChangeNumberRadioButton.addActionListener(radioButtonListener);
    myCreatedDateRadioButton.addActionListener(radioButtonListener);

    myChangesetsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(final ListSelectionEvent e) {
        myEventDispatcher.getMulticaster().selectionChanged(getSelectedChangeset());
      }
    });

    new DoubleClickListener() {
      @Override
      protected boolean onDoubleClick(MouseEvent e) {
        final Integer changeset = getSelectedChangeset();
        if (changeset != null) {
          myEventDispatcher.getMulticaster().selected(changeset);
          return true;
        }
        return false;
      }
    }.installOn(myChangesetsTable);

    myAllChangesRadioButton.setSelected(true);
    updateControls();
  }

  private void search() {
    VersionSpec versionFrom = null;
    VersionSpec versionTo = LatestVersionSpec.INSTANCE;

    try {
      if (myChangeNumberRadioButton.isSelected()) {
        if (myFromChangesetField.getText() != null && myFromChangesetField.getText().length() > 0) {
          versionFrom = new ChangesetVersionSpec(Integer.parseInt(myFromChangesetField.getText()));
        }
        if (myToChangesetField.getText() != null && myToChangesetField.getText().length() > 0) {
          versionTo = new ChangesetVersionSpec(Integer.parseInt(myToChangesetField.getText()));
        }
      }
      else if (myCreatedDateRadioButton.isSelected()) {
        if (myFromDateField.getText() != null && myFromDateField.getText().length() > 0) {
          versionFrom = new DateVersionSpec(DateFormat.getInstance().parse(myFromDateField.getText()));
        }
        if (myToDateField.getText() != null && myToDateField.getText().length() > 0) {
          versionTo = new DateVersionSpec(DateFormat.getInstance().parse(myToDateField.getText()));
        }
      }

      List<Changeset> changesets = myWorkspace.getServer().getVCS()
        .queryHistory(myWorkspace, myServerPath, myRecursive, myUserField.getText(), versionFrom, versionTo, getContentPane(),
                      TFSBundle.message("loading.history"), Integer.MAX_VALUE);

      if (changesets.isEmpty()) {
        Messages.showInfoMessage(panel, "No matching changesets found", "Find Changeset");
      }
      myChangesetsTableModel.setChangesets(changesets);
    }
    catch (TfsException ex) {
      myChangesetsTableModel.setChangesets(Collections.emptyList());
      Messages.showErrorDialog(panel, ex.getMessage(), "Find Changeset");
    }
    catch (NumberFormatException ex) {
      myChangesetsTableModel.setChangesets(Collections.emptyList());
      Messages.showErrorDialog(panel, "Invalid changeset number specified", "Find Changeset");
    }
    catch (ParseException e1) {
      myChangesetsTableModel.setChangesets(Collections.emptyList());
      Messages.showErrorDialog(panel, "Invalid date specified", "Find Changeset");
    }
  }

  private void updateControls() {
    myFromChangesetField.setEnabled(myChangeNumberRadioButton.isSelected());
    myToChangesetField.setEnabled(myChangeNumberRadioButton.isSelected());
    if (!myChangeNumberRadioButton.isSelected()) {
      myFromChangesetField.setText(null);
      myToChangesetField.setText(null);
    }

    myFromDateField.setEnabled(myCreatedDateRadioButton.isSelected());
    myToDateField.setEnabled(myCreatedDateRadioButton.isSelected());
    if (!myCreatedDateRadioButton.isSelected()) {
      myFromDateField.setText(null);
      myToDateField.setText(null);
    }
    else {
      String currentDate = DATE_FORMAT.format(new Date());
      myFromDateField.setText(currentDate);
      myToDateField.setText(currentDate);
    }
  }

  public JComponent getContentPane() {
    return panel;
  }

  @Nullable
  public Integer getSelectedChangeset() {
    if (myChangesetsTable.getSelectedRowCount() == 1) {
      return myChangesetsTableModel.getChangesets().get(myChangesetsTable.getSelectedRow()).getCset();
    }
    else {
      return null;
    }
  }

  public void addListener(Listener listener) {
    myEventDispatcher.addListener(listener);
  }

  public void removeListener(Listener listener) {
    myEventDispatcher.removeListener(listener);
  }

}
