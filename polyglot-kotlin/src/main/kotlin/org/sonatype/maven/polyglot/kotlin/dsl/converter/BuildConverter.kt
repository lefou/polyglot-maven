import PluginConverter.pluginsOf
import org.apache.maven.model.PluginManagement

object BuildConverter {

    fun buildOf(projectBuild: Build): org.apache.maven.model.Build {
        return org.apache.maven.model.Build().apply {
            sourceDirectory = projectBuild.sourceDirectory
            testSourceDirectory = projectBuild.testSourceDirectory
            finalName = projectBuild.finalName
            scriptSourceDirectory = projectBuild.scriptSourceDirectory
            outputDirectory = projectBuild.outputDirectory
            testOutputDirectory = projectBuild.testOutputDirectory
            directory = projectBuild.directory
            filters.addAll(projectBuild.filters)

            val (plugs, pluginManagement) = projectBuild
            if (plugs != null) plugins = pluginsOf(plugs.component1())
            pluginManagement?.let {
                this.pluginManagement = PluginManagement().apply {
                    val (plugs) = it
                    plugins = pluginsOf(plugs.component1())
                }
            }
        }
    }
}