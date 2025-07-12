@file:Suppress("unused")

package dev.mmrl.module

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.webui.interfaces.WXConsole
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.internal.ReflectStore
import dev.mmrl.internal.WXUInterface
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class Reflection(wxOptions: WXOptions) : WXUInterface(wxOptions) {
    override var name = "reflect"

    // region Core Javascript Interfaces
    @JavascriptInterface
    fun getClass(className: String): String? {
        return try {
            val clazz = Class.forName(className)
            ReflectStore.storeObject(clazz, modId)
        } catch (t: Throwable) {
            console.error(t)
            null
        }
    }

    @JavascriptInterface
    fun newInstance(classId: String, argsJson: String?): String? {
        return try {
            val clazz = ReflectStore.getObject<Class<*>>(classId) ?: return null
            val rawArgs = parseJsonArgs(argsJson)

            // IMPROVEMENT: Centralized logic to find constructor and coerce arguments.
            val signature = findMatchingCallable(clazz.declaredConstructors, rawArgs) ?: run {
                console.error("No suitable constructor found for ${clazz.name} with provided args.")
                return null
            }

            val instance = signature.callable.newInstance(*signature.coercedArgs)
            ReflectStore.storeObject(instance, modId)
        } catch (t: InvocationTargetException) {
            console.error(t.targetException ?: t)
            null
        } catch (t: Throwable) {
            console.error(t)
            null
        }
    }

    @JavascriptInterface
    fun callMethod(objectId: String, methodName: String, argsJson: String?): String? {
        try {
            val obj = ReflectStore.getObject<Any?>(objectId) ?: return null
            val rawArgs = parseJsonArgs(argsJson)
            val candidateMethods = obj.javaClass.methods.filter { it.name == methodName }

            if (candidateMethods.isEmpty()) {
                console.error("No method named '$methodName' found on object $objectId")
                return null
            }

            val signature = findMatchingCallable(candidateMethods, rawArgs) ?: run {
                console.error("No method overload for '$methodName' matches provided args on object $objectId")
                return null
            }

            // Invoke the method and let our robust helper decide how to represent the result.
            val result = signature.callable.invoke(obj, *signature.coercedArgs)
            return toJsRepresentation(result, modId)
        } catch (e: InvocationTargetException) {
            console.error(e.targetException ?: e)
            return null
        } catch (e: Throwable) {
            console.error(e)
            return null
        }
    }

    @JavascriptInterface
    fun getField(objectId: String, fieldName: String): String? {
        return try {
            val obj = ReflectStore.getObject<Any?>(objectId) ?: return null
            val field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            val result = field.get(obj)
            toJsRepresentation(result, modId)
        } catch (t: Throwable) {
            console.error(t)
            null
        }
    }

    @JavascriptInterface
    fun setField(objectId: String, fieldName: String, value: Any?): Boolean {
        return try {
            val obj = ReflectStore.getObject<Any?>(objectId) ?: return false
            val field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            // IMPROVEMENT: Coerce incoming JS value to the actual field type.
            val coercedValue = coerceArgument(field.type, value)
            field.set(obj, coercedValue)
            true
        } catch (t: Throwable) {
            console.error(t)
            false
        }
    }

    @JavascriptInterface
    fun createProxy(interfaceName: String, methodsMapJson: String): String? {
        return try {
            val interfaceClass = Class.forName(interfaceName)
            if (!interfaceClass.isInterface) {
                console.error("$interfaceName is not an interface")
                return null
            }

            val methodsMap = JSONObject(methodsMapJson)
            val proxy = Proxy.newProxyInstance(
                interfaceClass.classLoader,
                arrayOf(interfaceClass),
                ProxyInvocationHandler(methodsMap)
            )

            ReflectStore.storeObject(proxy, modId)
        } catch (t: Throwable) {
            console.error(t)
            null
        }
    }

    @JavascriptInterface
    fun releaseObject(objectId: String): Boolean {
        return try {
            ReflectStore.removeObject(objectId) != null
        } catch (t: Throwable) {
            console.error(t)
            false
        }
    }
    // endregion

    // region Private Helpers
    /** A helper class to hold a resolved method/constructor and its coerced arguments. */
    private data class CallableSignature<T : Executable>(
        val callable: T,
        val coercedArgs: Array<Any?>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CallableSignature<*>

            if (callable != other.callable) return false
            if (!coercedArgs.contentEquals(other.coercedArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = callable.hashCode()
            result = 31 * result + coercedArgs.contentHashCode()
            return result
        }
    }

    /**
     * Finds a matching executable (method or constructor) from a list of candidates
     * that is compatible with the given raw arguments from JavaScript.
     */
    private fun <T : Executable> findMatchingCallable(
        candidates: Array<T>,
        rawArgs: Array<Any>,
    ): CallableSignature<T>? {
        val compatibleCallable = candidates.firstOrNull { candidate ->
            val paramTypes = candidate.parameterTypes
            paramTypes.size == rawArgs.size && paramTypes.withIndex().all { (i, paramType) ->
                isTypeCompatible(paramType, rawArgs[i])
            }
        } ?: return null

        val coercedArgs = compatibleCallable.parameterTypes.mapIndexed { i, type ->
            coerceArgument(type, rawArgs[i])
        }.toTypedArray()

        return CallableSignature(compatibleCallable, coercedArgs)
    }

    // Overload for Lists (used by `callMethod`)
    private fun findMatchingCallable(
        candidates: List<Method>,
        rawArgs: Array<Any>,
    ) = findMatchingCallable(candidates.toTypedArray(), rawArgs)


    private fun isTypeCompatible(type: Class<*>, value: Any): Boolean {
        if (value == JSONObject.NULL) return !type.isPrimitive
        return when (type) {
            Char::class.javaPrimitiveType, Char::class.java -> value is String && value.length == 1
            Int::class.javaPrimitiveType, Integer::class.java -> value is Number
            Long::class.javaPrimitiveType, Long::class.java -> value is Number
            Float::class.javaPrimitiveType, Float::class.java -> value is Number
            Double::class.javaPrimitiveType, Double::class.java -> value is Number
            Boolean::class.javaPrimitiveType, Boolean::class.java -> value is Boolean
            else -> when {
                type.isInstance(value) -> true
                value is String && ReflectStore.isValid(value, modId) -> {
                    val obj = ReflectStore.getObject<Any?>(value)
                    obj != null && type.isInstance(obj)
                }

                else -> false
            }
        }
    }

    private fun coerceArgument(type: Class<*>, raw: Any?): Any? {
        if (raw == null || raw == JSONObject.NULL) return null
        return when (type) {
            Char::class.javaPrimitiveType, Char::class.java -> (raw as String)[0]
            Int::class.javaPrimitiveType, Integer::class.java -> (raw as Number).toInt()
            Long::class.javaPrimitiveType, Long::class.java -> (raw as Number).toLong()
            Float::class.javaPrimitiveType, Float::class.java -> (raw as Number).toFloat()
            Double::class.javaPrimitiveType, Double::class.java -> (raw as Number).toDouble()
            Boolean::class.javaPrimitiveType, Boolean::class.java -> raw as Boolean
            String::class.java -> raw.toString()
            else -> if (raw is String && ReflectStore.isValid(raw, modId)) ReflectStore.getObject(
                raw
            ) else raw
        }
    }

    private fun parseJsonArgs(json: String?): Array<Any> {
        if (json.isNullOrBlank() || json == "null") return emptyArray()
        val jsonArray = JSONArray(json)
        return Array(jsonArray.length()) { jsonArray.get(it) }
    }

    private fun toJsRepresentation(result: Any?, modId: ModId): String? {
        return when (result) {
            null -> null
            is String -> result
            is Int -> result.toString()
            is Float -> result.toString()
            is Double -> result.toString()
            is Boolean -> result.toString()
            else -> ReflectStore.storeObject(result, modId)
        }
    }
    // endregion

    // region Proxy Implementation
    private inner class ProxyInvocationHandler(
        private val methodsMap: JSONObject,
    ) : InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<Any?>?): Any? {
            // Handle standard Object methods locally
            when (method.name) {
                "hashCode" -> return System.identityHashCode(proxy)
                "equals" -> return proxy === args?.get(0)
                "toString" -> return "JavaScriptProxy@${
                    Integer.toHexString(
                        System.identityHashCode(
                            proxy
                        )
                    )
                }"
            }

            val callbackId = methodsMap.optString(method.name)
            if (callbackId.isEmpty()) {
                // No JS handler provided, return a default value
                return getDefaultReturnValue(method.returnType)
            }

            val argsArray = JSONArray()
            args?.forEach { arg -> argsArray.put(toJsRepresentation(arg, modId)) }

            // IMPORTANT: This is a "fire-and-forget" call. The JavaScript result is NOT returned here.
            // The function will immediately return a default value below.
            // For true two-way communication, the JS bridge architecture would need to be asynchronous.
            val script = "JavaObject.proxyHandlers.get('$callbackId')(${
                argsArray.toString().drop(1).dropLast(1)
            })"
            runJs(script)

            return getDefaultReturnValue(method.returnType)
        }

        private fun getDefaultReturnValue(type: Class<*>?): Any? {
            return when (type?.name) {
                "boolean" -> false
                "void" -> null
                "char" -> '\u0000'
                "byte", "short", "int", "long", "float", "double" -> 0
                else -> null // For all object types
            }
        }
    }
    // endregion
}