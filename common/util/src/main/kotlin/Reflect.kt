import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

object Reflect {
    fun <T: Any> toMap(obj: T): Map<String, Any?> {
        return (obj::class as KClass<T>).memberProperties.associate { prop ->
            prop.name to prop.get(obj)?.let { value ->
                if (value::class.isData) {
                    toMap(value)
                } else {
                    value
                }
            }
        }
    }
}