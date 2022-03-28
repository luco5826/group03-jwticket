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
    private val maxNumberOfClients = 256
    private val model: (Int) -> Pair<Int, Double>
    init {
        val modelList = Model.build(measurements.asIterable()
            .map { Measurement.ofConcurrency().andThroughput(it.first.toDouble(), it.second) }
            .toMutableList())

        model = {i: Int -> Pair(i, modelList.throughputAtConcurrency(i.toDouble()))}
    }

    fun createCSV(filename: String) {
        val file = File("server/src/main/kotlin/it/polito/wa2/group03/server/benchmark/$filename.csv")
        file.writeText("clients,rps\n")
        for (i in 1..maxNumberOfClients) {
            file.appendText("${model(i).first},${model(i).second}\n")
        }
    }

    fun plot(filename: String) {
        // Real measurements
        val (xs,ys) = measurements.unzip()
        val realData = mapOf (
                "Concurrency" to xs,
                "Throughput" to ys
        )

        // USL theoretical values
        val xs2 = mutableListOf<Int>()
        val ys2 = mutableListOf<Double>()
        for (i in 1..maxNumberOfClients) {
            val thrAtI = model(i)
            xs2.add(thrAtI.first)
            ys2.add(thrAtI.second)
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


        ggsave(p, "$filename.svg", path="server/src/main/kotlin/it/polito/wa2/group03/server/benchmark")
    }
}

fun main() {
    val statelessMeasurements = listOf((1 to 540.0), (2 to 1191.0), (4 to 1521.0), (8 to 1968.0), (16 to 2004.0),
            (32 to 2204.0), (64 to 2257.0), (128 to 2283.0), (256 to 2229.0))
    val statelessBenchmark = ExpectedBenchmark(statelessMeasurements)
    statelessBenchmark.createCSV("statelessResults")
    statelessBenchmark.plot("statelessConcurrencyPlot")

    val statefulMeasurements = listOf((1 to 450.0), (2 to 857.0), (4 to 1129.0), (8 to 1634.0), (16 to 1745.0),
            (32 to 1711.0), (64 to 1643.0), (128 to 1553.0), (256 to 1376.0))
    val statefulBenchmark = ExpectedBenchmark(statefulMeasurements)
    statefulBenchmark.createCSV("statefulResults")
    statefulBenchmark.plot("statefulConcurrencyPlot")
}
