package scan.sql.where

import scan.sql.common.BindQuery

object And:BindQuery{
    override fun invoke(bindingDecorator:(String)->String):String = " AND "
}