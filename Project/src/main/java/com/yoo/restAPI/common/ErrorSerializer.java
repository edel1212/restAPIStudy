package com.yoo.restAPI.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.validation.Errors;

import java.io.IOException;

/**
 * Java Bean 스팩에 맞지 않으면
 * SpringBean JSON에서  자동으로 JSON으로 만들어주지 않는다.
 * - 그럴 경우 아래와 같이 상속을 통해 만들어 줄 수 있음
 * */
//@JsonComponent // 👉 해당 어노테이션을 사용하면 쉽게 등록 가능함 따로 불러서 사용할 필요도 없다
public class ErrorSerializer extends JsonSerializer<Errors> {
    @Override
    public void serialize(Errors errors, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray(); // JSON 시작만듬 void

        // File 에러만 체크  - errors.rejectValue()
        errors.getFieldErrors().forEach(err ->{
            try {
                gen.writeStartObject();
                gen.writeStringField("field",err.getField());
                gen.writeStringField("message",err.getDefaultMessage());
                gen.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }// try - catch
        });

        // 글로벌 에러만 체크  - errors.reject()
        errors.getGlobalErrors().forEach(err ->{
            try {
                gen.writeStartObject();
                gen.writeStringField("wantMsg",err.getDefaultMessage());
                gen.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }// try - catch
        });

        gen.writeEndArray(); // JSON 끝을 만듬 void
    }
}
