package com.example.messagingstompwebsocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class JacksonConfig {

    public static  class BeanDeserializerWithValidation extends BeanDeserializer {

        protected BeanDeserializerWithValidation(BeanDeserializerBase src) {
            super(src);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Object instance = super.deserialize(p, ctxt);
            validate(instance);
            return instance;
        }

        private <T> void validate(T t) {
            Set<ConstraintViolation<T>> violations = validator.validate(t);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }

    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}