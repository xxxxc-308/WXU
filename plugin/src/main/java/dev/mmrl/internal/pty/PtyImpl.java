package dev.mmrl.internal.pty;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Java implementation for managing a pseudo-terminal (PTY) using JNA.
 * This class replaces the original JNI C++ implementation.
 */
public final class PtyImpl {
    private final int masterFd;
    private final int pid;
    private final Thread readThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private EventListener eventListener;

    // A reference to the singleton C library instance
    private static final CLibrary C_LIBRARY = CLibrary.INSTANCE;

    /**
     * Listener interface for receiving data and exit events from the PTY.
     */
    public interface EventListener {
        /**
         * Called when data is received from the PTY's output.
         * @param data The byte array containing the data.
         */
        void onData(byte[] data);

        /**
         * Called when the PTY process has exited.
         * @param exitCode The exit code of the process.
         */
        void onExit(int exitCode);
    }

    /**
     * Private constructor to be called from the static 'start' method.
     */
    private PtyImpl(int masterFd, int pid) {
        this.masterFd = masterFd;
        this.pid = pid;
        this.running.set(true);
        this.readThread = new Thread(this::readLoop);
        this.readThread.start();
    }

    /**
     * Sets the event listener for this PTY instance.
     * @param listener The listener to handle events.
     */
    public void setEventListener(EventListener listener) {
        this.eventListener = listener;
    }

    /**
     * Starts a new process in a pseudo-terminal.
     *
     * @param shell The path to the shell or executable.
     * @param args The arguments for the command.
     * @param envVars The environment variables in "KEY=VALUE" format.
     * @param cols The initial number of columns for the terminal.
     * @param rows The initial number of rows for the terminal.
     * @return A Pty instance managing the process.
     * @throws IOException if the PTY could not be created.
     */
    public static PtyImpl start(String shell, String[] args, String[] envVars, int cols, int rows) throws IOException {
        IntByReference masterFdRef = new IntByReference();
        Winsize size = new Winsize((short) rows, (short) cols);

        int pid = C_LIBRARY.forkpty(masterFdRef, null, null, size);

        if (pid < 0) {
            throw new IOException("forkpty failed. Errno: " + Native.getLastError());
        }

        if (pid == 0) { // Child process
            // Create argv array for execve (program name + args)
            String[] argv = new String[args.length + 1];
            argv[0] = shell;
            System.arraycopy(args, 0, argv, 1, args.length);

            // Execute the new process. This call will replace the current child process.
            C_LIBRARY.execve(shell, argv, envVars);

            // execve only returns if an error occurred.
            System.exit(127); // Standard exit code for "command not found" or exec failure
            return null; // Should be unreachable
        }

        // Parent process
        return new PtyImpl(masterFdRef.getValue(), pid);
    }

    /**
     * Writes data to the PTY's input.
     * @param data The byte array of data to write.
     */
    public void write(byte[] data) {
        if (running.get() && masterFd != -1) {
            C_LIBRARY.write(masterFd, data, data.length);
        }
    }

    /**
     * Resizes the pseudo-terminal window.
     * @param cols The new number of columns.
     * @param rows The new number of rows.
     */
    public void resize(int cols, int rows) {
        if (running.get() && masterFd != -1) {
            Winsize size = new Winsize((short) rows, (short) cols);
            C_LIBRARY.ioctl(masterFd, CLibrary.TIOCSWINSZ, size);
        }
    }

    /**
     * Kills the PTY process and cleans up resources.
     */
    public void kill() {
        if (!running.compareAndSet(true, false)) {
            return; // Already killed
        }

        if (pid > 0) {
            C_LIBRARY.kill(pid, CLibrary.SIGKILL);
        }

        if (masterFd >= 0) {
            C_LIBRARY.close(masterFd);
        }

        try {
            if (readThread != null && readThread.isAlive()) {
                readThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * The main loop for the reader thread. Reads from the master PTY file descriptor
     * and forwards data to the listener.
     */
    private void readLoop() {
        byte[] buffer = new byte[4096];
        while (running.get()) {
            int count = C_LIBRARY.read(masterFd, buffer, buffer.length);
            if (count > 0) {
                if (eventListener != null) {
                    byte[] data = new byte[count];
                    System.arraycopy(buffer, 0, data, 0, count);
                    eventListener.onData(data);
                }
            } else {
                // read returns 0 on EOF, or -1 on error. Both indicate the end.
                break;
            }
        }

        // Process has exited, wait for it to get the exit code
        if (pid > 0) {
            IntByReference status = new IntByReference();
            C_LIBRARY.waitpid(pid, status, 0);
            int exitCode = WIFEXITED(status.getValue()) ? WEXITSTATUS(status.getValue()) : -1;
            if (eventListener != null) {
                eventListener.onExit(exitCode);
            }
        }

        // Final cleanup
        running.set(false);
    }

    // --- JNA Native Mappings ---

    /**
     * JNA mapping for the standard C library (libc).
     * @noinspection UnusedReturnValue
     */
    private interface CLibrary extends Library {
        CLibrary INSTANCE = Native.load("c", CLibrary.class);

        // Constants
        int SIGKILL = 9;
        long TIOCSWINSZ = 0x5414; // Value for Linux, may vary on other systems

        // Functions
        int read(int fd, byte[] buffer, int count);
        int write(int fd, byte[] buffer, int count);
        int close(int fd);
        int kill(int pid, int sig);
        int ioctl(int fd, long request, Structure arg);
        int waitpid(int pid, IntByReference status, int options);
        int execve(String filename, String[] argv, String[] envp);
        int forkpty(IntByReference amaster, Pointer name, Pointer termios, Winsize winsize);
    }

    /**
     * JNA mapping for the C 'winsize' struct.
     */
    @Structure.FieldOrder({"ws_row", "ws_col", "ws_xpixel", "ws_ypixel"})
    public static class Winsize extends Structure {
        public short ws_row;
        public short ws_col;
        public short ws_xpixel;
        public short ws_ypixel;

        public Winsize() {}

        public Winsize(short rows, short cols) {
            this.ws_row = rows;
            this.ws_col = cols;
        }
    }

    // --- C Macro Re-implementations ---

    /**
     * Re-implementation of the WIFEXITED C macro.
     */
    private static boolean WIFEXITED(int status) {
        return (status & 0x7f) == 0;
    }

    /**
     * Re-implementation of the WEXITSTATUS C macro.
     */
    private static int WEXITSTATUS(int status) {
        return (status & 0xff00) >> 8;
    }
}
