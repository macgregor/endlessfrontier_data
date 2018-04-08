package com.macgregor.ef;

import com.macgregor.ef.converters.CanonicalModelConverter;
import com.macgregor.ef.converters.TranslationFieldConverter;
import com.macgregor.ef.exceptions.CanonicalConversionException;
import com.macgregor.ef.exceptions.DataLoadException;
import com.macgregor.ef.model.canonical.*;
import com.macgregor.ef.model.ekkor.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EndlessFrontierDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(EndlessFrontierDataLoader.class);
    private final SessionFactory sessionFactory;
    private final CanonicalModelConverter canonicalModelConverter;
    private final XmlPOJOExtractor extractor = new XmlPOJOExtractor();

    public EndlessFrontierDataLoader(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
        this.canonicalModelConverter = new CanonicalModelConverter(new TranslationFieldConverter()); //TODO: Implement database translator
    }

    public EndlessFrontierDataLoader(SessionFactory sessionFactory, CanonicalModelConverter canonicalModelConverter){
        this.sessionFactory = sessionFactory;
        this.canonicalModelConverter = canonicalModelConverter;
    }

    public <T> List<T> loadXmlData(String uri, String rawXPath, Class<T> type) throws DataLoadException {
        logger.info(String.format("[Data Load %s Processing] - Initializing data from %s using XPath %s", type.getSimpleName(), uri, rawXPath));

        List<T> extracted = extractor.extract(uri, rawXPath, type);

        logger.info(String.format("[Data Load %s Processing] - Loaded %d entities", type.getSimpleName(), extracted.size()));

        return extracted;
    }

    public <T, U> List<U> convertToCanonicalModels(List<T> extractedXMLModels, Class<T> xmlModelType, Class<U> canonicalModelType){
        logger.info(String.format("[Data Load %s to %s Conversion] - Converting models", xmlModelType.getSimpleName(), canonicalModelType.getSimpleName()));

        List<U> convertedModels = new ArrayList<U>();
        for(T nonCanonicalModel : extractedXMLModels){
            try {
                U canonicalModel = (U)canonicalModelConverter.convert(nonCanonicalModel);
                convertedModels.add(canonicalModel);
            } catch (CanonicalConversionException e) {
                logger.info(String.format("[Data Load %s to %s Conversion] - Error converting %010d to canonical model", xmlModelType.getSimpleName(), canonicalModelType.getSimpleName(), System.identityHashCode(nonCanonicalModel)));
            }
        }

        logger.info(String.format("[Data Load %s to %s Conversion] - Finished. %d successful, %d failures", xmlModelType.getSimpleName(), canonicalModelType.getSimpleName(), convertedModels.size(), extractedXMLModels.size() - convertedModels.size()));
        return convertedModels;
    }

    public <T> void persistModels(List<T> canoncialModels, Class<T> type){
        logger.info(String.format("[Data Load %s Persist] - Persisting models", type.getSimpleName()));

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        for(T t : canoncialModels){
            session.save(t);
        }
        tx.commit();
        session.close();

        int saveCount = count(type);

        logger.info(String.format("[Data Load %s Persist] - Finished. Counted %d entities persisted", type.getSimpleName(), saveCount));
    }

    public <T, U> void load(String uri, String rawXPath, Class<T> xmlModel, Class<U> canonicalModel) throws DataLoadException {
        List<T> extractedXMLModels = loadXmlData(uri, rawXPath, xmlModel);
        List<U> convertedCanonicalModels = convertToCanonicalModels(extractedXMLModels, xmlModel, canonicalModel);
        persistModels(convertedCanonicalModels, canonicalModel);
    }

    public void loadAll() throws DataLoadException {
        logger.info("==============================================");
        logger.info("=              Data Load Beginning           =");
        logger.info("==============================================");
        loadTribes();
        loadTranslations(); //Translations has no dependencies while other entities may rely on it to translate fields. Always load first.
        loadUnitSkills();
        loadPetSkills();
        loadPets();
        loadUnits();
        loadArtifacts();
        loadArtifactSets();
        logger.info("==============================================");
        logger.info("=             Data Load Complete             =");
        logger.info("==============================================");
    }

    public void loadTranslations() throws DataLoadException {
        List<Translation> extractedTranslations = loadXmlData("src/main/resources/ef/global/1.9.5/global/1.9.5/global.1.9.5-book.en.xml", "//text", Translation.class);
        persistModels(extractedTranslations, Translation.class);
    }

    public void loadUnits() {
        try {
            load("src/main/resources/ef/global/1.9.5/unitbook.xml", "//unit", UnitXML.class, Unit.class);
        } catch (DataLoadException e) {
            logger.error(String.format("[Data Load %s] Unable to load", Unit.class.getSimpleName()), e);
        }
    }

    public void loadUnitSkills() {
        try {
            load("src/main/resources/ef/global/1.9.5/unitbook.xml", "//unitSkill", UnitSkillXML.class, UnitSkill.class);
        } catch (DataLoadException e) {
            logger.error(String.format("[Data Load %s] Unable to load", UnitSkill.class.getSimpleName()), e);
        }
    }

    public void loadArtifacts() {
        try {
            load("src/main/resources/ef/global/1.9.5/treasurebook.xml", "//treasure", ArtifactXML.class, Artifact.class);
        } catch (DataLoadException e) {
            logger.error(String.format("[Data Load %s] Unable to load", Artifact.class.getSimpleName()), e);
        }
    }

    public void loadArtifactSets() {
        try {
            load("src/main/resources/ef/global/1.9.5/treasurebook.xml", "//treasureSet", ArtifactSetXML.class, ArtifactSet.class);
        } catch (DataLoadException e) {
            logger.error(String.format("[Data Load %s] Unable to load", ArtifactSet.class.getSimpleName()), e);
        }
    }

    public void loadPets() {
        try {
            load("src/main/resources/ef/global/1.9.5/petbook.xml", "//pet", PetXML.class, Pet.class);
        } catch (DataLoadException e) {
            logger.error(String.format("[Data Load %s] Unable to load", Pet.class.getSimpleName()), e);
        }
    }

    public void loadPetSkills() {
        try {
            load("src/main/resources/ef/global/1.9.5/petbook.xml", "//petSkill", PetSkillXML.class, PetSkill.class);
        } catch (DataLoadException e) {
            logger.error(String.format("[Data Load %s] Unable to load", PetSkill.class.getSimpleName()), e);
        }
    }

    public void loadTribes(){
        List<Tribe> tribes = new ArrayList<>(5);
        tribes.add(new Tribe(1, "Human"));
        tribes.add(new Tribe(2, "Elf"));
        tribes.add(new Tribe(3, "Undead"));
        tribes.add(new Tribe(4, "Orc"));
        tribes.add(new Tribe(5, "Dungeon"));

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        for(Tribe t : tribes){
            session.save(t);
        }
        tx.commit();
        session.close();
    }

    private <T> int count(Class<T> type){
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        Query q = session.createQuery(String.format("select count(*) from %s", type.getSimpleName()));
        int result = ((Long)q.uniqueResult()).intValue();
        tx.commit();
        session.close();
        return result;
    }
}
