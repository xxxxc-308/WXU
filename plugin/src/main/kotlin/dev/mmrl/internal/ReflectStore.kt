package dev.mmrl.internal

import com.dergoogler.mmrl.platform.model.ModId

object ReflectStore {
    val objectStore = mutableMapOf<String, Any>()
    var objectCounter = 0

    fun storeObject(obj: Any, modId: ModId): String {
        val id = "wx_reflect_obj_${objectCounter++}_$modId"
        objectStore[id] = obj
        return id
    }

    fun removeObject(id: String): Any? = objectStore.remove(id)

    fun isValid(id: String, modId: ModId): Boolean =
        id.startsWith("wx_reflect_obj_") && id.endsWith("_$modId")

    inline fun <reified T> getObject(id: String): T? = objectStore[id] as? T
}