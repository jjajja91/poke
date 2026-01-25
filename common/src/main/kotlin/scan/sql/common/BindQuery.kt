package scan.sql.common

interface BindQuery{
    operator fun invoke(bindingDecorator:(String)->String):String
}