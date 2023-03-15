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

import { ListDeployments, UpdateDeployment } from "@kie-tools-core/openshift/dist/api/kubernetes/Deployment";
import { DeploymentDescriptor, DeploymentGroupDescriptor } from "@kie-tools-core/openshift/dist/api/types";
import { ResourceFetcher } from "@kie-tools-core/openshift/dist/fetch/ResourceFetcher";
import { resolveDevModeResourceName } from "../devMode/DevModeContext";
import { OpenShiftPipeline, OpenShiftPipelineArgs } from "../OpenShiftPipeline";

interface RestartDevModePipelineArgs {
  webToolsId: string;
}

export class RestartDevModePipeline extends OpenShiftPipeline {
  constructor(protected readonly args: OpenShiftPipelineArgs & RestartDevModePipelineArgs) {
    super(args);
  }

  public async execute(): Promise<void> {
    const latestDeployment = await this.getLatestDevModeDeployment();

    if (!latestDeployment) {
      throw new Error("Dev Mode deployment cannot be found");
    }

    const scalledDownDeployment: DeploymentDescriptor = {
      ...latestDeployment,
      spec: {
        ...latestDeployment.spec,
        replicas: 0,
      },
    };

    await this.args.openShiftService.withFetch((fetcher: ResourceFetcher) =>
      fetcher.execute({
        target: new UpdateDeployment({
          namespace: this.args.namespace,
          resourceName: scalledDownDeployment.metadata.name,
          descriptor: scalledDownDeployment,
        }),
      })
    );

    const updatedDeployment = await this.getLatestDevModeDeployment();

    if (!updatedDeployment) {
      throw new Error("Dev Mode deployment cannot be found");
    }

    const scalledUpDeployment: DeploymentDescriptor = {
      ...updatedDeployment,
      spec: {
        ...updatedDeployment.spec,
        replicas: 1,
      },
    };

    await this.args.openShiftService.withFetch((fetcher: ResourceFetcher) =>
      fetcher.execute({
        target: new UpdateDeployment({
          namespace: this.args.namespace,
          resourceName: scalledUpDeployment.metadata.name,
          descriptor: scalledUpDeployment,
        }),
      })
    );
  }

  private async getLatestDevModeDeployment(): Promise<DeploymentDescriptor | undefined> {
    const deployments = (
      await this.args.openShiftService.withFetch((fetcher: ResourceFetcher) =>
        fetcher.execute<DeploymentGroupDescriptor>({
          target: new ListDeployments({
            namespace: this.args.namespace,
            labelSelector: this.args.webToolsId,
          }),
        })
      )
    ).items
      .filter((d) => d.metadata.name === resolveDevModeResourceName(this.args.webToolsId))
      .sort(
        (a, b) =>
          new Date(b.metadata.creationTimestamp ?? 0).getTime() - new Date(a.metadata.creationTimestamp ?? 0).getTime()
      );

    return deployments.length === 0 ? undefined : deployments[0];
  }
}
