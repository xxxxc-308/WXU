/**
 * Represents a running app or task in the system.
 *
 * This interface gives you basic info about a process — like what kind of rooting
 * solution it's running under (on Android), whether it's still running, and where
 * its app libraries are located.
 *
 * It's handy for building cross-platform tools or system monitors where you need
 * to understand what's running on the device.
 */
export interface Process {
  /**
   * Returns the platform or rooting method this process is using on Android.
   * Knowing this helps when you want to adjust behavior based on how root access is provided.
   *
   * You might see one of the following:
   * - `'Magisk'`: A popular way to root Android without modifying the system partition.
   * - `'KernelSU'`: Grants root by modifying the kernel itself; needs a special kernel.
   * - `'KsuNext'`: An updated or custom version of KernelSU with better hiding and support.
   * - `'APatch'`: A hybrid approach that mixes ideas from Magisk and KernelSU.
   * - `'MKSU'`, `'SukiSU'`, `'RKSU'`: These are likely custom or forked root solutions. What they mean might depend on your setup.
   * - `'NonRoot'`: The device isn't rooted or this process isn't running with root access.
   * - `'Unknown'`: We couldn’t figure out what kind of platform this is.
   *
   * @returns {string} The name of the platform or rooting method.
   */
  getPlatform(): string;

  /**
   * Checks if this process is still running.
   *
   * @returns {boolean} `true` if it’s alive and active, `false` if it’s stopped or crashed.
   */
  isAlive(): boolean;

  /**
   * Gets the full path to the folder that holds this process's native libraries or binaries.
   * This can be helpful if you need to load extra files, resources, or understand
   * where the app is installed.
   *
   * @returns {string | null} The path to the app's library folder, or `null` if it can't be found
   * (like if the process is dead or doesn't use a standard path).
   */
  getApplicationLibraryDir(): string | null;
}
