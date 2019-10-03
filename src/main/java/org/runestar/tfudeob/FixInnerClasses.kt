package org.runestar.tfudeob

import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

object FixInnerClasses : Transformer.Tree() {

    override fun transform(klasses: List<ClassNode>): List<ClassNode> {
        val classAccess = klasses.associate { it.name to it.access }
        for (klass in klasses) {
            val types = HashSet<Type>()
            types.add(Type.getObjectType(klass.name))
            types.add(Type.getObjectType(klass.superName))
            klass.fields.mapTo(types) { Type.getType(it.desc) }
            klass.methods.forEach { m ->
                m.instructions.iterator().forEach { insn ->
                    when (insn) {
                        is FieldInsnNode -> types.add(Type.getType(insn.desc))
                        is MethodInsnNode -> types.add(Type.getType(insn.desc))
                        is TypeInsnNode -> types.add(Type.getObjectType(insn.desc))
                        is MultiANewArrayInsnNode -> types.add(Type.getType(insn.desc))
                    }
                }
            }

            val classes = HashSet<Type>()
            types.forEach { t ->
                when (t.sort) {
                    Type.ARRAY -> classes.add(t.elementType)
                    Type.OBJECT -> classes.add(t)
                }
            }

            val classNames = classes.map { it.internalName }

            classNames.forEach { className ->
                if (!className.contains('$')) return@forEach
                val access = classAccess[className] ?: Class.forName(className.replace('/', '.')).modifiers
                val outer = className.substringBeforeLast('$')
                val simpleRaw = className.substringAfterLast('$')
                val simple = if (simpleRaw.toIntOrNull() == null) simpleRaw else null
                //klass.innerClasses.add(InnerClassNode(className, outer, simple, access))
            }

//            if (!klass.name.contains('$')) {
                klass.outerClass = null
                klass.outerMethod = null
                klass.outerMethodDesc = null
//            }
        }
        return klasses
    }
}