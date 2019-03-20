package org.runestar.tfudeob

import org.objectweb.asm.tree.ClassNode

object RemoveLibraries : Transformer {

    private val pkgs = arrayOf("ch", "org")

    override fun transform(klasses: Collection<ClassNode>): Collection<ClassNode> {
        return klasses.filter { k -> pkgs.none { p -> k.name.startsWith(p) } }
    }
}