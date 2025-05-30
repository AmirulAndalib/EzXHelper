@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.github.kyuubiran.ezxhelper.finders

import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.annotations.KotlinOnly
import com.github.kyuubiran.ezxhelper.finders.base.ExecutableFinder
import com.github.kyuubiran.ezxhelper.interfaces.IFindSuper
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

/**
 * Helper for finding method(s) in the class or collection.
 */
class MethodFinder private constructor(seq: Sequence<Method>) : ExecutableFinder<Method, MethodFinder>(seq), IFindSuper<MethodFinder> {

    private var clazz: Class<*>? = null

    override val name: String
        get() = "MethodFinder"

    @Suppress("ClassName")
    companion object `-Static` {

        @JvmStatic
        fun fromClass(clazz: Class<*>): MethodFinder {
            var seq = emptySequence<Method>()
            seq += clazz.declaredMethods.asSequence()
            seq += clazz.interfaces.flatMap { c -> c.declaredMethods.asSequence() }
            return MethodFinder(seq).apply {
                this.clazz = clazz
                exceptMessageScope { ctor(this@apply, "No such method found in class: ${clazz.name}") }
            }
        }

        @JvmStatic
        @JvmSynthetic
        @KotlinOnly
        fun fromClass(kclazz: KClass<*>): MethodFinder = fromClass(kclazz.java)

        @JvmStatic
        fun fromClass(clazzName: String, classLoader: ClassLoader = EzXHelper.classLoader) =
            fromClass(Class.forName(clazzName, false, classLoader))

        @JvmStatic
        fun fromSequence(seq: Sequence<Method>): MethodFinder {
            return MethodFinder(seq).apply {
                exceptMessageScope { ctor(this@apply, "No such method found in sequence(size=${seq.count()})") }
            }
        }

        @JvmStatic
        fun fromArray(array: Array<Method>): MethodFinder {
            return MethodFinder(array.asSequence()).apply {
                exceptMessageScope { ctor(this@apply, "No such method found in array(size=${array.count()})") }
            }
        }

        @JvmStatic
        fun fromVararg(vararg array: Method): MethodFinder {
            return MethodFinder(array.asSequence()).apply {
                exceptMessageScope { ctor(this@apply, "No such method found in vararg(size=${array.count()})") }
            }
        }

        @JvmStatic
        fun fromIterable(iterable: Iterable<Method>): MethodFinder {
            return MethodFinder(iterable.asSequence()).apply {
                exceptMessageScope { ctor(this@apply, "No such method found in iterable(size=${iterable.count()})") }
            }
        }

        @JvmSynthetic
        @KotlinOnly
        fun Class<*>.methodFinder() = fromClass(this)

        @JvmSynthetic
        @KotlinOnly
        fun Array<Method>.methodFinder() = fromArray(this)

        @JvmSynthetic
        @KotlinOnly
        fun Iterable<Method>.methodFinder() = fromIterable(this)

        @JvmSynthetic
        @KotlinOnly
        fun Sequence<Method>.methodFinder() = fromSequence(this)
    }

    // region filter by

    /**
     * Filter by method name.
     * @param name method name
     * @return [MethodFinder] this finder
     */
    fun filterByName(name: String) = makeNewFinder {
        sequence = sequence.filter { it.name == name }
        exceptMessageScope { condition("filterByName($name)") }
    }

    /**
     * Filter by method return type.
     * @param returnType method return type
     * @return [MethodFinder] this finder
     */
    fun filterByReturnType(returnType: Class<*>) = makeNewFinder {
        sequence = sequence.filter { it.returnType == returnType }
        exceptMessageScope { condition("filterByReturnType(${returnType.name})") }
    }

    fun filterByReturnType(returnType: KClass<*>) = makeNewFinder {
        sequence = sequence.filter { it.returnType == returnType.java }
        exceptMessageScope { condition("filterByReturnType(${returnType.java.name})") }
    }

    /**
     * Filter by method returns void type.
     * @return [MethodFinder] this finder
     */
    fun filterVoidReturnType() = makeNewFinder {
        sequence = sequence.filter { it.returnType == Void.TYPE }
        exceptMessageScope { condition("filterVoidReturnType") }
    }

    /**
     * Filter by method assignable return type.
     * @param returnType method return type
     * @return [MethodFinder] this finder
     */
    fun filterByAssignableReturnType(returnType: Class<*>) = makeNewFinder {
        sequence = sequence.filter { it.returnType.isAssignableFrom(returnType) || returnType.isAssignableFrom(it.returnType) }
        exceptMessageScope { condition("filterByAssignableReturnType(${returnType.name})") }
    }

    // endregion

    // region filter modifiers
    /**
     * Filter if they are abstract.
     * @return [MethodFinder] this finder.
     */
    fun filterAbstract() = makeNewFinder {
        filter { Modifier.isAbstract(this.modifiers) }
        exceptMessageScope { condition("filterAbstract") }
    }

    /**
     * Filter if they are non-abstract.
     * @return [MethodFinder] this finder.
     */
    fun filterNonAbstract() = makeNewFinder {
        filter { !Modifier.isAbstract(this.modifiers) }
        exceptMessageScope { condition("filterNonAbstract") }
    }

    /**
     * Filter if they are static.
     * @return [MethodFinder] this finder.
     */
    fun filterStatic() = makeNewFinder {
        sequence = sequence.filter { Modifier.isStatic(it.modifiers) }
        exceptMessageScope { condition("filterStatic") }
    }

    /**
     * Filter if they are non-static.
     * @return [MethodFinder] this finder.
     */
    fun filterNonStatic() = makeNewFinder {
        sequence = sequence.filter { !Modifier.isStatic(it.modifiers) }
        exceptMessageScope { condition("filterNonStatic") }
    }

    /**
     * Filter if they are final.
     * @return [MethodFinder] this finder.
     */
    fun filterFinal() = makeNewFinder {
        sequence = sequence.filter { Modifier.isFinal(it.modifiers) }
        exceptMessageScope { condition("filterFinal") }
    }

    /**
     * Filter if they are non-final.
     * @return [MethodFinder] this finder.
     */
    fun filterNonFinal() = makeNewFinder {
        sequence = sequence.filter { !Modifier.isFinal(it.modifiers) }
        exceptMessageScope { condition("filterNonFinal") }
    }

    // endregion

    // region overrides

    override fun getParameterTypes(member: Method): Array<Class<*>> = member.parameterTypes
    override fun getExceptionTypes(member: Method): Array<Class<*>> = member.exceptionTypes

    override fun findSuper(untilPredicate: (Class<*>.() -> Boolean)?) = makeNewFinder {
        if (clazz == null || clazz == Any::class.java) return@makeNewFinder

        var c: Class<*>? = clazz?.superclass ?: return@makeNewFinder

        val ml = if (exceptionMessageEnabled) mutableListOf<String>() else null

        while (c != Any::class.java) {
            if (c == null) return@makeNewFinder

            if (untilPredicate?.invoke(c) == true) break

            ml?.add(c.name)

            sequence += c.declaredMethods.asSequence()
            sequence += c.interfaces.flatMap { i -> i.declaredMethods.asSequence() }

            c = c.superclass
        }

        if (ml != null) {
            exceptMessageScope { condition("findSuper(CustomCondition)") }
        }
    }

    override fun newFinder(): MethodFinder = MethodFinder(sequence)

    // endregion
}