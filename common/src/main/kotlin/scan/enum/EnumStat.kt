package scan.enum

enum class EnumStat(val id:Int, val nameKr:String, val nameJp:String, val nameEn:String) {
    HP(1, "HP", "HP", "HP"),
    ATK(2, "공격", "こうげき", "Attack"),
    DEF(3, "방어", "ぼうぎょ", "Defense"),
    SATK(4, "특수공격", "とくこう", "Special Attack"),
    SDEF(5, "특수방어", "とくぼう", "Special Defense"),
    SPD(6, "스피드", "すばやさ", "Speed");
    companion object {
        operator fun invoke(id:Int):EnumStat = entries.find { it.id == id } ?: throw Throwable("unknown enum stat $id")
    }
}