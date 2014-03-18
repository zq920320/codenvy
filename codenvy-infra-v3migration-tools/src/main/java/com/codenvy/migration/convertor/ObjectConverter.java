package com.codenvy.migration.convertor;

/**
 * Interface defines method that will be convert object from one type to another
 *
 * @author Sergiy Leschenko
 */
public interface ObjectConverter<SourceClass, ResultClass> {
    public ResultClass convert(SourceClass object);
}
