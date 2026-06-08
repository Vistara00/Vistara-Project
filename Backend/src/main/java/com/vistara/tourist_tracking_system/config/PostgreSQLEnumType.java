package com.vistara.tourist_tracking_system.config;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.*;

public class PostgreSQLEnumType implements UserType<Enum<?>> {

    private final String typeName;

    public PostgreSQLEnumType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<Enum<?>> returnedClass() {
        //noinspection unchecked
        return (Class<Enum<?>>) (Class<?>) Enum.class;
    }

    @Override
    public boolean equals(Enum<?> x, Enum<?> y) {
        return x == y;
    }

    @Override
    public int hashCode(Enum<?> x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Enum<?> nullSafeGet(ResultSet rs, int position,
                               SharedSessionContractImplementor session,
                               Object owner) throws SQLException {
        return null; // Hibernate handles reading fine; only writing needs the cast
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Enum<?> value, int index,
                            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.name(), Types.OTHER);
        }
    }

    @Override
    public Enum<?> deepCopy(Enum<?> value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Enum<?> value) {
        return value == null ? null : value.name();
    }

    @Override
    public Enum<?> assemble(Serializable cached, Object owner) {
        return null;
    }
}