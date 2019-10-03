package org.runestar.tfudeob

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

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

fun <T> Stream<T>.forEachClose(action: (T) -> Unit) {
    forEach(action)
    close()
}

fun readClasses(dir: Path): List<ByteArray> {
    val classes = ArrayList<ByteArray>()
    Files.walk(dir).forEachClose { f ->
        if (!Files.isRegularFile(f) || !f.toString().endsWith(".class")) return@forEachClose
        classes.add(Files.readAllBytes(f))
    }
    return classes
}

fun writeClasses(classes: Iterable<ByteArray>, dir: Path) {
    classes.forEach { c ->
        val file = dir.resolve("${ClassReader(c).className}.class")
        Files.createDirectories(file.parent)
        Files.write(file, c)
    }
}