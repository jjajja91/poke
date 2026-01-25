@file:Suppress("NOTHING_TO_INLINE")

package scan.sql.where

import scan.sql.common.QueryData
import kotlin.jvm.JvmInline

@JvmInline
value class Where(val data:QueryData)