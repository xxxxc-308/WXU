# WebUI X Dex Plugin

This repository serves as a template to demonstrate the integration of WebUI plugins, providing a framework to enhance the WebUI experience with custom functionality.

## Building the Plugin

To build the plugin, simply run the following command:

```shell
gradlew build-dex
```

This will compile the necessary code and generate the required `.dex` files for use within your WebUI project.

## Setting Up the Plugins

To get started with the plugin integration, create a file named `config.json` within the `/data/adb/modules/<MODID>/webroot` directory. This file will specify the plugins to be loaded. The `.dex` file associated with these plugins should be placed in the `/data/adb/modules/<MODID>/webroot` directory for them to be properly loaded and utilized by the WebUI X.

Here's an example of what your `config.json` file should look like:

```jsonc
{
  "dexFiles": [
    {
      "type": "dex",
      "path": "plugins/webui.dex",
      "className": "dev.mmrl.Global"
    }
  ],
  // available from version 215
  "extra": {
    // package manager module
    "pm": {
      // enables the https://mui.kernelsu.org/.package/<name>/info.json and https://mui.kernelsu.org/.package/<name>/icon.(jpe?g|png|webp) URL
      "allowUrlPackageFetch": true
    }
  }
}
```

> Make sure to remove the comments from the JSON be usage.

## Usage

[Types Folder](./types)

```ts
type Module = FileSystem | Reflect | Process | Module | PackageManager | Pty.Shell;

interface Global {
  require(module: string): Module | null;
}

// Why `var`? Because this is not a `const` and can be changed at runtime from the JavaScript side
declare var global: Global;

const fs: FileSystem = global.require("wx:fs");
const reflect: Reflect = global.require("wx:reflect");
const process: Process = global.require("wx:process");
const module: Module = global.require("wx:module");
const pm: PackageManager = global.require("wx:pm");
// Requires at least WebUI X v213 
const pty: Pty.Shell = global.require("wx:pty");
```
