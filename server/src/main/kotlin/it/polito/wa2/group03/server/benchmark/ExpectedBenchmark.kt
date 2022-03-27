package it.polito.wa2.group03.server.benchmark
import com.codahale.usl4j.Measurement
import com.codahale.usl4j.Model
import java.io.File
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.geom.geomSmooth
import jetbrains.letsPlot.scale.scaleXDiscrete
import jetbrains.letsPlot.export.ggsave

/**
 * measurements: Accept a list of pair<NumberOfClients,Throughput as rps> of at least 6 elements
 */
class ExpectedBenchmark(private val measurements: List<Pair<Int, Double>>) {
    fun buildModel(): (Int) -> Pair<Int, Double> {
        val model = Model.build(measurements.asIterable()
            .map { Measurement.ofConcurrency().andThroughput(it.first.toDouble(), it.second) }
            .toMutableList())

        return {i: Int -> Pair(i, model.throughputAtConcurrency(i.toDouble()))}
    }

    fun plot() {
        val (xs,ys) = measurements.unzip()
        val data = mapOf (
                "Concurrency" to xs,
                "Throughput" to ys
        )

        val p = letsPlot(data) { x = "Concurrency"; y = "Throughput" } +
                geomLine(data, size=1) +
                geomPoint() +
                geomSmooth(color="red", deg=2, se=false) +
                scaleXDiscrete(name="Concurrency", breaks=xs, labels=xs.map{it.toString()})


        ggsave(p, "concurrencyPlot.svg", path="server/src/main/kotlin/it/polito/wa2/group03/server/benchmark")
    }
}

fun main() {
    val fileName = File("server/src/main/kotlin/it/polito/wa2/group03/server/benchmark/results.csv")
    val measurements = listOf((32 to 1113.0), (16 to 555.0), (8 to 410.0), (4 to 465.0), (2 to 459.0), (1 to 314.0))
    val benchmark = ExpectedBenchmark(measurements)
    val benchmarkModel = benchmark.buildModel()
    val maxNumberOfClients = 100
    var i = 0
    fileName.writeText("clients,rps\n")
    while (i < maxNumberOfClients) {
        fileName.appendText("${benchmarkModel(i).first},${benchmarkModel(i).second}\n")
        i++
    }

    benchmark.plot()
}
