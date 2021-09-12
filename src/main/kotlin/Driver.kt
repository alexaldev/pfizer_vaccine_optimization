import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

data class Codon(val value: String)
data class Aminoacid(val name: String, val codons: Set<Codon>)

fun readAminoAcidToCondon(verbose: Boolean = false): List<String> {

    if (verbose) println("Reading aminoacids from project_dir/src/main/resources/aminoacid_codon.csv")

    val partialResult = BufferedReader(
        FileReader(
            File("src/main/resources/aminoacid_codon.csv")
        )
    ).readLines()

    return partialResult.subList(1, partialResult.size)
}

fun readSarsCodons(verbose: Boolean = false): List<String> {

    if (verbose) println("Reading sars and vaccine codons from project_dir/src/main/resources/test_set.csv")

    val partialResult = BufferedReader(
        FileReader(
            File("src/main/resources/test_set.csv")
        )
    ).readLines()

    return partialResult.subList(1, partialResult.size)
}

fun codonToAminoAcid(codon: String, aminoacids: Set<Aminoacid>): Optional<Aminoacid> {
    aminoacids.forEach {
        if (it.codons.contains(Codon(codon)))
            return Optional.of(it)
    }
    return Optional.empty()
}

lateinit var cachedAminoacids: Set<Aminoacid> // Global variable

fun cacheAminoacids(aminoAcidsText: List<String>, verbose: Boolean = false) {

    if (verbose) println("Caching the aminoacids:")

    cachedAminoacids = aminoAcidsText
        .map { it.split(',') }
        .groupBy { it[0] }
        .map { e -> Aminoacid(e.key, e.value.map { Codon(it[1]) }.toSet()) }
        .toSet()

    if (verbose) println("Aminoacids cached successfully: $cachedAminoacids")
}

fun extractVirusAminoAcids(virusCodonText: List<String>, verbose: Boolean = false): List<Aminoacid> {

    val result = virusCodonText.map { codonToAminoAcid(it, cachedAminoacids).get() }
    if (verbose) println("Extracted the virus aminoacids successfully: $result")
    return result
}

fun extractVaccineCodons(vaccineText: List<String>, verbose: Boolean = false): List<Codon> {

    val result = vaccineText.map { Codon(it) }
    if (verbose) println("Extracted the vaccine codons successfully: $result")
    return result
}

data class EvaluationResult(
    val successRate: Float, // Max value 1.0, meaning 100% match rate
    val matchedCodons: List<Pair<Codon, Codon>>, // Vaccine-virus pairs
    val unmatchedCodons: List<Pair<Codon, Codon>> // Vaccine-Virus pairs
)

class Evaluator {
    fun evaluate(expected: List<Codon>, virus: List<Codon>): EvaluationResult {

        val matchedPairs = mutableListOf<Pair<Codon, Codon>>()
        val unmatchedPairs = mutableListOf<Pair<Codon, Codon>>()

        expected.forEachIndexed { i, expectedCodon ->
            if (expectedCodon == virus[i]) {
                matchedPairs.add(Pair(expectedCodon, virus[i]))
            } else
                unmatchedPairs.add(Pair(expectedCodon, virus[i]))
        }
        return EvaluationResult(matchedPairs.size.toFloat() / expected.size.toFloat(), matchedPairs, unmatchedPairs)
    }
}

fun main() {

    // Prepare the data
    cacheAminoacids(readAminoAcidToCondon(verbose = true), verbose = true)
    val sarsAndResult = readSarsCodons(verbose = true)
    val vaccine: List<Codon> = extractVaccineCodons(sarsAndResult.map { it.split(',')[2] }, verbose = true)
    val virusAminoacids: List<Aminoacid> =
        extractVirusAminoAcids(sarsAndResult.map { it.split(',')[1] }, verbose = true)

    // Run an optimization
    val algosFactory: OptimizerFactory = ByAlgoFactory()

    val cgOptimizer = algosFactory
        .createOptimizer(OptimizerFactory.Algo.MaxCG)

    println("Running an optimization with the following description:\n${cgOptimizer.getDescription()}\n")

    val cgResult: List<Codon> = cgOptimizer.optimize(virusAminoacids)

    // Check the results
    println(Evaluator().evaluate(vaccine, cgResult))
}