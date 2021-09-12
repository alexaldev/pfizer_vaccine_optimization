sealed class Optimizer {
    abstract fun optimize(condons: List<Aminoacid>): List<Codon>
    abstract fun getDescription(): String
}

// Optimizers ------------------------------------------------------------------------------

class CgOptimizer : Optimizer() {

    private val cgComparator: Comparator<Codon> = Comparator { c1, c2 ->
        when {
            c1.value == c2.value -> 0
            c1.value.count { it == 'C' || it == 'G' } > c2.value.count { it == 'C' || it == 'G' } -> 1
            else -> -1
        }
    }

    override fun optimize(condons: List<Aminoacid>): List<Codon> {
        return condons.map { it.codons.maxWith(cgComparator)!! }
    }

    override fun getDescription(): String {
        return "CG optimization replaces a codon of the virus with one of the codon of its aminoacid which has the most Cs and Gs"
    }

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }
}

// Factory API ------------------------------------------------------------------------------

interface OptimizerFactory {
    enum class Algo { MaxCG }

    fun createOptimizer(algo: Algo): Optimizer
}

class ByAlgoFactory : OptimizerFactory {
    override fun createOptimizer(algo: OptimizerFactory.Algo): Optimizer {
        return when (algo) {
            OptimizerFactory.Algo.MaxCG -> CgOptimizer()
        }
    }
}
