package org.runestar.tfudeob

import com.strobel.decompiler.Decompiler
import com.strobel.decompiler.DecompilerSettings
import com.strobel.decompiler.PlainTextOutput
import org.benf.cfr.reader.Main
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.zeroturnaround.zip.ZipUtil
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
    deob(Paths.get("tfu.jar"))
}

private fun deob(src: Path) {
    val srcSimple = src.fileName.toString().substringBeforeLast('.')
    val tempInDir = Paths.get("temp-in")
    val tempOutDir = Paths.get("temp-out")
    val outputCfr = src.resolveSibling("$srcSimple-cfr")
    val outputFernflower = src.resolveSibling("$srcSimple-fernflower")
    val outputProcyon = src.resolveSibling("$srcSimple-procyon")
    val outputJar = src.resolveSibling("out-" + src.fileName.toString())

    tempInDir.toFile().deleteRecursively()
    tempOutDir.toFile().deleteRecursively()
    outputCfr.toFile().deleteRecursively()
    outputFernflower.toFile().deleteRecursively()
    outputProcyon.toFile().deleteRecursively()
    Files.deleteIfExists(outputJar)

    Files.createDirectories(tempInDir)
    Files.createDirectories(tempOutDir)
    Files.createDirectories(outputCfr)
    Files.createDirectories(outputFernflower)
    Files.createDirectories(outputProcyon)

    ZipUtil.unpack(src.toFile(), tempInDir.toFile())
    val transformer = Transformer.Composite(
        RemoveLibraries,
        UnusedMethodRemover,
        CfnReplacer,
        FieldResolver,
        RenameLoggerFields,
        SortMembers,
        FixInnerClasses,
        FixEnums(),
        SrcCompatRenamer()
    )
    val classes = transformer.transform(readClasses(tempInDir))
    writeClasses(classes, tempOutDir)
    ZipUtil.pack(tempOutDir.toFile(), outputJar.toFile())

    decompileCfr(outputJar, outputCfr)
    decompileFernflower(tempOutDir, outputFernflower)
    decompileProcyon(tempOutDir, outputProcyon)
}

private fun decompileCfr(input: Path, output: Path) {
    Main.main(arrayOf(
        input.toString(),
        "--outputpath", output.toString()
    ))
}

private fun decompileFernflower(input: Path, output: Path) {
    ConsoleDecompiler.main(arrayOf(
        input.toString(),
        output.toString()
    ))
}

private fun decompileProcyon(input: Path, output: Path) {
    val settings = DecompilerSettings.javaDefaults()
    Files.walk(input).forEach { f ->
        if (Files.isDirectory(f)) return@forEach
        val classSimpleName = f.fileName.toString().substringBeforeLast('.')
        val outFile = output.resolve(input.relativize(f)).resolveSibling("$classSimpleName.java")
        Files.createDirectories(outFile.parent)
        println(f)
        try {
            Files.newBufferedWriter(outFile).use { writer ->
                Decompiler.decompile(f.toString(), PlainTextOutput(writer), settings)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun readClasses(dir: Path): Collection<ClassNode> {
    val classes = ArrayList<ClassNode>()
    Files.walk(dir).forEach { f ->
        if (Files.isDirectory(f)) return@forEach
        val reader = ClassReader(Files.readAllBytes(f))
        val node = ClassNode()
        reader.accept(node, 0)
        classes.add(node)
    }
    return classes
}

private fun writeClasses(classes: Collection<ClassNode>, dir: Path) {
    classes.forEach { node ->
        val copy = ClassNode()
        node.accept(copy)
        val writer = ClassWriter(0)
        node.accept(writer)
        val file = dir.resolve(node.name + ".class")
        Files.createDirectories(file.parent)
        Files.write(file, writer.toByteArray())
    }
}