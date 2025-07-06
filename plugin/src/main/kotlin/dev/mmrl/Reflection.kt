package dev.mmrl

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import org.json.JSONArray
import java.lang.reflect.InvocationTargetException

class Reflection(wxOptions: WXOptions): WXInterface(wxOptions) {
    private val objectStore = mutableMapOf<String, Any>()
    private var objectCounter = 0

    override var name: String = "reflection"

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
    fun newInstance(classId: String): String? {
        return try {
            val clazz = objectStore[classId] as? Class<*>
                ?: return null
            val instance = clazz.getDeclaredConstructor().newInstance()
            storeObject(instance)
        } catch (t: Throwable) {
            console.error(t)
            null
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
                                when (type) {
                                    Char::class.javaPrimitiveType -> rawArgs[i] is String && (rawArgs[i] as String).length == 1
                                    Int::class.javaPrimitiveType -> rawArgs[i] is Number
                                    Double::class.javaPrimitiveType -> rawArgs[i] is Number
                                    Float::class.javaPrimitiveType -> rawArgs[i] is Number
                                    Boolean::class.javaPrimitiveType -> rawArgs[i] is Boolean
                                    else -> type.isInstance(rawArgs[i])
                                }
                            } catch (t: Throwable) {
                                false
                            }
                        }
            } ?: return null

            val coercedArgs = method.parameterTypes.mapIndexed { i, type ->
                val raw = rawArgs[i]
                try {
                    when (type) {
                        Char::class.javaPrimitiveType -> (raw as String)[0]
                        Int::class.javaPrimitiveType -> (raw as Number).toInt()
                        Double::class.javaPrimitiveType -> (raw as Number).toDouble()
                        Float::class.javaPrimitiveType -> (raw as Number).toFloat()
                        Boolean::class.javaPrimitiveType -> raw as Boolean
                        else -> raw
                    }
                } catch (t: Throwable) {
                    console.error(t)
                    return null
                }
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
            field.set(obj, value)  // You might want to add type coercion here later
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
