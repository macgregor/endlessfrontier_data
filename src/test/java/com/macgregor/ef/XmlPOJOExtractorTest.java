package com.macgregor.ef;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.macgregor.ef.exceptions.DataLoadException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class XmlPOJOExtractorTest {

    private static final String testFile = "src/test/resources/dataloader/test_model.xml";
    private XmlPOJOExtractor extractor;
    private TestModel tm1;
    private TestModel tm2;

    @Before
    public void setUp(){
        extractor = new XmlPOJOExtractor();
        tm1 = new TestModel();
        tm1.id = 1;
        tm1.name = "foo";

        tm2 = new TestModel();
        tm2.id = 2;
        tm2.name = "bar";
    }

    @Test(expected = DataLoadException.class)
    public void shouldThrowDataLoadExceptionWhenFileNotFound() throws DataLoadException {
        extractor.extract("", "", TestModel.class);
    }

    @Test(expected = DataLoadException.class)
    public void shouldThrowDataLoadExceptionWithInvalidXPath() throws DataLoadException {
        extractor.extract(testFile, "", TestModel.class);
    }

    @Test
    public void shouldExtractListOfTestModels() throws DataLoadException {
        List<TestModel> extracted = extractor.extract(testFile, "//TestModel", TestModel.class);
        assertEquals(2, extracted.size());
        assertEquals(tm1, extracted.get(0));
        assertEquals(tm2, extracted.get(1));
    }

    @Test
    public void shouldContinueWhenFailingToParseNodeInList() throws DataLoadException {
        List<TestModel> extracted = extractor.extract("src/test/resources/dataloader/test_model_with_bad_node.xml", "//TestModel", TestModel.class);
        assertEquals(2, extracted.size());
        assertEquals(tm1, extracted.get(0));
        assertEquals(tm2, extracted.get(1));
    }

    @Test
    public void shouldExtractEmptyFieldAsNull() throws DataLoadException {
        List<TestModel> extracted = extractor.extract("src/test/resources/dataloader/test_model_with_empty_field.xml", "//TestModel", TestModel.class);
        tm1.name = null;

        assertEquals(2, extracted.size());
        assertEquals(tm1, extracted.get(0));
    }

    @Test
    public void shouldExtractShortEmptyFieldAsNul() throws DataLoadException {
        List<TestModel> extracted = extractor.extract("src/test/resources/dataloader/test_model_with_empty_field.xml", "//TestModel", TestModel.class);
        tm2.name = null;

        assertEquals(2, extracted.size());
        assertEquals(tm2, extracted.get(1));
    }


    @JacksonXmlRootElement(localName = "TestModel")
    static class TestModel{

        @JacksonXmlProperty(localName = "id")
        public Integer id;

        @JacksonXmlProperty(localName = "name")
        public String name;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestModel testModel = (TestModel) o;

            if (id != null ? !id.equals(testModel.id) : testModel.id != null) return false;
            return name != null ? name.equals(testModel.name) : testModel.name == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "TestModel{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
