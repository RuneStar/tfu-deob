package org.runestar.tfudeob

import com.strobel.decompiler.DecompilerDriver
import org.benf.cfr.reader.Main
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import org.zeroturnaround.zip.ZipUtil
import java.nio.file.Files
import java.nio.file.Path

fun main() {
    deob(Path.of("input"), Path.of("output"))
}

private fun deob(input: Path, output: Path) {
    output.toFile().deleteRecursively()
    val jar = input.resolve("tfu.jar")
    val outjar = output.resolve("tfu.jar")
    Files.createDirectories(output)

    val transformer = Transformer.Composite(
        RemoveLibraries,
        UnusedMethodRemover,
        CfnReplacer,
        ResolveFields,
        RenameLoggerFields,
        SortMembers,
        FixInnerClasses,
        FixEnums(),
        SrcCompatRenamer()
    )
    writeClasses(transformer.transform0(readClasses(jar)), outjar)
    val classesDir = output.resolve("classes")
    ZipUtil.unpack(outjar.toFile(), classesDir.toFile())

    val cfrDir = output.resolve("cfr")
    Files.createDirectories(cfrDir)
    decompileCfr(outjar, cfrDir)

    val fernflowerDir = output.resolve("fernflower")
    Files.createDirectories(fernflowerDir)
    decompileFernflower(classesDir, fernflowerDir)

    val procyonDir = output.resolve("procyon")
    Files.createDirectories(procyonDir)
    decompileProcyon(outjar, procyonDir)
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
    DecompilerDriver.main(arrayOf(
        "-jar", input.toString(),
        "-o", output.toString()
    ))
}