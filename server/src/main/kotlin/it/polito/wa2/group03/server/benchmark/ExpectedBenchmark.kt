package it.polito.wa2.group03.server.benchmark

import com.codahale.usl4j.Measurement
import com.codahale.usl4j.Model
import java.io.File
import jetbrains.letsPlot.letsPlot
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.scale.scaleXDiscrete
import jetbrains.letsPlot.export.ggsave

data class MeasurementL(
        val concurrentClients: Int,
        val rps: Double,
        val totalRequests: Int,
        val totalErrors: Int
)

/**
 * measurements: Accept a list of measurements of at least 6 elements
 */
class ExpectedBenchmark(private val measurements: List<MeasurementL>) {
    private val maxNumberOfClients = 256
    private val model: (Int) -> Pair<Int, Double>

    init {
        val modelList = Model.build(measurements.asIterable()
                .map { Measurement.ofConcurrency().andThroughput(it.concurrentClients.toDouble(), it.rps) }
                .toMutableList())

        model = { i: Int -> Pair(i, modelList.throughputAtConcurrency(i.toDouble())) }
    }

    fun createCSV(filename: String) {
        val file = File("server/src/main/kotlin/it/polito/wa2/group03/server/benchmark/$filename.csv")
        file.writeText("clients,rps\n")
        for (i in 1..maxNumberOfClients) {
            file.appendText("${model(i).first},${model(i).second}\n")
        }
    }

    fun plot(filename: String, plotErrors: Boolean = false) {
        // Real measurements
        val xs = mutableListOf<Int>()
        val ys = mutableListOf<Double>()
        val ysErr = mutableListOf<Int>()
        measurements.forEach {
            xs.add(it.concurrentClients)
            ys.add(it.rps)
            ysErr.add(it.totalErrors)
        }
        val realData = mapOf(
                "Concurrency" to xs,
                "Throughput" to ys,
                "Errors" to ysErr
        )

        // USL theoretical values
        val xs2 = mutableListOf<Int>()
        val ys2 = mutableListOf<Double>()
        for (i in 1..maxNumberOfClients) {
            val thrAtI = model(i)
            xs2.add(thrAtI.first)
            ys2.add(thrAtI.second)
        }
        val modelData = mapOf(
                "Concurrency" to xs2,
                "Throughput" to ys2
        )

        val p = letsPlot(null) { x = "Concurrency"; y = "Throughput" } +
                geomLine(modelData, size = 0.7, color = "red") +
                geomLine(realData, size = 1) +
                geomPoint(realData, size = 3) +
                scaleXDiscrete(name = "Concurrency", breaks = xs, labels = xs.map { it.toString() })

        ggsave(p, "$filename.svg", path = "server/src/main/kotlin/it/polito/wa2/group03/server/benchmark")

        if (plotErrors) {
            val pErr = letsPlot(null) { x = "Concurrency"; y = "Errors" } +
                    geomLine(realData, size = 1) +
                    geomPoint(realData, size = 3) +
                    scaleXDiscrete(name = "Concurrency", breaks = xs, labels = xs.map { it.toString() })
            ggsave(pErr, "${filename}Errors.svg", path = "server/src/main/kotlin/it/polito/wa2/group03/server/benchmark")
        }
    }
}

fun main() {
    var benchmark: ExpectedBenchmark
    val statelessMeasurements = listOf(
            MeasurementL(1, 540.0, 10000, 0),
            MeasurementL(2, 1191.0, 10000, 0),
            MeasurementL(4, 1521.0, 10000, 0),
            MeasurementL(8, 1968.0, 10000, 0),
            MeasurementL(16, 2004.0, 10000, 0),
            MeasurementL(32, 2204.0, 10000, 0),
            MeasurementL(64, 2257.0, 10000, 0),
            MeasurementL(128, 2283.0, 10000, 0),
            MeasurementL(256, 2229.0, 10000, 0)
    )
    benchmark = ExpectedBenchmark(statelessMeasurements)
    benchmark.createCSV("statelessResults")
    benchmark.plot("statelessConcurrency")

    val statefulMeasurements = listOf(
            MeasurementL(1, 450.0, 10000, 8),
            MeasurementL(2, 857.0, 10000, 12),
            MeasurementL(4, 1129.0, 10000, 19),
            MeasurementL(8, 1634.0, 10000, 34),
            MeasurementL(16, 1745.0, 10000, 41),
            MeasurementL(32, 1711.0, 10000, 53),
            MeasurementL(64, 1643.0, 10000, 56),
            MeasurementL(128, 1553.0, 10000, 61),
            MeasurementL(256, 1376.0, 10000, 77)
    )
    benchmark = ExpectedBenchmark(statefulMeasurements)
    benchmark.createCSV("statefulResults")
    benchmark.plot("statefulConcurrency")

    val statefulKeepalive = listOf(
            MeasurementL(1, 731.0, 10000, 5),
            MeasurementL(2, 1674.0, 10000, 16),
            MeasurementL(4, 2473.0, 10000, 17),
            MeasurementL(8, 3172.0, 10000, 33),
            MeasurementL(16, 2969.0, 10000, 45),
            MeasurementL(32, 2804.0, 10000, 47),
            MeasurementL(64, 2578.0, 10000, 47),
            MeasurementL(128, 2221.0, 10000, 66),
            MeasurementL(256, 1878.0, 10000, 79)
    )
    benchmark = ExpectedBenchmark(statefulKeepalive)
    benchmark.plot("statefulConcurrencyKeepalive")

    val statefulTimeout100 = listOf(
            MeasurementL(1, 468.0, 10000, 7),
            MeasurementL(2, 872.0, 10000, 15),
            MeasurementL(4, 1178.0, 10000, 19),
            MeasurementL(8, 1483.0, 10000, 38),
            MeasurementL(16, 1516.0, 10000, 31),
            MeasurementL(32, 1580.0, 10000, 48),
            MeasurementL(64, 1529.0, 10000, 56),
            MeasurementL(128, 1489.0, 10000, 186),
            MeasurementL(256, 1323.0, 10000, 2059)
    )
    benchmark = ExpectedBenchmark(statefulTimeout100)
    benchmark.plot("statefulConcurrencyTimeout", true)
}
