package org.runestar.tfudeob

import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode

object RenameLoggerFields : Transformer.Single() {

    object Remap : Remapper() {

        override fun mapFieldName(owner: String, name: String, descriptor: String): String {
            if (descriptor == "Lorg/slf4j/Logger;") return "logger"
            return name
        }
    }

    override fun transform(klass: ClassNode): ClassNode = remap(klass, Remap)
}