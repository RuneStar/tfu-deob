package org.runestar.tfudeob

import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.*

class FixEnums : Transformer.Tree() {

    private val elementNames = HashMap<String, String>()

    inner class Remap : Remapper() {

        override fun mapFieldName(owner: String, name: String, descriptor: String): String {
            return elementNames["$owner.$name"] ?: name
        }
    }

    override fun transform(klasses: List<ClassNode>): List<ClassNode> {
        klasses.forEach { k ->
            if (k.access and Opcodes.ACC_ENUM == 0) return@forEach
            val clinit = k.methods.firstOrNull { it.name == "<clinit>" } ?: return@forEach
            enumNames(k, clinit)
            k.fields.forEach { f ->
                if (f.access and Opcodes.ACC_SYNTHETIC != 0) elementNames["${k.name}.${f.name}"] = "\$VALUES"
            }
            k.signature = "Ljava/lang/Enum<L${k.name};>;"
            for (n in k.interfaces) {
                k.signature += "L$n;"
            }
        }
        val remapper = Remap()
        return klasses.map { remap(it, remapper) }
    }

    private fun enumNames(k: ClassNode, clinit: MethodNode) {
        val insns = clinit.instructions
        val itr = insns.iterator()
        while (itr.hasNext()) {
            val insn0 = itr.next()
            if (insn0.opcode != Opcodes.NEW) continue
            insn0 as TypeInsnNode
            if (!insn0.desc.startsWith(k.name)) continue
            itr.next()
            val insn1 = itr.next()
            if (insn1.opcode != Opcodes.LDC) continue
            insn1 as LdcInsnNode
            val enumElementName = insn1.cst as? String ?: continue
            var pf = itr.next()
            while (itr.hasNext() && pf.opcode != Opcodes.PUTSTATIC) {
                pf = itr.next()
            }
            pf as FieldInsnNode
            elementNames["${pf.owner}.${pf.name}"] = enumElementName
        }
    }
}