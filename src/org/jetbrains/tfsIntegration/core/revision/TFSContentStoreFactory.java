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

package org.jetbrains.tfsIntegration.core.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class TFSContentStoreFactory {

  public static TFSContentStore create(final String serverUri, final int itemId, final VcsRevisionNumber.Int revision) throws IOException {
    return new TFSTmpFileStore(serverUri, itemId, revision.getValue());
  }

  @Nullable
  public static TFSContentStore find(final String serverUri, final int itemId, final VcsRevisionNumber.Int revision) throws IOException {
    return TFSTmpFileStore.find(serverUri, itemId, revision.getValue());
  }
}

