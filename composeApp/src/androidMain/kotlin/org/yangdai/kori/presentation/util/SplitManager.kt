package org.yangdai.kori.presentation.util

import android.content.ComponentName
import android.content.Context
import androidx.window.embedding.RuleController
import androidx.window.embedding.SplitAttributes
import androidx.window.embedding.SplitPairFilter
import androidx.window.embedding.SplitPairRule
import androidx.window.embedding.SplitRule
import org.yangdai.kori.MainActivity
import org.yangdai.kori.ink.InkActivity

class SplitManager {
    companion object {
        fun createSplit(context: Context) {
            val splitPairFilter = SplitPairFilter(
                ComponentName(context, MainActivity::class.java),
                ComponentName(context, InkActivity::class.java),
                null
            )
            val filterSet = setOf(splitPairFilter)
            val splitAttributes: SplitAttributes = SplitAttributes.Builder()
                .setSplitType(SplitAttributes.SplitType.ratio(0.5f))
                .setLayoutDirection(SplitAttributes.LayoutDirection.LEFT_TO_RIGHT)
                .build()
            val splitPairRule = SplitPairRule.Builder(filterSet)
                .setDefaultSplitAttributes(splitAttributes)
                .setMinWidthDp(840)
                .setMinSmallestWidthDp(600)
                .setFinishPrimaryWithSecondary(SplitRule.FinishBehavior.NEVER)
                .setFinishSecondaryWithPrimary(SplitRule.FinishBehavior.ALWAYS)
                .setClearTop(false)
                .build()

            val ruleController = RuleController.getInstance(context)
            ruleController.addRule(splitPairRule)
        }
    }
}