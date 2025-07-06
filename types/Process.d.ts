interface Process {
  getPlatform(): string;
  isAlive(): boolean;
  getApplicationLibraryDir(): string | null;
}
