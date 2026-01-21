package scan.enum

enum class EnumFailDomain(val tableName: String, val apiKey:String) {
    TYPE("_type", "type"),
    VERSION("_version", "version"),
    MOVE("_move", "move"),
    ABILITY("_ability", "ability"),
    POKEMON("pokemon", "pokemon"),
    POKEMON_SPECIES("pokemon", "pokemon-species");
}