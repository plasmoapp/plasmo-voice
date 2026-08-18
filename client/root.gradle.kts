plugins {
    id("gg.essential.multi-version.root")
}

group = "$group.client-root"

preprocess {
    strictExtraMappings.set(false)

    val fabric260200 = createNode("26.2-fabric", 260200, "official")
    val neoForge260200 = createNode("26.2-neoforge", 260200, "official")

    val fabric260100 = createNode("26.1-fabric", 260100, "official")
    val neoForge260100 = createNode("26.1-neoforge", 260100, "official")

    val neoForge12111 = createNode("1.21.11-neoforge", 12111, "official")
    val fabric12111 = createNode("1.21.11-fabric", 12111, "official")

    val neoForge12107 = createNode("1.21.7-neoforge", 12106, "official")

    val neoForge12106 = createNode("1.21.6-neoforge", 12106, "official")
    val fabric12106 = createNode("1.21.6-fabric", 12106, "official")

    val neoForge12104 = createNode("1.21.4-neoforge", 12104, "official")
    val fabric12104 = createNode("1.21.4-fabric", 12104, "official")

    val neoForge12101 = createNode("1.21.1-neoforge", 12101, "official")
    val fabric12101 = createNode("1.21.1-fabric", 12101, "official")
    val forge12101 = createNode("1.21.1-forge", 12101, "official")

    val forge12004 = createNode("1.20.4-forge", 12004, "official")
    val fabric12004 = createNode("1.20.4-fabric", 12004, "official")

    val forge12001 = createNode("1.20.1-forge", 12001, "official")
    val fabric12001 = createNode("1.20.1-fabric", 12001, "official")

    fabric260200.link(fabric260100, file("26.2-26.1.txt"))
    neoForge260200.link(neoForge260100, file("26.2-26.1.txt"))

    fabric260100.link(fabric12111, file("26.1-1.21.11.txt"))
    neoForge260100.link(neoForge12111, file("26.1-1.21.11.txt"))

    fabric12111.link(fabric12106, file("1.21.9-1.21.8.txt"))
    neoForge12111.link(neoForge12107, file("1.21.9-1.21.8.txt"))

    neoForge12107.link(neoForge12106)

    fabric12106.link(fabric12104, file("1.21.6-1.21.5.txt"))
    neoForge12106.link(neoForge12104, file("1.21.6-1.21.5.txt"))

    fabric12104.link(fabric12101)
    neoForge12104.link(neoForge12101)

    neoForge12101.link(fabric12101)
    fabric12101.link(fabric12004, file("1.21-1.20.6.txt"))
    forge12101.link(forge12004, file("1.21-1.20.6.txt"))

    fabric12004.link(fabric12001, file("1.20.4-1.20.1.txt"))
    forge12004.link(forge12001, file("1.20.4-1.20.1.txt"))

    forge12001.link(fabric12001)

}
