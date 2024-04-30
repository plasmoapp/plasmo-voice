val mavenGroup: String by rootProject

plugins {
    id("gg.essential.multi-version.root")
}

group = "$mavenGroup.client-root"

preprocess {

    val fabric12006 = createNode("1.20.6-fabric", 12006, "official")
    val fabric12004 = createNode("1.20.4-fabric", 12004, "official")

//    val forge12002 = createNode("1.20.2-forge", 12002, "official")
    val fabric12002 = createNode("1.20.2-fabric", 12002, "official")
    val forge12001 = createNode("1.20.1-forge", 12001, "official")
    val fabric12001 = createNode("1.20.1-fabric", 12001, "official")
    val forge11904 = createNode("1.19.4-forge", 11904, "official")
    val fabric11904 = createNode("1.19.4-fabric", 11904, "official")
    val forge11903 = createNode("1.19.3-forge", 11903, "official")
    val fabric11903 = createNode("1.19.3-fabric", 11903, "official")
    val forge11902 = createNode("1.19.2-forge", 11902, "official")
    val fabric11902 = createNode("1.19.2-fabric", 11902, "official")

    fabric12006.link(fabric12004)

    fabric12004.link(fabric12002)

    fabric12002.link(fabric12001, file("1.20.2-1.20.1.txt"))
//    forge12002.link(fabric12002)

    fabric12001.link(fabric11904)
    forge12001.link(fabric12001)

    forge11903.link(fabric11903)

    fabric11904.link(fabric11903)
    forge11904.link(fabric11904)

    fabric11902.link(fabric11903, file("1.19.2-1.19.3.txt"))
    forge11902.link(fabric11902)
}
