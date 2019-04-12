package one.inve.mysql;

public class MysqlTableField {
    private String name;
    private String type;
    private int size;
    private int nullable;

    public MysqlTableField() {
    }

    private MysqlTableField(Builder builder) {
        setName(builder.name);
        setType(builder.type);
        setSize(builder.size);
        setNullable(builder.nullable);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNullable() {
        return nullable;
    }

    public void setNullable(int nullable) {
        this.nullable = nullable;
    }


    public static final class Builder {
        private String name;
        private String type;
        private int size;
        private int nullable;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder type(String val) {
            type = val;
            return this;
        }

        public Builder size(int val) {
            size = val;
            return this;
        }

        public Builder nullable(int val) {
            nullable = val;
            return this;
        }

        public MysqlTableField build() {
            return new MysqlTableField(this);
        }
    }
}
