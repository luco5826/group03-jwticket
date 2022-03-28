package it.polito.wa2.group03.server.benchmark
import com.codahale.usl4j.Measurement
import com.codahale.usl4j.Model
import java.io.File
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.geom.geomPoint
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
        val realData = mapOf (
                "Concurrency" to xs,
                "Throughput" to ys
        )

        val model = buildModel()
        var i = 1
        val maxNumberOfClients = 256
        val xs2 = mutableListOf<Int>()
        val ys2 = mutableListOf<Double>()
        while (i <= maxNumberOfClients) {
            val thrAtI = model(i)
            xs2.add(thrAtI.first)
            ys2.add(thrAtI.second)
            i++
        }
        val modelData = mapOf (
                "Concurrency" to xs2,
                "Throughput" to ys2
        )

        val p = letsPlot(null) { x = "Concurrency"; y = "Throughput" } +
                geomLine(modelData, size=0.7, color="red") +
                geomLine(realData, size=1) +
                geomPoint(realData, size=3) +
                scaleXDiscrete(name="Concurrency", breaks=xs, labels=xs.map{it.toString()})


        ggsave(p, "concurrencyPlot.svg", path="server/src/main/kotlin/it/polito/wa2/group03/server/benchmark")
    }
}

fun main() {
    val fileName = File("server/src/main/kotlin/it/polito/wa2/group03/server/benchmark/results.csv")
    val measurements = listOf((1 to 570.0), (2 to 1313.0), (4 to 1686.0), (8 to 2204.0), (16 to 2340.0),
            (32 to 2469.0), (64 to 2592.0), (128 to 2564.0), (256 to 2517.0))
    val benchmark = ExpectedBenchmark(measurements)
    val benchmarkModel = benchmark.buildModel()
    val maxNumberOfClients = 256
    var i = 1
    fileName.writeText("clients,rps\n")
    while (i <= maxNumberOfClients) {
        fileName.appendText("${benchmarkModel(i).first},${benchmarkModel(i).second}\n")
        i++
    }

    benchmark.plot()
}
