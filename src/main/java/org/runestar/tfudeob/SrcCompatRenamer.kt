package org.runestar.tfudeob

import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode

class SrcCompatRenamer : Transformer.Tree() {

    private class Remap : Remapper() {

        private var i = 1

        private val classNames = HashMap<String, String>()

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

    override fun transform(klasses: List<ClassNode>): List<ClassNode> {
        val remapper = Remap()
        return klasses.map { remap(it, remapper)}
    }
}