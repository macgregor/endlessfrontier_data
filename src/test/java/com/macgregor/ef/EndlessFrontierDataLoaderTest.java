package com.macgregor.ef;

import com.macgregor.ef.converters.CanonicalModelConverter;
import com.macgregor.ef.exceptions.DataLoadException;
import com.macgregor.ef.model.canonical.*;
import com.macgregor.ef.test_util.CanonicalTestModels;
import com.macgregor.ef.test_util.MockTranslationFieldConverter;
import io.dropwizard.testing.junit.DAOTestRule;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EndlessFrontierDataLoaderTest {

    @Rule
    public DAOTestRule database = DAOTestRule.newBuilder()
            .addEntityClass(Unit.class)
            .addEntityClass(UnitSkill.class)
            .addEntityClass(Artifact.class)
            .addEntityClass(ArtifactSet.class)
            .addEntityClass(Pet.class)
            .addEntityClass(PetSkill.class)
            .addEntityClass(Translation.class)
            .build();

    private EndlessFrontierDataLoader dataLoader;

    @Before
    public void setUp() {
        dataLoader = new EndlessFrontierDataLoader(database.getSessionFactory(),
                new CanonicalModelConverter(new MockTranslationFieldConverter()));
    }

    @Test
    public void testLoadDataExtractsUnits() throws DataLoadException {
        dataLoader.loadUnits();
        assertEquals(218, count(Unit.class));

        Unit unit = CanonicalTestModels.getTranslatedUnit();
        assertEquals(unit, find(Unit.class, unit.getId()));
    }

    @Test
    public void testLoadDataExtractsUnitSkills() throws DataLoadException {
        dataLoader.loadUnitSkills();
        assertEquals(65, count(UnitSkill.class));

        UnitSkill unitSkill = CanonicalTestModels.getTranslatedUnitSkill();
        assertEquals(unitSkill, find(UnitSkill.class, unitSkill.getId()));
    }

    @Test
    public void testLoadDataExtractsArtifacts() throws DataLoadException {
        dataLoader.loadArtifacts();
        assertEquals(189, count(Artifact.class));

        Artifact expected = CanonicalTestModels.getTranslatedArtifact();
        assertEquals(expected, find(Artifact.class, expected.getId()));
    }

    @Test
    public void testLoadDataExtractsArtifactSets() throws DataLoadException {
        dataLoader.loadArtifactSets();
        assertEquals(50, count(ArtifactSet.class));

        ArtifactSet expected = CanonicalTestModels.getTranslatedArtifactSet();
        assertEquals(expected, find(ArtifactSet.class, expected.getId()));
    }

    @Test
    public void testLoadDataExtractsPets() throws DataLoadException {
        dataLoader.loadPets();
        assertEquals(157, count(Pet.class));

        Pet expected = CanonicalTestModels.getTranslatedPet();
        assertEquals(expected, find(Pet.class, expected.getId()));
    }

    @Test
    public void testLoadDataExtractsPetSkills() throws DataLoadException {
        dataLoader.loadPetSkills();
        assertEquals(456, count(PetSkill.class));

        PetSkill expected = CanonicalTestModels.getTranslatedPetSkill();
        assertEquals(expected, find(PetSkill.class, expected.getId()));
    }

    @Test
    public void testLoadDataExtractsTranslations() throws DataLoadException {
        dataLoader.loadTranslations();
        assertEquals(2044, count(Translation.class));
    }

    private <T> int count(Class<T> type){
        Session session = database.getSessionFactory().getCurrentSession();
        Query q = session.createQuery(String.format("select count(*) from %s", type.getSimpleName()));
        return ((Long)q.uniqueResult()).intValue();
    }

    private <T> T find(Class<T> type, Integer id){
        Session session = database.getSessionFactory().getCurrentSession();
        T t = session.get(type, id);
        return t;
    }
}
