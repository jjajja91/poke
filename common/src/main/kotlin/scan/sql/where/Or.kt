package scan.sql.where

import scan.sql.common.BindQuery

object Or:BindQuery{
    override fun invoke(bindingDecorator:(String)->String):String = " OR "
}