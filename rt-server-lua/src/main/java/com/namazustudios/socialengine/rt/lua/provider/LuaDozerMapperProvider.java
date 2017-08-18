package com.namazustudios.socialengine.rt.lua.provider;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.dozer.loader.api.BeanMappingBuilder;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaDozerMapperProvider implements Provider<Mapper> {

    @Override
    public Mapper get() {


        final BeanMappingBuilder beanMappingBuilder = new BeanMappingBuilder() {
            @Override
            protected void configure() {

//            mapping(User.class, MongoUser.class)
//                .fields("id", "objectId", customConverter(ObjectIdConverter.class));


            }
        };

        final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;

    }

}
