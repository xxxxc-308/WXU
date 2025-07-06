/**
 * Defines a static-like interface for synchronous file system operations.
 * This interface provides a direct way to interact with the file system from a
 * sandboxed environment, like a WebView. All operations are synchronous and will
 * block execution until they complete. It serves as a factory for creating
* FileSystemInstance objects and for performing operations on paths directly.
 */
interface FileSystem {
  /**
   * Creates a new FileSystemInstance for a given path. This instance can then be
   * used to perform operations on that specific file or directory.
   *
   * @param {string} path - The absolute path to the file or directory.
   * @returns {FileSystemInstance | null} A new instance representing the path,
   * or `null` if the path is invalid or inaccessible.
   */
  newInstance(path: string): FileSystemInstance | null;

  /**
   * Reads the entire content of a file at a given path as a UTF-8 string.
   *
   * @param {string} path - The path to the file to read.
   * @returns {string | null} The content of the file as a string, or `null` if
   * the file does not exist or cannot be read.
   */
  readTextSync(path: string): string | null;

  /**
   * Writes a string to a file at a given path, overwriting the file if it exists.
   *
   * @param {string} path - The path to the file to write to.
   * @param {string} text - The string content to write to the file.
   * @returns {void | null} Returns `null` if the operation fails.
   */
  writeTextSync(path: string, text: string): void | null;

  /**
   * Lists the files and directories in a given path.
   *
   * @param {string} path - The path of the directory to list.
   * @param {string} [delimiter] - An optional delimiter to join the names. If not provided,
   * the result might be a JSON array string or a newline-separated string.
   * @returns {string | null} A string containing the names of the items in the directory,
   * or `null` if the path is not a directory or an error occurs.
   */
  listSync(path: string): string | null;
  listSync(path: string, delimiter: string): string | null;

  /**
   * Checks if a file or directory exists at the given path.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path exists, `false` otherwise.
   */
  existsSync(path: string): boolean;

  /**
   * Checks if the given path points to a regular file.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path is a file, `false` otherwise.
   */
  isFileSync(path: string): boolean;

  /**
   * Checks if the given path points to a directory.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path is a directory, `false` otherwise.
   */
  isDirectorySync(path: string): boolean;

  /**
   * Checks if the given path points to a symbolic link.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path is a symbolic link, `false` otherwise.
   */
  isSymlinkSync(path: string): boolean;

  /**
   * Checks if the given path points to a block device.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path is a block device, `false` otherwise.
   */
  isBlockSync(path: string): boolean;

  /**
   * Checks if the given path points to a named pipe (FIFO).
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path is a named pipe, `false` otherwise.
   */
  isNamedPipeSync(path: string): boolean;

  /**
   * Checks if the given path points to a character device.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path is a character device, `false` otherwise.
   */
  isCharacterSync(path: string): boolean;

  /**
   * Checks if the file or directory at the given path is hidden.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path is hidden, `false` otherwise.
   */
  isHiddenSync(path: string): boolean;

  /**
   * Checks if the given path points to a socket.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the path is a socket, `false` otherwise.
   */
  isSocketSync(path: string): boolean;

  /**
   * Creates a new directory. Fails if the parent directory does not exist.
   *
   * @param {string} path - The path of the directory to create.
   * @returns {boolean} `true` if the directory was created, `false` otherwise.
   */
  mkdirSync(path: string): boolean;

  /**
   * Creates a directory, including any necessary but non-existent parent directories.
   *
   * @param {string} path - The path of the directory to create.
   * @returns {boolean} `true` if the directories were created, `false` otherwise.
   */
  mkdirsSync(path: string): boolean;

  /**
   * Removes a file or an empty directory at the given path.
   *
   * @param {string} path - The path to remove.
   * @returns {boolean} `true` if the removal was successful, `false` otherwise.
   */
  rmSync(path: string): boolean;

  /**
   * Returns the size of the file at the given path in bytes.
   *
   * @param {string} path - The path to the file.
   * @returns {number | null} The size of the file in bytes, or `null` if it doesn't exist or is a directory.
   */
  sizeSync(path: string): number | null;

  /**
   * Renames or moves a file or directory.
   *
   * @param {string} path - The original path.
   * @param {string} newPath - The new path.
   * @returns {boolean} `true` if the operation was successful, `false` otherwise.
   */
  renameSync(path: string, newPath: string): boolean;

  /**
   * Copies a file from a source path to a destination path.
   *
   * @param {string} path - The source file path.
   * @param {string} dest - The destination file path.
   * @param {boolean} [overwrite=false] - If `true`, the destination file will be overwritten if it exists.
   */
  copyFileSync(path: string, dest: string): void;
  copyFileSync(path: string, dest: string, overwrite: boolean): void;

  /**
   * Retrieves file status information, typically the last modification time as a Unix timestamp.
   *
   * @param {string} path - The path to the file or directory.
   * @returns {number | null} The last modification time (e.g., in milliseconds since epoch),
   * or `null` if the path does not exist.
   */
  statSync(path: string): number | null;

  /**
   * Creates a new, empty file at the specified path.
   *
   * @param {string} path - The path where the new file should be created.
   * @returns {boolean} `true` if the file was created, `false` if it already exists or an error occurred.
   */
  createNewFileSync(path: string): boolean;

  /**
   * Checks if the application has execute permissions for the given path.
   *
   * @param {string} path - The path to check.
   * @returns {boolean} `true` if the file is executable, `false` otherwise.
   */
  canExecuteSync(path: string): boolean;
}