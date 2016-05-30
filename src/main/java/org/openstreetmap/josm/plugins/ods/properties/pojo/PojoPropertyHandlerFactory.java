package org.openstreetmap.josm.plugins.ods.properties.pojo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openstreetmap.josm.plugins.ods.properties.PropertyHandler;

public class PojoPropertyHandlerFactory {
    
    public <T1, T2> PropertyHandler<T1, T2> createPropertyHandler(Class<T1> objectClass, Class<T2> attrClass, String name) {
        PropertyHandler<T1, T2> result = new PojoPropertyHandler<T1, T2>(objectClass, attrClass, name);
        return result;
    }

    public <T1> PropertyHandler<T1, ?> createPropertyHandler(Class<T1> objType, String attributeName) {
        Method getter = PojoUtils.getAttributeGetter(objType, attributeName);
        if (getter != null) {
            Class<?> attrType = PojoUtils.getNonPrimitiveClass(getter.getReturnType());
            return new PojoPropertyHandler<>(objType, attrType, attributeName);
        }
        return null;
    }

    public class PojoPropertyHandler<T1, T2> implements PropertyHandler<T1, T2> {
        private final Class<T2> attrType;
        private Method setter;
        private Method getter;
        
        public PojoPropertyHandler(Class<T1> objType, Class<T2> attrType, String name) {
            super();
            this.attrType = attrType;
            getter = PojoUtils.getAttributeGetter(objType, name, attrType);
            setter = PojoUtils.getAttributeSetter(objType, name, attrType);
       }

        @Override
        public Class<T2> getType() {
            return attrType;
        }

        @Override
        public void set(T1 feature, T2 value) {
            if (setter != null) {
                try {
                    setter.invoke(feature, value);
                } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public T2 get(T1 obj) {
            if (getter != null) {
                try {
                    @SuppressWarnings("unchecked")
                    T2 result = (T2) getter.invoke(obj);
                    return result;
                } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }
}
