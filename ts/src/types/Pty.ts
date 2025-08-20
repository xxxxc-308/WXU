export namespace Pty {
  export interface Shell {
    start(
      sh: String,
      argsJson: String | null,
      envJson: String | null
    ): Instance | null;
    start(
      sh: String,
      argsJson: String,
      envJson: String,
      cols: number,
      rows: number
    ): Instance | null;
  }

  export interface Instance {
    write(data: String): void;
    kill(): void;
    resize(cols: number, rows: number): void;
  }
}
