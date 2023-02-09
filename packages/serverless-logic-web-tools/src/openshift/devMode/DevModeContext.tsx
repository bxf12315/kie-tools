/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { v4 as uuid } from "uuid";
import { WorkspaceFile } from "@kie-tools-core/workspaces-git-fs/dist/context/WorkspacesContext";
import * as React from "react";
import { useContext } from "react";
import { DevModeEndpoints, DevModeUploadResult, WEB_TOOLS_ID_KEY } from "./DevModeConstants";

export const resolveWebToolsId = () => {
  const webToolsId = localStorage.getItem(WEB_TOOLS_ID_KEY) ?? uuid();
  localStorage.setItem(WEB_TOOLS_ID_KEY, webToolsId);
  return webToolsId;
};

export const resolveDevModeResourceName = (webToolsId: string) => {
  return `devmode-${webToolsId}`;
};

export interface DevModeContextType {
  endpoints: DevModeEndpoints | undefined;
  upload(files: WorkspaceFile[]): Promise<DevModeUploadResult>;
  checkHealthReady(): Promise<boolean>;
}

export const DevModeContext = React.createContext<DevModeContextType>({} as any);

export function useDevMode() {
  return useContext(DevModeContext);
}