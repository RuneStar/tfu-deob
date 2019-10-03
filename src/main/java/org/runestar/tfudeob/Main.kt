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
    val tempin = output.resolve("temp-in")
    val tempout = output.resolve("temp-out")
    val outjar = output.resolve("tfu.jar")
    Files.createDirectories(tempin)
    Files.createDirectories(tempout)

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
    ZipUtil.unpack(jar.toFile(), tempin.toFile())
    writeClasses(transformer.transform0(readClasses(tempin)), tempout)
    ZipUtil.pack(tempout.toFile(), outjar.toFile())

    val cfrDir = output.resolve("cfr")
    Files.createDirectories(cfrDir)
    decompileCfr(outjar, cfrDir)

    val fernflowerDir = output.resolve("fernflower")
    Files.createDirectories(fernflowerDir)
    decompileFernflower(tempout, fernflowerDir)

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