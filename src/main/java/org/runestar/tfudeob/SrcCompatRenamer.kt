package org.runestar.tfudeob

import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode

class SrcCompatRenamer : Transformer, Remapper() {

    private var i = 1

    private val classNames = HashMap<String, String>()

    override fun transform(klasses: Collection<ClassNode>): Collection<ClassNode> {
        return klasses.map { k ->
            val c = ClassNode()
            k.accept(ClassRemapper(c, this))
            c
        }
    }

    override fun map(internalName: String): String {
        val simpleClassName = internalName.substringAfterLast('/')
        if (simpleClassName.length > 2) return internalName
        val pkg = internalName.substringBeforeLast('/')
        return classNames.getOrPut(internalName) { pkg + '/' + simpleClassName.capitalize() + i++ }
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        return mapName(name)
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        return mapName(name)
    }

    private fun mapName(name: String): String {
        return when (name) {
            "do" -> "do_"
            "if" -> "if_"
            else -> name
        }
    }
}