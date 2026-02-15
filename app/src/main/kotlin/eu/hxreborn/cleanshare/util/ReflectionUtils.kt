package eu.hxreborn.cleanshare.util

import java.lang.reflect.Method

// Find first loadable class from candidates
internal fun findClass(
    classLoader: ClassLoader,
    candidates: List<String>,
): Class<*>? =
    candidates.firstNotNullOfOrNull { name ->
        runCatching { classLoader.loadClass(name) }.getOrNull()
    }

// Walk class hierarchy to find method by name regardless of visibility
internal fun findMethodByName(
    clazz: Class<*>,
    name: String,
): Method? =
    generateSequence(clazz) { it.superclass }
        .takeWhile { it != Any::class.java }
        .firstNotNullOfOrNull { cls ->
            cls.declaredMethods.firstOrNull { it.name == name }
        }
