package oliv.omrl.v2;

public class OMRL2SQLConstants {

    // Labels/keywords/field names
    public static final String ALIGNMENT = "alignment";
    public static final String ATTRIBUTES = "attributes";
    public static final String BASE = "base";
    public static final String COLUMN_EXPANSIONS = "column_expansions";
    public static final String COLUMN_NAME = "column_name";
    public static final String COMPOSITE_ENTITY = "composite_entity";
    public static final String DEEP_LINK_URL = "deepLinkUrl";
    public static final String DEFAULT_ORDER_BY = "default_order_by";
    public static final String DISPLAY_IN_FORM = "displayInForm";
    public static final String DISPLAY_IN_TABLE = "displayInTable";
    public static final String DISPLAY_TYPE = "displayType";
    public static final String ENTITIES = "entities";
    public static final String ENTITY = "entity";
    public static final String ENTITY_NAME = "entity_name";
    public static final String FOREIGN_KEY = "foreign_key";
    public static final String FORM_COLUMNS = "form_columns";
    public static final String FROM = "from";
    public static final String HEADER_TEXT = "headerText";
    public static final String LABEL = "label";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String MULTIPLE_VALUES = "multiple_values";
    public static final String NAME = "name";
    public static final String NUMBER = "number";
    public static final String PRIMARY_KEY = "primary_key";
    public static final String PRM_VALUES = "prm-values";
    public static final String OMRL_QUERY = "omrl_query";
    public static final String QUERY = "query";
    public static final String RS_MAP = "rs-map";
    public static final String RS_ENTITY_MAP = "rs-entity-map";
    public static final String RESULT_SET = "result_set";
    public static final String RESULT_SET_FLATTENED = "result_set_flatten";
    public static final String RESPONSE_DATA = "responseData";
    public static final String ROOT_ENTITY = "root_entity";
    public static final String SCHEMA = "schema";
    public static final String SCHEMA_REF = "$ref";
    public static final String SELECT = "select";
    public static final String SQL = "sql";
    public static final String SQL_EXPRESSION = "sql_expression";
    public static final String SQL_MAPPING = "sql_mapping";
    public static final String SQL_SELECT = "sql_select";
    public static final String TABLE_NAME = "table_name";
    public static final String TYPE = "type";
    public static final String TYPE_COMPOSITE_ENTITY = "composite_entity";
    public static final String TYPE_ENTITY = "ENTITY";
    public static final String TYPE_UNKNOWN = "UNKNOWN";
    public static final String UNIQUE_VALUES = "uniqueValues";
    public static final String VALUE = "value";
    public static final String VALUE_LIST = "value_list";
    public static final String MINIMUM_ATTRIBUTES = "minimum_attributes";
    public static final String DEFAULT_ATTRIBUTES = "default_attributes";
    public static final String MEASURE_BY = "measure_by";
    public static final String INVERT_COMPARISON = "invert_comparisons";
    public static final String FUZZY_MATCH = "fuzzy_match";


    // Enums
    public enum ALIGNMENTS {
        LEFT,
        CENTER,
        RIGHT;

        public String displayName() {
            return name().toLowerCase();
        }
    }

    public enum LAYOUTS {
        AUTO,
        FORM,
        TABLE,
        TABLEFORM;

        public String displayName() {
            String name = name();
            if (name.equalsIgnoreCase("tableform")) {
                return "tableForm";
            }
            return name.toLowerCase();
        }
    }

}
