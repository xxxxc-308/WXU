/**
 * Defines an interface for reflecting the Java runtime from a WebView.
 * This interface provides a powerful, yet potentially insecure, bridge allowing JavaScript
 * running within a WebView to introspect and interact with the native Java environment
 * of an Android application. It allows for class lookup, object instantiation,
 * method invocation, and field manipulation.
 *
 * It is crucial to understand the security implications of exposing this interface
 * to a WebView, as it can grant untrusted web content significant control over the
 * native application.
 *
 * All identifiers for classes and objects (`classId`, `objectId`) are returned as
 * strings. The native Java side is responsible for managing a mapping between these
 * string identifiers and the actual Java Class and Object references.
 */
interface Reflect {
  /**
   * Retrieves a reference to a Java class by its fully qualified name.
   * This is the entry point for most reflection operations, allowing JavaScript
   * to get a handle on a specific Java class.
   *
   * @example
   * // Get a reference to the java.io.File class
   * const fileClassId = window.myJavaReflector.getClass('java.io.File');
   *
   * @param {string} className - The fully qualified name of the Java class to look up (e.g., "java.lang.String").
   * @returns {string | null} A string identifier representing the class, which can be used in
   * subsequent calls like `newInstance`. Returns `null` if the class cannot be found
   * or if access is denied for security reasons.
   */
  getClass(className: string): string | null;

  /**
   * Creates a new instance of a Java class using its class identifier.
   * This is equivalent to calling a constructor in Java. Note that this example
   * assumes a default (no-argument) constructor. More complex implementations
   * might handle constructors with arguments via `callMethod` with a special
   * method name like "<init>".
   *
   * @example
   * const fileClassId = window.myJavaReflector.getClass('java.io.File');
   * if (fileClassId) {
   * // This assumes a constructor that takes a string. We'll use callMethod for that.
   * // For a no-arg constructor:
   * // const fileObjectId = window.myJavaReflector.newInstance(fileClassId);
   * }
   *
   * @param {string} classId - The string identifier for the class, previously obtained from `getClass`.
   * @returns {string | null} A string identifier for the newly created object instance.
   * Returns `null` if instantiation fails (e.g., the class is abstract, no suitable
   * constructor is found, or an exception occurs during construction).
   */
  newInstance(classId: string): string | null;

  /**
   * Invokes a method on a specific Java object instance.
   * This is a highly versatile method that allows for execution of almost any
   * method on an exposed Java object. Arguments are passed as a JSON string
   * to accommodate various data types.
   *
   * @example
   * // Assuming 'fileObject' is an instance of java.io.File with a path
   * const objectId = ...; // ID for a File object
   * const args = JSON.stringify([]); // No arguments for getAbsolutePath
   * const absolutePath = window.myJavaReflector.callMethod(objectId, 'getAbsolutePath', args);
   * console.log('The absolute path is:', absolutePath);
   *
   * @param {string} objectId - The identifier of the object instance on which to invoke the method.
   * @param {string} methodName - The name of the method to call.
   * @param {string} argsJson - A JSON-formatted string representing an array of arguments
   * to be passed to the method. The native implementation must deserialize this
   * JSON into the appropriate Java types. An empty array `[]` should be passed
   * for methods with no arguments.
   * @returns {string | null} A string representation of the value returned by the Java method.
   * Complex objects returned by the Java method might be returned as a new `objectId`
   * or as a JSON string. Primitive types would be converted to their string equivalent.
   * Returns `null` if the method does not exist, arguments are incorrect, or an
   * exception is thrown during invocation.
   */
  callMethod(
    objectId: string,
    methodName: string,
    argsJson: string
  ): string | null;

  /**
   * Retrieves the value of a field (a public member variable) from a Java object instance.
   *
   * @example
   * // This is less common as fields are often private. But for a public field:
   * // class MyObject { public String publicName = "Test"; }
   * const myObjectId = ...;
   * const name = window.myJavaReflector.getField(myObjectId, 'publicName'); // Returns "Test"
   *
   * @param {string} objectId - The identifier of the object instance.
   * @param {string} fieldName - The name of the field whose value is to be retrieved.
   * @returns {string | null} A string representation of the field's value. Returns `null` if
   * the field does not exist, is not accessible, or an error occurs.
   */
  getField(objectId: string, fieldName: string): string | null;

  /**
   * Modifies the value of a field (a public member variable) on a Java object instance.
   *
   * @example
   * // class MyObject { public String publicName = "Test"; }
   * const myObjectId = ...;
   * const success = window.myJavaReflector.setField(myObjectId, 'publicName', 'New Value');
   * // success will be true
   *
   * @param {string} objectId - The identifier of the object instance.
   * @param {string} fieldName - The name of the field to modify.
   * @param {string} value - The new value for the field, provided as a string. The native
   * implementation is responsible for converting this string to the appropriate Java type.
   * @returns {boolean} `true` if the field was set successfully; `false` otherwise.
   */
  setField(objectId: string, fieldName: string, value: string): boolean;

  /**
   * Releases the reference to a Java object held by the native bridge.
   * This is a crucial memory management function. JavaScript should call this when it is
   * finished with a Java object to allow the Java garbage collector to reclaim its memory.
   * Failure to do so will result in memory leaks in the native application.
   *
   * @example
   * const objectId = ...;
   * // ... use the object ...
   * window.myJavaReflector.releaseObject(objectId); // Clean up
   *
   * @param {string} objectId - The identifier of the object to release.
   * @returns {boolean} `true` if the object reference was found and successfully removed;
   * `false` otherwise.
   */
  releaseObject(objectId: string): boolean;
}
