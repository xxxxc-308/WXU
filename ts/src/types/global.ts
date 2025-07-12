import type { Module } from "./Module";
import type { Process } from "./Process";
import type { Reflect } from "./Reflect";
import type { FileSystem } from "./FileSystem";

export {};

interface Global {
  require(module: string): FileSystem | Reflect | Process | Module | null;
}

declare global {
  interface Window {
    global: Global | undefined;
  }
}
