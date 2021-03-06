@Scope open class Project() {
    val project = this

    lateinit var artifactId: String
    var groupId: String? = null //default = parent.groupId
        get() = field ?: superParent?.groupId
    var version: String? = null //default = parent.version
        get() = field ?: superParent?.version

    protected var superParent: Parent? = null

    fun parent(block: (@Scope Parent).(Parent) -> Unit) {
        check(superParent == null, { "Parent is defined several times" })
        superParent = Parent()
        block(superParent!!, superParent!!)
    }

    var parent: String? = null
        set(value) {
            check(superParent == null, { "Parent is defined several times" })

            val parentSegments = requireNotNull(value).split(":")
            check(parentSegments.size == 3, { "Wrong Project.parent format. Expected: groupId:artifactId:version" })
            val path = if (::relativePath.isInitialized) relativePath else "../pom.kts"

            superParent = Parent().apply {
                groupId = parentSegments[0]
                artifactId = parentSegments[1]
                version = parentSegments[2]
                relativePath = path
            }
            field = value
        }

    private lateinit var relativePath: String
    infix fun String.relativePath(path: String): String {
        check(!this@Project::relativePath.isInitialized, { "relativePath was already is defined" })
        check(superParent == null, { "Please define relativePath inside parent {\n}" })

        relativePath = path
        return this
    }

    var packaging: String = jar

    lateinit var name: String
    var modelVersion: String = "4.0.0"
    var description: String? = null
    var url: String? = null
    var inceptionYear: String? = null

    //properties
    protected var props: MutableMap<String, Any> = mutableMapOf<String, Any>()

    inline fun properties(function: () -> Unit) = function()

    infix fun String.assign(value: Any) {
        props.put(this, value)
    }

    operator fun String.plusAssign(value: Any) {
        props.put(this, value)
    }

    operator fun get(property: String): Any {
        return props[property] ?: throw IllegalArgumentException()
    }

    protected var deps: Dependencies? = null
        set(value) {
            check(deps == null, { "Dependencies is defined several times" })
            field = value
        }

    fun dependencies(block: (@Scope Dependencies).() -> Unit) {
        deps = Dependencies()
        block(deps!!)
    }

    protected var thisBuild: Build? = null
        set(value) {
            check(thisBuild == null, { "Build is defined several times" })
            field = value
        }

    var modules = arrayOf<String>()
    operator fun Array<String>.get(vararg moduleNames: String) {
        assert(modules === this, { "Unexpected Project.modules DSL usage" })
        modules = moduleNames as Array<String>
    }

    fun build(block: Build.() -> Unit) {
        val build = Build()
        build.finalName = "${artifactId}-${version}"
        thisBuild = build
        block(build)
    }

    protected var dependencyManagement: DependencyManagement? = null
    fun dependencyManagement(block: (@Scope DependencyManagement).() -> Unit) {
        dependencyManagement = DependencyManagement(Dependencies())
        block(dependencyManagement!!)
    }

    protected var thisProfiles: Profiles? = null
        set(value) {
            check(thisProfiles == null, { "Profiles is defined several times" })
            field = value
        }
    fun profiles(block: (@Scope Profiles).() -> Unit) {
        thisProfiles = Profiles()
        block(thisProfiles!!)
    }

    protected constructor(project: Project) : this() {
        props = project.props
        deps = project.deps
        superParent = project.superParent
        thisBuild = project.thisBuild
        dependencyManagement = project.dependencyManagement
        thisProfiles = project.thisProfiles
    }
}

//artifacts
val jar = "jar"
val war = "war"
val ear = "ear"
val pom = "pom"

//phase
val compile = "compile"
val test_compile = "test-compile"

inline fun project(block: Project.(Project) -> Unit): Project {
    val project = Project()
    block(project, project)
    return project
}
//operator fun Any.get(vararg lines: String): Array<out String> = lines