package com.andrei1058.stevesus.common.database.type;


import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.dbi.column.Column;
import com.andrei1058.dbi.column.SqlColumnType;

public class LanguageColumn implements Column<CommonLocale> {

    private final String name;
    private final int size;

    public LanguageColumn(String name, int size){
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getSize() {
        return size;
    }

    @Override
    public CommonLocale getDefaultValue() {
        return CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getDefaultLocale();
    }

    @Override
    public SqlColumnType getSqlType() {
        return SqlColumnType.STRING;
    }

    @Override
    public Object toExport(Object o) {
        return o instanceof CommonLocale ? ((CommonLocale)o).getIsoCode() : o;
    }

    @Override
    public CommonLocale castResult(Object o) {
        return o instanceof String ? CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getLocale(o.toString()) : getDefaultValue();
    }
}
