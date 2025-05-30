@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.github.kyuubiran.ezxhelper.finders

import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.MemberExtensions.isAbstract
import com.github.kyuubiran.ezxhelper.MemberExtensions.isFinal
import com.github.kyuubiran.ezxhelper.MemberExtensions.isPublic
import com.github.kyuubiran.ezxhelper.finders.base.BaseFinder
import com.github.kyuubiran.ezxhelper.misc.Utils.findDexClassLoader
import com.github.kyuubiran.ezxhelper.misc.Utils.getAllClassesList
import dalvik.system.BaseDexClassLoader

class ClassFinder private constructor(seq: Sequence<Class<*>>) :
    BaseFinder<Class<*>, ClassFinder>(seq) {
    override val name: String
        get() = "ClassFinder"

    @Suppress("RemoveRedundantBackticks")
    companion object `Static` {
        @JvmStatic
        fun fromClassloader(classLoader: ClassLoader) = ClassFinder(
            classLoader.getAllClassesList().asSequence()
                .map { Class.forName(it, false, classLoader) }).apply {
            exceptMessageScope { ctor(this@apply, "No such class found in classloader") }
        }

        @JvmStatic
        fun fromClassloader(
            classLoader: ClassLoader,
            delegator: (BaseDexClassLoader) -> BaseDexClassLoader = { cl -> cl }
        ): ClassFinder {
            val cl = classLoader.findDexClassLoader(delegator)

            return ClassFinder(
                (cl?.getAllClassesList()?.asSequence() ?: emptySequence())
                    .map { Class.forName(it, false, cl) }).apply {
                exceptMessageScope { ctor(this@apply, "No such class found in classloader") }
            }
        }

        @JvmStatic
        fun fromArray(array: Array<Class<*>>) = ClassFinder(array.asSequence()).apply {
            exceptMessageScope { ctor(this@apply, "No such class found in array(size=${array.count()})") }
        }

        @JvmStatic
        fun of(vararg array: Class<*>) = ClassFinder(array.asSequence()).apply {
            exceptMessageScope { ctor(this@apply, "No such class found in array(size=${array.count()})") }
        }

        @JvmStatic
        fun of(classLoader: ClassLoader = EzXHelper.safeClassLoader, vararg className: String) {
            val classes = className.map { Class.forName(it, false, classLoader) }
            ClassFinder(classes.asSequence()).apply {
                exceptMessageScope { ctor(this@apply, "No such class found in array(size=${classes.count()})") }
            }
        }

        @JvmStatic
        fun fromIterable(iterable: Iterable<Class<*>>) = ClassFinder(iterable.asSequence()).apply {
            exceptMessageScope { ctor(this@apply, "No such class found in iterable)") }
        }

        @JvmStatic
        fun fromSequence(seq: Sequence<Class<*>>) = ClassFinder(seq).apply {
            exceptMessageScope { ctor(this@apply, "No such class found in sequence(size=${seq.count()})") }
        }
    }

    fun filterPackage(packageName: String) = makeNewFinder {
        sequence = sequence.filter { it.name.startsWith(packageName) }
        exceptMessageScope { condition("filterPackage($packageName)") }
    }

    fun filterHasFieldType(type: Class<*>) = makeNewFinder {
        sequence = sequence.filter { it.fields.any { field -> field.type == type } }
        exceptMessageScope { condition("filterHasFieldType(${type.name})") }
    }

    fun filterHasFieldTypeAndCount(type: Class<*>, count: Int) = makeNewFinder {
        sequence = sequence.filter { it.fields.count { field -> field.type == type } == count }
        exceptMessageScope { condition("filterHasFieldTypeAndCount(Type=${type.name}, Cnt=$count)") }
    }

    @JvmOverloads
    fun filterHasFieldTypeAndCountIn(type: Class<*>, min: Int = 1, max: Int = Int.MAX_VALUE) =
        makeNewFinder {
            sequence = sequence.filter {
                val count = it.fields.count { field -> field.type == type }
                count in min..max
            }
            exceptMessageScope { condition("filterHasFieldTypeAndCountIn(Type=${type.name}, Min=$min, Max=$max)") }
        }


    fun filterHasFieldTypeAndCountIn(type: Class<*>, range: IntRange) =
        makeNewFinder {
            sequence = sequence.filter {
                val count = it.fields.count { field -> field.type == type }
                count in range
            }
            exceptMessageScope { condition("filterHasFieldTypeAndCountIn(Type=${type.name}, Min=${range.first}, Max=${range.last})") }
        }

    fun filterHasFieldTypeName(typeName: String) = makeNewFinder {
        sequence = sequence.filter { it.fields.any { field -> field.type.name == typeName } }
        exceptMessageScope { condition("filterHasFieldTypeName($typeName)") }
    }

    fun filterHasFieldName(fieldName: String) = makeNewFinder {
        sequence = sequence.filter { it.fields.any { field -> field.name == fieldName } }
        exceptMessageScope { condition("filterHasFieldName($fieldName)") }
    }

    fun filterHasMethodName(methodName: String) = makeNewFinder {
        sequence = sequence.filter { it.methods.any { method -> method.name == methodName } }
        exceptMessageScope { condition("filterHasMethodName($methodName)") }
    }

    fun filterHasMethodReturnType(returnType: Class<*>) = makeNewFinder {
        sequence = sequence.filter { it.methods.any { method -> method.returnType == returnType } }
        exceptMessageScope { condition("filterHasMethodReturnType($returnType)") }
    }

    fun filterHasMethodSignature(returnType: Class<*>, vararg paramTypes: Class<*>) = makeNewFinder {
        sequence = sequence.filter { clazz ->
            clazz.methods.any { method ->
                method.returnType == returnType && method.parameterTypes.contentEquals(paramTypes)
            }
        }
        exceptMessageScope {
            condition("filterHasMethodSignature(Ret=${returnType.name}, Params=(${paramTypes.joinToString { it.name }}))")
        }
    }

    fun filterHasConstructorSignature(vararg paramTypes: Class<*>) = makeNewFinder {
        sequence = sequence.filter { clazz ->
            clazz.constructors.any { constructor -> constructor.parameterTypes.contentEquals(paramTypes) }
        }
        exceptMessageScope {
            condition("filterHasConstructorSignature(Params=(${paramTypes.joinToString { it.name }}))")
        }
    }

    fun filterImplementInterfaces(vararg interfaces: Class<*>) = makeNewFinder {
        sequence = sequence.filter { clazz ->
            interfaces.all { interfaceClass -> interfaceClass.isAssignableFrom(clazz) }
        }
        exceptMessageScope {
            condition("filterImplementInterfaces(${interfaces.joinToString { it.name }})")
        }
    }

    fun filterHasConstructorCount(cnt: Int) = makeNewFinder {
        sequence = sequence.filter { clazz -> clazz.constructors.count() == cnt }
        exceptMessageScope {
            condition("filterHasConstructorCount($cnt)")
        }
    }

    fun filterHasConstructorCountIn(range: IntRange) = makeNewFinder {
        sequence = sequence.filter { clazz -> clazz.constructors.count() in range }
        exceptMessageScope {
            condition("filterHasConstructorCountIn(Min=${range.first}, Max=${range.last})")
        }
    }

    @JvmOverloads
    fun filterHasConstructorCountIn(min: Int = 1, max: Int = Int.MAX_VALUE) = makeNewFinder {
        sequence = sequence.filter { clazz -> clazz.constructors.count() in min..max }
        exceptMessageScope {
            condition("filterHasConstructorCountIn(Min=$min, Max=$max)")
        }
    }

    fun filterIsSubclassOf(superclass: Class<*>) = makeNewFinder {
        sequence = sequence.filter { superclass.isAssignableFrom(it) }
        exceptMessageScope { condition("filterIsSubclassOf(${superclass.name})") }
    }

    fun filterIsAbstract() = makeNewFinder {
        sequence = sequence.filter { it.isAbstract }
        exceptMessageScope { condition("filterIsAbstract") }
    }

    fun filterIsNotAbstract() = makeNewFinder {
        sequence = sequence.filter { !it.isAbstract }
        exceptMessageScope { condition("filterIsNotAbstract") }
    }

    fun filterIsInterface() = makeNewFinder {
        sequence = sequence.filter { it.isInterface }
        exceptMessageScope { condition("filterIsInterface") }
    }

    fun filterIsNotInterface() = makeNewFinder {
        sequence = sequence.filter { !it.isInterface }
        exceptMessageScope { condition("filterIsNotInterface") }
    }

    fun filterIsEnum() = makeNewFinder {
        sequence = sequence.filter { it.isEnum }
        exceptMessageScope { condition("filterIsEnum") }
    }

    fun filterIsNotEnum() = makeNewFinder {
        sequence = sequence.filter { !it.isEnum }
        exceptMessageScope { condition("filterIsNotEnum") }
    }

    fun filterIsAnnotation() = makeNewFinder {
        sequence = sequence.filter { it.isAnnotation }
        exceptMessageScope { condition("filterIsAnnotation") }
    }

    fun filterIsNotAnnotation() = makeNewFinder {
        sequence = sequence.filter { !it.isAnnotation }
        exceptMessageScope { condition("filterIsNotAnnotation") }
    }

    fun filterIsPublic() = makeNewFinder {
        sequence = sequence.filter { it.isPublic }
        exceptMessageScope { condition("filterIsPublic") }
    }

    fun filterIsNotPublic() = makeNewFinder {
        sequence = sequence.filter { !it.isPublic }
        exceptMessageScope { condition("filterIsNotPublic") }
    }

    fun filterIsFinal() = makeNewFinder {
        sequence = sequence.filter { it.isFinal }
        exceptMessageScope { condition("filterIsFinal") }
    }

    fun filterIsNotFinal() = makeNewFinder {
        sequence = sequence.filter { !it.isFinal }
        exceptMessageScope { condition("filterIsNotFinal") }
    }

    fun filterIsSynthetic() = makeNewFinder {
        sequence = sequence.filter { it.isSynthetic }
        exceptMessageScope { condition("filterIsSynthetic") }
    }

    fun filterIsNotSynthetic() = makeNewFinder {
        sequence = sequence.filter { !it.isSynthetic }
        exceptMessageScope { condition("filterIsNotSynthetic") }
    }

    fun filterIsAnonymous() = makeNewFinder {
        sequence = sequence.filter { it.isAnonymousClass }
        exceptMessageScope { condition("filterIsAnonymous") }
    }

    fun filterIsNotAnonymous() = makeNewFinder {
        sequence = sequence.filter { !it.isAnonymousClass }
        exceptMessageScope { condition("filterIsNotAnonymous") }
    }

    fun filterIsLocal() = makeNewFinder {
        sequence = sequence.filter { it.isLocalClass }
        exceptMessageScope { condition("filterIsLocal") }
    }

    fun filterIsNotLocal() = makeNewFinder {
        sequence = sequence.filter { !it.isLocalClass }
        exceptMessageScope { condition("filterIsNotLocal") }
    }

    fun filterIsMember() = makeNewFinder {
        sequence = sequence.filter { it.isMemberClass }
        exceptMessageScope { condition("filterIsMember") }
    }

    fun filterIsNotMember() = makeNewFinder {
        sequence = sequence.filter { !it.isMemberClass }
        exceptMessageScope { condition("filterIsNotMember") }
    }

    fun filterIsPrimitive() = makeNewFinder {
        sequence = sequence.filter { it.isPrimitive }
        exceptMessageScope { condition("filterIsPrimitive") }
    }

    fun filterIsNotPrimitive() = makeNewFinder {
        sequence = sequence.filter { !it.isPrimitive }
        exceptMessageScope { condition("filterIsNotPrimitive") }
    }

    fun filterIsArray() = makeNewFinder {
        sequence = sequence.filter { it.isArray }
        exceptMessageScope { condition("filterIsArray") }
    }

    fun filterIsNotArray() = makeNewFinder {
        sequence = sequence.filter { !it.isArray }
        exceptMessageScope { condition("filterIsNotArray") }
    }

    fun filterIsAnnotationPresent(annotation: Class<out Annotation>) = makeNewFinder {
        sequence = sequence.filter { it.isAnnotationPresent(annotation) }
        exceptMessageScope { condition("filterIsAnnotationPresent(${annotation.name})") }
    }

    fun filterIsNotAnnotationPresent(annotation: Class<out Annotation>) = makeNewFinder {
        sequence = sequence.filter { !it.isAnnotationPresent(annotation) }
        exceptMessageScope { condition("filterIsNotAnnotationPresent(${annotation.name})") }
    }

    override fun newFinder(): ClassFinder = ClassFinder(sequence)
}
