package one.inve.mysql;

import java.util.List;

public class MysqlTable {
    private String name;
    private List<MysqlTableField> fields;

    public MysqlTable() {
    }

    private MysqlTable(Builder builder) {
        setName(builder.name);
        setFields(builder.fields);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MysqlTableField> getFields() {
        return fields;
    }

    public void setFields(List<MysqlTableField> fields) {
        this.fields = fields;
    }

    public static final class Builder {
        private String name;
        private List<MysqlTableField> fields;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder fields(List<MysqlTableField> val) {
            fields = val;
            return this;
        }

        public MysqlTable build() {
            return new MysqlTable(this);
        }
    }
}
