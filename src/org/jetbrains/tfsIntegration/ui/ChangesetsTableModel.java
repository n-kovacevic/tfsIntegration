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

import com.intellij.util.text.DateFormatUtil;
import com.microsoft.schemas.teamfoundation._2005._06.versioncontrol.clientservices._03.Changeset;
import org.jetbrains.tfsIntegration.core.tfs.TfsUtil;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ChangesetsTableModel extends AbstractTableModel {
  enum Column {
    Changeset("Changeset", 60) {
      @Override
      public String getValue(Changeset changeset) {
        return String.valueOf(changeset.getCset());
      }
    },
    Date("Date", 95) {
      @Override
      public String getValue(Changeset changeset) {
        return DateFormatUtil.formatPrettyDateTime(changeset.getDate().getTimeInMillis());
      }
    },
    User("User", 90) {
      @Override
      public String getValue(Changeset changeset) {
        return TfsUtil.getNameWithoutDomain(changeset.getOwner());
      }
    },
    Comment("Comment", 180) {
      @Override
      public String getValue(Changeset changeset) {
        return changeset.getComment();
      }
    };

    private final String myCaption;
    private final int myWidth;

    Column(String caption, int width) {
      myCaption = caption;
      myWidth = width;
    }

    public String getCaption() {
      return myCaption;
    }

    public abstract String getValue(Changeset changeset);

    public int getWidth() {
      return myWidth;
    }
  }

  private List<Changeset> myChangesets;

  public void setChangesets(List<Changeset> changesets) {
    myChangesets = changesets;
    fireTableDataChanged();
  }

  public List<Changeset> getChangesets() {
    return myChangesets;
  }

  @Override
  public String getColumnName(final int column) {
    return Column.values()[column].getCaption();
  }

  @Override
  public int getRowCount() {
    return myChangesets != null ? myChangesets.size() : 0;
  }

  @Override
  public int getColumnCount() {
    return Column.values().length;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    return Column.values()[columnIndex].getValue(myChangesets.get(rowIndex));
  }
}
