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
import { useContext, useState, useEffect, useCallback, useMemo } from "react";
import {
  buildEndpoints,
  DevModeEndpoints,
  DevModeUploadResult,
  WEB_TOOLS_ID_KEY,
  ZIP_FILE_NAME,
  ZIP_FILE_PART_KEY,
} from "./DevModeConstants";
import { useEnv } from "../../env/EnvContext";
import { useSettings, useSettingsDispatch } from "../../settings/SettingsContext";
import { OpenShiftInstanceStatus } from "../OpenShiftInstanceStatus";
import { AppDistributionMode } from "../../AppConstants";
import { SpinUpDevModePipeline } from "../pipelines/SpinUpDevModePipeline";
import { fetchWithTimeout } from "../../fetch";
import { zipFiles } from "../../zip";
import { isServerlessWorkflow, isSupportedByDevMode } from "../../extension";

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
}

export interface DevModeDispatchContextType {
  upload(args: { targetFile: WorkspaceFile; allFiles: WorkspaceFile[] }): Promise<DevModeUploadResult>;
  checkHealthReady(): Promise<boolean>;
}

export const DevModeContext = React.createContext<DevModeContextType>({} as any);
export const DevModeDispatchContext = React.createContext<DevModeDispatchContextType>({} as any);

export function useDevMode() {
  return useContext(DevModeContext);
}

export function useDevModeDispatch() {
  return useContext(DevModeDispatchContext);
}

export function DevModeContextProvider(props: React.PropsWithChildren<{}>) {
  const { env } = useEnv();
  const settings = useSettings();
  const settingsDispatch = useSettingsDispatch();
  const [endpoints, setEndpoints] = useState<DevModeEndpoints | undefined>();

  useEffect(() => {
    if (
      settings.openshift.status !== OpenShiftInstanceStatus.CONNECTED ||
      env.FEATURE_FLAGS.MODE !== AppDistributionMode.OPERATE_FIRST
    ) {
      setEndpoints(undefined);
      return;
    }

    try {
      const spinUpDevModePipeline = new SpinUpDevModePipeline({
        webToolsId: resolveWebToolsId(),
        namespace: settings.openshift.config.namespace,
        openShiftService: settingsDispatch.openshift.service,
      });

      spinUpDevModePipeline
        .execute()
        .then((routeUrl) => {
          if (routeUrl) {
            setEndpoints(buildEndpoints(routeUrl));
          }
        })
        .catch((e) => console.debug(e));
    } catch (e) {
      console.debug(e);
    }
  }, [
    settings.openshift.status,
    settings.openshift.config.namespace,
    settingsDispatch.openshift.service,
    env.FEATURE_FLAGS.MODE,
  ]);

  const checkHealthReady = useCallback(async () => {
    if (!endpoints) {
      return false;
    }

    try {
      const readyResponse = await fetchWithTimeout(endpoints.health.ready, { timeout: 2000 });
      return readyResponse.ok;
    } catch (e) {
      console.debug(e);
    }
    return false;
  }, [endpoints]);

  const upload = useCallback(
    async (args: { targetFile: WorkspaceFile; allFiles: WorkspaceFile[] }): Promise<DevModeUploadResult> => {
      if (!endpoints) {
        console.error("Route URL for Dev Mode deployment not available.");
        return {
          success: false,
          reason: "NOT_READY",
        };
      }

      if (!(await checkHealthReady())) {
        console.error("Dev Mode deployment is not ready");
        return {
          success: false,
          reason: "NOT_READY",
        };
      }

      if (!isServerlessWorkflow(args.targetFile.relativePath)) {
        console.error(`File is not Serverless Workflow: ${args.targetFile.relativePath}`);
        return {
          success: false,
          reason: "ERROR",
        };
      }

      try {
        // TODO CAPONETTO: no need to have `targetFile` if uploading all sw files.
        // Uncomment when supporting other assets like application.properties and spec files
        // const filesToUpload = [
        //   args.targetFile,
        //   ...args.allFiles.filter(
        //     (f) => f.relativePath !== args.targetFile.relativePath && isSupportedByDevMode(f.relativePath)
        //   ),
        // ];
        const filesToUpload = [args.targetFile];
        const zipBlob = await zipFiles(filesToUpload);

        const formData = new FormData();
        formData.append(ZIP_FILE_PART_KEY, zipBlob, ZIP_FILE_NAME);

        await fetch(endpoints.upload, { method: "POST", body: formData });

        return { success: true };
      } catch (e) {
        console.debug(e);
      }
      return {
        success: false,
        reason: "ERROR",
      };
    },
    [checkHealthReady, endpoints]
  );

  const value = useMemo(() => ({ endpoints }), [endpoints]);
  const dispatch = useMemo(() => ({ upload, checkHealthReady }), [upload, checkHealthReady]);

  return (
    <DevModeContext.Provider value={value}>
      <DevModeDispatchContext.Provider value={dispatch}>{props.children}</DevModeDispatchContext.Provider>
    </DevModeContext.Provider>
  );
}