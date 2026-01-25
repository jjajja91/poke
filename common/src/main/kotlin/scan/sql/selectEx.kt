@file:Suppress("NOTHING_TO_INLINE")

package scan.sql

import scan.sql.common.QueryData
import scan.sql.common.QueryType
import scan.sql.select.Select

inline fun select():Select = Select(QueryData(QueryType.SELECT))