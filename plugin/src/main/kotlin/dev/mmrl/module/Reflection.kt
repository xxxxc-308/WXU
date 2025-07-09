@file:Suppress("unused")

package dev.mmrl.module

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import org.json.JSONArray
import java.lang.reflect.InvocationTargetException

class Reflection(wxOptions: WXOptions) : WXInterface(wxOptions) {
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
    fun newInstance(classId: String, argsJson: String? = null): String? {
        return try {
            val clazz = objectStore[classId] as? Class<*> ?: return null

            if (argsJson == null || argsJson == "null") {
                val instance = clazz.getDeclaredConstructor().newInstance()
                storeObject(instance)
            } else {
                val argsArray = JSONArray(argsJson)
                val rawArgs = Array(argsArray.length()) { i -> argsArray.get(i) }

                val constructors = clazz.declaredConstructors

                val constructor = constructors.firstOrNull { ctor ->
                    ctor.parameterTypes.size == rawArgs.size &&
                            ctor.parameterTypes.withIndex().all { (i, paramType) ->
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
    fun callMethod(objectId: String, methodName: String, argsJson: String): String? {
        return try {
            val obj = objectStore[objectId] ?: return null
            val args = JSONArray(argsJson)
            val rawArgs = Array(args.length()) { i -> args.get(i) }
            val methods = obj.javaClass.methods.filter { it.name == methodName }

            val method = methods.firstOrNull { m ->
                m.parameterTypes.size == rawArgs.size &&
                        m.parameterTypes.withIndex().all { (i, type) ->
                            try {
                                isTypeCompatible(type, rawArgs[i])
                            } catch (t: Throwable) {
                                false
                            }
                        }
            } ?: return null

            val coercedArgs = method.parameterTypes.mapIndexed { i, type ->
                coerceArg(type, rawArgs[i])
            }.toTypedArray()

            val result = method.invoke(obj, *coercedArgs)
            when (result) {
                is String -> result
                null -> null
                else -> storeObject(result)
            }
        } catch (t: InvocationTargetException) {
            console.error(t.targetException ?: t)
            null
        } catch (t: Throwable) {
            console.error(t)
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
