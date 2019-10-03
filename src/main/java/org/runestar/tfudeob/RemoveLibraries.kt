package org.runestar.tfudeob

import org.objectweb.asm.tree.ClassNode

object RemoveLibraries : Transformer.Tree() {

    private val pkgs = arrayOf("ch", "org")

    override fun transform(klasses: List<ClassNode>): List<ClassNode> {
        return klasses.filter { k -> pkgs.none { p -> k.name.startsWith(p) } }
    }
}