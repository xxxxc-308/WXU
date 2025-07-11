@file:Suppress("unused")

package dev.mmrl.module

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.internal.WXUInterface
import org.json.JSONArray
import java.lang.reflect.InvocationTargetException

class Reflection(wxOptions: WXOptions) : WXUInterface(wxOptions) {
    override var name = "reflect"

    private val objectStore = mutableMapOf<String, Any>()
    private var objectCounter = 0

    private fun storeObject(obj: Any): String {
        val id = "obj_${objectCounter++}"
        objectStore[id] = obj
        return id
    }

    @JavascriptInterface
    fun getClass(className: String): String? {
        return try {
            val clazz = Class.forName(className)
            storeObject(clazz)
        } catch (t: Throwable) {
            console.error(t)
            null
        }
    }

    @JavascriptInterface
    fun newInstance(classId: String, argsJson: String?): String? {
        return try {
            val clazz = objectStore[classId] as? Class<*> ?: return null

            if (argsJson.isNullOrBlank() || argsJson == "null") {
                val instance = clazz.getDeclaredConstructor().newInstance()
                storeObject(instance)
            } else {
                val argsArray = JSONArray(argsJson)
                val rawArgs = Array(argsArray.length()) { i -> argsArray.get(i) }

                val constructors = clazz.declaredConstructors

                val constructor = constructors.firstOrNull { ctor ->
                    val paramTypes = ctor.parameterTypes
                    paramTypes.size == rawArgs.size &&
                            paramTypes.withIndex().all { (i, paramType) ->
                                try {
                                    isTypeCompatible(paramType, rawArgs[i])
                                } catch (t: Throwable) {
                                    false
                                }
                            }
                } ?: return null

                val coercedArgs = constructor.parameterTypes.mapIndexed { i, type ->
                    coerceArg(type, rawArgs[i])
                }.toTypedArray()

                val instance = constructor.newInstance(*coercedArgs)
                storeObject(instance)
            }
        } catch (t: InvocationTargetException) {
            console.error(t.targetException ?: t)
            null
        } catch (t: Throwable) {
            console.error(t)
            null
        }
    }

    private fun isTypeCompatible(type: Class<*>, value: Any): Boolean {
        return when {
            type.isInstance(value) -> true
            value is String && value.startsWith("obj_") -> {
                val obj = objectStore[value]
                obj != null && type.isInstance(obj)
            }
            type == Char::class.javaPrimitiveType -> value is String && value.length == 1
            type == Int::class.javaPrimitiveType -> value is Number
            type == Float::class.javaPrimitiveType -> value is Number
            type == Double::class.javaPrimitiveType -> value is Number
            type == Boolean::class.javaPrimitiveType -> value is Boolean
            else -> false
        }
    }

    private fun coerceArg(type: Class<*>, raw: Any): Any? {
        return when {
            raw is String && raw.startsWith("obj_") -> objectStore[raw]
            type == Char::class.javaPrimitiveType -> (raw as String)[0]
            type == Int::class.javaPrimitiveType -> (raw as Number).toInt()
            type == Float::class.javaPrimitiveType -> (raw as Number).toFloat()
            type == Double::class.javaPrimitiveType -> (raw as Number).toDouble()
            type == Boolean::class.javaPrimitiveType -> raw as Boolean
            else -> raw
        }
    }

    @JavascriptInterface
    fun callMethod(objectId: String, methodName: String, argsJson: String?): String? {
        return try {
            val obj = objectStore[objectId] ?: return null

            val candidateMethods = obj.javaClass.methods.filter { it.name == methodName }

            if (argsJson.isNullOrBlank()) {
                // No args case: find method with zero parameters
                val method = candidateMethods.firstOrNull { it.parameterTypes.isEmpty() } ?: return null
                val result = method.invoke(obj)
                when (result) {
                    null -> null
                    is String -> result
                    is Int -> result.toString()
                    is Float -> result.toString()
                    is Double -> result.toString()
                    is Boolean -> result.toString()
                    else -> storeObject(result)
                }
            } else {
                val argsArray = JSONArray(argsJson)
                val rawArgs = Array(argsArray.length()) { argsArray.get(it) }

                val method = candidateMethods.firstOrNull { m ->
                    val paramTypes = m.parameterTypes
                    if (paramTypes.size != rawArgs.size) return@firstOrNull false
                    paramTypes.withIndex().all { (i, type) ->
                        try {
                            isTypeCompatible(type, rawArgs[i])
                        } catch (_: Throwable) {
                            false
                        }
                    }
                } ?: return null

                val coercedArgs = method.parameterTypes.mapIndexed { i, type ->
                    coerceArg(type, rawArgs[i])
                }.toTypedArray()

                val result = method.invoke(obj, *coercedArgs)
                when (result) {
                    null -> null
                    is String -> result
                    else -> storeObject(result)
                }
            }
        } catch (e: InvocationTargetException) {
            console.error(e.targetException ?: e)
            null
        } catch (e: Throwable) {
            console.error(e)
            null
        }
    }


    @JavascriptInterface
    fun getField(objectId: String, fieldName: String): String? {
        return try {
            val obj = objectStore[objectId] ?: return null
            val field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            val result = field.get(obj)
            when (result) {
                is String -> result
                null -> null
                else -> storeObject(result)
            }
        } catch (t: Throwable) {
            console.error(t)
            null
        }
    }

    @JavascriptInterface
    fun setField(objectId: String, fieldName: String, value: String): Boolean {
        return try {
            val obj = objectStore[objectId] ?: return false
            val field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(obj, value)  // Type coercion can be added here
            true
        } catch (t: Throwable) {
            console.error(t)
            false
        }
    }

    @JavascriptInterface
    fun releaseObject(objectId: String): Boolean {
        return try {
            objectStore.remove(objectId) != null
        } catch (t: Throwable) {
            console.error(t)
            false
        }
    }
}
