package com.macgregor.ef.test_util;

import com.macgregor.ef.converters.TranslationFieldConverter;

public class MockTranslationFieldConverter extends TranslationFieldConverter {

    public MockTranslationFieldConverter(){
        super();
        super.translator = new Translator(){
            @Override
            public String translate(String key){
                return "success";
            }
        };
    }

    public void setTranslator(Translator translator){
        this.translator = translator;
    }
}
