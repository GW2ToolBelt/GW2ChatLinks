package com.gw2tb.build.tasks

import com.github.javaparser.JavaParser
import com.github.javaparser.ParserConfiguration
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.expr.Name
import com.github.javaparser.ast.modules.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.property

private val String.asBinaryName get(): String {
    val parts = split('.')
    var inClass = false

    return buildString {
        append(parts[0])

        for (i in 1 until parts.size) {
            append(if (inClass || parts[i][0].isUpperCase()) {
                inClass = true
                "$"
            } else "/")
            append(parts[i])
        }
    }
}

@CacheableTask
open class CompileModuleInfo : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val source: RegularFileProperty = project.objects.fileProperty()

    @get:Input
    val version = project.objects.property<String>()

    @get:OutputFile
    val output: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun compile() {
        val version = this.version.orNull

        val parser = JavaParser(ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_9))
        val decl = parser.parseModuleDeclaration(source.get().asFile.readText()).result.get()

        val classWriter = org.objectweb.asm.ClassWriter(0)
        classWriter.visit(org.objectweb.asm.Opcodes.V9, org.objectweb.asm.Opcodes.ACC_MODULE, "module-info", null, null, null)

        val moduleAccess: Int = if (decl.isOpen) org.objectweb.asm.Opcodes.ACC_SYNTHETIC or org.objectweb.asm.Opcodes.ACC_OPEN else org.objectweb.asm.Opcodes.ACC_SYNTHETIC
        val mv: org.objectweb.asm.ModuleVisitor = classWriter.visitModule(decl.nameAsString, moduleAccess, version)

        decl.findAll(ModuleRequiresDirective::class.java).forEach {
            var mod = 0
            if (it.hasModifier(Modifier.Keyword.STATIC)) mod = mod or org.objectweb.asm.Opcodes.ACC_STATIC_PHASE
            if (it.hasModifier(Modifier.Keyword.TRANSITIVE)) mod = mod or org.objectweb.asm.Opcodes.ACC_TRANSITIVE

            mv.visitRequire(it.nameAsString, mod, null)
        }

        decl.findAll(ModuleExportsDirective::class.java).forEach {
            mv.visitExport(it.nameAsString, 0, *it.moduleNames.map(Name::asString).toTypedArray())
        }

        decl.findAll(ModuleProvidesDirective::class.java).forEach {
            mv.visitProvide(it.nameAsString.asBinaryName, *it.with.map(Name::asString).map(String::asBinaryName).toTypedArray())
        }

        decl.findAll(ModuleUsesDirective::class.java).forEach {
            mv.visitUse(it.nameAsString.asBinaryName)
        }

        decl.findAll(ModuleOpensDirective::class.java).forEach {
            mv.visitOpen(it.nameAsString, 0, *it.moduleNames.map(Name::asString).toTypedArray())
        }

        mv.visitRequire("java.base", org.objectweb.asm.Opcodes.ACC_MANDATED, null)
        mv.visitEnd()

        classWriter.visitEnd()

        val outputFile = output.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeBytes(classWriter.toByteArray())
    }

}