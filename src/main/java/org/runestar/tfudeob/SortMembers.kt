package org.runestar.tfudeob

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import java.lang.reflect.Modifier

object SortMembers : Transformer.Single {

    override fun transform(klass: ClassNode): ClassNode {
        klass.fields = klass.fields.sortedWith(FIELD_COMPARATOR)
        val lineNums = klass.methods.associate { it to (it.firstLineNum() ?: Integer.MAX_VALUE) }
        klass.methods = klass.methods.sortedBy { lineNums.getValue(it) }
        return klass
    }

    private val FIELD_COMPARATOR: Comparator<FieldNode> = compareBy<FieldNode> { it.access and Opcodes.ACC_ENUM == 0 }
        .thenBy { it.access and Opcodes.ACC_SYNTHETIC != 0 }
        .thenBy { !Modifier.isStatic(it.access) }
        .thenBy { it.name }

    private fun MethodNode.firstLineNum(): Int? {
        for (insn in instructions) {
            if (insn is LineNumberNode) {
                return insn.line
            }
        }
        return null
    }
}