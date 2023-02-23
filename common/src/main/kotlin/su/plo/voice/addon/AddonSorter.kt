package su.plo.voice.addon

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import su.plo.voice.api.addon.AddonContainer
import java.util.*
import java.util.stream.Collectors

class AddonSorter {

    companion object {

        fun sort(addons: List<AddonContainer>): List<AddonContainer> {
            val sortedById = addons.sortedWith(Comparator.comparing { it.id })

            val graph: MutableGraph<AddonContainer> = GraphBuilder.directed()
                .allowsSelfLoops(false)
                .expectedNodeCount(sortedById.size)
                .build()

            val addonsMap: Map<String, AddonContainer> = sortedById.associateBy { it.id }
            for (addon in sortedById) {
                graph.addNode(addon)

                for (dependency in addon.dependencies) {
                    addonsMap[dependency.id]?.let {
                        graph.putEdge(addon, it)
                    }
                }
            }

            val sorted: MutableList<AddonContainer> = Lists.newArrayList()
            val marks: MutableMap<AddonContainer, Mark> = Maps.newHashMap()

            for (node in graph.nodes()) {
                visitNode(
                    graph,
                    node,
                    marks,
                    sorted,
                    ArrayDeque()
                )
            }

            return sorted
        }

        private fun visitNode(
            graph: Graph<AddonContainer>,
            node: AddonContainer,
            visited: MutableMap<AddonContainer, Mark>,
            sorted: MutableList<AddonContainer>,
            currentDependencies: Deque<AddonContainer>
        ) {
            val mark = visited.getOrDefault(node, Mark.NOT_VISITED)
            if (mark == Mark.VISITED) {
                return
            } else if (mark == Mark.VISITING) {
                currentDependencies.addLast(node)
                val loop = currentDependencies.stream()
                    .map { obj -> obj.id }
                    .collect(Collectors.joining(" -> "))
                throw IllegalStateException("Circular dependency detected: $loop")
            }

            currentDependencies.addLast(node)
            visited[node] = Mark.VISITING
            for (edge in graph.successors(node)) {
                visitNode(graph, edge, visited, sorted, currentDependencies)
            }

            visited[node] = Mark.VISITED
            currentDependencies.removeLast()
            sorted.add(node)
        }

        private enum class Mark {
            NOT_VISITED, VISITING, VISITED
        }
    }
}
