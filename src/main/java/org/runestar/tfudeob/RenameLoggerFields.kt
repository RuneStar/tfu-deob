package org.runestar.tfudeob

import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode

object RenameLoggerFields : Transformer.Single, Remapper() {

    override fun transform(klass: ClassNode): ClassNode {
        val c = ClassNode()
        klass.accept(ClassRemapper(c, this))
        return c
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        if (descriptor == "Lorg/slf4j/Logger;") return "logger"
        return name
    }
}