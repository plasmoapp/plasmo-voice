plugins {
    id("gg.essential.multi-version.root")
}

group = "$group.client-root"

preprocess {
    strictExtraMappings.set(false)

    val neoForge12111 = createNode("1.21.11-neoforge", 12111, "official")
    val fabric12111 = createNode("1.21.11-fabric", 12111, "official")

    val neoForge12109 = createNode("1.21.9-neoforge", 12109, "official")
    val fabric12109 = createNode("1.21.9-fabric", 12109, "official")

    val neoForge12107 = createNode("1.21.7-neoforge", 12106, "official")

    val neoForge12106 = createNode("1.21.6-neoforge", 12106, "official")
    val fabric12106 = createNode("1.21.6-fabric", 12106, "official")

    val neoForge12105 = createNode("1.21.5-neoforge", 12105, "official")
    val fabric12105 = createNode("1.21.5-fabric", 12105, "official")

    val neoForge12104 = createNode("1.21.4-neoforge", 12104, "official")
    val fabric12104 = createNode("1.21.4-fabric", 12104, "official")

    val neoForge12103 = createNode("1.21.3-neoforge", 12103, "official")
    val fabric12103 = createNode("1.21.3-fabric", 12103, "official")

    val neoForge12100 = createNode("1.21-neoforge", 12100, "official")
    val fabric12100 = createNode("1.21-fabric", 12100, "official")
    val forge12100 = createNode("1.21-forge", 12100, "official")

    val forge12004 = createNode("1.20.4-forge", 12004, "official")
    val fabric12004 = createNode("1.20.4-fabric", 12004, "official")

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

    fabric12111.link(fabric12109)
    neoForge12111.link(neoForge12109)

    fabric12109.link(fabric12106, file("1.21.9-1.21.8.txt"))
    neoForge12109.link(neoForge12107, file("1.21.9-1.21.8.txt"))

    neoForge12107.link(neoForge12106)

    fabric12106.link(fabric12105, file("1.21.6-1.21.5.txt"))
    neoForge12106.link(neoForge12105, file("1.21.6-1.21.5.txt"))

    fabric12105.link(fabric12104)
    neoForge12105.link(neoForge12104)

    fabric12104.link(fabric12103)
    neoForge12104.link(neoForge12103)

    fabric12103.link(fabric12100)
    neoForge12103.link(neoForge12100)

    neoForge12100.link(fabric12100)
    fabric12100.link(fabric12004, file("1.21-1.20.6.txt"))
    forge12100.link(forge12004, file("1.21-1.20.6.txt"))

    fabric12004.link(fabric12001, file("1.20.4-1.20.1.txt"))
    forge12004.link(forge12001, file("1.20.4-1.20.1.txt"))

    fabric12001.link(fabric11904)
    forge12001.link(forge11904)

    // fabric 1.19.4 main project
    forge11904.link(fabric11904)

    fabric11903.link(fabric11904)
    forge11903.link(forge11904)

    fabric11902.link(fabric11903, file("1.19.2-1.19.3.txt"))
    forge11902.link(forge11903, file("1.19.2-1.19.3.txt"))

    fabric11802.link(fabric11902)
    forge11802.link(forge11902, file("1.18.2-1.19.2.txt"))

    fabric11701.link(fabric11802, file("1.17.1-1.18.2.txt"))
    forge11701.link(forge11802, file("1.17.1-1.18.2.txt"))

    fabric11605.link(fabric11701, file("1.16.5-1.17.1.txt"))
    forge11605.link(forge11701, file("1.16.5-1.17.1.txt"))
}
