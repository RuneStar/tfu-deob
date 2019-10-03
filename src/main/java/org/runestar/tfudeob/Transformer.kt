package org.runestar.tfudeob

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

interface Transformer {

    fun transform0(classes: List<ByteArray>): List<ByteArray>

    abstract class Tree : Transformer {

        final override fun transform0(classes: List<ByteArray>): List<ByteArray> {
            val nodes = classes.map { ClassNode(it, ClassReader.SKIP_FRAMES or ClassReader.SKIP_DEBUG) }
            val nodes2 = transform(nodes)
//            nodes2.forEach { analyze(it) }
            return nodes2.map { it.toByteArray() }
        }

        abstract fun transform(klasses: List<ClassNode>): List<ClassNode>
    }

    abstract class Single : Tree() {

        final override fun transform(klasses: List<ClassNode>): List<ClassNode> = klasses.map { transform(it) }

        abstract fun transform(klass: ClassNode): ClassNode
    }

    class Composite(vararg val transformers: Transformer) : Transformer {

        override fun transform0(classes: List<ByteArray>): List<ByteArray> {
            var cs = classes
            transformers.forEach { cs = it.transform0(cs) }
            return cs
        }
    }
}