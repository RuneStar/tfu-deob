package org.runestar.tfudeob

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.zeroturnaround.zip.ByteSource
import org.zeroturnaround.zip.ZipUtil
import java.nio.file.Path

fun analyze(classNode: ClassNode) {
    for (m in classNode.methods) {
        try {
            Analyzer(BasicInterpreter()).analyze(classNode.name, m)
        } catch (e: Exception) {
            throw Exception("${classNode.name}.${m.name}${m.desc}", e)
        }
    }
}

fun ClassNode(classFile: ByteArray, parsingOptions: Int): ClassNode {
    val c = ClassNode()
    ClassReader(classFile).accept(c, parsingOptions)
    return c
}

fun ClassNode.toByteArray(): ByteArray {
    val w = ClassWriter(0)
    accept(w)
    return w.toByteArray()
}

fun remap(classNode: ClassNode, remapper: Remapper): ClassNode {
    val c = ClassNode()
    classNode.accept(ClassRemapper(c, remapper))
    return c
}

fun readClasses(jar: Path): List<ByteArray> {
    val classes = ArrayList<ByteArray>()
    ZipUtil.iterate(jar.toFile()) { input, entry ->
        if (!entry.name.endsWith(".class")) return@iterate
        classes.add(input.readAllBytes())
    }
    return classes
}

fun writeClasses(classes: Iterable<ByteArray>, jar: Path) {
    ZipUtil.pack(classes.map { ByteSource("${ClassReader(it).className}.class", it) }.toTypedArray(), jar.toFile())
}