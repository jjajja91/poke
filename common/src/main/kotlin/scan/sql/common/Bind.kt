package scan.sql.common



class Bind(val name:String, val converter:(Any)->Any? = pass){
    class ListBind(val list:Any?)
    companion object{
        val pass:(Any)->Any? = {
            when(it){
                is List<*>->ListBind(it.ifEmpty { null })
//                is UtcDateTime->it.toDbString()
//                is VO -> it.toJSON()
                is Boolean -> if(it) 1 else 0
                else->it
            }
        }
    }
}