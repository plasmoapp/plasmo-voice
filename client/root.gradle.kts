plugins {
    id("gg.essential.multi-version.root")
}

group = "$group.client-root"

preprocess {

    val forge12004 = createNode("1.20.4-forge", 12004, "official")
    val fabric12004 = createNode("1.20.4-fabric", 12004, "official")

    val forge12002 = createNode("1.20.2-forge", 12002, "official")
    val fabric12002 = createNode("1.20.2-fabric", 12002, "official")

    val forge12001 = createNode("1.20.1-forge", 12001, "official")
    val fabric12001 = createNode("1.20.1-fabric", 12001, "official")

    val forge11904 = createNode("1.19.4-forge", 11904, "official")
    val fabric11904 = createNode("1.19.4-fabric", 11904, "official")

    val forge11903 = createNode("1.19.3-forge", 11903, "official")
    val fabric11903 = createNode("1.19.3-fabric", 11903, "official")

    val forge11902 = createNode("1.19.2-forge", 11902, "official")
    val fabric11902 = createNode("1.19.2-fabric", 11902, "official")

    val forge11802 = createNode("1.18.2-forge", 11802, "official")
    val fabric11802 = createNode("1.18.2-fabric", 11802, "official")

    val forge11701 = createNode("1.17.1-forge", 11701, "official")
    val fabric11701 = createNode("1.17.1-fabric", 11701, "official")

    val forge11605 = createNode("1.16.5-forge", 11605, "official")
    val fabric11605 = createNode("1.16.5-fabric", 11605, "official")

    fabric12004.link(fabric12002, file("1.20.4-1.20.2.txt"))
    forge12004.link(forge12002, file("1.20.4-1.20.2.txt"))

    fabric12002.link(fabric12001, file("1.20.2-1.20.1.txt"))
    forge12002.link(forge12001, file("1.20.2-1.20.1.txt"))

    fabric12001.link(fabric11904)
    forge12001.link(forge11904)

    fabric11904.link(fabric11903)
    forge11904.link(forge11903)

    forge11903.link(fabric11903)

    fabric11902.link(fabric11903, file("1.19.2-1.19.3.txt"))
    forge11902.link(forge11903, file("1.19.2-1.19.3.txt"))

    fabric11802.link(fabric11902)
    forge11802.link(forge11902, file("1.18.2-1.19.2.txt"))

    fabric11701.link(fabric11802, file("1.17.1-1.18.2.txt"))
    forge11701.link(forge11802, file("1.17.1-1.18.2.txt"))

    fabric11605.link(fabric11701)
    forge11605.link(forge11701, file("1.16.5-1.17.1.txt"))
}
