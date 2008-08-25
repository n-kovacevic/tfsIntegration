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

package org.jetbrains.tfsIntegration.core.tfs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.tfsIntegration.exceptions.TfsException;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.DeletedState;
import org.jetbrains.tfsIntegration.stubs.versioncontrol.repository.ExtendedItem;
import org.jetbrains.tfsIntegration.core.TFSVcs;

public class TfsUtil {

  @Nullable
  public static ExtendedItem getExtendedItem(final FilePath localPath) throws TfsException {
    WorkspaceInfo workspace = Workstation.getInstance().findWorkspace(localPath);
    if (workspace == null) {
      return null;
    }
    //noinspection ConstantConditions
    String serverPath = workspace.findServerPathByLocalPath(localPath);
    return workspace.getServer().getVCS()
      .getExtendedItem(workspace.getName(), workspace.getOwnerName(), serverPath, DeletedState.Any);
  }


  @Nullable
  public static ExtendedItem getExtendedItem(Project project, final FilePath localPath, final String errorTabTitle) {
    try {
      return getExtendedItem(localPath);
    }
    catch (TfsException e) {
      //noinspection ThrowableInstanceNeverThrown
      AbstractVcsHelper.getInstance(project).showError(new VcsException(e.getMessage(), e), errorTabTitle);
      return null;
    }
  }

  public static VcsRevisionNumber getCurrentRevisionNumber(Project project, FilePath path) {
    ExtendedItem item = getExtendedItem(project, path, TFSVcs.TFS_NAME);
    return (item != null && item.getLver() != Integer.MIN_VALUE) ? new VcsRevisionNumber.Int(item.getLver()) : VcsRevisionNumber.NULL;

  }

}