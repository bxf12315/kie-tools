/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { EffectCallback, useCallback, useEffect, useRef, useState } from "react";

export function usePrevious<T>(value: T) {
  const ref = useRef<T>();

  useEffect(() => {
    if (ref.current !== value) {
      ref.current = value;
    }
  }, [value]);

  return ref.current;
}

export function useController<T>(): [T | undefined, (controller: T) => void] {
  const [controller, setController] = useState<T | undefined>(undefined);

  const ref = useCallback((controller: T) => {
    setController(controller);
  }, []);

  return [controller, ref];
}

export type ArrowFunction<A, B> = (a: A) => B;

export class Holder<T> {
  constructor(private value: T) {}
  public readonly get = () => this.value;
  public readonly set = (newValue: T) => (this.value = newValue);
}

export type CancelableEffectParams = {
  canceled: Holder<boolean>;
};

export function useCancelableEffect(effect: (args: CancelableEffectParams) => ReturnType<EffectCallback>) {
  useEffect(() => {
    const canceled = new Holder(false);

    const effectCleanup = effect({ canceled });

    return () => {
      canceled.set(true);
      if (effectCleanup) {
        effectCleanup();
      }
    };
  }, [effect]);
}