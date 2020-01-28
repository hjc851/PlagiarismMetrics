package pmetrics.extractor

import org.eclipse.jdt.core.dom.CompilationUnit
import pmetrics.features.NumericFeature
import pmetrics.featureset.FeatureSet
import pmetrics.tokens.TokenExtractor
import pmetrics.tokens.Token
import kotlin.streams.toList

object TokenFeatureExtractor: FeatureExtractor() {
    override fun extract(cus: List<CompilationUnit>, pfs: FeatureSet.ProjectFeatureSet) {
        val tokens = cus.map { TokenExtractor.extract(it) }

        val tokenCount = tokens.map { it.size }.sum()
        val tokensPerCU = tokenCount.toDouble().div(tokens.size)

        val maxToken = tokens.map { it.size }.max() ?: 0
        val minToken = tokens.map { it.size }.min() ?: 0

        pfs.add(NumericFeature("TOKENS_TOKEN_COUNT", (tokenCount.toDouble())))
        pfs.add(NumericFeature("TOKENS_TOKEN_PER_CU", (tokensPerCU.toDouble())))
        pfs.add(NumericFeature("TOKENS_MAX_TOKEN_COUNT", (maxToken.toDouble())))
        pfs.add(NumericFeature("TOKENS_MIN_TOKEN_COUNT", (minToken.toDouble())))

        val allTokens = tokens.stream()
            .flatMap { it.stream() }
            .toList()

        val tokensByType = allTokens.groupBy { it.type }
        for ((type, tokens) in tokensByType) {
            val perc = tokens.size.div(allTokens.size.toDouble())
//            pfs.add(NumericFeature("TOKENS_TYPE_${type.name}_PERC", perc))
            pfs.add(NumericFeature("TOKENS_TYPE_${type.name}_LOG", Math.log(perc)))
//            pfs.add(NumericFeature("TOKENS_TYPE_${type.name}_COUNT", tokens.count()))
        }

//        val tokensByLexeme = allTokens.groupBy { it.lexeme }
//        for ((type, tokens) in tokensByLexeme) {
//            val escaped = type.replace("{", "__LBRACE")
//                .replace("}", "__RBRACE")
//                .replace("(", "__LPAREN")
//                .replace(")", "__RPAREN")
//                .replace("[", "__LARR")
//                .replace("]", "__RARR")
//                .replace("\"", "__DQUOTE")
//                .replace("'", "__SQUOTE")
//                .replace(":", "__COLON")
//                .replace("->", "__ARROW")
//                .replace("+", "__PLUS")
//                .replace("-", "__MINUS")
//                .replace("/", "__DIVIDE")
//                .replace("*", "__MULTIPLY")
//                .replace("%", "__REMAINDER")
//                .replace("<", "__LESS")
//                .replace(">", "__GREATER")
//                .replace("!", "__NOT")
//                .replace("=", "__EQUALS")
//                .replace("*", "__ASTERIX")
//                .replace("&", "__AND")
//                .replace("|", "__PIPE")
//                .replace("?", "__QUESTION")
//                .replace(".", "__DOT")
//
//            val perc = tokens.size.div(allTokens.size.toDouble())
//            pfs.add(NumericFeature("TOKENS_TYPE_${escaped}_PERC", perc))
//            pfs.add(NumericFeature("TOKENS_TYPE_${escaped}_LOG", Math.log(perc)))
//            pfs.add(NumericFeature("TOKENS_LEXEME_${escaped}_COUNT", tokens.count()))
//        }

        // Grams
        val i = 3
        val grams = extractGrams(i, allTokens)

        val groups = grams.groupBy { it.tokens.map { it.type }.joinToString("_") }
        for (group in groups) {
            val perc = group.value.size.toDouble().div(grams.count())
//            pfs.add(NumericFeature("TOKENS_NGRAMS_${i}_${group.key}_PERC", perc))
            pfs.add(NumericFeature("TOKENS_NGRAMS_${i}_${group.key}_LOG", Math.log(perc)))
//            pfs.add(NumericFeature("TOKENS_NGRAMS_${i}_${group.key}_COUNT", group.value.count()))
        }
    }

    class Gram(val tokens: Array<Token>)

    private fun extractGrams(n: Int, tokens: List<Token>): List<Gram> {
        if (n < 2) throw IllegalArgumentException("Grams must be greater than 1")
        if (tokens.size < n) return emptyList()

        val grams = mutableListOf<Gram>()
        for (i in n until tokens.size-n) {
            val gram = tokens.subList(0, i).toTypedArray()
            grams.add(Gram(gram))
        }
        return grams
    }
}