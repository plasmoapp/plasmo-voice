val mavenGroup: String by rootProject

plugins {
    id("gg.essential.multi-version.root")
}

group = "$mavenGroup.client-root"

preprocess {

    val forge11904 = createNode("1.19.4-forge", 11904, "official")
    val fabric11904 = createNode("1.19.4-fabric", 11904, "official")
    val forge11903 = createNode("1.19.3-forge", 11903, "official")
    val fabric11903 = createNode("1.19.3-fabric", 11903, "official")
    val forge11902 = createNode("1.19.2-forge", 11902, "official")
    val fabric11902 = createNode("1.19.2-fabric", 11902, "official")

    forge11903.link(fabric11903)

    fabric11904.link(fabric11903)
    forge11904.link(fabric11904)

    fabric11902.link(fabric11903, file("1.19.2-1.19.3.txt"))
    forge11902.link(fabric11902)
}
